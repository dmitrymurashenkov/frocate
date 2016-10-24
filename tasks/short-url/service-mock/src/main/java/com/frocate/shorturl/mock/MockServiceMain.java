package com.frocate.shorturl.mock;

import java.io.FileInputStream;
import java.util.Properties;

public class MockServiceMain
{
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.out.println("Config file argument not provided");
            System.exit(1);
        }
        System.out.println("Reading properties from: " + args[0]);
        Properties properties = new Properties();
        properties.load(new FileInputStream(args[0]));
        Config config = new Config(properties);
        MockServiceBootstrap bootstrap = new MockServiceBootstrap("0.0.0.0", config.getThisNodePort(), config);
        bootstrap.start().dumpStderr().join();
    }
}
