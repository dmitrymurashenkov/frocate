package com.frocate.taskrunner.result;

import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.Task;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public interface Result
{
    String getId();
    ExecutableType getExecutableType();
    Date getStartTime();

    Task getTask();

    List<TestResult> getTestResults();
    List<Metric> getMetrics();

    long getExecutableSize();

    InputStream getExecutable();
    InputStream getExecutableLog();
    InputStream getTestLog();

    File getFile();
}
