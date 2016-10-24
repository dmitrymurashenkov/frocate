package com.frocate.money.transfer.balance;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.InetSocketAddress;
import java.util.Collection;

public class BalanceBootstrap
{
    private final Server server;
    private final BalanceService service;

    public BalanceBootstrap(Collection<Account> accounts)
    {
        this(8080, "0.0.0.0", new BalanceServiceImpl(accounts));
    }

    public BalanceBootstrap(int port, String host, BalanceService balanceService)
    {
        server = new Server(new InetSocketAddress(host, port));
        service = balanceService;

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        servletHandler.addServletWithMapping(new ServletHolder(new BalanceServlet(service)), "/debit");
    }

    public BalanceService getBalanceService()
    {
        return service;
    }

    public BalanceBootstrap start()
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

    public BalanceBootstrap dumpStderr()
    {
        server.dumpStdErr();
        return this;
    }
}
