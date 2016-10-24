package com.frocate.shorturl.mock;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.InetSocketAddress;

public class MockServiceBootstrap
{
    private final Server server;
    private final MockService service;

    public MockServiceBootstrap(String bindHost, int bindPort, Config config) throws Exception
    {
        server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setRequestHeaderSize(20*1024);
        ServerConnector connector = new ServerConnector(server, -1, -1, new HttpConnectionFactory(httpConfig));
        connector.setHost(bindHost);
        connector.setPort(bindPort);
        server.addConnector(connector);
        service = new MockServiceImpl(config);

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        ServletHolder servletHolder = new ServletHolder(new MockServlet(service));
        servletHandler.addServletWithMapping(servletHolder, "/shorten");
        servletHandler.addServletWithMapping(servletHolder, "/expand");
        servletHandler.addServletWithMapping(servletHolder, "/status");
    }

    public MockServiceBootstrap start()
    {
        try
        {
            server.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void join()
    {
        try
        {
            server.join();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void stop()
    {
        try
        {
            server.stop();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public MockServiceBootstrap dumpStderr()
    {
        server.dumpStdErr();
        return this;
    }
}

