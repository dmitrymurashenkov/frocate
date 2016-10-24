package com.frocate.shorturl.test;

import org.eclipse.jetty.client.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.frocate.shorturl.test.ShortUrlTask.clusterControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SingleNodeFunctionalTest
{
    private final HttpClient http = new HttpClient();
    private ShortUrlService service;

    @BeforeClass
    public static void classSetUp() throws Exception
    {
        clusterControl.restartClusterWithNodes(1);
    }

    @Before
    public void setUp() throws Exception
    {
        http.start();
        http.setMaxConnectionsPerDestination(1000);
        http.setRequestBufferSize(20*1024);
        service = clusterControl.getService(http, 0);
    }

    @After
    public void tearDown() throws Exception
    {
        http.stop();
    }

    @Test(timeout = 3000)
    public void shorten_shouldReturnShortUrl()
    {
        for (String url : getSampleURLs())
        {
            String shortUrl = service.shorten(url);
            assertTrue("Short url length must not exceed 8 symbols: input=" + url + ", output=" + shortUrl, shortUrl.length() <= 8);
        }
    }

    @Test(timeout = 3000)
    public void shorten_shouldAcceptLongUrl()
    {
        String url = new URLGenerator(10*1024).nextUrl();
        String shortUrl = service.shorten(url);
        assertEquals("Failed to process long input url", url, service.expand(shortUrl));
    }

    @Test(timeout = 3000)
    public void shorten_shouldReturnAlphanumericUrl()
    {
        for (String url : getSampleURLs())
        {
            String shortUrl = service.shorten(url);
            assertTrue("Short url must only contain a-z, A-Z or 0-9 chars: input=" + url + ", output=" + shortUrl, shortUrl.matches("[0-9a-zA-Z]*"));
        }
    }

    @Test(timeout = 3000)
    public void shorten_shouldReturnSameOutputForSameInput()
    {
        for (String url : getSampleURLs())
        {
            String shortUrl1 = service.shorten(url);
            String shortUrl2 = service.shorten(url);
            assertEquals("Shorten one url twice returned different results: input=" + url + ", output1=" + shortUrl1 + ", output2=" + shortUrl2, shortUrl1, shortUrl2);
        }
    }

    @Test(timeout = 3000)
    public void expand_shouldReturnSameOriginalUrl()
    {
        for (String url : getSampleURLs())
        {
            String shortUrl = service.shorten(url);
            assertEquals("Expand didn't return original url: input=" + url + ", output=" + shortUrl, url, service.expand(shortUrl));
        }
    }

    @Test(expected = RuntimeException.class, timeout = 3000)
    public void expand_shouldThrowException_ifURLNotFound1()
    {
        service.expand("01234unknownUrl");
    }

    @Test(expected = RuntimeException.class, timeout = 3000)
    public void expand_shouldThrowException_ifURLNotFound2()
    {
        service.expand("unknownUrl01234");
    }

    private List<String> getSampleURLs()
    {
        return Arrays.asList(
                "http://google.com",
                "0123456789012345678901234567890123456789",
                "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz",
                "!@#$%^&*()_+~?|/\\,.<>[]{}"
        );
    }
}
