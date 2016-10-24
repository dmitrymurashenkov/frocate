package com.frocate.money.transfer.balance;

public class HttpStatuses
{
    public static final String HEADER_ERROR_REASON = "Balance-error-reason";

    public static final String ERROR_REASON_MALFORMED_REQUEST = "MALFORMED_REQUEST";
    public static final String ERROR_REASON_NOT_ENOUGH_MONEY = "NOT_ENOUGH_MONEY";
    public static final String ERROR_REASON_TOO_MANY_ERRORS = "TOO_MANY_ERRORS";
    public static final String ERROR_REASON_UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String ERROR_REASON_ROLLBACK_TX = "ROLLBACK_TX";

    public static final String PARAM_TX_ID = "txId";
    public static final String PARAM_ACCOUNT_ID = "accountId";
    public static final String PARAM_AMOUNT = "amount";
}
