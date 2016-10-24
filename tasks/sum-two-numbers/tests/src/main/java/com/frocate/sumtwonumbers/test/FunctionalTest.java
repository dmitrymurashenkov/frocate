package com.frocate.sumtwonumbers.test;

import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class FunctionalTest extends AbstractTest
{
    @Test(timeout = 5000)
    public void sum_shouldSumNumbers() throws InterruptedException
    {
        assertSum(1, 1);
        assertSum(1, 2);
        assertSum(0, 1);
        assertSum(0, 0);
        assertSum(-1, 0);
        assertSum(-1, 1);
        assertSum(-1, 2);
    }

    @Test(timeout = 5000)
    public void sum_shouldSumLargeNumbers() throws InterruptedException
    {
        BigInteger a = new BigInteger(Long.MAX_VALUE + "" + Long.MAX_VALUE);
        BigInteger b = new BigInteger(Long.MAX_VALUE + "" + Long.MAX_VALUE);
        assertEquals(a.add(b), service.sum(a, b));

        a = new BigInteger(Long.MAX_VALUE + "" + Long.MAX_VALUE);
        b = new BigInteger("-" + Long.MAX_VALUE + "" + Long.MAX_VALUE);
        assertEquals(a.add(b), service.sum(a, b));
    }
}
