package com.frocate.taskrunner;

import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.Range;
import com.frocate.taskrunner.result.ResultInfo;
import com.frocate.taskrunner.result.TestResult;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import static com.frocate.taskrunner.FileUtils.createTmpFile;
import static org.junit.Assert.assertEquals;

public class ResultInfoTest
{
    @Test
    public void save_shouldSaveDataToFile() throws IOException
    {
        ResultInfo info = new ResultInfo(
                "result1",
                ExecutableType.JAR,
                new Date(0),
                "task1",
                Arrays.asList(
                        new TestResult("test1", true, null),
                        new TestResult("test2", false, "Error message")
                        ),
                Arrays.asList(
                        new Metric("metric1", null, 1, "unit", new Range(0, 1), new Range(2, 3), false),
                        new Metric("metric2", null, 2, "unit", new Range(0, 1), new Range(2, 3), false)
                )
        );

        File tmp = createTmpFile();

        info.save(tmp);
        ResultInfo copy = ResultInfo.load(tmp);
        assertEquals(info, copy);
    }

    @Test
    public void toJson_shouldConvertObjectToJsonStream() throws IOException
    {
        ResultInfo info = new ResultInfo(
                "result1",
                ExecutableType.JAR,
                new Date(0),
                "task1",
                Arrays.asList(
                        new TestResult("test1", true, null),
                        new TestResult("test2", false, "Error message")
                ),
                Arrays.asList(
                        new Metric("metric1", null, 1, "unit", new Range(0, 1), new Range(2, 3), false),
                        new Metric("metric2", null, 2, "unit", new Range(0, 1), new Range(2, 3), false)
                )
        );

        InputStream stream = new ByteArrayInputStream(info.toJson().getBytes());
        ResultInfo copy = ResultInfo.load(stream);

        assertEquals(info, copy);
    }
}
