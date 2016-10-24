package com.frocate.shorturl.test;

import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class URLGenerator
{
    private final long seed = ThreadLocalRandom.current().nextLong();
    private final int urlLength;
    private AtomicInteger counter = new AtomicInteger();

    public URLGenerator(int urlLength)
    {
        this.urlLength = urlLength;
    }

    public String nextUrl()
    {
        return generateUrl(counter.incrementAndGet());
    }

    /**
     * Consistently (within same object instance) generates unique long url for provided index.
     */
    public String generateUrl(int index)
    {
        SplittableRandom random = new SplittableRandom(index + seed);
        StringBuilder sb = new StringBuilder(urlLength);
        while (sb.length() < urlLength)
        {
            sb.append(random.nextLong());
        }
        return sb.toString();
    }
}
