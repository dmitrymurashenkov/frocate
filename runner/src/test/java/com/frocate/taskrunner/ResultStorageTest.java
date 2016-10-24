package com.frocate.taskrunner;

import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.result.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.frocate.taskrunner.FileUtils.*;
import static org.junit.Assert.*;

public class ResultStorageTest
{
    @Test
    public void get_shouldReturnAddedResult() throws IOException
    {
        File dir = createTmpFile();
        try
        {
            dir.delete();
            dir.mkdirs();

            Result result1 = createResult();

            ResultStorage storage = new ResultStorage(dir);
            storage.add(result1);
            Result result2 = storage.get(result1.getId());

            assertEquals(result1.getId(), result2.getId());
            assertEquals(result1.getExecutableSize(), result2.getExecutableSize());
            assertEquals(result1.getExecutableType(), result2.getExecutableType());
            assertEquals(result1.getTestResults(), result2.getTestResults());
            assertEquals(result1.getMetrics(), result2.getMetrics());
        }
        finally
        {
            deleteDirRecursive(dir);
        }
    }

    @Test
    public void get_shouldReturnNull_ifResultNotFound() throws IOException
    {
        File dir = createTmpFile();
        dir.delete();
        dir.mkdirs();
        assertNull(new ResultStorage(dir).get("buildId"));
    }

    private Result createResult() throws IOException
    {
        String resultId = UUID.randomUUID().toString();
        Date creationDate = new Date(0);
        List<TestResult> testResults = Arrays.asList(
                new TestResult("test1", true, null),
                new TestResult("test2", false, "Error message")
        );
        List<Metric> metrics = Arrays.asList(
                new Metric("metric1", null, 1, "unit", new Range(0, 1), new Range(2, 3), false),
                new Metric("metric2", null, 2, "unit", new Range(0, 1), new Range(2, 3), false)
        );
        ResultInfo info = new ResultInfo(
                resultId,
                ExecutableType.JAR,
                creationDate,
                "task1",
                testResults,
                metrics
        );

        File executableFile = createTmpFileWithContent("Executable content");
        File executableLog = createTmpFileWithContent("Executable log");
        File testLog = createTmpFileWithContent("Test log");
        Executable executable = new Executable(executableFile, ExecutableType.JAR);

        return new ResultImpl(createTmpFile(), executable, executableLog, testLog, info);
    }
}
