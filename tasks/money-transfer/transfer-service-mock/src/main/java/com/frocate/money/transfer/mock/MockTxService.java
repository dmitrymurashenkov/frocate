package com.frocate.money.transfer.mock;

import java.util.List;

public interface MockTxService
{
    void transfer(String txId, List<Operation> operations, Runnable callback) throws Exception;

    public static class Operation
    {
        public final String from;
        public final String to;
        public final int amount;

        public Operation(String from, String to, int amount)
        {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }
    }
}
