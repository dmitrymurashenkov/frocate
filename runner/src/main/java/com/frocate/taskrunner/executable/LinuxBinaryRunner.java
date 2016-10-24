package com.frocate.taskrunner.executable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinuxBinaryRunner implements Runner
{

    @Override
    public String buildCommand(File executable, long containerMemoryBytes, String[] args)
    {
        return "chmod +x " + executable.getAbsolutePath() + " && " + executable.getAbsolutePath() + " " + Arrays.asList(args).stream().collect(Collectors.joining(" "));
    }
}
