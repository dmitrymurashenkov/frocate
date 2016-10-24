package com.frocate.taskrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * DockerVM runs processes under user "test" and copies files to/from his home directory by default.
 */
public class DockerVM implements VM
{
    public static final Logger log = LoggerFactory.getLogger(DockerVM.class);

    /**
     * User inside the container that we run all commands on behalf of. File paths that doesn't start with '/'
     * are appended to '/home/test'.
     */
    public static final String DOCKER_USER = "test";
    public static final String DOCKER_IMAGE_FOR_EXECUTABLE = "frocate-executable-vm:1";
    public static final String DOCKER_IMAGE_FOR_TESTS = "frocate-tests-vm:1";
    public static final String SHELL_NAME = "bash";

    private final String name;
    /**
     * Attach this DockerVM to network of specified container
     */
    private final String network;
    /**
     * Set to true to forward docker unix-socket to this VM and allow "docker" tool from inside VM to control
     * docker on host - only enable this on VM that runs tests and not executable because of security reasons
     */
    private final boolean allowDockerControl;
    /**
     * Limits physical memory of the container, but swap remains unlimited so processes can allocate unlimited
     * memory some of which would be swapped.
     */
    private final long memoryLimitBytes;

    public DockerVM(String name)
    {
        this(name, null);
    }

    public DockerVM(String name, String network)
    {
        this(name, network, false);
    }

    public DockerVM(String name, String network, boolean allowDockerControl)
    {
        this(name, network, allowDockerControl, -1);
    }

    public DockerVM(String name, String network, boolean allowDockerControl, long memoryLimitBytes)
    {
        checkNameValid(name);
        this.name = name;
        this.network = network;
        this.allowDockerControl = allowDockerControl;
        if (memoryLimitBytes < -1 || memoryLimitBytes == 0)
        {
            throw new IllegalArgumentException("Memory limit must be positive or -1 to disable, actual: " + memoryLimitBytes);
        }
        this.memoryLimitBytes = memoryLimitBytes;
    }

