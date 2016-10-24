package com.frocate.money.transfer.mock;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxServlet extends HttpServlet
{
    private final MockTxService service;

    public TxServlet(MockTxService service)
    {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String txId = req.getParameter("txId");
        List<String> transfers = Arrays.asList(req.getParameterValues("transfer"));

        List<MockTxService.Operation> operations = new ArrayList<>();
        for (String fromToAmount : transfers)
        {
            List<String> transferParams = TxTools.parseSeparatedString(fromToAmount, ",");
            operations.add(new MockTxService.Operation(
                    transferParams.get(0),
                    transferParams.get(1),
                    Integer.parseInt(transferParams.get(2))
                )
            );
        }
        AsyncContext context = req.startAsync();

        try
        {
            service.transfer(txId, operations, context::complete);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            resp.sendError(400);
            context.complete();
        }
    }
}
