package com.frocate.taskrunner.result;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MetricBuilderTest
{
    private final List<Metric> appendTo = new ArrayList<>();

    @Test
    public void calculate_shouldCalculateMetric() throws Exception
    {
        Metric metric = new MetricBuilder()
                .name("name")
                .unit("unit")
                .description("description")
                .excellentRange(new Range(0, 1))
                .goodRange(new Range(2, 3))
                .calculate(() -> 1L);
        assertFalse(metric.isError());
        assertEquals(1, metric.getValue());
        assertEquals(new Range(0, 1), metric.getExcellentRange());
        assertEquals(new Range(2, 3), metric.getGoodRange());
        assertEquals("name", metric.getName());
        assertEquals("description", metric.getDescription());
    }

    @Test
    public void calculate_shouldAppendMetricToList() throws Exception
    {
        Metric metric = new MetricBuilder()
                .name("name")
                .unit("unit")
                .description("description")
                .excellentRange(new Range(0, 1))
                .goodRange(new Range(2, 3))
                .appendResultTo(appendTo)
                .calculate(() -> 1L);
        assertEquals(1, appendTo.size());
        assertTrue(appendTo.contains(metric));
    }

    @Test
    public void calculate_shouldNotAddMetricToCollection_ifInternalExceptionWasThrown() throws Exception
    {
        assertEquals(0, appendTo.size());
        try
        {
            new MetricBuilder()
                    .name("name")
                    .unit("unit")
                    .description("description")
                    .appendResultTo(appendTo)
                    .calculate(() -> 1L);
            fail("Exception expected");
        }
        catch (IllegalArgumentException e)
        {
            //ok
        }
        assertEquals(0, appendTo.size());
    }

    @Test
    public void calculate_shouldAddMetricToCollection_ifExceptionWasThrownByCallable() throws Exception
    {
        assertEquals(0, appendTo.size());
        try
        {
            new MetricBuilder()
                    .name("name")
                    .unit("unit")
                    .description("description")
                    .excellentRange(new Range(0, 1))
                    .goodRange(new Range(2, 3))
                    .appendResultTo(appendTo)
                    .calculate(() -> {
                        throw new RuntimeException();
                    });
            fail("Exception expected");
        }
        catch (RuntimeException e)
        {
            //ok
        }
        assertEquals(1, appendTo.size());
        Metric metric = appendTo.get(0);
        assertTrue(metric.isError());
        assertEquals("name", metric.getName());
        assertEquals("description", metric.getDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate_shouldThrowException_ifExcellentRangeNotSpecified() throws Exception
    {
        new MetricBuilder()
                .name("name")
                .unit("unit")
                .description("description")
                .goodRange(new Range(0, 1))
                .calculate(() -> 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate_shouldThrowException_ifRangesIntersec() throws Exception
    {
        new MetricBuilder()
                .name("name")
                .unit("unit")
                .description("description")
                .excellentRange(new Range(0, 1))
                .goodRange(new Range(1, 2))
                .calculate(() -> 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate_shouldThrowException_ifGoodRangeNotSpecified() throws Exception
    {
        new MetricBuilder()
                .name("name")
                .unit("unit")
                .description("description")
                .excellentRange(new Range(0, 1))
                .calculate(() -> 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate_shouldThrowException_ifTestDidNotReturnValue() throws Exception
    {
        new MetricBuilder()
                .name("name")
                .unit("unit")
                .description("description")
                .excellentRange(new Range(0, 1))
                .goodRange(new Range(2, 3))
                .calculate(() -> null);
    }
}
