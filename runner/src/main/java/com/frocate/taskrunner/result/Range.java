package com.frocate.taskrunner.result;

public class Range
{
    private final long startInclusive;
    private final long endInclusive;

    public Range(long startInclusive, long endInclusive)
    {
        if (endInclusive < startInclusive)
        {
            throw new IllegalArgumentException("Start must be less than end");
        }
        this.startInclusive = startInclusive;
        this.endInclusive = endInclusive;
    }

    public boolean intersects(Range other)
    {
        return this.contains(other.startInclusive)
                || this.contains(other.endInclusive)
                || other.contains(startInclusive)
                || other.contains(endInclusive);
    }

    public boolean contains(long value)
    {
        return value >= startInclusive && value <= endInclusive;
    }

    public long getStartInclusive()
    {
        return startInclusive;
    }

    public long getEndInclusive()
    {
        return endInclusive;
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

        Range range = (Range) o;

        if (startInclusive != range.startInclusive)
        {
            return false;
        }
        return endInclusive == range.endInclusive;

    }

    @Override
    public int hashCode()
    {
        int result = (int) (startInclusive ^ (startInclusive >>> 32));
        result = 31 * result + (int) (endInclusive ^ (endInclusive >>> 32));
        return result;
    }
}
