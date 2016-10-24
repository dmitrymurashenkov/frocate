package com.frocate.money.transfer.test;

import org.eclipse.jetty.client.api.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface TxService
{
    void transfer(Tx tx);
    TransferFuture transferAsync(Tx tx);

    public static interface TransferFuture
    {
        Response get(long timeout, TimeUnit unit) throws Exception;
    }

    public static class Tx
    {
        final String id;
        final List<Transfer> transfers = new ArrayList<>();

        public Tx(String id, List<Transfer> transfers)
        {
            this.id = id;
            this.transfers.addAll(transfers);
        }
    }

    public static class Transfer
    {
        final String from;
        final String to;
        final int amount;

        public Transfer(String from, String to, int amount)
        {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }
    }
}
