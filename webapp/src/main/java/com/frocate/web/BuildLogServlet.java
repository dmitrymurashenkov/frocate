package com.frocate.web;

import com.frocate.taskrunner.FileUtils;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.junit.TaskProgress;
import com.frocate.taskrunner.result.*;
import com.google.common.io.CharStreams;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;


@WebServlet("buildLog")
public class BuildLogServlet extends HttpServlet
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
        CharStreams.copy(new InputStreamReader(result.getExecutableLog()), resp.getWriter());
    }
}
