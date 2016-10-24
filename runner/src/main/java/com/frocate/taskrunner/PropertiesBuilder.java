package com.frocate.taskrunner;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PropertiesBuilder
{
    private final LinkedHashMap<String, String> properties = new LinkedHashMap<>();

    public PropertiesBuilder add(String key, String value)
    {
        properties.put(key, value);
        return this;
    }

    @Override
    public String toString()
    {
        List<String> lines = properties.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.toList());
        return lines.stream().collect(Collectors.joining("\n"));
    }

    public File toFile(File file)
    {
        try
        {
            Files.write(file.toPath(), toString().getBytes());
            return file;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
