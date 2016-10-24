package com.frocate.money.transfer.mock.tx;

public interface OperationListener
{
    void onPerformed(OperationStatus status);
}
