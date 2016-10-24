package com.frocate.taskrunner;

import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.TestResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to return junit tests result from VM
 */
public class TaskResult
{
    private int totalTests;
    private List<TestResult> tests = new ArrayList<>();
    private List<Metric> metrics = new ArrayList<>();
    private File executableLog;
    private File testLog;

    public TaskResult() {}

    public TaskResult(int totalTests, List<TestResult> tests, List<Metric> metrics, File executableLog, File testLog)
    {
        this.totalTests = totalTests;
        this.tests = Collections.unmodifiableList(tests);
        this.metrics = Collections.unmodifiableList(metrics);
        this.executableLog = executableLog;
        this.testLog = testLog;
    }

    public int getTotalTests()
    {
        return totalTests;
    }

    public List<TestResult> getTests()
    {
        return tests;
    }

    public List<Metric> getMetrics()
    {
        return metrics;
    }

    public File getExecutableLog()
    {
        return executableLog;
    }

    public File getTestLog()
    {
        return testLog;
    }

    public List<TestResult> getPassedTests()
    {
        return tests.stream().filter(TestResult::isSuccess).collect(Collectors.toList());
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

        TaskResult that = (TaskResult) o;

        if (tests != null ? !tests.equals(that.tests) : that.tests != null)
        {
            return false;
        }
        if (metrics != null ? !metrics.equals(that.metrics) : that.metrics != null)
        {
            return false;
        }
        if (executableLog != null ? !executableLog.equals(that.executableLog) : that.executableLog != null)
        {
            return false;
        }
        return testLog != null ? testLog.equals(that.testLog) : that.testLog == null;

    }

    @Override
    public int hashCode()
    {
        int result = tests != null ? tests.hashCode() : 0;
        result = 31 * result + (metrics != null ? metrics.hashCode() : 0);
        result = 31 * result + (executableLog != null ? executableLog.hashCode() : 0);
        result = 31 * result + (testLog != null ? testLog.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return getPassedTests().size() + "/" + getTests().size() + " PASSED";
    }
}
