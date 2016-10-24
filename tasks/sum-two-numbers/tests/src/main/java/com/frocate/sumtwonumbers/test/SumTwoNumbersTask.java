package com.frocate.sumtwonumbers.test;

import com.frocate.taskrunner.*;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.JarRunner;
import com.frocate.taskrunner.junit.SimpleJUnitRunner;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.ProgressListener;
import com.google.common.io.CharStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static com.frocate.taskrunner.VMFile.vmFile;
import static com.frocate.taskrunner.executable.Runner.MB;

public class SumTwoNumbersTask extends AbstractTask implements Task
{
    public static final String TASK_NAME = "Sum two numbers (tutorial task)";
    public static final String TASK_ID = "task-sum-two-numbers";

    public static final List<Metric> metrics = Collections.synchronizedList(new ArrayList<>());

    private final int timeoutSeconds;

    public SumTwoNumbersTask()
    {
        timeoutSeconds = 150;
    }

    public SumTwoNumbersTask(int timeoutSeconds)
    {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    protected void runTests(Executable executableOnHost, Env env, ProgressListener listener)
    {
        VM executableVM = startExecutableVM(env, executableOnHost);
        VM testsVM = startTestsVM(env);
        ProcessFuture future = testsVM.runCommand(new JarRunner()
                .buildCommand(TESTS_JAR_FILE_IN_VM, 768*MB, new String[]{ executableVM.getIp(), "8080" }) + " &> test.log"
        );

        progress = awaitTestsComplete(env, testsVM, future, timeoutSeconds, listener);

        //get results
        executableVM.copyToHostIfExists(vmFile("output.log"), executableLog);
        testsVM.copyToHostIfExists(vmFile("test.log"), testLog);
    }

    private VM startTestsVM(Env env)
    {
        VM testsVM = env.createVM("tests");
        testsVM.start();
        testsVM.copyFromHost(env.getJarWithClass(SumTwoNumbersTask.class), TESTS_JAR_FILE_IN_VM);
        return testsVM;
    }

    private VM startExecutableVM(Env env, Executable executableOnHost)
    {
        File configOnHost = env.buildProperties()
                .add("host", "0.0.0.0")
                .add("port", "8080")
                .toFile(env.createTmpFile("config.properties"));

        VM executableVM = env.createVM("sumService");
        executableVM.start();

        executableVM.copyFromHost(executableOnHost.getFile(), EXECUTABLE_FILE_IN_VM);
        executableVM.copyFromHost(configOnHost, EXECUTABLE_CONFIG_FILE_IN_VM);

        executableVM.runCommand(executableOnHost.getType().getRunner()
                .buildCommand(EXECUTABLE_FILE_IN_VM, 768*MB, new String[]{EXECUTABLE_CONFIG_FILE_IN_VM.getAbsolutePath()}) + " &> output.log"
        );

        return executableVM;
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println("Running tests - " + SumTwoNumbersTask.class.getName());
        if (args.length != 2)
        {
            System.out.println("Ip and port must be passed as arguments");
            return;
        }
        System.out.println("Assuming sum service at " + args[0] + ":" + args[1]);
        System.setProperty("host", args[0]);
        System.setProperty("port", args[1]);
        try
        {
            new SimpleJUnitRunner()
                    .runTests(
                            new File(PROGRESS_FILE_IN_VM.getName()),
                            metrics,
                            AwaitServiceReadyTest.class,
                            FunctionalTest.class,
                            WarmUpTest.class,
                            SpeedTest.class
                    );

            System.out.println("Tests completed successfully");
            System.exit(0);
        }
        catch (Throwable e)
        {
            System.out.println("Tests failed with exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public String getId()
    {
        return TASK_ID;
    }

    @Override
    public String getName()
    {
        return TASK_NAME;
    }

    @Override
    public String getShortDescription()
    {
        try
        {
            return CharStreams.toString(new InputStreamReader(SumTwoNumbersTask.class.getClassLoader().getResourceAsStream("task-short-description-sum-two-numbers.html")));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription()
    {
        try
        {
            return CharStreams.toString(new InputStreamReader(SumTwoNumbersTask.class.getClassLoader().getResourceAsStream("task-description-sum-two-numbers.html")));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<String> getTags()
    {
        return Arrays.asList("tutorial", "http");
    }
}
