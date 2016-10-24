package com.frocate.sumtwonumbers.test;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;

import java.math.BigInteger;

public class SumServiceImpl implements SumService
{
    private final HttpClient client;
    private final String host;
    private final int port;

    public SumServiceImpl(HttpClient client, String host, int port)
    {
        this.client = client;
        this.host = host;
        this.port = port;
    }

    @Override
    public BigInteger sum(BigInteger a, BigInteger b)
    {
        try
        {
            String url = "http://" + host + ":" + port + "/sum?a=" + a + "&b=" + b;
            ContentResponse response = client.GET(url);
            if (response.getStatus() == 200)
            {
                return new BigInteger(response.getContentAsString());
            }
            else
            {
                throw new RuntimeException("Sum failed with status: " + response.getStatus() + ", a=" + a + ", b=" + b + ", response=" + response.getContentAsString());
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
