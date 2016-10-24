package com.frocate.taskrunner.executable;

import java.io.File;

public class Executable
{
    private final File file;
    private final ExecutableType type;

    public Executable(File file, ExecutableType type)
    {
        this.file = file;
        this.type = type;
    }

    public File getFile()
    {
        return file;
    }

    public ExecutableType getType()
    {
        return type;
    }
}
