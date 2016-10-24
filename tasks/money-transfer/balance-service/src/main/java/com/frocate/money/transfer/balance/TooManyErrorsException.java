package com.frocate.money.transfer.balance;

public class TooManyErrorsException extends Exception
{
    public TooManyErrorsException(String message)
    {
        super(message);
    }
}
