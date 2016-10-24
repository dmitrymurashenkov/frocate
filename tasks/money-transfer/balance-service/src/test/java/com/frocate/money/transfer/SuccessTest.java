package com.frocate.money.transfer;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SuccessTest extends FunctionalTest
{
    @Test
    public void httpGet_shouldReturn200ForValidRequests() throws InterruptedException, ExecutionException, TimeoutException
    {
        startService();
        assertEquals(200, http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=10").getStatus());
    }

    @Test
    public void httpGet_shouldDebitAccountWithPositiveAmount() throws InterruptedException, ExecutionException, TimeoutException
    {
        startService();
        Assert.assertEquals(10, service.getBalance("1"));
        http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=5");
        Assert.assertEquals(15, service.getBalance("1"));
    }

    @Test
    public void httpGet_shouldCreditAccountWithNegativeAmount() throws InterruptedException, ExecutionException, TimeoutException
    {
        startService();
        Assert.assertEquals(10, service.getBalance("1"));
        http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-5");
        Assert.assertEquals(5, service.getBalance("1"));
    }

    @Test
    public void httpGet_shouldWorkInCaseOfExecutionDelay() throws InterruptedException, ExecutionException, TimeoutException
    {
        long delayMs = 100;
        int requests = 10;

        startWithExecutionDelay(delayMs);
        Assert.assertEquals(10, service.getBalance("1"));
        long startTime = System.nanoTime();
        for (int i = 0; i < requests; i++)
        {
            http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=5");
        }
        long endTime = System.nanoTime();
        assertTrue(TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) > (requests-1)*delayMs);
        Assert.assertEquals(10 + 10*5, service.getBalance("1"));
    }
}
