package com.frocate.taskrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils
{
    public static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static File createTmpFile()
    {
        try
        {
            File tmp = File.createTempFile("tmp", null).getAbsoluteFile();
            tmp.deleteOnExit();
            return tmp;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static File createTmpFileWithContent(String content)
    {
        try
        {
            File tmp = File.createTempFile("tmp", null).getAbsoluteFile();
            tmp.deleteOnExit();
            Files.write(tmp.toPath(), content.getBytes());
            return tmp;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String readContent(File file)
    {
        try
        {
            return new String(Files.readAllBytes(file.toPath()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void deleteDirRecursive(File file)
    {
        File[] children = file.listFiles();
        if (children != null)
        {
            for (File child : children)
            {
                deleteDirRecursive(child);
            }
        }
        if (!file.delete())
        {
            System.out.println("Failed to cleanup file: " + file);
        }
    }
}
