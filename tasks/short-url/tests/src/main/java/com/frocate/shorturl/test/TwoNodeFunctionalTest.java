package com.frocate.shorturl.test;

import org.eclipse.jetty.client.HttpClient;
import org.junit.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.frocate.shorturl.test.ShortUrlTask.clusterControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TwoNodeFunctionalTest
{
    private final HttpClient http = new HttpClient();
    private ShortUrlService node1;
    private ShortUrlService node2;

    private static final URLGenerator urlGenerator = new URLGenerator(30);

    @BeforeClass
    public static void classSetUp() throws Exception
    {
        clusterControl.restartClusterWithNodes(2);
    }

    @Before
    public void setUp() throws Exception
    {
        http.start();
        http.setMaxConnectionsPerDestination(1000);
        node1 = clusterControl.getService(http, 0);
        node2 = clusterControl.getService(http, 1);
    }

    @After
    public void tearDown() throws Exception
    {
        http.stop();
    }

    @Test(timeout = 3000)
    public void shorten_shouldReturnSameUrl_onBothNodes()
    {
        String url1 = urlGenerator.nextUrl();
        String url2 = urlGenerator.nextUrl();
        String url3 = urlGenerator.nextUrl();

        String url1Node1 = node1.shorten(url1);
        String url2Node1 = node1.shorten(url2);
        String url3Node1 = node1.shorten(url3);

        String url3Node2 = node2.shorten(url3);
        String url2Node2 = node2.shorten(url2);
        String url1Node2 = node2.shorten(url1);

        assertEquals("Method shorten returned different results on two nodes for same url: input=" + url1 + ", output1=" + url1Node1 + ", output2=" + url1Node2, url1Node1, url1Node2);
        assertEquals("Method shorten returned different results on two nodes for same url: input=" + url2 + ", output1=" + url2Node1 + ", output2=" + url2Node2, url2Node1, url2Node2);
        assertEquals("Method shorten returned different results on two nodes for same url: input=" + url3 + ", output1=" + url3Node1 + ", output2=" + url3Node2, url3Node1, url3Node2);
    }

    @Test(timeout = 3000)
    public void expand_shouldReturnOriginalUrlOnAllNodes()
    {
        String url = urlGenerator.nextUrl();
        String shortUrl = node1.shorten(url);
        String longUrl1 = node2.expand(shortUrl);
        String longUrl2 = node1.expand(shortUrl);
        assertEquals(url, longUrl1);
        assertEquals(url, longUrl2);
    }

    @Test(timeout = 60000)
    public void shorten_shouldHandleConcurrentCalls() throws ExecutionException, InterruptedException
    {
        int iterations = 4000;
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < iterations; i++)
        {
            String url = urlGenerator.nextUrl();
            List<Future> node1futures = new ArrayList<>();
            List<Future> node2futures = new ArrayList<>();
            for (int j = 0; j < threads; j++)
            {
                node1futures.add(executor.submit(() -> node1.shorten(url)));
                node2futures.add(executor.submit(() -> node2.shorten(url)));
            }
            Set<String> result = new HashSet<>();
            for (Future<String> future : node1futures)
            {
                result.add(future.get());
            }
            for (Future<String> future : node2futures)
            {
                result.add(future.get());
            }
            if (result.size() != 1)
            {
                fail("Got several different short urls from concurrent calss with same input: input=" + url + ", output=" + result.stream().collect(Collectors.joining(", ")));
            }
        }
    }
}
