package com.frocate.money.transfer.test;

import com.frocate.taskrunner.*;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.JarRunner;
import com.frocate.taskrunner.junit.SimpleJUnitRunner;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.ProgressListener;
import com.google.common.io.CharStreams;

import java.io.*;
import java.util.*;

import static com.frocate.taskrunner.VMFile.vmFile;
import static com.frocate.taskrunner.executable.Runner.MB;

public class MoneyTransferTask extends AbstractTask implements Task
{
    public static final String TASK_NAME = "Money transfer";
    public static final String TASK_ID = "task-money-transfer";
    public static final int TIMEOUT_SECONDS = 60*5;

    public static final List<Metric> metrics = Collections.synchronizedList(new ArrayList<>());

    @Override
    protected void runTests(Executable executableOnHost, Env env, ProgressListener listener)
    {
        //start VMs to get their ip addresses
        VM executableVM = env.createVM("transferService");
        executableVM.start();

        VM testsVM = env.createVM("testsAndBalanceService");
        testsVM.start();

        File config = env.buildProperties()
                .add("balance-service-host", testsVM.getIp())
                .add("balance-service-port", "8081")
                .add("transfer-service-host", executableVM.getIp())
                .add("transfer-service-port", "8080")
                .toFile(env.createTmpFile("transfer.properties"));

        executableVM.copyFromHost(executableOnHost.getFile(), EXECUTABLE_FILE_IN_VM);
        executableVM.copyFromHost(config, EXECUTABLE_CONFIG_FILE_IN_VM);
        executableVM.runCommand(executableOnHost.getType().getRunner()
                .buildCommand(EXECUTABLE_FILE_IN_VM, 768*MB, new String[]{EXECUTABLE_CONFIG_FILE_IN_VM.getAbsolutePath()}) + " &> output.log"
        );

        testsVM.copyFromHost(config, TESTS_CONFIG_FILE_IN_VM);
        testsVM.copyFromHost(env.getJarWithClass(MoneyTransferTask.class), TESTS_JAR_FILE_IN_VM);

        ProcessFuture future = testsVM.runCommand(new JarRunner()
                .buildCommand(TESTS_JAR_FILE_IN_VM, 768*MB, new String[]{TESTS_CONFIG_FILE_IN_VM.getAbsolutePath()}) + " &> test.log"
        );

        progress = awaitTestsComplete(env, testsVM, future, TIMEOUT_SECONDS, listener);

        executableVM.copyToHostIfExists(vmFile("output.log"), executableLog);
        testsVM.copyToHostIfExists(vmFile("test.log"), testLog);
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length > 0)
        {
            System.out.println("Reading properties from: " + args[0]);
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[0]));
            for (String key : properties.stringPropertyNames())
            {
                System.setProperty(key, properties.getProperty(key));
            }
        }
        else
        {
            System.out.println("Config file not specified - using default property values");
        }
        System.out.println("Running tests - " + MoneyTransferTask.class.getName());
        try
        {
            new SimpleJUnitRunner()
                    .runTests(AbstractTask.PROGRESS_FILE_IN_VM,
                            metrics,
                            SmokeTest.class,
                            TransferTest.class,
                            WarmUpTest.class,
                            SpeedTest.class,
                            LoadTest.class);

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
    public String getName()
    {
        return TASK_NAME;
    }

    @Override
    public String getId()
    {
        return TASK_ID;
    }

    @Override
    public Collection<String> getTags()
    {
        return Arrays.asList("concurrency", "http", "integration");
    }

    @Override
    public String getShortDescription()
    {
        try
        {
            return CharStreams.toString(new InputStreamReader(MoneyTransferTask.class.getClassLoader().getResourceAsStream("task-short-description-money-transfer.html")));
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
            return CharStreams.toString(new InputStreamReader(MoneyTransferTask.class.getClassLoader().getResourceAsStream("task-description-money-transfer.html")));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
