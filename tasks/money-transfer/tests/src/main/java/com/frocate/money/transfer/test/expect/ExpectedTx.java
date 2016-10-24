package com.frocate.money.transfer.test.expect;

import com.frocate.money.transfer.balance.RollbackTxException;
import com.frocate.money.transfer.balance.UnknownException;
import com.frocate.money.transfer.test.TxService;

import java.util.*;

public class ExpectedTx
{
    private final String id;
    final Map<String, ExpectedOperation> operations = new HashMap<>();
    private final TxService.Tx tx;

    public ExpectedTx(String id, List<ExpectedOperation> operations, TxService.Tx tx)
    {
        this.id = id;
        this.tx = tx;
        for (ExpectedOperation operation : operations)
        {
            if (this.operations.put(operation.accountId, operation) != null)
            {
                throw new RuntimeException("Tx '" + id + "' already contains operation with account id '" + operation.accountId + "'");
            }
        }
    }

    public String getId()
    {
        return id;
    }

    public int operationsCount()
    {
        return operations.size();
    }

    public synchronized void onOperation(String accountId, int amount) throws RollbackTxException, UnknownException
    {
        ExpectedOperation operation = operations.get(accountId);
        if (operation != null)
        {
            operation.onPerform();
        }
    }

    public synchronized boolean shouldRollback()
    {
        for (ExpectedOperation operation : operations.values())
        {
            if (operation.throwRollbackTxException)
            {
                return true;
            }
        }
        return false;
    }

    synchronized ExpectedOperation getOperation(String accountId)
    {
        return operations.get(accountId);
    }

    public interface Builder
    {
        TransferBuilder transfer(String from, String to, int amount);
        ExpectedTx build();
    }

    public interface TransferBuilder extends Builder
    {
        TransferBuilder throwExceptionsDuringDebit(int count);
        TransferBuilder throwExceptionsDuringCredit(int count);
        TransferBuilder throwRollbackDuringDebit();
        TransferBuilder throwRollbackDuringCredit();
        TransferBuilder processingTimeCreditMs(long delayCreditMs);
        TransferBuilder processingTimeDebitMs(long delayDebitMs);
    }

    public TxService.Tx toTx()
    {
        return tx;
    }

    public static Builder createTx(String txId)
    {
        return new ExpectedTxBuilderImpl(txId);
    }

    public static Builder createTx()
    {
        return new ExpectedTxBuilderImpl();
    }
}
