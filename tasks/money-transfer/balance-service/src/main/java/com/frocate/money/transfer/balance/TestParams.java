package com.frocate.money.transfer.balance;

public class TestParams
{
    public static final String TRANSFER_SERVICE_HOST = System.getProperty("transfer-service-host", "127.0.0.1");
    public static final int TRANSFER_SERVICE_PORT = Integer.parseInt(System.getProperty("transfer-service-port", "8080"));

    public static final String BALANCE_SERVICE_HOST = System.getProperty("balance-service-host", "127.0.0.1");
    public static final int BALANCE_SERVICE_PORT = Integer.parseInt(System.getProperty("balance-service-port", "8081"));
}
