package com.frocate.money.transfer.mock;

import com.frocate.money.transfer.mock.tx.HttpClientWrapper;
import com.frocate.money.transfer.mock.tx.Transaction;
import org.eclipse.jetty.client.HttpClient;

import java.util.List;
import java.util.concurrent.*;

public class MockTxServiceImpl implements MockTxService
{
    private final String balanceServiceHost;
    private final int balanceServicePort;
    private final HttpClient http;
    public final ExecutorService executor;

    public MockTxServiceImpl(String balanceServiceHost, int balanceServicePort, int maxConcurrentRequests)
    {
        this.balanceServiceHost = balanceServiceHost;
        this.balanceServicePort = balanceServicePort;
        executor = Executors.newCachedThreadPool();
        this.http = new HttpClient();
        http.setMaxConnectionsPerDestination(maxConcurrentRequests);
        try
        {
            http.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void transfer(String txId, List<Operation> operations, Runnable callback) throws Exception
    {
        new Transaction(new HttpClientWrapper(balanceServiceHost, balanceServicePort, http), executor, txId, operations, callback);
    }
}
