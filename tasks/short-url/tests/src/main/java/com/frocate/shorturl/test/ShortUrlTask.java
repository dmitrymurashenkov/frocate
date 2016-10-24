package com.frocate.shorturl.test;

import com.frocate.taskrunner.*;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.executable.JarRunner;
import com.frocate.taskrunner.junit.SimpleJUnitRunner;
import com.frocate.taskrunner.junit.TaskProgress;
import com.frocate.taskrunner.result.Metric;
import com.frocate.taskrunner.result.ProgressListener;
import com.frocate.taskrunner.result.TestResult;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.frocate.taskrunner.VMFile.vmFile;
import static com.frocate.taskrunner.executable.Runner.MB;

public class ShortUrlTask extends AbstractTask implements Task
{
    public static final String TASK_NAME = "Url shortening service";
    public static final String TASK_ID = "task-short-url";
    public static final String PROGRESS_FILE = "taskProgress.json";
    public static final int TIMEOUT_SECONDS = 10*60;
    public static final int MEMORY_LIMIT_PER_NODE = 300*1024*1024;

    public static final List<Metric> metrics = Collections.synchronizedList(new ArrayList<>());
    public static ClusterControl clusterControl;

    static
    {
        //init for tests outside VM
        clusterControl = new ClusterControl(Arrays.asList("127.0.0.1:8080"));
//        clusterControl = new ClusterControl(Arrays.asList("127.0.0.1:8080", "127.0.0.1:8081"));
    }

    @Override
    protected void runTests(Executable executableOnHost, Env env, ProgressListener listener)
    {
        int nodes = 2;
        List<VM> vms = new ArrayList<>();
        for (int i = 0; i < nodes; i++)
        {
            vms.add(env.createVM("service-" + i));
        }

        VM testsVM = env.createVM("tests", true);
        testsVM.start();

        testsVM.copyFromHost(env.getJarWithClass(ShortUrlTask.class), TESTS_JAR_FILE_IN_VM);
        testsVM.copyFromHost(executableOnHost.getFile(), EXECUTABLE_FILE_IN_VM);

        //run tests and check progress
        ProcessFuture future = testsVM.runCommand(new JarRunner()
                .buildCommand(TESTS_JAR_FILE_IN_VM, 512 * MB, new String[]{executableOnHost.getType().toString(), env.getBuildId(), vms.get(0).getName(), vms.get(1).getName()}) + " &> test.log"
        );

        progress = awaitTestsComplete(env, testsVM, future, TIMEOUT_SECONDS, listener);

        testsVM.copyToHostIfExists(vmFile("test.log"), testLog);

        File tmpExecutableLog = env.createTmpFile("tmp-output.log", "");
        //get results
        for (int i = 0; i < nodes; i++)
        {
            appendToFile("Output from node-" + i + ":\n", executableLog);
            try
            {
                VM vm = vms.get(i);
                VMFile log = vmFile(ClusterControl.EXECUTABLE_LOG_FILENAME);
                if (vm.isFileExists(log))
                {
                    vm.copyToHost(log, tmpExecutableLog);
                    concatFiles(tmpExecutableLog, executableLog);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                appendToFile("Exception while copying logs: " + e.getMessage() + "\n", executableLog);
            }
        }
    }

    private void appendToFile(String message, File file)
    {
        try
        {
            Files.append(message, file, Charset.defaultCharset());
        }
        catch (Exception e)
        {
            //non-fatal in our case
            e.printStackTrace();
        }
    }

    private void concatFiles(File source, File appendTo)
    {
        try (FileInputStream fis = new FileInputStream(source); FileOutputStream fos = new FileOutputStream(appendTo, true))
        {
            ByteStreams.copy(fis, fos);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length == 4)
        {
            ExecutableType type = ExecutableType.valueOf(args[0]);
            String vmNetwork = args[1];
            String vm1Name = args[2];
            String vm2Name = args[3];
            clusterControl = new ClusterControl(type, new File("executable"), vmNetwork, new String[] {vm1Name, vm2Name});
        }
        else
        {
            throw new RuntimeException("Executable type and VM names must be provided to launch tests");
        }
        System.out.println("Running tests - " + ShortUrlTask.class.getName());
        try
        {
            List<TestResult> testResults = new SimpleJUnitRunner()
                    .runTests(
                            new File(PROGRESS_FILE),
                            metrics,
                            SingleNodeFunctionalTest.class,
                            TwoNodeFunctionalTest.class,
                            SpeedTestSingleNode.class,
                            SpeedTestTwoNodes.class
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
    public Collection<String> getTags()
    {
        return Arrays.asList("scalability", "http", "cluster");
    }


    @Override
    public String getShortDescription()
    {
        try
        {
            return CharStreams.toString(new InputStreamReader(ShortUrlTask.class.getClassLoader().getResourceAsStream("task-short-description-short-url.html")));
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
            return CharStreams.toString(new InputStreamReader(ShortUrlTask.class.getClassLoader().getResourceAsStream("task-description-short-url.html")));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
