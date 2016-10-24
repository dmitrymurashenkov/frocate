package com.frocate.taskrunner.result;

import com.frocate.taskrunner.FileUtils;
import com.frocate.taskrunner.junit.TaskProgress;
import com.frocate.taskrunner.junit.TestProgressListener;
import com.frocate.taskrunner.junit.TestProgressListenerImpl;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestProgressListenerImplTest
{
    @Test
    public void constructor_shouldDumpProgressToFile()
    {
        File progressFile = FileUtils.createTmpFile();
        TestProgressListener listener = new TestProgressListenerImpl(new ArrayList<>(), 3, progressFile);

        TaskProgress progress = TaskProgress.load(progressFile);

        assertEquals(3, progress.getTotalTests());
        assertEquals(0, progress.getTests().size());
        assertEquals(0, progress.getMetrics().size());
        assertNull(progress.getCurrentTest());
    }

    @Test
    public void onTestStarted_shouldDumpProgressToFile()
    {
        File progressFile = FileUtils.createTmpFile();
        TestProgressListener listener = new TestProgressListenerImpl(new ArrayList<>(), 3, progressFile);
        listener.onTestStarted("test1");

        TaskProgress progress = TaskProgress.load(progressFile);

        assertEquals(3, progress.getTotalTests());
        assertEquals(0, progress.getTests().size());
        assertEquals(0, progress.getMetrics().size());
        assertEquals("test1", progress.getCurrentTest());
    }

    @Test
    public void onTestFinished_shouldDumpProgressToFile()
    {
        File progressFile = FileUtils.createTmpFile();
        TestProgressListener listener = new TestProgressListenerImpl(new ArrayList<>(), 3, progressFile);
        listener.onTestStarted("test1");
        TestResult result = new TestResult("test1", true, null);
        listener.onTestFinished(result);

        TaskProgress progress = TaskProgress.load(progressFile);

        assertEquals(3, progress.getTotalTests());
        assertEquals(1, progress.getTests().size());
        assertEquals(result, progress.getTests().get(0));
        assertEquals(0, progress.getMetrics().size());
        assertEquals("test1", progress.getCurrentTest());
    }
}
