package com.frocate.taskrunner;

import com.google.common.io.CharStreams;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.result.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.frocate.taskrunner.FileUtils.createTmpFile;
import static com.frocate.taskrunner.FileUtils.createTmpFileWithContent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResultImplTest
{
    @Test
    public void constructor_shouldLoadDataFromFile() throws IOException
    {
        String resultId = "result1";
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

        Result result = new ResultImpl(createTmpFile(), executable, executableLog, testLog, info);
        Result copy = new ResultImpl(result.getFile());

        assertEquals("Executable log", read(copy.getExecutableLog()));
        assertEquals("Test log", read(copy.getTestLog()));
        assertEquals("Executable content", read(copy.getExecutable()));
        assertEquals("Executable content".length(), copy.getExecutableSize());
        assertEquals(resultId, copy.getId());
        assertEquals(creationDate, copy.getStartTime());
        assertEquals(metrics, copy.getMetrics());
        assertEquals(testResults, copy.getTestResults());
        assertEquals(ExecutableType.JAR, copy.getExecutableType());
    }

    private String read(InputStream stream) throws IOException
    {
        return CharStreams.toString(new InputStreamReader(stream));
    }
}
