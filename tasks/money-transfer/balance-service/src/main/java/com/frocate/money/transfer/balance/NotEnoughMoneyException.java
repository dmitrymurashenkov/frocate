package com.frocate.money.transfer.balance;

public class NotEnoughMoneyException extends Exception
{
    public NotEnoughMoneyException(String message)
    {
        super(message);
    }
}
