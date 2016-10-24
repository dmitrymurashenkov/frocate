package com.frocate.taskrunner.junit;

import com.frocate.taskrunner.FileUtils;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestProgressListenerImpl implements TestProgressListener
{
    public static final Logger log = LoggerFactory.getLogger(TestProgressListenerImpl.class);

    private final File progressFile;
    private final long startTimeNanos;
    private final int totalTests;
    private final List<TestResult> testResults = new ArrayList<>();
    private final List<Metric> metrics;
    private String currentTest;

    public TestProgressListenerImpl(List<Metric> metrics, int totalTests, File progressFile)
    {
        this.metrics = metrics;
        this.totalTests = totalTests;
        this.startTimeNanos = System.nanoTime();
        this.progressFile = progressFile;
        dumpProgressToFile();
    }

    @Override
    public void onTestStarted(String name)
    {
        currentTest = name;
        dumpProgressToFile();
    }

    @Override
    public void onTestFinished(TestResult result)
    {
        testResults.add(result);
        dumpProgressToFile();
        currentTest = null;
    }

    @Override
    public List<TestResult> getTestResults()
    {
        return testResults;
    }

    private void dumpProgressToFile()
    {
        long runningTimeNanos = System.nanoTime() - startTimeNanos;
        File tmp = FileUtils.createTmpFile();
        new TaskProgress(
                testResults,
                metrics,
                totalTests,
                currentTest,
                TimeUnit.SECONDS.convert(runningTimeNanos, TimeUnit.NANOSECONDS)
                ).save(tmp);
        //need to atmoically replace the file in case other process is reading it
        if (!tmp.renameTo(progressFile))
        {
            log.warn("Failed to overwrite progress file: " + progressFile.getAbsolutePath());
        }
    }
}
