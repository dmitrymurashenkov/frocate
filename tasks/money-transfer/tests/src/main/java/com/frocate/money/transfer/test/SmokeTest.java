package com.frocate.money.transfer.test;

import com.frocate.money.transfer.balance.AccountGenerator;
import org.junit.Test;

import static com.frocate.money.transfer.test.expect.ExpectedTx.createTx;

public class SmokeTest extends FunctionalTest
{
    @Test(timeout = 20*1000)
    public void awaitServiceReady() throws InterruptedException
    {
        startBootstrap(AccountGenerator.withBalance(10, 10));
        TxService.Tx tx = balance.expect(createTx().transfer("1", "2", 1).build());
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                service.transfer(tx);
                return;
            }
            catch (RuntimeException e)
            {
                //service not yet ready
                Thread.sleep(100);
            }
        }
        service.transfer(tx);
    }
}
