package com.frocate.shorturl.test;

import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.MetricBuilder;
import com.frocate.taskrunner.result.Range;
import org.eclipse.jetty.client.HttpClient;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.frocate.shorturl.test.ShortUrlTask.clusterControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpeedTestSingleNode
{
    private final HttpClient http = new HttpClient();
    private List<ShortUrlService> nodes;

    private static final Map<Integer, String> shortUrls = new ConcurrentHashMap<>();
    private static final SpeedTestParams params = new SpeedTestParams(
                                                     ShortUrlTask.MEMORY_LIMIT_PER_NODE + 100*1024*1024,
                                                     8*1024
                                                 );
    //Single ASCII symbol is 1 byte
    private static final URLGenerator urlGenerator = new URLGenerator(params.urlLength);

    private static class SpeedTestParams
    {
        final int totalBytes;
        final int urlLength;
        final int totalUrls;

        SpeedTestParams(int totalBytes, int urlLength)
        {
            this.totalBytes = totalBytes;
            this.urlLength = urlLength;
            this.totalUrls = totalBytes/urlLength;
        }
    }

    @BeforeClass
    public static void classSetUp() throws Exception
    {
        clusterControl.restartClusterWithNodes(1);
    }

    @Before
    public void setUp() throws Exception
    {
        http.setRequestBufferSize(20*1024);
        http.setMaxConnectionsPerDestination(1000);
        http.setMaxRequestsQueuedPerDestination(10000);
        http.start();
        nodes = getNodes(http);
    }

    @After
    public void tearDown() throws Exception
    {
        http.stop();
    }

    @Test(timeout = 150000)
    public void t1_concurrentShorten() throws Exception
    {
        int requests = params.totalUrls;
        int threads = Runtime.getRuntime().availableProcessors()*10;

        Metric result = new MetricBuilder()
                .name("Single node concurrent shorten (" + params.totalUrls + " urls of " + params.urlLength + " length, " + threads + " threads)")
                .unit("req/seq")
                .description("Several threads calling \"shorten\" method loading urls into service")
                .excellentRange(new Range(1001, 10000))
                .goodRange(new Range(501, 1000))
                .appendResultTo(ShortUrlTask.metrics)
                .calculate(() -> shortenPerformance_test(threads, requests));

        assertEquals(params.totalUrls, shortUrls.size());
        System.out.println(result);
    }

    private long shortenPerformance_test(int threads, int requests) throws Exception
    {
        return performRequestsConcurrently(threads, requests, new Operation()
        {
            @Override
            void run(int operationIndex, AtomicReference<Throwable> error) throws Exception
            {
                String url = urlGenerator.generateUrl(operationIndex);
                String shortUrl = getRandomNode(nodes).shorten(url);
                assertNotNull("Service returned empty response for url: " + url, shortUrl);
                shortUrls.put(operationIndex, shortUrl);
            }
        });
    }

    @Test(timeout = 30000)
    public void t2_expandWarmUp() throws Exception
    {
        concurrentExpandHotUrl_test(Runtime.getRuntime().availableProcessors()*2, 15000, 300);
    }

    @Test(timeout = 60000)
    public void t3_concurrentExpandRandomUrl() throws Exception
    {
        int threads = Runtime.getRuntime().availableProcessors()*2;
        int requests = 10000;

        Metric result = new MetricBuilder()
                .name("Single node expand random url (" + params.totalUrls + " urls, " + requests + " requests, " + threads + " threads)")
                .unit("req/seq")
                .description("Having " + params.totalUrls + " urls already shortened we choose one randomly and expand it, several urls may be expanded in parallel")
                .excellentRange(new Range(1001, 10000))
                .goodRange(new Range(500, 1000))
                .appendResultTo(ShortUrlTask.metrics)
                .calculate(() -> concurrentExpandRandomUrl_test2(threads, requests));
        System.out.println(result);
    }

    private long concurrentExpandRandomUrl_test2(int threads, int requests) throws Exception
    {
        return performRequestsConcurrently(threads, requests, new Operation()
        {
            @Override
            void run(int operationIndex, AtomicReference<Throwable> error) throws Exception
            {
                int urlIndex = (int)(Math.random()*shortUrls.size());
                String originalUrl = urlGenerator.generateUrl(urlIndex);
                String shortUrl = shortUrls.get(urlIndex);
                assertNotNull("Test data broken - seems there was an error before, during shortening test which loads data for expand test (url with index " + urlIndex + " not found)", shortUrl);
                String longUrl = getRandomNode().expand(shortUrl);
                assertEquals("Expanded url not equal to original, original=" + originalUrl + ", short=" + shortUrl + ", expanded=" + longUrl, originalUrl, longUrl);
            }
        });
    }

    @Test(timeout = 60000)
    public void t4_concurrentExpandHotUrl() throws Exception
    {
        int threads = Runtime.getRuntime().availableProcessors()*2;
        int requests = params.totalUrls;
        int topUrls = 300;

        Metric result = new MetricBuilder()
                .name("Single node expand of hot URL (" + params.totalUrls + " total urls, using " + topUrls + " hot urls in requests, " + requests + " requests)")
                .unit("req/seq")
                .description("Having " + params.totalUrls + " urls already shortened we select " + topUrls + " of them and " +
                        "try to expand them sending " + requests + " sequential requests")
                .excellentRange(new Range(3001, 10000))
                .goodRange(new Range(2000, 3000))
                .appendResultTo(ShortUrlTask.metrics)
                .calculate(() -> concurrentExpandHotUrl_test(threads, requests, topUrls));
        System.out.println(result);
    }

    private long concurrentExpandHotUrl_test(int threads, int requests, int topUrls) throws Exception
    {
        return performRequestsConcurrently(threads, requests, new Operation()
        {
            @Override
            void run(int operationIndex, AtomicReference<Throwable> error) throws Exception
            {
                int urlIndex = (int)(Math.random()*topUrls);
                String originalUrl = urlGenerator.generateUrl(urlIndex);
                String shortUrl = shortUrls.get(urlIndex);
                assertNotNull("Test data broken - seems there was an error before, during shortening test which loads data for expand test", shortUrl);
                String longUrl = getRandomNode().expand(shortUrl);
                assertEquals("Expanded url not equal to original, original=" + originalUrl + ", short=" + shortUrl + ", expanded=" + longUrl, originalUrl, longUrl);
            }
        });
    }

    private ShortUrlService getRandomNode()
    {
        return getRandomNode(nodes);
    }

    static ShortUrlService getRandomNode(List<ShortUrlService> nodes)
    {
        return nodes.get((int)(Math.random()*nodes.size()));
    }

    private static List<ShortUrlService> getNodes(HttpClient http) throws Exception
    {
        List<ShortUrlService> nodes = new ArrayList<>();
        for (int i = 0; i < clusterControl.getNodesCount(); i++)
        {
            nodes.add(clusterControl.getService(http, i));
        }
        return Collections.synchronizedList(nodes);
    }

    private long performRequestsConcurrently(int threads, int requests, Operation operation) throws Exception
    {
        long startTime = System.nanoTime();
        AtomicLong operationsPerformed = new AtomicLong();
        AtomicReference<Throwable> error = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < requests; i++)
        {
            int finalI = i;
            executor.submit(() -> {
                operation.perform(finalI, error);
                operationsPerformed.incrementAndGet();
            });
        }
        executor.shutdown();
        int timeoutSeconds = 150;
        while (!executor.isTerminated() && TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS) < timeoutSeconds)
        {
            System.out.println("Operations during last second: " + operationsPerformed.get());
            operationsPerformed.set(0);
            Thread.sleep(1000);
        }
        executor.awaitTermination(1, TimeUnit.SECONDS);
        if (error.get() != null)
        {
            fail(error.get().getMessage());
        }
        long endTime = System.nanoTime();
        return (long)(1000*((double)requests/ TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS)));
    }

    static class Operation
    {
        final void perform(int operationIndex, AtomicReference<Throwable> error)
        {
            try
            {
                run(operationIndex, error);
            }
            catch (Throwable e)
            {
                error.compareAndSet(null, e);
            }
        }

        void run(int operationIndex, AtomicReference<Throwable> error) throws Exception
        {

        }
    }
}
