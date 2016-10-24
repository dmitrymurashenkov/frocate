package com.frocate.taskrunner;

import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.junit.TaskProgress;
import com.frocate.taskrunner.result.ProgressListener;
import com.frocate.taskrunner.result.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.frocate.taskrunner.VMFile.vmFile;

public abstract class AbstractTask implements Task
{
    public static final Logger log = LoggerFactory.getLogger(AbstractTask.class);
    public static final VMFile PROGRESS_FILE_IN_VM = vmFile("taskProgress.json");
    public static final VMFile TESTS_JAR_FILE_IN_VM = vmFile("test.jar");
    public static final VMFile TESTS_CONFIG_FILE_IN_VM = vmFile("config.txt");
    public static final VMFile EXECUTABLE_FILE_IN_VM = vmFile("executable");
    public static final VMFile EXECUTABLE_CONFIG_FILE_IN_VM = vmFile("config.txt");

    protected File executableLog;
    protected File testLog;
    protected TaskProgress progress;

    protected TaskProgress awaitTestsComplete(
            Env env,
            VM testsVM,
            ProcessFuture future,
            int timeoutSeconds,
            ProgressListener listener
    )
    {
        TaskProgress taskProgress = new TaskProgress(new ArrayList<>(), new ArrayList<>(), 0, "Starting tests...", 0);
        listener.onProgress(env.getBuildId(), taskProgress);
        long startTime = System.nanoTime();
        File progressFileOnHost = env.createTmpFile(PROGRESS_FILE_IN_VM.getName());
        while (!future.waitFor(1, TimeUnit.SECONDS))
        {
            if (testsVM.isFileExists(PROGRESS_FILE_IN_VM))
            {
                testsVM.copyToHost(PROGRESS_FILE_IN_VM, progressFileOnHost);
                taskProgress = TaskProgress.load(progressFileOnHost);
                listener.onProgress(env.getBuildId(), taskProgress);
            }
            if (TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS) > timeoutSeconds)
            {
                taskProgress.addTestResult(new TestResult("Tests should finish in " + timeoutSeconds + " seconds", false, "Tests timed out"));
                return taskProgress;
            }
        }
        if (testsVM.isFileExists(PROGRESS_FILE_IN_VM))
        {
            testsVM.copyToHost(PROGRESS_FILE_IN_VM, progressFileOnHost);
            taskProgress = TaskProgress.load(progressFileOnHost);
        }
        else
        {
            log.error("Task progress file not found: buildId=" + env.getBuildId());
            taskProgress.addTestResult(new TestResult("Tests should finish without errors", false, "Tests failed to produce results, seems some internal error"));
        }
        int exitCode = future.exitCode(1, TimeUnit.MILLISECONDS);
        if (exitCode != 0)
        {
            taskProgress.addTestResult(new TestResult("Tests process should finish with code 0", false, "Exit code: " + exitCode));
        }
        return taskProgress;
    }

    @Override
    public TaskResult run(Executable executableOnHost, Env env, ProgressListener listener)
    {
        executableLog = env.createTmpFile("output.log", "");
        testLog = env.createTmpFile("test.log", "");
        progress = new TaskProgress();
        try
        {
            runTests(executableOnHost, env, listener);
        }
        catch (Throwable e)
        {
            progress.addTestResult(new TestResult("Tests should finish without exceptions", false, "Unexpected exception: " + e.getMessage()));
        }
        return buildResult(progress, executableLog, testLog);
    }

    protected abstract void runTests(Executable executableOnHost, Env env, ProgressListener listener);

    protected TaskResult buildResult(TaskProgress progress, File executableLog, File testsLog)
    {
        return new TaskResult(progress.getTotalTests(), progress.getTests(), progress.getMetrics(), executableLog, testsLog);
    }
}
