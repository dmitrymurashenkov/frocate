package com.frocate.taskrunner;

import com.frocate.taskrunner.result.ProgressListener;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import static com.frocate.taskrunner.FileUtils.createTmpFileWithContent;
import static org.junit.Assert.assertEquals;

public class TaskRunnerTest
{
    private MockLog4jAppender appender = new MockLog4jAppender(TaskRunner.class);

    @After
    public void tearDown()
    {
        appender.close();
    }

    @Test
    public void trimLogs_shouldTrimLogsToSpecifiedSize_ifLogFileIsBiggerThanMaxBytes() throws IOException
    {
        File logs = createTmpFileWithContent("0123456789");
        TaskRunner.trimLogs(logs, 9);
        assertEquals("012345678\nLogs trimmed to 9 bytes", new String(Files.readAllBytes(logs.toPath())));
    }

    @Test
    public void trimLogs_shouldDoNothing_ifLogFileIsSmallerThanMaxBytes() throws IOException
    {
        File logs = createTmpFileWithContent("0123456789");
        TaskRunner.trimLogs(logs, 10);
        assertEquals("0123456789", new String(Files.readAllBytes(logs.toPath())));
    }

    @Test
    public void findAndLogErrors_shouldLogErrorLinesFromFileToLogger() throws IOException
    {
        File logs = createTmpFileWithContent(
                "line1\n" +
                "ERROR line2\n" +
                "line3\n" +
                "WARN line4\n" +
                "Error line5\n" +
                "Warn line 6\n" +
                "line7\n" +
                "ERROR line8\n");
        new TaskRunner(1000, 1000, ProgressListener.STUB).findAndLogErrors("build1", logs);
        assertEquals(3, appender.getEventsWithLevel(Level.ERROR).size());
        assertEquals(0, appender.getEventsWithLevel(Level.WARN).size());
        appender.assertContainsMessage(Level.ERROR, "line2");
        appender.assertContainsMessage(Level.ERROR, "line8");
        appender.assertContainsMessage(Level.ERROR, "line4");
    }

    @Test
    public void generateBuildId_shouldGenerateUniqueBuildId()
    {
        TaskRunner.counter.set(0);
        assertEquals("task1__1970-01-01__03h-00m-00s-b1", TaskRunner.generateBuildId("task1", new Date(0)));
        assertEquals("task1__1970-01-01__03h-00m-00s-b2", TaskRunner.generateBuildId("task1", new Date(0)));
        assertEquals("task1__1970-01-01__03h-00m-00s-b3", TaskRunner.generateBuildId("task1", new Date(0)));
    }
}
