package com.frocate.money.transfer.test;

import com.frocate.money.transfer.balance.AccountGenerator;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.frocate.money.transfer.test.expect.ExpectedTx.createTx;

public class WarmUpTest extends FunctionalTest
{
    @Test(timeout = 60000)
    public void warmup() throws Exception
    {
        int transactions = 50000;
        int accounts = 1000;
        startBootstrap(AccountGenerator.withBalance(accounts, 1000*1000));
        long startTime = System.nanoTime();
        for (int i = 0; i < transactions; i++)
        {
            String fromAccountId = i % accounts + "";
            String toAccountId = (i + 1) % accounts + "";
            TxService.Tx tx = balance.expect(createTx()
                    .transfer(fromAccountId, toAccountId, 1)
                    .build());
            service.transfer(tx);
            if (i % 5000 == 0)
            {
                long elapsedMs = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
                System.out.println("Warmup progress: " + i + "/" + transactions + " transactions in " + elapsedMs + "ms");
            }
        }
    }
}
