package com.frocate.money.transfer;

import com.frocate.money.transfer.balance.*;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ErrorTest extends FunctionalTest
{
    @Test
    public void debit_shouldReturn404OnUrlsExceptDebit() throws InterruptedException, ExecutionException, TimeoutException
    {
        startService();
        assertEquals(404, http.GET("http://localhost:8080/").getStatus());
    }

    @Test
    public void debit_shouldReturn400IfRequestMalformed() throws InterruptedException, ExecutionException, TimeoutException
    {
        startService();
        ContentResponse response = http.GET("http://localhost:8080/debit");
        assertEquals(400, response.getStatus());
        Assert.assertEquals(HttpStatuses.ERROR_REASON_MALFORMED_REQUEST, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
        Assert.assertEquals("Missing parameter 'txId'", response.getContentAsString());

        response = http.GET("http://localhost:8080/debit?txId=1");
        assertEquals(400, response.getStatus());
        Assert.assertEquals(HttpStatuses.ERROR_REASON_MALFORMED_REQUEST, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
        Assert.assertEquals("Missing parameter 'accountId'", response.getContentAsString());

        response = http.GET("http://localhost:8080/debit?txId=1&accountId=1");
        assertEquals(400, response.getStatus());
        Assert.assertEquals(HttpStatuses.ERROR_REASON_MALFORMED_REQUEST, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
        Assert.assertEquals("Missing parameter 'amount'", response.getContentAsString());

        assertEquals(10, service.getBalance("1"));
    }

    @Test
    public void debit_shouldReturn400IfNotEnoughMoney() throws InterruptedException, ExecutionException, TimeoutException
    {
        startService();
        ContentResponse response = http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");
        assertEquals(400, response.getStatus());
        assertEquals(HttpStatuses.ERROR_REASON_NOT_ENOUGH_MONEY, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
        assertEquals("Account with id '1' hasn't enough money to be debited by -100$", response.getContentAsString());
        assertEquals(10, service.getBalance("1"));
    }

    @Test
    public void debit_shouldReturn400IfTooManyErrors() throws InterruptedException, ExecutionException, TimeoutException
    {
        startService();
        http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");
        http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");
        http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");

        ContentResponse response = http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");
        assertEquals(400, response.getStatus());
        assertEquals(HttpStatuses.ERROR_REASON_NOT_ENOUGH_MONEY, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));

        response = http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");
        assertEquals(400, response.getStatus());
        assertEquals(HttpStatuses.ERROR_REASON_TOO_MANY_ERRORS, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
        assertEquals(10, service.getBalance("1"));
    }

    @Test
    public void debit_shouldReturn400IfTooManyUnknownErrors() throws InterruptedException, ExecutionException, TimeoutException
    {
        AtomicInteger exceptionsToThrow = new AtomicInteger(6);
        startWithService(new BalanceServiceImpl(AccountGenerator.one("1", 10))
        {
            @Override
            protected void beforeBalanceUpdate(String txId, String accountId, int amount) throws NotEnoughMoneyException, TooManyErrorsException, UnknownException, RollbackTxException, RequestMalformedException
            {
                if (exceptionsToThrow.decrementAndGet() > 0)
                {
                    throw new UnknownException("Unknown exception");
                }
            }
        });
        http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=1");
        http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=1");
        http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=1");

        ContentResponse response = http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=1");
        assertEquals(400, response.getStatus());
        assertEquals(HttpStatuses.ERROR_REASON_UNKNOWN_ERROR, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));

        response = http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=1");
        assertEquals(400, response.getStatus());
        assertEquals(HttpStatuses.ERROR_REASON_TOO_MANY_ERRORS, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
        assertEquals(10, service.getBalance("1"));
    }

    @Test
    public void debit_shouldRefreshTooManyErrorsFlagIfNewRequestBeforeFlagCleared() throws InterruptedException, ExecutionException, TimeoutException
    {
        startService();
        for (int i = 0; i < 5; i++)
        {
            //trigger too many errors
            http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");
        }
        for (int i = 0; i < 15; i++)
        {
            //do not wait long enough for "too many error" flag to clear - it should be refreshed if timeout not expired yet
            ContentResponse response = http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");
            assertEquals(400, response.getStatus());
            assertEquals(HttpStatuses.ERROR_REASON_TOO_MANY_ERRORS, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
            Thread.sleep(200);
        }
        Thread.sleep(1000);

        //after timeout we should get our usual error
        ContentResponse response = http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=-100");
        assertEquals(400, response.getStatus());
        assertEquals(HttpStatuses.ERROR_REASON_NOT_ENOUGH_MONEY, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
    }

    @Test
    public void debit_shouldReturn400IfListenerThrowsRollbackException() throws InterruptedException, ExecutionException, TimeoutException
    {
        startWithService(new BalanceServiceImpl(AccountGenerator.one("1", 10))
        {
            @Override
            protected void beforeBalanceUpdate(String txId, String accountId, int amount) throws NotEnoughMoneyException, TooManyErrorsException, UnknownException, RollbackTxException, RequestMalformedException
            {
                throw new RollbackTxException("Rollback!");
            }
        });
        ContentResponse response = http.GET("http://localhost:8080/debit?txId=1&accountId=1&amount=100");
        assertEquals(400, response.getStatus());
        assertEquals(HttpStatuses.ERROR_REASON_ROLLBACK_TX, response.getHeaders().get(HttpStatuses.HEADER_ERROR_REASON));
        assertEquals(10, service.getBalance("1"));
    }
}
