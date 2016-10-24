package com.frocate.money.transfer.balance;

import java.lang.*;

public interface BalanceService
{
    void debit(String txId, String accountId, int amount) throws NotEnoughMoneyException, TooManyErrorsException, UnknownException, RollbackTxException, RequestMalformedException;
    void modifyBalanceInternal(String accountId, int amount);
    int getBalance(String accountId);
    long getOperationDelay(String txId, String accountId, int amount);
}
