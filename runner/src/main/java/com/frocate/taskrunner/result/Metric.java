package com.frocate.taskrunner.result;

public class Metric
{
    public static enum Rating
    {
        BAD,
        GOOD,
        EXCELLENT
    }

    private String name;
    private String description;
    private long value;
    private String unit;
    private Range excellentRange;
    private Range goodRange;
    private boolean error;

    public Metric(String name, String description, long value, String unit, Range excellentRange, Range goodRange, boolean error)
    {
        if (name == null
                || excellentRange == null
                || goodRange == null
                || unit == null
                )
        {
            throw new IllegalArgumentException("Name, unit of measurement and ranges must not be null");
        }
        this.name = name;
        this.description = description;
        this.value = value;
        this.unit = unit;
        this.excellentRange = excellentRange;
        this.goodRange = goodRange;
        this.error = error;
    }

    public String getName()
    {
        return name;
    }

    public long getValue()
    {
        return value;
    }

    public String getUnit()
    {
        return unit;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isError()
    {
        return error;
    }

    public Rating getRating()
    {
        if (error)
        {
            throw new IllegalArgumentException("Metric was not calculated because of error");
        }
        else if (excellentRange.contains(value))
        {
            return Rating.EXCELLENT;
        }
        else if (goodRange.contains(value))
        {
            return Rating.GOOD;
        }
        else
        {
            return Rating.BAD;
        }
    }

    public Range getExcellentRange()
    {
        return excellentRange;
    }

    public Range getGoodRange()
    {
        return goodRange;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Metric metric = (Metric) o;

        if (value != metric.value)
        {
            return false;
        }
        if (error != metric.error)
        {
            return false;
        }
        if (name != null ? !name.equals(metric.name) : metric.name != null)
        {
            return false;
        }
        if (description != null ? !description.equals(metric.description) : metric.description != null)
        {
            return false;
        }
        if (unit != null ? !unit.equals(metric.unit) : metric.unit != null)
        {
            return false;
        }
        if (excellentRange != null ? !excellentRange.equals(metric.excellentRange) : metric.excellentRange != null)
        {
            return false;
        }
        return goodRange != null ? goodRange.equals(metric.goodRange) : metric.goodRange == null;

    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (int) (value ^ (value >>> 32));
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (excellentRange != null ? excellentRange.hashCode() : 0);
        result = 31 * result + (goodRange != null ? goodRange.hashCode() : 0);
        result = 31 * result + (error ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return name + ": " + (isError() ? "ERROR" : (value + " " + unit + " (" + getRating() + ")"));
    }
}
