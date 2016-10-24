package com.frocate.money.transfer.mock.tx;

import com.frocate.money.transfer.mock.MockTxService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class Transaction
{
    private final HttpClientWrapper http;
    private final ExecutorService executor;
    private final String id;
    private final Runnable onCompleteCallback;
    private final AtomicBoolean isMarkedForRollback = new AtomicBoolean();
    private final Map<DebitOperation, OperationStatus> operations = new HashMap<>();

    public Transaction(HttpClientWrapper http, ExecutorService executor, String id, List<MockTxService.Operation> operations, Runnable onCompleteCallback)
    {
        this.http = http;
        this.executor = executor;
        this.id = id;
        this.onCompleteCallback = onCompleteCallback;
        for (MockTxService.Operation operation : operations)
        {
            DebitOperation creditOperation = new DebitOperation(operation.from, -operation.amount);
            DebitOperation debitOperation = new DebitOperation(operation.to, operation.amount);

            this.operations.put(creditOperation, OperationStatus.IN_PROGRESS);
            this.operations.put(debitOperation, OperationStatus.IN_PROGRESS);

            perform(creditOperation);
            perform(debitOperation);
        }
    }

    private void perform(DebitOperation operation)
    {
        executor.submit(new DebitRunnable(http, executor, isMarkedForRollback, false, id, operation.accountId, operation.amount, status -> onComplete(operation, status)));
    }

    private void rollback(DebitOperation operation)
    {
        executor.submit(new DebitRunnable(http, executor, isMarkedForRollback, true, id, operation.accountId, -operation.amount, status -> onComplete(operation, OperationStatus.ROLLBACK_COMPLETED)));
    }

    public synchronized void onComplete(DebitOperation operation, OperationStatus status)
    {
        operations.put(operation, status);
        if (status == OperationStatus.ROLLBACK_COMPLETED)
        {
            isMarkedForRollback.set(true);
        }
        if (isCompleted())
        {
            onCompleteCallback.run();
        }
        else if (isMarkedForRollback.get())
        {
            for (Map.Entry<DebitOperation, OperationStatus> entry : operations.entrySet())
            {
                if (entry.getValue() == OperationStatus.COMPLETED)
                {
                    operations.put(entry.getKey(), OperationStatus.ROLLBACK_IN_PROGRESS);
                    rollback(entry.getKey());
                }
            }
        }
    }

    private boolean isCompleted()
    {
        int operationsCompleted = 0;
        int operationsRolledBack = 0;
        for (OperationStatus status : operations.values())
        {
            if (status == OperationStatus.COMPLETED)
            {
                operationsCompleted++;
            }
            else if (status == OperationStatus.ROLLBACK_COMPLETED)
            {
                operationsRolledBack++;
            }
        }
        if (isMarkedForRollback.get() && operationsRolledBack == operations.size())
        {
            return true;
        }
        else if (!isMarkedForRollback.get() && operationsCompleted == operations.size())
        {
            return true;
        }
        return false;
    }

    private static class DebitOperation
    {
        private final String accountId;
        private final int amount;

        public DebitOperation(String accountId, int amount)
        {
            this.accountId = accountId;
            this.amount = amount;
        }
    }
}
