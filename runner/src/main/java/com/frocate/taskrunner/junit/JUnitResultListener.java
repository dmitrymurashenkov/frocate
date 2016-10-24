package com.frocate.taskrunner.junit;

import com.frocate.taskrunner.result.TestResult;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JUnitResultListener extends RunListener
{
    public static final Logger log = LoggerFactory.getLogger(JUnitResultListener.class);

    private final LinkedHashMap<Description, TestResult> testResults = new LinkedHashMap<>();
    private final TestProgressListener listener;

    public JUnitResultListener()
    {
        this(TestProgressListener.STUB);
    }

    public JUnitResultListener(TestProgressListener listener)
    {
        this.listener = listener;
    }

    @Override
    public synchronized void testStarted(Description description) throws Exception
    {
        log.info("Test '" + description + "' started");
        listener.onTestStarted(getTestName(description));
    }

    @Override
    public synchronized void testRunFinished(Result result) throws Exception
    {
    }

    @Override
    public synchronized void testFailure(Failure failure) throws Exception
    {
        if (Description.TEST_MECHANISM.equals(failure.getDescription()))
        {
            log.error("Test failed with internal error", failure.getException());
        }
        Description description = failure.getDescription();
        if (isInternalFailure(failure))
        {
            log.error("Test failed with internal error", failure.getException());
            //if error occured in @After then frocate have passed so no need to overwrite it's result
            onTestFinished(failure.getDescription(), new TestResult(getTestName(description), false,
                    "Internal error in frocate framework (wow! seems our tests themselves are not that bug-free, " +
                            "but don't worry - this incident is reported and we will fix it right away!), message: " + failure.getMessage()));
        }
        else
        {
            //if error occured in @Before then frocate itself is not run, so result should be absent in map at this moment
            TestResult newResult = new TestResult(getTestName(description), false, failure.getMessage() == null ? "" : failure.getMessage());
            TestResult prevResult = onTestFinished(failure.getDescription(), newResult);
            if (prevResult != null)
            {
                log.warn("Test failed but some other failure was already recorded: prev=" + prevResult + ", new=" + newResult, failure.getException());
            }
            else
            {
                log.info("Test '" + description + "' failed: " + failure.getMessage(), failure.getException());
            }
        }
    }

    @Override
    public synchronized void testFinished(Description description) throws Exception
    {
        log.info("Test '" + description + "' finished");
        onTestFinished(description, new TestResult(getTestName(description), true, null));
    }

    private synchronized TestResult onTestFinished(Description test, TestResult result)
    {
        TestResult prev = testResults.putIfAbsent(test, result);
        if (prev == null)
        {
            listener.onTestFinished(result);
        }
        return prev;
    }

    @Override
    public synchronized void testIgnored(Description description) throws Exception
    {
    }

    private String getTestName(Description description)
    {
        return description.getTestClass().getSimpleName() + "." + description.getMethodName();
    }

    /**
     * Returns false if this is ordinary assumption failure on exception inside frocate method and true otherwise.
     * Internal failure means that our tests are written incorrectly and must be fixed.
     */
    private boolean isInternalFailure(Failure failure)
    {
        //since junit doesn't provide approach to distinguish between exceptions from @Before/@After methods and
        //actual frocate failures we assume that if exception occurred outside frocate method - it is some internal problem
        Throwable e = failure.getException();
        while (e != null)
        {
            for (StackTraceElement line : e.getStackTrace())
            {
                if (line.getClassName().equals(failure.getDescription().getClassName())
                        && line.getMethodName().equals(failure.getDescription().getMethodName()))
                {
                    return false;
                }
            }
            e = e.getCause();
        }
        return true;
    }

    public synchronized List<TestResult> getResults()
    {
        return new ArrayList<>(testResults.values());
    }
}
