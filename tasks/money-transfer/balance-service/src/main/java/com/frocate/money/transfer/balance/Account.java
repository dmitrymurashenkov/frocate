package com.frocate.money.transfer.balance;

import java.util.concurrent.TimeUnit;

public class Account
{
    private final String id;
    private int balance;

    private int sequentialErrors;
    private long lastErrorNanos;

    public Account(String id, int balance)
    {
        this.id = id;
        this.balance = balance;
    }

    public String getId()
    {
        return id;
    }

    public int getBalance()
    {
        return balance;
    }

    public boolean isTooManyErrors()
    {
        return sequentialErrors >= BalanceServiceParams.TOO_MANY_EXCEPTIONS_COUNT
                && TimeUnit.MILLISECONDS.convert(System.nanoTime() - lastErrorNanos, TimeUnit.NANOSECONDS) < BalanceServiceParams.TOO_MANY_EXCEPTIONS_TIMEOUT_MS;
    }

    public void onError()
    {
        sequentialErrors++;
        lastErrorNanos = System.nanoTime();
    }

    public void updateBalance(int amount)
    {
        balance += amount;
        sequentialErrors = 0;
        lastErrorNanos = 0;
    }
}
