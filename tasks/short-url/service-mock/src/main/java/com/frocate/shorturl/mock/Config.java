package com.frocate.shorturl.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Config
{
    private final int nodes;
    private final int thisNodeIndex;
    private final long memoryLimitBytes;
    private final List<String> ipPorts = Collections.synchronizedList(new ArrayList<>());

    public Config(Properties properties)
    {
        nodes = Integer.parseInt(properties.getProperty("nodes"));
        thisNodeIndex = Integer.parseInt(properties.getProperty("thisNodeIndex"));
        memoryLimitBytes = Long.parseLong(properties.getProperty("memoryLimitBytes"));
        for (int i = 0; i < nodes; i++)
        {
            ipPorts.add(properties.getProperty("node-" + i));
        }
    }

    public String getNodeIp(int index)
    {
        String ipPort = ipPorts.get(index);
        return ipPort.replaceAll(":.*", "");
    }

    public int getNodePort(int index)
    {
        //made for tests when we start all nodes on same ip
        String ipPort = ipPorts.get(index);
        int portIndex = ipPort.indexOf(":");
        return Integer.parseInt(ipPort.substring(portIndex + 1));
    }

    public int getThisNodeIndex()
    {
        return thisNodeIndex;
    }

    public int getNodesCount()
    {
        return nodes;
    }

    public String getThisNodeIp()
    {
        return getNodeIp(thisNodeIndex);
    }

    public int getThisNodePort()
    {
        return getNodePort(thisNodeIndex);
    }

    public long getMemoryLimitBytes()
    {
        return memoryLimitBytes;
    }
}
