package com.frocate.taskrunner;

import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ProcessFuture
{
    public static final Logger log = LoggerFactory.getLogger(ProcessFuture.class);

    public static final int MAX_LOG_BYTES = 10*1024;

    private final Process process;
    private final StringBuffer stdout = new StringBuffer();
    private final StringBuffer stderr = new StringBuffer();
    private final Thread stderrReaderThread;
    private final Thread stdoutReaderThread;

    public ProcessFuture(Process process)
    {
        this.process = process;
        stdoutReaderThread = appendProcessOutputToBuffer(process.getInputStream(), stdout);
        stderrReaderThread = appendProcessOutputToBuffer(process.getErrorStream(), stderr);
        stdoutReaderThread.start();
        stderrReaderThread.start();
    }

    public boolean waitFor(long timeout, TimeUnit unit)
    {
        try
        {
            return process.waitFor(timeout, unit);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public int exitCode(long timeout, TimeUnit unit) throws ProcessTimeoutException
    {
        if (waitFor(timeout, unit))
        {
            return process.exitValue();
        }
        else
        {
            throw createException();
        }
    }

    public String stdout(long timeout, TimeUnit unit) throws ProcessTimeoutException
    {
        exitCode(timeout, unit);
        join(stdoutReaderThread);
        return stdout.toString();
    }

    public String stderr(long timeout, TimeUnit unit)
    {
        exitCode(timeout, unit);
        join(stderrReaderThread);
        return stderr.toString();
    }

    private void join(Thread threadToJoin)
    {
        try
        {
            threadToJoin.join(50000);
            if (threadToJoin.isAlive())
            {
                throw new ProcessTimeoutException("Timeout waiting stdout/stderr reading thread to read all data after process has ended");
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Thread appendProcessOutputToBuffer(InputStream stream, StringBuffer appendTo)
    {
        return new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    boolean tooMuchOutput = false;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        if (!tooMuchOutput)
                        {
                            if (appendTo.length() > 0)
                            {
                                appendTo.append("\n");
                            }
                            appendTo.append(line);
                            if (appendTo.length() > MAX_LOG_BYTES)
                            {
                                appendTo.append("\n").append("Too much output! " + MAX_LOG_BYTES + " bytes of output received - ignoring further output");
                                tooMuchOutput = true;
                            }
                        }
                        log.info("Process output: " + line);
                    }
                }
                catch (Exception e)
                {
                    log.error("Error reading process output: " + e.getMessage(), e);
                }
            }
        };
    }

    protected ProcessTimeoutException createException()
    {
        return new ProcessTimeoutException("Process timed out: " + process);
    }
}
