package com.frocate.shorturl.test;

import com.frocate.shorturl.mock.MockServiceBootstrap;
import com.frocate.shorturl.mock.MockServiceMain;
import com.frocate.taskrunner.*;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import org.eclipse.jetty.client.HttpClient;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClusterControlTest
{
    @Test
    public void buildConfig_shouldSetAllPropertiesForNode()
    {
        List<String> ipPorts = Arrays.asList("127.0.0.1:8080", "192.168.1.1:8081", "8.8.8.8:8888");
        ClusterControl clusterControl = new ClusterControl(ipPorts);
        String node0Config =
                "nodes=2\n" +
                "thisNodeIndex=0\n" +
                "memoryLimitBytes=314572800\n" +
                "node-0=127.0.0.1:8080\n" +
                "node-1=192.168.1.1:8081";
        String node1Config =
                "nodes=2\n" +
                "thisNodeIndex=1\n" +
                "memoryLimitBytes=314572800\n" +
                "node-0=127.0.0.1:8080\n" +
                "node-1=192.168.1.1:8081";
        assertEquals(node0Config, clusterControl.buildConfig(2, 0).toString());
        assertEquals(node1Config, clusterControl.buildConfig(2, 1).toString());
    }

    @Test
    public void restart_shouldStartNewProcessOnEachNode() throws Exception
    {
        try (Env env = new EnvImpl("build1"))
        {
            VM vm1 = env.createVM("vm1");
            VM vm2 = env.createVM("vm2");
            ClusterControl clusterControl = new ClusterControl(ExecutableType.JAR, env.getJarWithClass(MockServiceBootstrap.class), env.getNetworkName(), new String[] {vm1.getName(), vm2.getName()});
            clusterControl.restartClusterWithNodes(2);

            assertTrue(vm1.isRunning());
            assertTrue(vm2.isRunning());
            assertEquals(0, vm1.runCommand("ps u -p $(pidof java) | grep executable").exitCode(3, TimeUnit.SECONDS));
            assertEquals(0, vm2.runCommand("ps u -p $(pidof java) | grep executable").exitCode(3, TimeUnit.SECONDS));
        }
    }

    @Test
    public void restart_shouldStartSpecifiedSubsetOfNodes() throws Exception
    {
        try (Env env = new EnvImpl("build1"))
        {
            VM vm1 = env.createVM("vm1");
            VM vm2 = env.createVM("vm2");
            ClusterControl clusterControl = new ClusterControl(ExecutableType.JAR, env.getJarWithClass(MockServiceBootstrap.class), env.getNetworkName(), new String[] {vm1.getName(), vm2.getName()});
            clusterControl.restartClusterWithNodes(1);

            assertTrue(vm1.isRunning());
            assertFalse(vm2.isRunning());
            assertEquals(0, vm1.runCommand("ps u -p $(pidof java) | grep executable").exitCode(3, TimeUnit.SECONDS));
        }
    }
}
