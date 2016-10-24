package com.frocate.money.transfer.balance;

import java.lang.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BalanceServiceImpl implements BalanceService
{
    private final Map<String, Account> accounts = new HashMap<String, Account>();

    public BalanceServiceImpl(Collection<Account> accounts)
    {
        for (Account account : accounts)
        {
            this.accounts.put(account.getId(), account);
        }
    }

    public synchronized void modifyBalanceInternal(String accountId, int amount)
    {
        Account account = accounts.get(accountId);
        if (account == null)
        {
            throw new RuntimeException("Account with id '" + accountId + "' not found");
        }
        account.updateBalance(amount);
    }

    public synchronized void debit(String txId, String accountId, int amount) throws NotEnoughMoneyException, TooManyErrorsException, UnknownException, RollbackTxException, RequestMalformedException
    {
        Account account = accounts.get(accountId);
        if (account == null)
        {
            throw new RequestMalformedException("Account with id '" + accountId + "' not found");
        }
        if (account.isTooManyErrors())
        {
            account.onError();
            throw new TooManyErrorsException("Account with id '" + accountId + "' is temporary locked because too many " +
                    "errors occured (" + BalanceServiceParams.TOO_MANY_EXCEPTIONS_COUNT+ " or more sequential " +
                    "operations with this money ended with errors), " +
                    "wait for " + BalanceServiceParams.TOO_MANY_EXCEPTIONS_TIMEOUT_MS + "ms before " +
                    "sending next request to update this money");
        }
        if (amount < 0 && account.getBalance() + amount < 0)
        {
            account.onError();
            throw new NotEnoughMoneyException("Account with id '" + accountId + "' hasn't enough money " +
                    "to be debited by " + amount + "$");
        }
        try
        {
            beforeBalanceUpdate(txId, accountId, amount);
        }
        catch (Exception e)
        {
            account.onError();
            throw e;
        }
        account.updateBalance(amount);
    }

    @Override
    public synchronized long getOperationDelay(String txId, String accountId, int amount)
    {
        return 0;
    }

    protected void beforeBalanceUpdate(String txId, String accountId, int amount) throws NotEnoughMoneyException, TooManyErrorsException, UnknownException, RollbackTxException, RequestMalformedException
    {
        //override this to provide some behavior
    }

    public synchronized int getBalance(String accountId)
    {
        return accounts.get(accountId).getBalance();
    }
}
