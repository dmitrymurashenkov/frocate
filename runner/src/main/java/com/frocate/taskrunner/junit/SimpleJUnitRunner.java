package com.frocate.taskrunner.junit;

import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.TestResult;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.RunnerBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class SimpleJUnitRunner
{
    public List<TestResult> runTests(File progressFile, List<Metric> metrics, Class... classes)
    {
        try
        {
            JUnitCore core = new JUnitCore();

            int totalTests = new Computer().getSuite(new JUnit4Builder(), classes).testCount();
            TestProgressListener listener = new TestProgressListenerImpl(metrics, totalTests, progressFile);
            JUnitResultListener resultListener = new JUnitResultListener(listener);
            Runner runner = new Computer().getSuite(new FailFastRunnerBuilder(listener), classes);

            core.addListener(resultListener);

            core.run(Request.runner(runner));

            return resultListener.getResults();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Skips all test if any single test has failed
     */
    static class FailFastRunnerBuilder extends RunnerBuilder
    {
        private final TestProgressListener listener;

        public FailFastRunnerBuilder(TestProgressListener listener)
        {
            this.listener = listener;
        }

        @Override
        public Runner runnerForClass(Class<?> testClass) throws Throwable
        {
            return new BlockJUnit4ClassRunner(testClass)
            {
                @Override
                protected void runChild(FrameworkMethod method, RunNotifier notifier)
                {
                    if (!hasFailedTests())
                    {
                        super.runChild(method, notifier);
                    }
                }
            };
        }

        private boolean hasFailedTests()
        {
            return listener.getTestResults()
                    .stream()
                    .filter(result -> !result.isSuccess())
                    .count() > 0;
        }
    }
}
