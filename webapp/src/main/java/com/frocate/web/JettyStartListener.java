package com.frocate.web;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.io.IoBuilder;
import org.apache.logging.log4j.io.LoggerOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.PrintStream;

@WebListener
public class JettyStartListener implements ServletContextListener
{
    public static final Logger log = LoggerFactory.getLogger(JettyStartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        log.info("Starting Frocate app");
        boolean redirectStdOutToSlf4j = Boolean.parseBoolean(System.getProperty("com.frocate.web.JettyStartListener.STDOUT_TO_SLF4J", "true"));
        if (redirectStdOutToSlf4j)
        {
            System.out.println("Redirecting stdout and stderr to slf4j");
            System.out.println("Only other processes started with ProcessBuilder.inheritIO() will write to this file");

            System.setOut(IoBuilder.forLogger("STDOUT").setLevel(Level.INFO).buildPrintStream());
            System.setErr(IoBuilder.forLogger("STDERR").setLevel(Level.ERROR).buildPrintStream());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
    }
}
