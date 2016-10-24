package com.frocate.sumtwonumbers.test;

import com.frocate.taskrunner.Env;
import com.frocate.taskrunner.EnvImpl;
import com.frocate.taskrunner.Task;
import com.frocate.taskrunner.TaskRunner;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.result.ProgressListener;
import com.frocate.taskrunner.result.Result;
import com.frocate.taskrunner.result.TestResult;
import com.frocate.sumtwonumbers.mock.SumServiceMain;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SumTwoNumbersTaskTest
{
    @Test
    public void test() throws Exception
    {
        Result result = null;
        try
        {
            String buildId = "build-sumtwonumbers-task-test";
            Executable executable = getExecutable();
            Task task = new SumTwoNumbersTask();

            result = new TaskRunner(Long.MAX_VALUE, Long.MAX_VALUE, ProgressListener.STUB)
                    .run(
                            buildId,
                            executable,
                            task
                    );

            int totalTests = 6;
            int totalMetrics = 2;
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
                throw new RuntimeException("Test failed: " + e.getMessage() + ", result file: " + savedResult.getAbsolutePath(), e);
            }
            else
            {
                throw e;
            }
        }
    }

    private Executable getExecutable()
    {
        try (Env env = new EnvImpl("build-" + System.nanoTime()))
        {
            File executable = env.getJarWithClass(SumServiceMain.class);
            return new Executable(executable, ExecutableType.JAR);
        }
    }
}
