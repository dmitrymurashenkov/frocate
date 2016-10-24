package com.frocate.money.transfer.mock;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.frocate.money.transfer.mock.TxTools.parseCommaSeparatedString;
import static org.junit.Assert.assertEquals;

public class TxToolsTest
{
    @Test
    public void parseCommaSeparatedString_shouldReturnExpectedResults()
    {
        assertEquals(list(), parseCommaSeparatedString(null));
        assertEquals(list(), parseCommaSeparatedString(""));
        assertEquals(list(), parseCommaSeparatedString(" "));
        assertEquals(list(), parseCommaSeparatedString(","));
        assertEquals(list(), parseCommaSeparatedString(" ,"));
        assertEquals(list(), parseCommaSeparatedString(", "));
        assertEquals(list(), parseCommaSeparatedString(" , "));
        assertEquals(list(), parseCommaSeparatedString(",,"));
        assertEquals(list(), parseCommaSeparatedString(" , , "));

        assertEquals(list("1"), parseCommaSeparatedString("1"));
        assertEquals(list("1"), parseCommaSeparatedString(" 1"));
        assertEquals(list("1"), parseCommaSeparatedString("1 "));
        assertEquals(list("1"), parseCommaSeparatedString(" 1 "));
        assertEquals(list("1"), parseCommaSeparatedString("1,"));
        assertEquals(list("1"), parseCommaSeparatedString(",1"));
        assertEquals(list("1"), parseCommaSeparatedString(",1,"));
        assertEquals(list("1"), parseCommaSeparatedString(",1,"));
        assertEquals(list("1"), parseCommaSeparatedString(", 1,"));
        assertEquals(list("1"), parseCommaSeparatedString(",1 ,"));
        assertEquals(list("1"), parseCommaSeparatedString(", 1 ,"));

        assertEquals(list("1", "2"), parseCommaSeparatedString("1,2"));
        assertEquals(list("1", "2"), parseCommaSeparatedString("1,2,"));
        assertEquals(list("1", "2"), parseCommaSeparatedString(",1,2,"));
        assertEquals(list("1", "2"), parseCommaSeparatedString(", 1,2 ,"));
    }

    private List<String> list(String... values)
    {
        return Arrays.asList(values);
    }
}
