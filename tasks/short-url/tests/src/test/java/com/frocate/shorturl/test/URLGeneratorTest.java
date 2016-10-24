package com.frocate.shorturl.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class URLGeneratorTest
{
    @Test
    public void generateUrl_shouldBeFast()
    {
        URLGenerator generator = new URLGenerator(10*1024);
        int iterations = 100000;
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++)
        {
            generator.generateUrl(i);
        }
        long endTime = System.nanoTime();
        int requestsPerSecond = (int)(1000*((double)iterations/ TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS)));
        System.out.println("URL generation performance: " + requestsPerSecond);
        System.out.println("Time to generate 100 000 urls: " + TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) + "ms");
    }

    @Test
    public void generateUrl_shouldBeConsistent()
    {
        URLGenerator generator = new URLGenerator(10*1024);
        int iterations = 50000;
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < iterations; i++)
        {
            urls.add(generator.generateUrl(i));
        }
        for (int i = 0; i < iterations; i++)
        {
            assertEquals(urls.get(i), generator.generateUrl(i));
        }
    }
}
