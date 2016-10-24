package com.frocate.money.transfer;

import com.frocate.money.transfer.balance.*;
import org.eclipse.jetty.client.HttpClient;
import org.junit.After;
import org.junit.Before;

public class FunctionalTest
{
    private BalanceBootstrap bootstrap;
    protected BalanceService service;
    protected HttpClient http;

    @Before
    public void setUp() throws Exception
    {
        http = new HttpClient();
        http.start();
    }

    @After
    public void tearDown() throws Exception
    {
        if (bootstrap != null)
        {
            bootstrap.stop();
        }
        if (http != null)
        {
            http.stop();
        }
    }

    public void startService()
    {
        bootstrap = new BalanceBootstrap(AccountGenerator.one("1", 10)).start();
        service = bootstrap.getBalanceService();
    }

    public void startWithExecutionDelay(long delayMs)
    {
        bootstrap = new BalanceBootstrap(8080, "0.0.0.0", new BalanceServiceImpl(AccountGenerator.one("1", 10))
        {
            @Override
            public synchronized long getOperationDelay(String txId, String accountId, int amount)
            {
                return delayMs;
            }
        }).start();
        service = bootstrap.getBalanceService();
    }

    public void startWithService(BalanceService service)
    {
        bootstrap = new BalanceBootstrap(8080, "0.0.0.0", service).start();
        this.service = bootstrap.getBalanceService();
    }
}
