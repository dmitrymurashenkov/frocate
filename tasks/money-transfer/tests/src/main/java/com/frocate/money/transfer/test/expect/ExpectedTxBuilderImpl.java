package com.frocate.money.transfer.test.expect;

import com.frocate.money.transfer.test.TxService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ExpectedTxBuilderImpl implements ExpectedTx.Builder
{
    public static final AtomicLong counter = new AtomicLong();
    private final List<TransferBuilderImpl> transferBuilders = new ArrayList<>();

    private final String txId;

    public ExpectedTxBuilderImpl()
    {
        txId = counter.incrementAndGet() + "";
    }

    public ExpectedTxBuilderImpl(String txId)
    {
        this.txId = txId;
    }

    @Override
    public ExpectedTx.TransferBuilder transfer(String from, String to, int amount)
    {
        TransferBuilderImpl builder = new TransferBuilderImpl(this, from, to, amount);
        transferBuilders.add(builder);
        return builder;
    }

    @Override
    public ExpectedTx build()
    {
        List<ExpectedOperation> expectedOperations = new ArrayList<>();
        List<TxService.Transfer> transfers = new ArrayList<>();
        for (TransferBuilderImpl builder : transferBuilders)
        {
            expectedOperations.add(builder.buildCreditOperation());
            expectedOperations.add(builder.buildDebitOperation());
            transfers.add(builder.buildTransfer());
        }
        return new ExpectedTx(txId, expectedOperations, new TxService.Tx(txId, transfers));
    }
}
