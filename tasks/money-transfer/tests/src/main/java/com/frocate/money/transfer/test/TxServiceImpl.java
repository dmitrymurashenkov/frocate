package com.frocate.money.transfer.test;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;

import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TxServiceImpl implements TxService
{
    private final String host;
    private final int port;
    private final HttpClient http;

    public TxServiceImpl(String host, int port, HttpClient http)
    {
        this.host = host;
        this.port = port;
        this.http = http;
    }

    @Override
    public TransferFuture transferAsync(Tx tx)
    {
        try
        {
            String url = txToUrl(tx);
            BlockingQueue responseQueue = new ArrayBlockingQueue(1);
            http.newRequest(url).send(
                    new Response.CompleteListener()
                    {
                        @Override
                        public void onComplete(Result result)
                        {
                            if (result.isFailed())
                            {
                                responseQueue.add(result.getFailure());
                            }
                            else
                            {
                                responseQueue.add(result.getResponse());
                            }
                        }
                    }
            );
            return new TransferFuture()
            {
                @Override
                public Response get(long timeout, TimeUnit unit) throws Exception
                {
                    try
                    {
                        Object result = responseQueue.poll(timeout, unit);
                        if (result instanceof Exception)
                        {
                            throw (Exception)result;
                        }
                        else if (result instanceof Response)
                        {
                            Response response = (Response)result;
                            int status = response.getStatus();
                            if (status != 200)
                            {
                                throw new RuntimeException("Got http answer with code " + status + " for url '" + url + "'");
                            }
                            return response;
                        }
                        else
                        {
                            throw new RuntimeException("Unknown response: " + result);
                        }
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void transfer(Tx tx)
    {
        try
        {
            String url = txToUrl(tx);
            int status = http.GET(url).getStatus();
            if (status != 200)
            {
                throw new RuntimeException("Got http answer with code " + status + " for url '" + url + "'");
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private String txToUrl(Tx tx)
    {
        String transfers = "&transfer=" + tx.transfers.stream().map(
                transfer -> transfer.from + "," + transfer.to + "," + transfer.amount)
                .collect(Collectors.joining("&transfer="));
        return  "http://" + host + ":" + port + "/transaction?txId=" + tx.id + transfers;
    }
}
