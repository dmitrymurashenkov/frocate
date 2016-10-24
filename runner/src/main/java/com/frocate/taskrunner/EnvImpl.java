package com.frocate.taskrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.frocate.taskrunner.FileUtils.deleteDirRecursive;

public class EnvImpl implements Env
{
    public static final Logger log = LoggerFactory.getLogger(EnvImpl.class);

    private final String buildId;
    private final File dir;
    private final Collection<VM> vms = new ArrayList<>();
    private volatile boolean closed;
    private final Thread shutdownHook;

    public EnvImpl(String buildId)
    {
        try
        {
            this.buildId = buildId;
            dir = File.createTempFile(buildId, null).getAbsoluteFile();
            dir.deleteOnExit();
            if (!dir.delete() || !dir.mkdirs())
            {
                throw new RuntimeException("Failed to allocate dir for frocate environment: " + dir.getAbsolutePath());
            }
            shutdownHook = new Thread(() -> {
                //if everything works correctly cleanup should be performed via direct close() invocation from code
                log.warn("Test env '" + dir + "' closed by shutdown hook - possible leak");
                close(false);
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            createDockerNetwork();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * We launch VMs in "internal" so that they cannot contact machines outside host. However if host has processes
     * listening on 0.0.0.0 they can be contacted by VM (todo).
     */
    private void createDockerNetwork() throws Exception
    {
        Process p = new ProcessBuilder().command("docker", "network", "create", "--internal", buildId).start();
        if (!p.waitFor(3, TimeUnit.SECONDS) || p.exitValue() != 0)
        {
            throw new RuntimeException("Failed to create tmp docker network");
        }
    }

    private void removeDockerNetwork()
    {
        try
        {
            Process p = new ProcessBuilder().command("docker", "network", "rm", buildId).start();
            if (!p.waitFor(3, TimeUnit.SECONDS) || p.exitValue() != 0)
            {
                throw new RuntimeException("Failed to remove tmp docker network");
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBuildId()
    {
        return buildId;
    }

    File getDir()
    {
        return dir;
    }

    public synchronized void close()
    {
        close(true);
    }

    private synchronized void close(boolean removeHook)
    {
        if (!closed)
        {
            closed = true;
            deleteDirRecursive(dir);
            for (VM vm : vms)
            {
                vm.stop();
            }
            removeDockerNetwork();
            if (removeHook)
            {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        if (!closed)
        {
            log.warn("Test env '" + dir.getAbsolutePath() + "' not closed - possible leak");
        }
        super.finalize();
    }

    @Override
    public String getNetworkName()
    {
        return buildId;
    }

    public VM createVM(String name)
    {
        return createVM(name, false);
    }

    @Override
    public VM createVM(String name, boolean allowDockerControl)
    {
        VM vm = new DockerVM(buildId + "-" + name, getNetworkName(), allowDockerControl);
        vms.add(vm);
        return vm;
    }

    public File createTmpFile(String name)
    {
        return new File(dir, name);
    }

    @Override
    public File createTmpFile(String name, String content)
    {
        try
        {
            File file = createTmpFile(name);
            Files.write(file.toPath(), content.getBytes());
            return file;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PropertiesBuilder buildProperties()
    {
        return new PropertiesBuilder();
    }

    @Override
    public File getJarWithClass(Class clazz)
    {
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getFile()).getAbsoluteFile();
    }
}
