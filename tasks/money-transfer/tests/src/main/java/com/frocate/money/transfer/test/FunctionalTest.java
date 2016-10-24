package com.frocate.money.transfer.test;

import com.frocate.money.transfer.balance.*;
import com.frocate.money.transfer.test.expect.AssertingBalanceService;
import org.eclipse.jetty.client.HttpClient;
import org.junit.After;
import org.junit.Before;

import java.util.Collection;

import static com.frocate.money.transfer.balance.TestParams.*;
import static org.junit.Assert.assertEquals;

public class FunctionalTest
{
    protected BalanceBootstrap bootstrap;
    protected AssertingBalanceService balance;
    protected TxService service;
    protected HttpClient http;

    @Before
    public void setUp() throws Exception
    {
        http = new HttpClient();
        //linux default max open files is 1000
        http.setMaxConnectionsPerDestination(1000);
        http.start();
        service = new TxServiceImpl(TRANSFER_SERVICE_HOST, TRANSFER_SERVICE_PORT, http);
    }

    @After
    public void tearDown() throws Exception
    {
        http.stop();
        if (bootstrap != null)
        {
            bootstrap.stop();
        }
    }

    protected void startBootstrap(Collection<Account> accounts)
    {
        bootstrap = new BalanceBootstrap(
                Integer.parseInt(System.getProperty("balance-service-port", "8081")),
                System.getProperty("balance-service-host", "0.0.0.0"),
                new AssertingBalanceService(accounts)
        ).start();
        balance = (AssertingBalanceService) bootstrap.getBalanceService();
    }
}
