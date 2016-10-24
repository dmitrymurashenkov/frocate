package com.frocate.taskrunner;

import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.result.ProgressListener;
import com.frocate.taskrunner.result.Result;
import com.frocate.taskrunner.result.ResultImpl;
import com.frocate.taskrunner.result.ResultInfo;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.frocate.taskrunner.FileUtils.createTmpFile;

public class TaskRunner
{
    public static final Logger log = LoggerFactory.getLogger(TaskRunner.class);

    static AtomicLong counter = new AtomicLong();
    private final long maxTestLogSize;
    private final long maxExecutableLogSize;
    private final ProgressListener listener;

    public TaskRunner(long maxTestLogSize, long maxExecutableLogSize, ProgressListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Progress listener must not be null");
        }
        this.maxTestLogSize = maxTestLogSize;
        this.maxExecutableLogSize = maxExecutableLogSize;
        this.listener = listener;
    }

    public static String generateBuildId(String taskId, Date currentDate)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd__HH'h'-mm'm'-ss's'-'b'" + counter.incrementAndGet());
        return taskId + "__" + format.format(currentDate);
    }

    public Result run(String buildId, Executable executable, Task task)
    {
        log.info("Build '{}' starting", buildId, task.getId());
        try (Env env = new EnvImpl(buildId))
        {
            Date startTime = new Date();
            TaskResult taskResult = task.run(executable, env, listener);
            log.info("Build '{}' finished: {}, executable log size: {}, test log size: {}",
                    buildId,
                    taskResult.getPassedTests().size() + "/" + taskResult.getTests().size() + " PASSED",
                    taskResult.getExecutableLog().length(),
                    taskResult.getTestLog().length()
            );
            ResultInfo info = new ResultInfo(
                    buildId,
                    executable.getType(),
                    startTime,
                    task.getId(),
                    taskResult.getTests(),
                    taskResult.getMetrics()
            );
            File tmpFile = createTmpFile();
            findAndLogErrors(buildId, taskResult.getTestLog());
            return new ResultImpl(
                    tmpFile,
                    executable,
                    trimLogs(taskResult.getExecutableLog(), maxExecutableLogSize),
                    //todo in case we need to trim - preserve ERROR messages?
                    trimLogs(taskResult.getTestLog(), maxTestLogSize),
                    info
            );
        }
    }

    void findAndLogErrors(String buildId, File logFile)
    {
        try
        {
            Files.readLines(logFile, Charset.defaultCharset(), new LineProcessor<Object>()
            {
                @Override
                public boolean processLine(String line) throws IOException
                {
                    if (line.contains("ERROR") || line.contains("WARN"))
                    {
                        log.error("Error during build '" + buildId + "': " + line);
                    }
                    return true;
                }

                @Override
                public Object getResult()
                {
                    return null;
                }
            });
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    static File trimLogs(File logs, long maxBytes)
    {
        try (RandomAccessFile file = new RandomAccessFile(logs, "rw"))
        {
            if (logs.length() > maxBytes)
            {
                file.setLength(maxBytes);
                file.seek(maxBytes);
                file.write(("\nLogs trimmed to " + maxBytes + " bytes").getBytes());
            }
            return logs;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
