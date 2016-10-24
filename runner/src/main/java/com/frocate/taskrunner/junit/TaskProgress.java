package com.frocate.taskrunner.junit;

import com.frocate.taskrunner.TaskResult;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.TestResult;
import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TaskProgress
{
    private List<TestResult> tests = new ArrayList<>();
    private List<Metric> metrics = new ArrayList<>();
    private int totalTests;
    private String currentTest;
    private long runningTimeSeconds;

    public TaskProgress()
    {
        this(new ArrayList<TestResult>(), new ArrayList<Metric>(), 0, null, 0);
    }

    public TaskProgress(List<TestResult> tests, List<Metric> metrics, int totalTests, String currentTest, long runningTimeSeconds)
    {
        this.tests = new ArrayList<>(tests);
        this.metrics = new ArrayList<>(metrics);
        this.totalTests = totalTests;
        this.currentTest = currentTest;
        this.runningTimeSeconds = runningTimeSeconds;
    }

    public int getTotalTests()
    {
        return totalTests;
    }

    public void addTestResult(TestResult result)
    {
        tests.add(result);
    }

    public List<TestResult> getTests()
    {
        return tests;
    }

    public List<Metric> getMetrics()
    {
        return metrics;
    }

    public String getCurrentTest()
    {
        return currentTest;
    }

    public long getRunningTimeSeconds()
    {
        return runningTimeSeconds;
    }

    public String toJson()
    {
        return new Gson().toJson(this);
    }

    public void save(File file)
    {
        try
        {
            Files.write(toJson().getBytes(), file);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static TaskProgress load(File file)
    {
        try
        {
            return new Gson().fromJson(new FileReader(file), TaskProgress.class);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        TaskProgress progress = (TaskProgress) o;

        if (totalTests != progress.totalTests)
        {
            return false;
        }
        if (runningTimeSeconds != progress.runningTimeSeconds)
        {
            return false;
        }
        if (tests != null ? !tests.equals(progress.tests) : progress.tests != null)
        {
            return false;
        }
        if (metrics != null ? !metrics.equals(progress.metrics) : progress.metrics != null)
        {
            return false;
        }
        return currentTest != null ? currentTest.equals(progress.currentTest) : progress.currentTest == null;

    }

    @Override
    public int hashCode()
    {
        int result = tests != null ? tests.hashCode() : 0;
        result = 31 * result + (metrics != null ? metrics.hashCode() : 0);
        result = 31 * result + totalTests;
        result = 31 * result + (currentTest != null ? currentTest.hashCode() : 0);
        result = 31 * result + (int) (runningTimeSeconds ^ (runningTimeSeconds >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return tests.size() + "/" + totalTests + " test passed, currentTest=" + currentTest;
    }
}
