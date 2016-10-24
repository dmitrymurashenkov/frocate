package com.frocate.sumtwonumbers.test;

import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.MetricBuilder;
import com.frocate.taskrunner.result.Range;
import org.eclipse.jetty.client.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class SpeedTest extends AbstractTest
{
    @Test(timeout = 60000)
    public void sequentialRequests() throws Exception
    {
        int requests = 10000;
        Metric result = new MetricBuilder()
                .name("Sequential requests (" + requests + " requests)")
                .description("Performing a number of sequential request, calculating number of requests handled per second")
                .unit("req/sec")
                .goodRange(new Range(500, 1000))
                .excellentRange(new Range(1001, 100000))
                .appendResultTo(SumTwoNumbersTask.metrics)
                .calculate(() -> sequentialRequests_test(requests));
        System.out.println(result);
    }

    private long sequentialRequests_test(int requests)
    {
        long startTime = System.nanoTime();
        for (int i = 0; i < requests; i++)
        {
            assertSum(i, i+1);
        }
        long endTime = System.nanoTime();
        return (long)(1000*(requests/(double)TimeUnit.MILLISECONDS.convert(endTime-startTime, TimeUnit.NANOSECONDS)));
    }

    @Test(timeout = 60000)
    public void parallelRequests() throws Exception
    {
        int requests = 40000;
        int threads = Runtime.getRuntime().availableProcessors()*2;
        Metric result = new MetricBuilder()
                .name("Parallel requests (" + threads + " threads, " + requests + " requests)")
                .description("Sending a number of requests in parallel, calculating number of requests handled per second")
                .unit("req/sec")
                .goodRange(new Range(1000, 2000))
                .excellentRange(new Range(2001, 100000))
                .appendResultTo(SumTwoNumbersTask.metrics)
                .calculate(() -> parallelRequests_test(requests, threads));
        System.out.println(result);
    }

    private long parallelRequests_test(int requests, int threads) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future> futures = Collections.synchronizedList(new ArrayList<>());
        long startTime = System.nanoTime();
        for (int i = 0; i < requests; i++)
        {
            int finalI = i;
            Future future = executor.submit(() ->
            {
                assertSum(finalI, finalI+1);
            });
            futures.add(future);
        }
        executor.shutdown();
        for (Future future : futures)
        {
            future.get();
        }
        long endTime = System.nanoTime();
        return (long)(1000*(requests/(double)TimeUnit.MILLISECONDS.convert(endTime-startTime, TimeUnit.NANOSECONDS)));
    }
}

