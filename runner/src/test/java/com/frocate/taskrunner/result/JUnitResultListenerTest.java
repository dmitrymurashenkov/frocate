package com.frocate.taskrunner.result;

import com.frocate.taskrunner.MockLog4jAppender;
import com.frocate.taskrunner.junit.JUnitResultListener;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JUnitResultListenerTest
{
    private MockLog4jAppender appender = new MockLog4jAppender(JUnitResultListener.class);;

    @After
    public void tearDown()
    {
        appender.close();
    }

    @Test
    public void getResults_shouldReturnTestResults()
    {
        JUnitResultListener listener = runTest(MockTest.class);
        assertEquals(Arrays.asList(
                new TestResult("MockTest.failingTest1", false, "Test exception"),
                new TestResult("MockTest.failingTest2", false, "expected:<1> but was:<2>"),
                new TestResult("MockTest.failingTest3", false, ""),
                new TestResult("MockTest.passingTest", true, null)
        ), listener.getResults());
        appender.assertNoMessagesWithLevel(Level.ERROR);
        appender.assertNoMessagesWithLevel(Level.WARN);
    }

    @Test
    public void getResults_shouldReturnReults_ifNoTestsRun() throws Exception
    {
        JUnitResultListener listener = new JUnitResultListener();
        listener.testRunFinished(new Result());
        assertTrue(listener.getResults().isEmpty());
        appender.assertNoMessagesWithLevel(Level.ERROR);
        appender.assertNoMessagesWithLevel(Level.WARN);
    }

    @Test
    public void getErrorLogs_shouldOutputError_ifExceptionInBefore() throws Exception
    {
        JUnitResultListener listener = runTest(MockTestWithFailingBefore.class);
        assertEquals(Arrays.asList(
                new TestResult("MockTestWithFailingBefore.failingTest", false, "Internal error in frocate framework " +
                        "(wow! seems our tests themselves are not that bug-free, but don't worry - this incident is " +
                        "reported and we will fix it right away!), message: Exception in setUp method")
                ),
                listener.getResults());
        appender.assertContainsMessage(Level.ERROR, "Test failed with internal error");
    }

    @Test
    public void getErrorLogs_shouldOutputError_ifExceptionInAfter() throws Exception
    {
        JUnitResultListener listener = runTest(MockTestWithFailingAfter.class);
        assertEquals(Arrays.asList(
                new TestResult("MockTestWithFailingAfter.failingTest", false, "Test exception")
                ),
                listener.getResults());
        appender.assertContainsMessage(Level.ERROR, "Test failed with internal error");
    }

    public static class MockTest
    {
        @Test
        public void passingTest()
        {
        }

        @Test
        public void failingTest1()
        {
            throw new RuntimeException("Test exception");
        }

        @Test
        public void failingTest2()
        {
            assertEquals(1, 2);
        }

        @Test
        public void failingTest3()
        {
            assertTrue(false);
        }
    }

    public static class MockIgnoredTest
    {
        @Test
        @Ignore
        public void ignoredTest()
        {
        }
    }

    public static class MockTestWithFailingBefore
    {
        @Before
        public void setUp() throws Exception
        {
            throw new RuntimeException("Exception in setUp method");
        }

        @Test
        public void failingTest()
        {
            throw new RuntimeException("Test exception");
        }
    }

    public static class MockTestWithFailingAfter
    {
        @After
        public void tearDown() throws Exception
        {
            throw new RuntimeException("Exception in tearDown method");
        }

        @Test
        public void failingTest()
        {
            throw new RuntimeException("Test exception");
        }
    }

    private JUnitResultListener runTest(Class testClass)
    {
        JUnitCore core = new JUnitCore();
        JUnitResultListener listener = new JUnitResultListener();
        core.addListener(listener);
        core.run(testClass);
        return listener;
    }
}
