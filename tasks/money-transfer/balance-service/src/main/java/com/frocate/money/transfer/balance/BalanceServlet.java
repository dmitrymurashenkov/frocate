package com.frocate.money.transfer.balance;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BalanceServlet extends HttpServlet
{
    public static final Logger log = LoggerFactory.getLogger(BalanceServlet.class);

    private final BalanceService service;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public BalanceServlet(BalanceService service)
    {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            checkRequestValid(req);
        }
        catch (Exception e)
        {
            resp.setHeader(HttpStatuses.HEADER_ERROR_REASON, HttpStatuses.ERROR_REASON_MALFORMED_REQUEST);
            ByteStreams.copy(new ByteArrayInputStream(e.getMessage().getBytes()), resp.getOutputStream());
            resp.setStatus(400);
            return;
        }

        AsyncContext asyncContext = req.startAsync();
        String txId = req.getParameter(HttpStatuses.PARAM_TX_ID);
        String account = req.getParameter(HttpStatuses.PARAM_ACCOUNT_ID);
        int amount = Integer.parseInt(req.getParameter(HttpStatuses.PARAM_AMOUNT));
        executor.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    try
                    {
                        service.debit(txId, account, amount);
                    }
                    catch (RequestMalformedException e)
                    {
                        resp.setHeader(HttpStatuses.HEADER_ERROR_REASON, HttpStatuses.ERROR_REASON_MALFORMED_REQUEST);
                        ByteStreams.copy(new ByteArrayInputStream(e.getMessage().getBytes()), resp.getOutputStream());
                        resp.setStatus(400);
                    }
                    catch (NotEnoughMoneyException e)
                    {
                        resp.setHeader(HttpStatuses.HEADER_ERROR_REASON, HttpStatuses.ERROR_REASON_NOT_ENOUGH_MONEY);
                        ByteStreams.copy(new ByteArrayInputStream(e.getMessage().getBytes()), resp.getOutputStream());
                        resp.setStatus(400);
                    }
                    catch (TooManyErrorsException e)
                    {
                        resp.setHeader(HttpStatuses.HEADER_ERROR_REASON, HttpStatuses.ERROR_REASON_TOO_MANY_ERRORS);
                        ByteStreams.copy(new ByteArrayInputStream(e.getMessage().getBytes()), resp.getOutputStream());
                        resp.setStatus(400);
                    }
                    catch (UnknownException e)
                    {
                        resp.setHeader(HttpStatuses.HEADER_ERROR_REASON, HttpStatuses.ERROR_REASON_UNKNOWN_ERROR);
                        ByteStreams.copy(new ByteArrayInputStream(e.getMessage().getBytes()), resp.getOutputStream());
                        resp.setStatus(400);
                    }
                    catch (RollbackTxException e)
                    {
                        resp.setHeader(HttpStatuses.HEADER_ERROR_REASON, HttpStatuses.ERROR_REASON_ROLLBACK_TX);
                        ByteStreams.copy(new ByteArrayInputStream(e.getMessage().getBytes()), resp.getOutputStream());
                        resp.setStatus(400);
                    }
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                    log.error("Critical error during request", e);
                    try
                    {
                        resp.sendError(500);
                    }
                    catch (IOException e1)
                    {
                        e.printStackTrace();
                    }
                }
                finally
                {
                    asyncContext.complete();
                }
            }
        }, service.getOperationDelay(txId, account, amount), TimeUnit.MILLISECONDS);
    }

    private void checkRequestValid(HttpServletRequest req)
    {
        for (String param : new String[] { HttpStatuses.PARAM_TX_ID, HttpStatuses.PARAM_ACCOUNT_ID, HttpStatuses.PARAM_AMOUNT })
        {
            if (req.getParameter(param) == null)
            {
                throw new IllegalArgumentException("Missing parameter '" + param + "'");
            }
        }
        Integer.parseInt(req.getParameter(HttpStatuses.PARAM_AMOUNT));
    }
}
