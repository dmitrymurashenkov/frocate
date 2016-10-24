package com.frocate.money.transfer.mock;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.InetSocketAddress;

public class MockTxBootstrap
{
    private final Server server;
    private final MockTxService service;

    public MockTxBootstrap(String bindHost, int bindPort, String balanceServiceHost, int balanceServicePort)
    {
        server = new Server(new InetSocketAddress(bindHost, bindPort));
        service = new MockTxServiceImpl(balanceServiceHost, balanceServicePort, 1000);

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        servletHandler.addServletWithMapping(new ServletHolder(new TxServlet(service)), "/transaction");
    }

    public MockTxBootstrap start()
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

    public MockTxBootstrap dumpStderr()
    {
        server.dumpStdErr();
        return this;
    }
}
