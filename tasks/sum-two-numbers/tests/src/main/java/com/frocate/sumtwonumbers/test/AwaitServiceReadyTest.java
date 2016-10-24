package com.frocate.sumtwonumbers.test;

import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

public class AwaitServiceReadyTest extends AbstractTest
{
    @Test(timeout = 30000)
    public void awaitServiceReady() throws InterruptedException
    {
        while (true)
        {
            try
            {
                service.sum(BigInteger.ONE, BigInteger.ZERO);
                return;
            }
            catch (Exception e)
            {
                System.out.println("Service not ready, waiting, exception: " + e.getMessage());
                Thread.sleep(500);
            }
        }
    }
}
