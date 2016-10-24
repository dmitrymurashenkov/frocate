package com.frocate.shorturl.test;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.InputStreamContentProvider;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ShortUrlServiceImpl implements ShortUrlService
{
    private final String host;
    private final int port;
    private final HttpClient http;

    public ShortUrlServiceImpl(String host, int port, HttpClient http)
    {
        this.host = host;
        this.port = port;
        this.http = http;
    }

    @Override
    public String shorten(String url)
    {
        String requestUrl = "http://" + host + ":" + port + "/shorten";
        return performPOST(requestUrl, url);
    }

    @Override
    public String expand(String shortUrl)
    {
        String requestUrl = "http://" + host + ":" + port + "/expand";
        return performPOST(requestUrl, shortUrl);
    }

    @Override
    public boolean nodeReady()
    {
        try
        {
            String requestUrl = "http://" + host + ":" + port + "/status";
            ContentResponse response = http.GET(requestUrl);
            return response.getStatus() == 200;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private String performPOST(String url, String content)
    {

        try
        {
            ContentResponse response = http.POST(url).content(new BytesContentProvider(content.getBytes())).send();
            if (response.getStatus() != 200)
            {
                throw new RuntimeException("Request returned status " + response.getStatus() + " for url: " + content + " with message: " + response.getContentAsString());
            }
            else
            {
                return response.getContentAsString();
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
