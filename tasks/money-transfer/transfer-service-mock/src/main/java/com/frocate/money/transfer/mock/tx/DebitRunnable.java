package com.frocate.money.transfer.mock.tx;

import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class DebitRunnable implements Runnable
{
    private final HttpClientWrapper http;
    private final ExecutorService executorForRetry;
    private final AtomicBoolean isTxMarkedForRollback;
    private final boolean isRollback;
    private final String txId;
    private final String accountId;
    private final int amount;
    private final OperationListener onComplete;
    private volatile int errors;

    public DebitRunnable(HttpClientWrapper http, ExecutorService executorForRetry, AtomicBoolean isTxMarkedForRollback, boolean isRollback, String txId, String accountId, int amount, OperationListener onComplete)
    {
        this.http = http;
        this.executorForRetry = executorForRetry;
        this.isTxMarkedForRollback = isTxMarkedForRollback;
        this.isRollback = isRollback;
        this.txId = txId;
        this.accountId = accountId;
        this.amount = amount;
        this.onComplete = onComplete;
    }

    @Override
    public void run()
    {
        if (isTxMarkedForRollback.get() && !isRollback)
        {
            //we haven't yet modified anything during this operation then there is nothing to rollback so we can
            //shortcut to exit and consider current state as rolled back
            onComplete.onPerformed(OperationStatus.ROLLBACK_COMPLETED);
        }
        http.request(txId, accountId, amount).send(new Response.CompleteListener()
        {
            @Override
            public void onComplete(Result result)
            {
                Response response = result.getResponse();
                if (result.isFailed())
                {
                    System.out.println("Exception during request to '" + result.getRequest().getURI() + "': " + result.getFailure().getMessage());
                    if (errors++ > 10)
                    {
                        System.out.println("Aborting request to '" + result.getRequest().getURI() + "' because of " + errors + " errors");
                        return;
                    }
                    executorForRetry.submit(DebitRunnable.this);
                }
                else if (response.getStatus() == 200)
                {
                    onComplete.onPerformed(OperationStatus.COMPLETED);
                }
                else if (response.getStatus() == 400)
                {
                    String reason = response.getHeaders().get("Balance-error-reason");
                    String message = response.getHeaders().get("Balance-error-reason");
                    if ("NOT_ENOUGH_MONEY".equals(reason))
                    {
                        executorForRetry.submit(DebitRunnable.this);
                    }
                    else if ("TOO_MANY_ERRORS".equals(reason))
                    {
                        //todo speed up
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e)
                        {
                            throw new RuntimeException(e);
                        }
                        executorForRetry.submit(DebitRunnable.this);
                    }
                    else if ("UNKNOWN_ERROR".equals(reason))
                    {
                        executorForRetry.submit(DebitRunnable.this);
                    }
                    else if ("ROLLBACK_TX".equals(reason))
                    {
                        onComplete.onPerformed(OperationStatus.ROLLBACK_COMPLETED);
                    }
                    else
                    {
                        throw new RuntimeException("Balance service answered http code: " + response.getStatus()
                                + " with reason: " + reason
                                + " and message: " + message);
                    }
                }
            }
        });
    }
}
