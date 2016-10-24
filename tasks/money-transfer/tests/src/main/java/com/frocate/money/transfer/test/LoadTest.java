package com.frocate.money.transfer.test;

import com.frocate.money.transfer.balance.AccountGenerator;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.MetricBuilder;
import com.frocate.taskrunner.result.Range;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.frocate.money.transfer.test.expect.ExpectedTx.createTx;

public class LoadTest extends FunctionalTest
{
    @Test(timeout = 60*1000)
    public void sequentialRequests() throws Exception
    {
        int transactions = 50000;
        Metric result = new MetricBuilder()
                .name("Sequential requests (" + transactions + " transactions)")
                .unit("req/sec")
                .description("Single thread client makes request after receiving response to previous one")
                .excellentRange(new Range(1501, 10*1000))
                .goodRange(new Range(500, 1500))
                .appendResultTo(MoneyTransferTask.metrics)
                .calculate(() -> sequentialRequests_test(transactions));
        System.out.println(result);
    }

    private long sequentialRequests_test(int transactions)
    {
        startBootstrap(AccountGenerator.withBalance(5, 1000*1000));
        long startTime = System.nanoTime();
        for (int i = 0; i < transactions; i++)
        {
            TxService.Tx tx = balance.expect(createTx()
                    .transfer("1", "2", 1)
                    .build());
            service.transfer(tx);
        }
        long endTime = System.nanoTime();
        balance.assertAllTxPerformed();
        return (long)(1000*(transactions/(double)TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS)));
    }

    @Test(timeout = 60*1000)
    public void parallelRequests() throws Exception
    {
        int transactions = 50000;
        int concurrentRequests = 100;
        Metric result = new MetricBuilder()
                .name("Parallel requests (" + transactions + " transactions, " + concurrentRequests + " concurrent requests max)")
                .unit("req/sec")
                .description("Several https connections are opened and sequential requests are sent through each")
                .excellentRange(new Range(2001, 10*1000))
                .goodRange(new Range(1000, 2000))
                .appendResultTo(MoneyTransferTask.metrics)
                .calculate(() -> parallelRequests_test(transactions, concurrentRequests));
        System.out.println(result);
    }

    private long parallelRequests_test(int transactions, int concurrentRequests) throws Exception
    {
        int accounts = 10000;
        http.setMaxConnectionsPerDestination(concurrentRequests);
        http.setMaxRequestsQueuedPerDestination(transactions);
        startBootstrap(AccountGenerator.withBalance(accounts, 1000*1000));
        List<TxService.TransferFuture> futures = new ArrayList<>();
        long startTime = System.nanoTime();
        for (int i = 0; i < transactions; i++)
        {
            String fromAccountId = i % accounts + "";
            String toAccountId = (i + 1) % accounts + "";
            TxService.Tx tx = balance.expect(createTx()
                    .transfer(fromAccountId, toAccountId, 1)
                    .build());
            futures.add(service.transferAsync(tx));
        }
        for (TxService.TransferFuture future : futures)
        {
            future.get(30, TimeUnit.SECONDS);
        }
        long endTime = System.nanoTime();
        balance.assertAllTxPerformed();
        return (long)(1000*(transactions/(double)TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS)));
    }
}
