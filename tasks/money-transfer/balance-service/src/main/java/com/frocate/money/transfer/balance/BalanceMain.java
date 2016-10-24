package com.frocate.money.transfer.balance;

public class BalanceMain
{
    public static void main(String[] args)
    {
        System.out.println("Starting balance service on 0.0.0.0:8081 with 100 users (ids from 0 to 99) each with balance 1000");
        new BalanceBootstrap(
                8081,
                "0.0.0.0",
                new BalanceServiceImpl(AccountGenerator.withBalance(100, 1000))
        ).start().join();
    }
}
