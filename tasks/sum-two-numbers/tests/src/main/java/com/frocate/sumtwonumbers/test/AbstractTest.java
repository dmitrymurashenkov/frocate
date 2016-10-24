package com.frocate.sumtwonumbers.test;

import org.eclipse.jetty.client.HttpClient;
import org.junit.After;
import org.junit.Before;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public abstract class AbstractTest
{
    protected final HttpClient client = new HttpClient();
    protected final SumService service = new SumServiceImpl(client, System.getProperty("host", "127.0.0.1"), Integer.getInteger("port", 8080));

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

    protected void assertSum(int a, int b)
    {
        BigInteger aNum = new BigInteger(a + "");
        BigInteger bNum = new BigInteger(b + "");
        BigInteger sum = service.sum(aNum, bNum);
        assertEquals("Incorrect sum: a=" +a + ", b=" + b, new BigInteger((a + b) + ""), sum);
    }
}
