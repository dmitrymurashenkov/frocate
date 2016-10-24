package com.frocate.money.transfer.mock.tx;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;

public class HttpClientWrapper
{
    private final String balanceServiceHost;
    private final int balanceServicePort;
    private final HttpClient client;

    public HttpClientWrapper(String balanceServiceHost, int balanceServicePort, HttpClient client)
    {
        this.balanceServiceHost = balanceServiceHost;
        this.balanceServicePort = balanceServicePort;
        this.client = client;
    }

    public Request request(String txId, String accountId, int amount)
    {
        String url = "http://" + balanceServiceHost + ":" + balanceServicePort + "/debit?txId=" + txId + "&accountId=" + accountId + "&amount=" + amount;
        return client.newRequest(url);
    }
}
