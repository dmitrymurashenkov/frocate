package com.frocate.shorturl.mock;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;

public class HttpNodeImpl implements Node
{
    private final String host;
    private final int port;
    private final HttpClient http;

    public HttpNodeImpl(String host, int port, HttpClient http)
    {
        this.host = host;
        this.port = port;
        this.http = http;
    }

    public String getHost()
    {
        return host;
    }

    @Override
    public int getPort()
    {
        return port;
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

    private String performPOST(String url, String content)
    {

        try
        {
            ContentResponse response = http.POST(url).content(new BytesContentProvider(content.getBytes())).send();
            if (response.getStatus() != 200)
            {
                throw new RuntimeException("Request returned status " + response.getStatus() + " for url: " + content);
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
