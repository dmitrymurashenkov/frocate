package com.frocate.shorturl.test;

import com.frocate.taskrunner.*;
import com.frocate.taskrunner.executable.ExecutableType;
import org.eclipse.jetty.client.HttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.frocate.taskrunner.AbstractTask.EXECUTABLE_CONFIG_FILE_IN_VM;
import static com.frocate.taskrunner.AbstractTask.EXECUTABLE_FILE_IN_VM;
import static com.frocate.taskrunner.VMFile.vmFile;

public class ClusterControl
{
    public static final String EXECUTABLE_LOG_FILENAME = "output.log";

    private final File executable;
    private final List<String> ipPorts = new ArrayList<>();
    private final List<VM> vms = new ArrayList<>();
    private final ExecutableType type;

    /**
     * For launching tests manually without VMs
     * @param ipPorts
     */
    ClusterControl(List<String> ipPorts)
    {
        this.ipPorts.addAll(ipPorts);
        this.type = ExecutableType.JAR;
        this.executable = null;
    }

    public ClusterControl(ExecutableType type, File executable, String vmNetwork, String[] vmNames)
    {
        this.executable = executable;
        for (String vmName : vmNames)
        {
            vms.add(new DockerVM(vmName, vmNetwork, false, ShortUrlTask.MEMORY_LIMIT_PER_NODE));
        }
        this.type = type;
    }

    public void restartClusterWithNodes(int nodes) throws Exception
    {
        if (vms.isEmpty() && !ipPorts.isEmpty())
        {
            System.out.println("Skipping nodes restart - seems tests are running without VMs");
            return;
        }
        System.out.println("Restarting cluster in " + nodes + "-node mode");
        System.out.println("Shutting current nodes down");
        for (VM vm : vms)
        {
            vm.stop();
        }
        ipPorts.clear();
        System.out.println("Starting new node processes");
        for (int i = 0; i < nodes; i++)
        {
            //need to start all nodes and get ips and only then build configs
            VM vm = vms.get(i);
            vm.start();
            ipPorts.add(vm.getIp() + ":8080");
        }
        for (int j = 0; j < nodes; j++)
        {
            VM vm = vms.get(j);
            //if this is run from VM then "docker cp" copies file from this container to specified container
            vm.copyFromHost(executable, vmFile("executable"));
            PropertiesBuilder config = buildConfig(nodes, j);

            int status = vm.runCommand("echo -e '" + config.toString() + "' > " + EXECUTABLE_CONFIG_FILE_IN_VM.getAbsolutePath()).exitCode(3, TimeUnit.SECONDS);
            if (status != 0)
            {
                throw new RuntimeException("Failed to create updated config file on vm '" + vm.getName() + "'");
            }
            vm.runCommand(type.getRunner().buildCommand(EXECUTABLE_FILE_IN_VM, ShortUrlTask.MEMORY_LIMIT_PER_NODE, new String[] {EXECUTABLE_CONFIG_FILE_IN_VM.getAbsolutePath()}) + " >> " + EXECUTABLE_LOG_FILENAME + " 2>&1");
        }
        HttpClient client = new HttpClient();
        client.start();
        System.out.println("Awaiting nodes up");
        awaitNodesStarted(client, nodes);
        client.stop();
        System.out.println("Cluster restarted in " + nodes + "-node mode");
    }

    private void awaitNodesStarted(HttpClient http, int nodes) throws InterruptedException
    {
        for (int i = 0; i < nodes; i++)
        {
            for (int j = 0; j < 30; j++)
            {
                if (getService(http, i).nodeReady())
                {
                    break;
                }
                else
                {
                    System.out.println("Waiting for node-" + i + " (" + getNodeIp(i) + ":" + getNodePort(i) + ") to start");
                    Thread.sleep(1000);
                }
            }
            if (!getService(http, i).nodeReady())
            {
                throw new RuntimeException("Node failed to start before timeout");
            }
        }
    }

    public ShortUrlService getService(HttpClient http, int nodeIndex)
    {
        return new ShortUrlServiceImpl(getNodeIp(nodeIndex), getNodePort(nodeIndex), http);
    }

    public String getNodeIp(int index)
    {
        return ipPorts.get(index).replaceAll(":.*", "");
    }

    public int getNodePort(int index)
    {
        String ipPort = ipPorts.get(index);
        int portIndex = ipPort.indexOf(":");
        return Integer.parseInt(ipPort.substring(portIndex + 1));
    }

    PropertiesBuilder buildConfig(int nodes, int thisNodeIndex)
    {
        PropertiesBuilder config = new PropertiesBuilder();
        config.add("nodes", nodes + "");
        config.add("thisNodeIndex", thisNodeIndex + "");
        config.add("memoryLimitBytes", ShortUrlTask.MEMORY_LIMIT_PER_NODE + "");
        for (int i = 0; i < nodes; i++)
        {
            config.add("node-" + i, ipPorts.get(i));
        }
        return config;
    }

    List<String> getIpPorts()
    {
        return ipPorts;
    }

    List<VM> getVms()
    {
        return vms;
    }

    ExecutableType getExecutableType()
    {
        return type;
    }

    public int getNodesCount()
    {
        return ipPorts.size();
    }
}
