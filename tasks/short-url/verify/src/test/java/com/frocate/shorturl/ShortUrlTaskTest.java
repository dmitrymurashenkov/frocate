package com.frocate.shorturl;

import com.frocate.shorturl.mock.MockServiceMain;
import com.frocate.shorturl.test.ShortUrlTask;
import com.frocate.taskrunner.Env;
import com.frocate.taskrunner.EnvImpl;
import com.frocate.taskrunner.Task;
import com.frocate.taskrunner.TaskRunner;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.junit.TaskProgress;
import com.frocate.taskrunner.result.ProgressListener;
import com.frocate.taskrunner.result.Result;
import com.frocate.taskrunner.result.TestResult;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShortUrlTaskTest
{
    @Test
    public void test() throws Exception
    {
        Result result = null;
        try
        {
            String buildId = "build1";
            Executable executable = getExecutable();
            Task task = new ShortUrlTask();
            Set<TaskProgress> taskProgress = new HashSet<>();
            ProgressListener listener = (actualBuildId, progress) -> {
                if (actualBuildId.equals(buildId))
                {
                    taskProgress.add(progress);
                    System.out.println("Progress: " + progress);
                }
            };

            result = new TaskRunner(Long.MAX_VALUE, Long.MAX_VALUE, listener)
                    .run(
                            buildId,
                            executable,
                            task
                    );

            int totalTests = 18;
            int totalMetrics = 6;
            assertTrue(CharStreams.toString(new InputStreamReader(result.getExecutableLog())).length() > 0);
            assertTrue(CharStreams.toString(new InputStreamReader(result.getTestLog())).length() > 0);
            assertEquals(totalTests, result.getTestResults().stream().filter(TestResult::isSuccess).count());
            assertEquals(0, result.getTestResults().stream().filter((testResult) -> !testResult.isSuccess()).count());
            assertEquals(totalMetrics, result.getMetrics().size());

            result.getFile().delete();
        }
        catch (AssertionError e)
        {
            if (result != null)
            {
                System.out.println("Some test failed, individual results: ");
                for (TestResult testResult : result.getTestResults())
                {
                    if (!testResult.isSuccess())
                    {
                        System.out.println(testResult.getName() + ": " + testResult.getError());
                    }
                }
                File savedResult = File.createTempFile("result-", ".zip");
                Files.copy(result.getFile(), savedResult);
                throw new AssertionFailedError("Test failed: " + e.getMessage() + ", result file: " + savedResult.getAbsolutePath());
            }
            else
            {
                throw e;
            }
        }
    }

    private Executable getExecutable()
    {
        try (Env env = new EnvImpl("build1"))
        {
            File executable = env.getJarWithClass(MockServiceMain.class);
            return new Executable(executable, ExecutableType.JAR);
        }
    }
}
