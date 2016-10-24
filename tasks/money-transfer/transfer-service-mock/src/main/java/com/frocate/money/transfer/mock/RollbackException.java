package com.frocate.money.transfer.mock;

public class RollbackException extends Exception
{
    public RollbackException(String message)
    {
        super(message);
    }
}
