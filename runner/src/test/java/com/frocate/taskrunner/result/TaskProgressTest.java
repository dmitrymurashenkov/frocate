package com.frocate.taskrunner.result;

import com.frocate.taskrunner.FileUtils;
import com.frocate.taskrunner.junit.TaskProgress;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TaskProgressTest
{
    @Test
    public void load_shouldLoadProgressFromFile()
    {
        TaskProgress progress = new TaskProgress(
                Arrays.asList(
                        new TestResult("test1", true, null),
                        new TestResult("test2", false, "Error")
                ),
                Arrays.asList(
                        new Metric("metric1", "Description1", 123, "ms", new Range(0, 50), new Range(51, 200), false),
                        new Metric("metric2", "Description2", 42, "req/seq", new Range(10, 20), new Range(21, 100), true)
                ),
                3,
                "test3",
                15
        );
        File tmp = FileUtils.createTmpFile();
        progress.save(tmp);
        TaskProgress copy = TaskProgress.load(tmp);
        assertEquals(progress, copy);
    }
}
