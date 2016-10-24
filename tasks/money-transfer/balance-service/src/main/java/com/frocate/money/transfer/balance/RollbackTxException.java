package com.frocate.money.transfer.balance;

public class RollbackTxException extends Exception
{
    public RollbackTxException(String message)
    {
        super(message);
    }
}
