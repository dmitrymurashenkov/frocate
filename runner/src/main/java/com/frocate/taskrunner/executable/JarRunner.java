package com.frocate.taskrunner.executable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JarRunner implements Runner
{
    @Override
    public String buildCommand(File executable, long containerMemoryBytes, String[] args)
    {
        //JVM uses memory for Metaspace and some native memory outside of heap
        long jvmMemory = containerMemoryBytes - 64*1024*1024;
        return "java -Xmx" + jvmMemory + " -Xms" + jvmMemory + " -jar " + executable.getAbsolutePath() + " " + Arrays.asList(args).stream().collect(Collectors.joining(" "));
    }
}
