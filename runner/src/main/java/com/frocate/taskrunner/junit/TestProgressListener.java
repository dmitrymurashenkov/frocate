package com.frocate.taskrunner.junit;

import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.TestResult;

import java.util.ArrayList;
import java.util.List;

public interface TestProgressListener
{
    void onTestStarted(String name);
    void onTestFinished(TestResult result);
    List<TestResult> getTestResults();

    public static final TestProgressListener STUB = new TestProgressListener()
    {
        @Override
        public void onTestStarted(String name) {}

        @Override
        public void onTestFinished(TestResult result) {}

        @Override
        public List<TestResult> getTestResults()
        {
            return new ArrayList<>();
        }
    };
}
