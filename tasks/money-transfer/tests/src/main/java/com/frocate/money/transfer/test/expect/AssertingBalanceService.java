package com.frocate.money.transfer.test.expect;

import com.frocate.money.transfer.balance.*;
import com.frocate.money.transfer.test.TxService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AssertingBalanceService extends BalanceServiceImpl
{
    private final Map<String, ExpectedTx> expectedTx = new ConcurrentHashMap<>();
    private final Map<String, List<DebitRequest>> allRequests = new ConcurrentHashMap<>();

    public TxService.Tx expect(ExpectedTx expectedTx)
    {
        this.expectedTx.put(expectedTx.getId(), expectedTx);
        return expectedTx.toTx();
    }

    public AssertingBalanceService(Collection<Account> accounts)
    {
        super(accounts);
    }

    @Override
    public long getOperationDelay(String txId, String accountId, int amount)
    {
        ExpectedTx tx = expectedTx.get(txId);
        if (tx == null)
        {
            return 0;
        }
        else
        {
            ExpectedOperation operation = tx.getOperation(accountId);
            if (operation == null)
            {
                return 0;
            }
            return operation.processingDelayMs;
        }
    }

    @Override
    public synchronized void debit(String txId, String accountId, int amount) throws NotEnoughMoneyException, TooManyErrorsException, UnknownException, RollbackTxException, RequestMalformedException
    {
        try
        {
            super.debit(txId, accountId, amount);
            addDebitRequest(txId, new DebitRequest(accountId, amount, null));
        }
        catch (NotEnoughMoneyException e)
        {
            addDebitRequest(txId, new DebitRequest(accountId, amount, "Not enough money exception thrown: " + e.getMessage()));
            throw e;
        }
        catch (TooManyErrorsException e)
        {
            addDebitRequest(txId, new DebitRequest(accountId, amount, "Too many errors exception thrown: " + e.getMessage()));
            throw e;
        }
        catch (RequestMalformedException e)
        {
            addDebitRequest(txId, new DebitRequest(accountId, amount, "Request malformed exception thrown: " + e.getMessage()));
            throw e;
        }
        catch (UnknownException e)
        {
            addDebitRequest(txId, new DebitRequest(accountId, amount, "Unknown exception thrown"));
            throw e;
        }
        catch (RollbackTxException e)
        {
            addDebitRequest(txId, new DebitRequest(accountId, amount, "Rollback exception thrown"));
            throw e;
        }
        catch (Throwable e)
        {
            addDebitRequest(txId, new DebitRequest(accountId, amount, "Internal test framework error: " + e.getMessage()));
            throw e;
        }
    }

    @Override
    protected void beforeBalanceUpdate(String txId, String accountId, int amount) throws UnknownException, RollbackTxException
    {
        ExpectedTx tx = expectedTx.get(txId);
        if (tx != null)
        {
            tx.onOperation(accountId, amount);
        }
    }

    private void addDebitRequest(String txId, DebitRequest request)
    {
        List<DebitRequest> requestsInTx = allRequests.computeIfAbsent(txId, (s) -> Collections.synchronizedList(new ArrayList<>()));
        requestsInTx.add(request);
    }

    public void assertAllTxPerformed()
    {
        assertNoUnexpectedTransactions();
        assertAllTxAttempted();
        assertAllOperationsPerformed();
    }

    private void assertAllOperationsPerformed()
    {
        for (ExpectedTx expectedTx : this.expectedTx.values())
        {
            List<DebitRequest> requestsInTx = allRequests.get(expectedTx.getId());
            for (ExpectedOperation expectedOperation : expectedTx.operations.values())
            {
                if (expectedTx.shouldRollback() && sumAmountForAccount(expectedOperation.accountId, requestsInTx) != 0)
                {
                    String errorMessage = "Tx '" + expectedTx.getId() + "' was not fully rolled back, " +
                            "account '" + expectedOperation.accountId + "' was changed by " + sumAmountForAccount(expectedOperation.accountId, requestsInTx);
                    printTxToStdout(expectedTx.getId(), errorMessage);
                    throw new AssertionError(errorMessage + ", see test logs for list of expected and actual requests in this tx");
                }
                else if (!expectedTx.shouldRollback() && sumAmountForAccount(expectedOperation.accountId, requestsInTx) != expectedOperation.amount)
                {
                    String errorMessage = "Tx '" + expectedTx.getId() + "' was performed incorrectly, " +
                            "expected change of account '" + expectedOperation.accountId + "': " + expectedOperation.amount + ", " +
                            "actual change " + sumAmountForAccount(expectedOperation.accountId, requestsInTx);
                    printTxToStdout(expectedTx.getId(), errorMessage);
                    throw new AssertionError(errorMessage + ", see test logs for list of expected and actual requests in this tx");
                }
            }
        }
    }

    private int sumAmountForAccount(String accountId, List<DebitRequest> requests)
    {
        int sum = 0;
        for (DebitRequest request : requests)
        {
            if (request.accountId.equals(accountId) && request.isCompleted())
            {
                sum += request.amount;
            }
        }
        return sum;
    }

    private void assertAllTxAttempted()
    {
        Set<String> expectedTx = new HashSet<>(this.expectedTx.keySet());
        expectedTx.removeAll(allRequests.keySet());
        if (!expectedTx.isEmpty())
        {
            String unexpectedTxId = expectedTx.iterator().next();
            String errorMessage = "Tx '" + unexpectedTxId + "' was not performed (no requests in this tx)";
            printTxToStdout(unexpectedTxId, errorMessage);
            throw new AssertionError(errorMessage + ", see test logs for list of expected and actual requests in this tx");
        }
    }

    private void assertNoUnexpectedTransactions()
    {
        Set<String> performedTxIds = new HashSet<>(allRequests.keySet());
        performedTxIds.removeAll(expectedTx.keySet());
        if (!performedTxIds.isEmpty())
        {
            String unexpectedTxId = performedTxIds.iterator().next();
            String errorMessage = "Tx '" + unexpectedTxId + "' was not expected";
            printTxToStdout(unexpectedTxId, errorMessage);
            throw new AssertionError(errorMessage + ", see test logs for list of expected and actual requests in this tx");
        }
    }

    private void printTxToStdout(String txId, String errorMessage)
    {
        System.out.println(errorMessage);
        if (allRequests.containsKey(txId))
        {
            System.out.println("Operations attempted within tx '" + txId + "':");
            for (DebitRequest request : allRequests.get(txId))
            {
                System.out.println("AccountId: " + request.accountId + ", amount: " + request.amount + ", result: " + (request.isCompleted() ? "OK" : request.errorMessage));
            }
        }
        if (this.expectedTx.containsKey(txId))
        {
            System.out.println("Operations expected within tx '" + txId + "':");
            for (ExpectedOperation expectedOperation : this.expectedTx.get(txId).operations.values())
            {
                String result = expectedOperation.throwRollbackTxException ? "Rollback exception thrown" : "OK";
                System.out.println("AccountId: " + expectedOperation.accountId + ", amount: " + expectedOperation.amount + ", result: " + result);
            }
        }
    }

    private static class DebitRequest
    {
        private final String accountId;
        private final int amount;
        private final String errorMessage;

        DebitRequest(String accountId, int amount, String errorMessage)
        {
            this.accountId = accountId;
            this.amount = amount;
            this.errorMessage = errorMessage;
        }

        private boolean isCompleted()
        {
            return errorMessage == null;
        }
    }
}
