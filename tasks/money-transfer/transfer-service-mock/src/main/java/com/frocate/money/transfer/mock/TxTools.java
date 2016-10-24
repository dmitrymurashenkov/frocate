package com.frocate.money.transfer.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TxTools
{
    public static List<String> parseCommaSeparatedString(String input)
    {
        return parseSeparatedString(input, ",");
    }

    public static List<String> parseSeparatedString(String input, String separator)
    {
        if (input == null)
        {
            return new ArrayList<>();
        }
        return Stream.of(input.split(separator))
                .filter(s -> !"".equals(s.trim()))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
