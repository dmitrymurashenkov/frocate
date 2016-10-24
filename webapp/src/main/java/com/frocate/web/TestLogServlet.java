package com.frocate.web;

import com.frocate.taskrunner.result.Result;
import com.google.common.io.CharStreams;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;


@WebServlet("testLog")
public class TestLogServlet extends HttpServlet
{
    public static final String PARAM_BUILD_ID = "buildId";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (req.getParameter(PARAM_BUILD_ID) == null)
        {
            throw new IllegalArgumentException("Request must contain param: " + PARAM_BUILD_ID);
        }

        String buildId = req.getParameter(PARAM_BUILD_ID);
        Result result = TaskRunnerFarm.INSTANCE.getResult(buildId);
        if (result == null)
        {
            throw new IllegalArgumentException("Logs for build with id '" + buildId + "' not found");
        }

        resp.setContentType("text/plain");
        CharStreams.copy(new InputStreamReader(result.getTestLog()), resp.getWriter());
    }
}
