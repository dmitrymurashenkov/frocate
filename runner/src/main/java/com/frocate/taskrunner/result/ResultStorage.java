package com.frocate.taskrunner.result;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class ResultStorage
{
    private final File dir;

    public ResultStorage(File dir)
    {
        if (!dir.exists() || !dir.isDirectory())
        {
            throw new IllegalArgumentException("Dir not exists or not a directory: " + dir.getAbsolutePath());
        }
        this.dir = dir;
    }

    public void add(Result result)
    {
        try
        {
            Files.copy(result.getFile(), resultIdToFile(result.getId()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Result get(String buildId)
    {
        File result = resultIdToFile(buildId);
        if (result.exists())
        {
            return new ResultImpl(result);
        }
        else
        {
            return null;
        }
    }

    private File resultIdToFile(String resultId)
    {
        return new File(dir, resultId + ".zip");
    }
}
