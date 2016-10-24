package com.frocate.taskrunner.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class MetricBuilder
{
    public static final Logger log = LoggerFactory.getLogger(MetricBuilder.class);

    private String name;
    private String description;
    private String unit;
    private Range excellentRange;
    private Range goodRange;
    private List<Metric> appendTo;

    public MetricBuilder name(String name)
    {
        this.name = name;
        return this;
    }

    public MetricBuilder unit(String unit)
    {
        this.unit = unit;
        return this;
    }

    public MetricBuilder appendResultTo(List<Metric> appendTo)
    {
        this.appendTo = appendTo;
        return this;
    }

    public MetricBuilder description(String description)
    {
        this.description = description;
        return this;
    }

    public MetricBuilder excellentRange(Range excellentRange)
    {
        this.excellentRange = excellentRange;
        return this;
    }

    public MetricBuilder goodRange(Range goodRange)
    {
        this.goodRange = goodRange;
        return this;
    }

    public Metric calculate(Callable<Long> test) throws Exception
    {
        Metric result = null;
        if (excellentRange == null || goodRange == null)
        {
            throw new IllegalArgumentException("Ranges must not be null");
        }
        if (excellentRange.intersects(goodRange))
        {
            throw new IllegalArgumentException("Ranges must not intersect");
        }
        try
        {
            Long value = test.call();
            if (value == null)
            {
                throw new IllegalArgumentException("Metric was not calculated");
            }
            result = new Metric(
                    name,
                    description,
                    value,
                    unit,
                    excellentRange,
                    goodRange,
                    false
            );
        }
        catch (Exception e)
        {
            result = new Metric(
                    name,
                    description,
                    0,
                    unit,
                    excellentRange,
                    goodRange,
                    true
            );
            throw e;
        }
        finally
        {
            if (result != null && appendTo != null)
            {
                appendTo.add(result);
            }
        }
        return result;
    }
}
