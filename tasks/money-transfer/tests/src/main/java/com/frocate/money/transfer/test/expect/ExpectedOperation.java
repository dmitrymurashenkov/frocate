package com.frocate.money.transfer.test.expect;

import com.frocate.money.transfer.balance.RollbackTxException;
import com.frocate.money.transfer.balance.UnknownException;

public class ExpectedOperation
{
    final String accountId;
    final int amount;
    int unknownExceptionsToThrow;
    final long processingDelayMs;
    final boolean throwRollbackTxException;

    public ExpectedOperation(String accountId, int amount, long processingDelayMs, int unknownExceptionsToThrow, boolean throwRollbackTxException)
    {
        this.accountId = accountId;
        this.amount = amount;
        this.processingDelayMs = processingDelayMs;
        this.unknownExceptionsToThrow = unknownExceptionsToThrow;
        this.throwRollbackTxException = throwRollbackTxException;
    }

    public void onPerform() throws UnknownException, RollbackTxException
    {
        if (unknownExceptionsToThrow > 0)
        {
            unknownExceptionsToThrow--;
            throw new UnknownException("Unknown error occurred");
        }
        else if (throwRollbackTxException)
        {
            throw new RollbackTxException("This transaction must be rolled back");
        }
    }
}