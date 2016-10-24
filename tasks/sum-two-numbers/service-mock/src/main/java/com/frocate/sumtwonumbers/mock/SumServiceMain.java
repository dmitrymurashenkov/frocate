package com.frocate.sumtwonumbers.mock;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Properties;

public class SumServiceMain
{
    public static void main(String[] args) throws Exception
    {
        String host = "127.0.0.1";
        int port = 8080;
        if (args.length == 1)
        {
            System.out.println("Reading properties from: " + args[0]);
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[0]));
            host = properties.getProperty("host");
            port = Integer.parseInt(properties.getProperty("port"));
            System.out.println("Binding server to " + host + ":" + port);
        }
        else
        {
            System.out.println("No config file provided, binding to default address: 127.0.0.1:8080");
        }
        Server server = new Server(new InetSocketAddress(host, port));

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        servletHandler.addServletWithMapping(new ServletHolder(new SumServlet()), "/sum");

        server.start();
        server.dumpStdErr();
        server.join();
    }

    static class SumServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            BigInteger a = new BigInteger(req.getParameter("a"));
            BigInteger b = new BigInteger(req.getParameter("b"));
            BigInteger sum = a.add(b);
            resp.getWriter().write(sum.toString());
        }
    }
}
