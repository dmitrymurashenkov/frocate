package com.frocate.shorturl.mock;


import com.google.common.io.CharStreams;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

public class MockServlet extends HttpServlet
{
    private final MockService service;

    public MockServlet(MockService service)
    {
        this.service = service;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            if ("/shorten".equals(req.getServletPath()))
            {
                String url = CharStreams.toString(new InputStreamReader(req.getInputStream()));
                String shortUrl = service.shorten(url);
                resp.getWriter().write(shortUrl);
            }
            else if ("/expand".equals(req.getServletPath()))
            {
                String shortUrl = CharStreams.toString(new InputStreamReader(req.getInputStream()));
                String url = service.expand(shortUrl);
                resp.getWriter().write(url);
            }
            else if ("/status".equals(req.getServletPath()))
            {
                return;
            }
            else
            {
                resp.getWriter().write("Unknown operation: " + req.getContextPath());
                resp.setStatus(404);
            }
        }
        catch (Exception e)
        {
            String uri = req.getMethod() + " " + ((Request)req).getHttpURI();
            String errorMessage = "Exception processing request: " + uri + " " + e.getMessage();
            System.out.println(errorMessage);
            e.printStackTrace();
            resp.getWriter().write(errorMessage);
            resp.setStatus(500);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if ("/status".equals(req.getServletPath()))
        {
            return;
        }
        else
        {
            resp.getWriter().write("Unknown operation: " + req.getContextPath());
            resp.setStatus(404);
        }
    }
}
