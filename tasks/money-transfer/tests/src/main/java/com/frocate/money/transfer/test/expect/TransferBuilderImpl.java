package com.frocate.money.transfer.test.expect;

import com.frocate.money.transfer.test.TxService;

public class TransferBuilderImpl implements ExpectedTx.TransferBuilder
{
    private final ExpectedTx.Builder builder;
    private final String from;
    private final String to;
    private final int amount;
    private int throwExceptionsDuringDebit;
    private int throwExceptionsDuringCredit;
    private boolean throwRollbackDuringDebit;
    private boolean throwRollbackDuringCredit;
    private long delayCreditMs;
    private long delayDebitMs;

    public TransferBuilderImpl(ExpectedTx.Builder builder, String from, String to, int amount)
    {
        this.builder = builder;
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public ExpectedTx.TransferBuilder throwExceptionsDuringDebit(int throwExceptionsDuringDebit)
    {
        this.throwExceptionsDuringDebit = throwExceptionsDuringDebit;
        return this;
    }

    @Override
    public ExpectedTx.TransferBuilder throwExceptionsDuringCredit(int throwExceptionsDuringCredit)
    {
        this.throwExceptionsDuringCredit = throwExceptionsDuringCredit;
        return this;
    }

    @Override
    public ExpectedTx.TransferBuilder throwRollbackDuringDebit()
    {
        this.throwRollbackDuringDebit = true;
        return this;
    }

    @Override
    public ExpectedTx.TransferBuilder throwRollbackDuringCredit()
    {
        this.throwRollbackDuringCredit = true;
        return this;
    }

    @Override
    public ExpectedTx.TransferBuilder transfer(String from, String to, int amount)
    {
        return builder.transfer(from, to, amount);
    }

    @Override
    public ExpectedTx.TransferBuilder processingTimeCreditMs(long delayCreditMs)
    {
        this.delayCreditMs = delayCreditMs;
        return this;
    }

    @Override
    public ExpectedTx.TransferBuilder processingTimeDebitMs(long delayDebitMs)
    {
        this.delayDebitMs = delayDebitMs;
        return this;
    }

    @Override
    public ExpectedTx build()
    {
        return builder.build();
    }

    public ExpectedOperation buildCreditOperation()
    {
        return new ExpectedOperation(from, -amount, delayCreditMs, throwExceptionsDuringCredit, throwRollbackDuringCredit);
    }

    public ExpectedOperation buildDebitOperation()
    {
        return new ExpectedOperation(to, amount, delayDebitMs, throwExceptionsDuringDebit, throwRollbackDuringDebit);
    }

    public TxService.Transfer buildTransfer()
    {
        return new TxService.Transfer(from, to, amount);
    }
}
