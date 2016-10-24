package com.frocate.taskrunner.result;

import com.frocate.taskrunner.FileUtils;
import com.frocate.taskrunner.junit.SimpleJUnitRunner;
import com.frocate.taskrunner.junit.TaskProgress;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class SimpleJUnitRunnerTest
{
    private final File progressFile = FileUtils.createTmpFile();
    private final List<Metric> metrics = new ArrayList<>();

    @Test
    public void runTests_shouldProduceProgressFile()
    {
        new SimpleJUnitRunner().runTests(progressFile, metrics, Test1.class);
        TaskProgress progress = TaskProgress.load(progressFile);

        assertEquals(1, progress.getTotalTests());
        assertEquals(Arrays.asList(new TestResult("Test1.test1", true, null)), progress.getTests());
        assertEquals(metrics, progress.getMetrics());
        assertEquals("Test1.test1", progress.getCurrentTest());
    }

    @Test
    public void runTests_shouldStopAfterFirstFailedTest()
    {
        new SimpleJUnitRunner().runTests(progressFile, metrics, Test2.class);
        TaskProgress progress = TaskProgress.load(progressFile);

        assertEquals(3, progress.getTotalTests());
        assertEquals(Arrays.asList(
                new TestResult("Test2.test1", true, null),
                new TestResult("Test2.test2", false, "Error")
            ), progress.getTests());
        assertEquals(metrics, progress.getMetrics());
        assertEquals("Test2.test2", progress.getCurrentTest());
    }

    @Test
    public void runTests_shouldOutputProgressFileAfterEachTest() throws InterruptedException
    {
        new Thread()
        {
            @Override
            public void run()
            {
                new SimpleJUnitRunner().runTests(progressFile, metrics, Test3.class);
            }
        }.start();

        Test3.latch1.await(3, TimeUnit.SECONDS);

        TaskProgress progress = TaskProgress.load(progressFile);
        assertEquals(2, progress.getTotalTests());
        assertEquals(Arrays.asList(
                new TestResult("Test3.test1", true, null)
        ), progress.getTests());
        assertEquals(metrics, progress.getMetrics());
        assertEquals("Test3.test2", progress.getCurrentTest());

        Test3.latch2.countDown();
    }

    public static class Test1
    {
        @Test
        public void test1() {}
    }

    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class Test2
    {
        @Test public void test1() {}

        @Test public void test2() { fail("Error"); }

        @Test public void test3() { }
    }

    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class Test3
    {
        private static final CountDownLatch latch1 = new CountDownLatch(1);
        private static final CountDownLatch latch2 = new CountDownLatch(1);

        @Test public void test1() {}

        @Test public void test2() throws InterruptedException
        {
            latch1.countDown();
            latch2.await(3, TimeUnit.SECONDS);
        }
    }
}
