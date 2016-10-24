package com.frocate.money.transfer.mock;

import java.io.FileInputStream;
import java.util.Properties;

public class MockServiceMain
{
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
        String bindHost = System.getProperty("transfer-service-host", "127.0.0.1");
        int bindPort = Integer.parseInt(System.getProperty("transfer-service-port", "8080"));
        String balanceServiceHost = System.getProperty("balance-service-host", "127.0.0.1");
        int balanceServicePort = Integer.parseInt(System.getProperty("balance-service-port", "8081"));
        new MockTxBootstrap(bindHost, bindPort, balanceServiceHost, balanceServicePort).start().dumpStderr().join();
    }
}
