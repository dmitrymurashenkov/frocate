package com.frocate.money.transfer;

import com.frocate.money.transfer.balance.AccountGenerator;
import com.frocate.money.transfer.balance.BalanceBootstrap;
import com.frocate.money.transfer.balance.BalanceService;
import com.frocate.money.transfer.balance.BalanceServiceImpl;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadTest
{
    private BalanceBootstrap bootstrap;
    private HttpClient http;

    @Before
    public void setUp() throws Exception
    {
        http = new HttpClient();
        //using relatively small value not to exceed default linux max open files limit of 1024
        http.setMaxConnectionsPerDestination(1000);
        http.start();
    }

    @After
    public void tearDown() throws Exception
    {
        bootstrap.stop();
        http.stop();
    }

    @Test
    public void serviceProcessesRequestsWithNoDelay() throws InterruptedException
    {
        int accounts = 1000;
        int updates = 100000;
        CountDownLatch requestsToPerform = new CountDownLatch(updates);

        bootstrap = new BalanceBootstrap(AccountGenerator.withBalance(accounts, 0));
        BalanceService service = bootstrap.getBalanceService();
        bootstrap.start();

        AtomicLong errors = new AtomicLong();
        BlockingQueue<Integer> requestsInProgess = new ArrayBlockingQueue<>(http.getMaxRequestsQueuedPerDestination());
        Thread progressThread = startPrintProgressThread(requestsToPerform);
        try
        {
            for (int i = 0; i < updates; i++)
            {
                int requestId = i;
                requestsInProgess.offer(requestId, 5, TimeUnit.SECONDS);
                http.newRequest("http://localhost:8080/debit?txId=1&accountId=" + i % accounts + "&amount=1")
                        .send(new Response.CompleteListener()
                        {
                            @Override
                            public void onComplete(Result result)
                            {
                                requestsInProgess.remove(requestId);
                                requestsToPerform.countDown();
                                if (result.isFailed() || result.getResponse().getStatus() != 200)
                                {
                                    errors.incrementAndGet();
                                }
                            }
                        });
            }
            progressThread.join(30*1000);
            assertEquals(0, errors.get());

            for (int i = 0; i < accounts; i++)
            {
                assertTrue(service.getBalance(i + "") >= updates/accounts);
            }
        }
        finally
        {
            progressThread.interrupt();
            bootstrap.stop();
        }
    }

    @Test
    public void serviceProcessesRequestsWithExecutionDelay() throws InterruptedException
    {
        int accounts = 1000;
        int updates = 100000;
        int delayMs = 100;
        CountDownLatch requestsToPerform = new CountDownLatch(updates);

        bootstrap = new BalanceBootstrap(8080, "0.0.0.0", new BalanceServiceImpl(AccountGenerator.withBalance(accounts, 0))
        {
            @Override
            public synchronized long getOperationDelay(String txId, String accountId, int amount)
            {
                return delayMs;
            }
        });
        BalanceService service = bootstrap.getBalanceService();
        bootstrap.start();

        AtomicLong errors = new AtomicLong();
        BlockingQueue<Integer> requestsInProgess = new ArrayBlockingQueue<>(http.getMaxRequestsQueuedPerDestination());
        Thread progressThread = startPrintProgressThread(requestsToPerform);

        try
        {
            long startTime = System.nanoTime();
            for (int i = 0; i < updates; i++)
            {
                int requestId = i;
                requestsInProgess.offer(requestId, 5, TimeUnit.SECONDS);
                http.newRequest("http://localhost:8080/debit?txId=" + updates + "&accountId=" + requestId % accounts + "&amount=1")
                        .send(new Response.CompleteListener()
                        {
                            @Override
                            public void onComplete(Result result)
                            {
                                requestsToPerform.countDown();
                                requestsInProgess.remove(requestId);
                                if (result.isFailed() || result.getResponse().getStatus() != 200)
                                {
                                    errors.incrementAndGet();
                                    result.getFailure().printStackTrace();
                                }
                            }
                        });
            }
            progressThread.join(30*1000);
            long endTime = System.nanoTime();
            assertEquals(0, errors.get());

            for (int i = 0; i < accounts; i++)
            {
                assertTrue(service.getBalance(i + "") >= updates/accounts);
            }

            //1000/delayMs - requests per second through single connection
            //assert that it took not more than 50% from best possible time
            //to complete the task - this "proves" that requests were processed
            //with high level of concurrency both on http and server
            assertTrue(TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS)*0.5 < http.getMaxConnectionsPerDestination()*1000/delayMs);
        }
        finally
        {
            progressThread.interrupt();
            bootstrap.stop();
        }
    }

    private Thread startPrintProgressThread(CountDownLatch requestToPerform)
    {
        Thread t = new Thread(() -> {
            try
            {
                long prevRequests = requestToPerform.getCount();
                while (!requestToPerform.await(1000, TimeUnit.MILLISECONDS) && !Thread.currentThread().isInterrupted())
                {
                    long currentRequests = requestToPerform.getCount();
                    System.out.println("Requests during last second: " + (prevRequests - currentRequests));
                    prevRequests = currentRequests;
                }
            }
            catch (InterruptedException e)
            {
                return;
            }
        });
        t.start();
        return t;
    }
}
