package com.frocate.taskrunner;

import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.junit.TaskProgress;
import com.frocate.taskrunner.result.ProgressListener;
import com.frocate.taskrunner.result.TestResult;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AbstractTaskTest
{
    @Test
    public void run_shouldAddErrorIfExceptionThrown()
    {
        try (Env env = new EnvImpl("build1"))
        {
            Task task = new StubTask()
            {
                @Override
                protected void runTests(Executable executableOnHost, Env env, ProgressListener listener)
                {
                    progress = new TaskProgress(Arrays.asList(
                            new TestResult("test1", true, null)
                    ),
                    new ArrayList<>(),
                    2,
                    null,
                    0);
                    throw new RuntimeException("Error");
                }
            };
            TaskResult result = task.run(buildExecutable(), env, ProgressListener.STUB);
            assertEquals(2, result.getTotalTests());
            assertEquals(Arrays.asList(
                    new TestResult("test1", true, null),
                    new TestResult("Tests should finish without exceptions", false, "Unexpected exception: Error")
            ), result.getTests());
        }
    }

    @Test
    public void run_shouldCreateEmptyLogFiles_ifNoneReturnedByTask()
    {
        try (Env env = new EnvImpl("build1"))
        {
            Task task = new StubTask();
            TaskResult result = task.run(buildExecutable(), env, ProgressListener.STUB);
            assertEquals("", FileUtils.readContent(result.getExecutableLog()));
            assertEquals("", FileUtils.readContent(result.getTestLog()));
        }
    }

    @Test
    public void run_returnLogFilesSetByImpl()
    {
        try (Env env = new EnvImpl("build1"))
        {
            Task task = new StubTask()
            {
                @Override
                protected void runTests(Executable executableOnHost, Env env, ProgressListener listener)
                {
                    executableLog = FileUtils.createTmpFileWithContent("executableLog");
                    testLog = FileUtils.createTmpFileWithContent("testLog");
                }
            };
            TaskResult result = task.run(buildExecutable(), env, ProgressListener.STUB);
            assertEquals("executableLog", FileUtils.readContent(result.getExecutableLog()));
            assertEquals("testLog", FileUtils.readContent(result.getTestLog()));
        }
    }

    @Test
    public void awaitTestsComplete_shouldReturnLastAvailableProgressFile()
    {
        try (Env env = new EnvImpl("build1"))
        {
            VM testVM = env.createVM("tests");
            testVM.start();
            ProcessFuture future = testVM.runCommand("sleep 0");
            copyTaskProgressToVM(testVM, new TestResult("test1"));

            AbstractTask task = new StubTask();
            Listener listener = new Listener();

            TaskProgress progress = task.awaitTestsComplete(env, testVM, future, 1, listener);

            assertTrue(listener.progressList.size() > 0);
            assertEquals(Arrays.asList(new TestResult("test1")), progress.getTests());
            assertEquals(1, progress.getTotalTests());
        }
    }

    @Test
    public void awaitTestsComplete_shouldAddReturnErrorIfTimeout()
    {
        try (Env env = new EnvImpl("build1"))
        {
            VM testVM = env.createVM("tests");
            testVM.start();
            ProcessFuture future = testVM.runCommand("sleep 5");
            copyTaskProgressToVM(testVM, new TestResult("test1"));

            AbstractTask task = new StubTask();
            Listener listener = new Listener();

            TaskProgress progress = task.awaitTestsComplete(env, testVM, future, 1, listener);

            assertTrue(listener.progressList.size() > 0);
            assertEquals(Arrays.asList(
                    new TestResult("test1"),
                    new TestResult("Tests should finish in 1 seconds", false, "Tests timed out")
                    ), progress.getTests());
            assertEquals(1, progress.getTotalTests());
        }
    }

    @Test
    public void awaitTestsComplete_shouldAddReturnErrorIfNoProgressFileProduced()
    {
        try (Env env = new EnvImpl("build1"))
        {
            VM testVM = env.createVM("tests");
            testVM.start();
            ProcessFuture future = testVM.runCommand("sleep 0");

            AbstractTask task = new StubTask();
            Listener listener = new Listener();

            TaskProgress progress = task.awaitTestsComplete(env, testVM, future, 1, listener);

            assertTrue(listener.progressList.size() > 0);
            assertEquals(Arrays.asList(
                    new TestResult("Tests should finish without errors", false, "Tests failed to produce results, seems some internal error")
            ), progress.getTests());
            assertEquals(0, progress.getTotalTests());
        }
    }

    private Executable buildExecutable()
    {
        File jarPath = new File(System.getProperty("mock-executable-jar-path", JarRunnerTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "../mock-jar/mock-executable.jar"));
        return new Executable(jarPath, ExecutableType.JAR);
    }

    private void copyTaskProgressToVM(VM vm, TestResult testResult)
    {
        TaskProgress progress = new TaskProgress(Arrays.asList(testResult), new ArrayList<>(), 1, null, 0);
        File tmp = FileUtils.createTmpFile();
        progress.save(tmp);
        vm.copyFromHost(tmp, AbstractTask.PROGRESS_FILE_IN_VM);
    }

    static class Listener implements ProgressListener
    {
        private List<TaskProgress> progressList = new ArrayList<>();

        @Override
        public void onProgress(String buildId, TaskProgress progress)
        {
            progressList.add(progress);
        }
    }

    static class StubTask extends AbstractTask
    {
        @Override
        protected void runTests(Executable executableOnHost, Env env, ProgressListener listener)
        {

        }

        @Override
        public String getId()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public String getShortDescription()
        {
            return null;
        }

        @Override
        public String getDescription()
        {
            return null;
        }

        @Override
        public Collection<String> getTags()
        {
            return null;
        }
    }
}
