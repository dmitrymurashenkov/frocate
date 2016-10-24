package com.frocate.money.transfer.balance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class AccountGenerator
{
    public static Collection<Account> one(String id, int balance)
    {
        return Arrays.asList(new Account(id, balance));
    }

    public static Collection<Account> withBalance(int count, int balance)
    {
        Collection<Account> accounts = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            accounts.add(new Account(i + "", balance));
        }
        return accounts;
    }
}
