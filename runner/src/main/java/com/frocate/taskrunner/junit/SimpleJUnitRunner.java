package com.frocate.taskrunner.junit;

import com.frocate.taskrunner.IncorrectTestException;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.TestResult;
import org.junit.Test;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.RunnerBuilder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class SimpleJUnitRunner
{
    public List<TestResult> runTests(File progressFile, List<Metric> metrics, Class... classes) throws IncorrectTestException
    {
        try
        {
            assertTestsCorrect(classes);

            JUnitCore core = new JUnitCore();

            int totalTests = new Computer().getSuite(new JUnit4Builder(), classes).testCount();
            TestProgressListener listener = new TestProgressListenerImpl(metrics, totalTests, progressFile);
            JUnitResultListener resultListener = new JUnitResultListener(listener);
            Runner runner = new Computer().getSuite(new FailFastRunnerBuilder(listener), classes);

            core.addListener(resultListener);

            core.run(Request.runner(runner));

            return resultListener.getResults();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertTestsCorrect(Class... classes)
    {
        for (Class clazz : classes)
        {
            for (Method method : clazz.getDeclaredMethods())
            {
                Test testAnnotation = method.getAnnotation(Test.class);
                if (testAnnotation != null)
                {
                    if (testAnnotation.timeout() == 0)
                    {
                        //timeout is needed because user app can get stuck
                        throw new IncorrectTestException("Test has no timeout set: " + clazz.getSimpleName() + "." + method.getName());
                    }
                    if (testAnnotation.expected() != Test.None.class)
                    {
                        //when using expected exception we cannot provide user detailed message, so this should be avoided
                        throw new IncorrectTestException("Test should not expect exception: " + clazz.getSimpleName() + "." + method.getName());
                    }
                }
            }
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
