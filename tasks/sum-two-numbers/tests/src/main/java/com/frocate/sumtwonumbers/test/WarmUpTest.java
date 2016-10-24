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

public class WarmUpTest
{
    private final HttpClient client = new HttpClient();
    private final SumService service = new SumServiceImpl(client, System.getProperty("host", "127.0.0.1"), Integer.getInteger("port", 8080));

    @Before
    public void setUp() throws Exception
    {
        client.start();
    }

    @After
    public void tearDown() throws Exception
    {
        client.stop();
    }

    @Test(timeout = 60000)
    public void warmUp() throws Exception
    {
        int requests = 15000;
        for (int i = 0; i < requests; i++)
        {
            BigInteger a = new BigInteger(i + "");
            BigInteger b = new BigInteger((i + 1) + "");
            assertEquals("Incorrect sum: a=" + a + ", b=" + b, a.add(b), service.sum(a, b));
        }
    }
}