    private void checkNameValid(String name)
    {
        if (!name.matches("[a-zA-Z0-9-_]*"))
        {
            throw new IllegalArgumentException("Invalid docker container name: " + name);
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getIp()
    {
        try
        {
            //format only works if VM added to single network
            Process p = new ProcessBuilder().command("docker", "inspect", "--format", "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}", name).start();
            ProcessFuture result = new ProcessFuture(p);
            String ip = result.stdout(3, TimeUnit.SECONDS);
            String error = result.stdout(3, TimeUnit.SECONDS);
            if (ip.length() > 0)
            {
                return ip;
            }
            else if (error.length() > 0)
            {
                throw new RuntimeException("Error getting container ip: " + error);
            }
            else if (isRunning())
            {
                //if not attached to network
                return null;
            }
            else
            {
                throw new RuntimeException("Ip can't be requested - VM not running");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start()
    {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        //run in background as detached
        command.add("-d");
        command.add("--name=" + name);
        if (memoryLimitBytes != -1)
        {
            command.add("-m");
            command.add(memoryLimitBytes + "");
        }
        if (allowDockerControl)
        {
            command.add("-v");
            command.add("/var/run/docker.sock:/var/run/docker.sock");
        }
        if (network == null)
        {
            command.add("--net=none");
            //written to /etc/hostname if not specified then InetAddress.getLocalHost() throws exception
            //can only by specified if network of this container not attached to another container
            command.add("--hostname=localhost");
        }
        else
        {
            command.add("--net=" + network);
            if (!network.startsWith("container:"))
            {
                //cannot specify hostname if we share network stack
                command.add("--hostname=" + "host-" + System.nanoTime());
            }
        }
        if (allowDockerControl)
        {
            //Image for tests-vm has docker installed for "docker" tool to be available
            command.add(DOCKER_IMAGE_FOR_TESTS);
        }
        else
        {
            command.add(DOCKER_IMAGE_FOR_EXECUTABLE);
        }
        //detached container exits once this command is finished so we perform infinite wait
        command.add("tail");
        command.add("-f");
        command.add("/dev/null");
        try
        {
            log.info("Docker container '" + name + "' starting with command: " + command.stream().collect(Collectors.joining(" ")));
            Process p = new ProcessBuilder().command(command).inheritIO().start();
            p.waitFor(10, TimeUnit.SECONDS);
            if (!p.waitFor(10, TimeUnit.SECONDS))
            {
                throw new ProcessTimeoutException("Docker container '" + name + "' start timed out");
            }
            if (p.exitValue() != 0)
            {
                throw new RuntimeException("Docker container '" + name + "' start failed with code: " + p.exitValue());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop()
    {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("rm");
        command.add("-f");
        command.add(name);
        try
        {
            log.info("Docker container '" + name + "' stopping with command: " + command.stream().collect(Collectors.joining(" ")));
            Process p = new ProcessBuilder().command(command).inheritIO().start();
            if (!p.waitFor(10, TimeUnit.SECONDS))
            {
                throw new ProcessTimeoutException("Docker container '" + name + "' stop timed out");
            }
            if (p.exitValue() != 0)
            {
                throw new RuntimeException("Docker container '" + name + "' stop failed with code: " + p.exitValue());
            }
        }
        catch (Exception e)
        {
            try
            {
                //if container was actually stopped - do not throw exception
                if (isRunning())
                {
                    throw new RuntimeException(e);
                }
            }
            catch (Exception e1)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isRunning()
    {
        List<String> command = new ArrayList<>();
        command.add(SHELL_NAME);
        command.add("-c");
        command.add("docker inspect -f {{.State.Running}} " + name + " | grep -Fx \"true\"");
        try
        {
            log.info("Docker container '" + name + "' checking running status: " + command.stream().collect(Collectors.joining(" ")));
            Process p = new ProcessBuilder().command(command).inheritIO().start();
            if (!p.waitFor(10, TimeUnit.SECONDS))
            {
                throw new ProcessTimeoutException("Docker container '" + name + "' check status timed out");
            }
            switch (p.exitValue())
            {
                case 0: return true;
                case 1: return false;
                default: throw new RuntimeException("Docker container '" + name + "' check status failed with code: " + p.exitValue());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void copyFromHost(File fileOnHost, VMFile fileInVm)
    {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("cp");
        command.add(fileOnHost.getAbsolutePath());
        command.add(name + ":" + fileInVm);
        try
        {
            log.info("Docker container '" + name + "' copying file from host: " + command.stream().collect(Collectors.joining(" ")));
            Process p = new ProcessBuilder().command(command).inheritIO().start();
            if (!p.waitFor(10, TimeUnit.SECONDS))
            {
                throw new ProcessTimeoutException("Docker container '" + name + "' copy from host timed out");
            }
            if (p.exitValue() != 0)
            {
                throw new RuntimeException("Docker container '" + name + "' copy from host failed with code: " + p.exitValue());
            }
            int chownExitCode = runCommand("chown " + DOCKER_USER + ":" + DOCKER_USER + " " + fileInVm.getAbsolutePath(), "root").exitCode(5, TimeUnit.SECONDS);
            if (chownExitCode != 0)
            {
                throw new RuntimeException("Docker container '" + name + "' copy from host succeeded but chown failed with code: " + chownExitCode);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProcessFuture runCommand(String command)
    {
        return runCommand(command, null);
    }

    /**
     * @return command exit code from inside container
     */
    private ProcessFuture runCommand(String cmd, String user)
    {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("exec");
        if (user != null)
        {
            command.add("-u");
            command.add(user);
        }
        command.add(name);
        command.add(SHELL_NAME);
        command.add("-c");
        command.add(cmd);
        try
        {
            log.info("Docker container '" + name + "' executing command: " + command.stream().collect(Collectors.joining(" ")));
            Process p = new ProcessBuilder().command(command).start();

            return new ProcessFuture(p)
            {
                @Override
                protected ProcessTimeoutException createException()
                {
                    return new ProcessTimeoutException("Docker container '" + name + "' command didn't exit before timeout: " + command.stream().collect(Collectors.joining(" ")));
                }
            };
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isFileExists(VMFile fileInVm)
    {
        return runCommand("ls -l " + fileInVm.getAbsolutePath(), null).exitCode(5, TimeUnit.SECONDS) == 0;
    }

    @Override
    public File copyToHost(VMFile fileInVm)
    {
        try
        {
            File fileOnHost = File.createTempFile(fileInVm.getName() + "-", null);
            fileOnHost.deleteOnExit();
            copyToHost(fileInVm, fileOnHost);
            return fileOnHost;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void copyToHost(VMFile fileInVm, File fileOnHost)
    {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("cp");
        command.add(name + ":" + fileInVm);
        command.add(fileOnHost.getAbsolutePath());
        try
        {
            log.info("Docker container '" + name + "' copying file to host: " + command.stream().collect(Collectors.joining(" ")));
            Process p = new ProcessBuilder().command(command).inheritIO().start();
            if (!p.waitFor(10, TimeUnit.SECONDS))
            {
                throw new ProcessTimeoutException("Docker container '" + name + "' copy to host timed out");
            }
            if (p.exitValue() != 0)
            {
                throw new RuntimeException("Docker container '" + name + "' copy to host failed with code: " + p.exitValue());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void copyToHostIfExists(VMFile fileInVm, File fileOnHost)
    {
        if (isFileExists(fileInVm))
        {
            copyToHost(fileInVm, fileOnHost);
        }
    }
}
