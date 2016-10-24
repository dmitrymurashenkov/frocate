package com.frocate.money.transfer.test;

import com.frocate.money.transfer.balance.AccountGenerator;
import com.frocate.money.transfer.test.expect.ExpectedTx;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.frocate.money.transfer.test.expect.ExpectedTx.createTx;
import static org.junit.Assert.assertEquals;

public class TransferTest extends FunctionalTest
{
    @Test(timeout = 5*1000)
    public void transfer_shouldTransferMoneyBetween1PairOfAccounts()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 5).build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldTransferMoneyBetween2PairsOfAccounts()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx()
                .transfer("1", "2", 5)
                .transfer("3", "4", 10)
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldTransferMoneyBetween50PairsOfAccounts()
    {
        startBootstrap(AccountGenerator.withBalance(100, 10));
        ExpectedTx.Builder builder = createTx();
        for (int i = 0; i < 50; i++)
        {
            int from = i*2;
            int to = from + 1;
            builder.transfer(from + "", to + "", 10);
        }
        TxService.Tx tx = balance.expect(builder.build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldCompleteSeveralTx()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx1 = balance.expect(createTx().transfer("1", "2", 5).build());
        TxService.Tx tx2 = balance.expect(createTx().transfer("1", "2", 5).build());
        service.transfer(tx1);
        service.transfer(tx2);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 8*1000)
    public void transfer_shouldCompleteTx_ifProcessingDelayPresent()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx()
                .transfer("1", "2", 5)
                .processingTimeDebitMs(500)
                .processingTimeCreditMs(500)
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldCompleteTx_ifUnknownExceptionDuringCredit()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 5)
                .throwExceptionsDuringCredit(1)
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldCompleteTx_ifUnknownExceptionDuringDebit()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 5)
                .throwExceptionsDuringDebit(1)
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldRollbackEachAlreadyCompletedOperationOnlyOnce()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx()
                .transfer("1", "2", 5)
                .transfer("3", "4", 5)
                .processingTimeCreditMs(500)
                .throwRollbackDuringCredit()
                .processingTimeDebitMs(1000)
                .throwRollbackDuringDebit()
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldRollbackTx_andRollbackOperationsInProgress()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx()
                .transfer("1", "2", 5)
                .processingTimeCreditMs(500)
                .processingTimeDebitMs(1000)
                .transfer("3", "4", 5)
                .processingTimeCreditMs(100)
                .throwRollbackDuringCredit()
                .processingTimeDebitMs(300)
                .throwRollbackDuringDebit()
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldRollbackTx_ifRollbackExceptionDuringCredit()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 5)
                .throwRollbackDuringCredit()
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldRollbackTx_ifRollbackExceptionDuringDebit()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 5)
                .throwRollbackDuringDebit()
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 5*1000)
    public void transfer_shouldRollbackTx_ifSeveralRollbackExceptionsThrown()
    {
        startBootstrap(AccountGenerator.withBalance(10, 10));
        TxService.Tx tx = balance.expect(createTx()
                .transfer("1", "2", 5)
                .throwRollbackDuringCredit()
                .throwRollbackDuringDebit()
                .transfer("3", "4", 5)
                .throwRollbackDuringCredit()
                .throwRollbackDuringDebit()
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 8*1000)
    public void transfer_shouldRollbackTx_ifRollbackIsThrownAfterAllOperationsCompleted()
    {
        startBootstrap(AccountGenerator.withBalance(10, 10));
        TxService.Tx tx = balance.expect(createTx()
                .transfer("1", "2", 5)
                .processingTimeCreditMs(500)
                .throwRollbackDuringCredit()
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 8*1000)
    public void transfer_shouldRollbackTx_ifRollbackIsThrownBeforeAllOperationsCompleted()
    {
        startBootstrap(AccountGenerator.withBalance(10, 10));
        TxService.Tx tx = balance.expect(createTx()
                .transfer("1", "2", 5)
                .processingTimeDebitMs(500)
                .throwRollbackDuringCredit()
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 8*1000)
    public void transfer_shouldCompleteTx_ifSeveralUnknownExceptionsThrown()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 5)
                .throwExceptionsDuringCredit(6)
                .build());
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 8*1000)
    public void transfer_shouldRetryTx_ifNotEnoughMoney()
    {
        startBootstrap(AccountGenerator.withBalance(5, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 15)
                .build());
        new Thread(() -> {
            sleepNoEx(2000);
            balance.modifyBalanceInternal("1", 100);
        }).start();
        service.transfer(tx);
        balance.assertAllTxPerformed();
    }

    @Test(timeout = 30*1000)
    public void transfer_shouldRetryAllTx_ifNotEnoughMoney() throws Exception
    {
        startBootstrap(AccountGenerator.withBalance(10, 0));
        List<TxService.TransferFuture> futures = new ArrayList<>();
        futures.add(service.transferAsync(balance.expect(createTx().transfer("1", "2", 1).build())));
        futures.add(service.transferAsync(balance.expect(createTx().transfer("2", "3", 1).build())));
        futures.add(service.transferAsync(balance.expect(createTx().transfer("3", "4", 1).build())));
        futures.add(service.transferAsync(balance.expect(createTx().transfer("4", "5", 1).build())));
        futures.add(service.transferAsync(balance.expect(createTx().transfer("5", "6", 1).build())));
        sleepNoEx(100);
        balance.modifyBalanceInternal("1", 1);
        for (TxService.TransferFuture future : futures)
        {
            future.get(30, TimeUnit.SECONDS);
        }
        balance.assertAllTxPerformed();
    }

    public static void sleepNoEx(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}