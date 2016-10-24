package com.frocate.web;

import com.google.common.io.ByteStreams;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("resources/*")
public class ResourceServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        //first char is always "/"
        String resource = req.getPathInfo().substring(1);
        InputStream stream = ResourceServlet.class.getClassLoader().getResourceAsStream(resource);
        if (stream == null)
        {
            resp.sendError(404);
        }
        else
        {
            ByteStreams.copy(stream, resp.getOutputStream());
        }
    }
}
