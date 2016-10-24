package com.frocate.taskrunner.executable;

import java.io.File;
import java.util.List;

public interface Runner
{
    public static final long MB = 1024*1024;

    /**
     * Returned command is passed to "/bin/bash -c" as argument
     * @param containerMemoryBytes total RAM available for container (for example JVM should have heap a bit smaller than that)
     */
    String buildCommand(File executable, long containerMemoryBytes, String[] args);
}
