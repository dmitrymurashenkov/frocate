package com.frocate.taskrunner.result;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangeTest
{
    @Test
    public void contains_shouldReturnTrueIfValueIsWithinRange()
    {
        Range range = new Range(0, 2);
        assertFalse(range.contains(-1));
        assertTrue(range.contains(0));
        assertTrue(range.contains(1));
        assertTrue(range.contains(2));
        assertFalse(range.contains(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_shouldThrowExceptionIfEndLessThanStart()
    {
        new Range(2, 1);
    }

    @Test
    public void intersects_shouldReturnTrueIfAnyPointIsWithinRange()
    {
        Range range = new Range(0, 2);
        assertFalse(range.intersects(new Range(-2, -1)));
        assertFalse(range.intersects(new Range(3, 4)));
        assertTrue(range.intersects(new Range(-1, 0)));
        assertTrue(range.intersects(new Range(0, 1)));
        assertTrue(range.intersects(new Range(1, 2)));
        assertTrue(range.intersects(new Range(1, 2)));
        assertTrue(range.intersects(new Range(0, 0)));
        assertTrue(range.intersects(new Range(1, 1)));
        assertTrue(range.intersects(new Range(2, 3)));
        assertTrue(range.intersects(new Range(-1, 3)));
    }
}
