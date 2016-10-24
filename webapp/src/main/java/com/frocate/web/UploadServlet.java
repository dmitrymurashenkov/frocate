package com.frocate.web;

import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.web.task.TaskManager;
import com.frocate.web.task.TaskManagerImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@WebServlet("upload")
@MultipartConfig
public class UploadServlet extends HttpServlet
{
    private final TaskManager taskManager = new TaskManagerImpl();
    private final Map<String, ExecutableType> executableTypes = new HashMap<>();

    public UploadServlet() {
        executableTypes.put("jar", ExecutableType.JAR);
        executableTypes.put("linuxbinary", ExecutableType.LINUX_BINARY);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            String taskId = req.getParameter("taskId");
            Part filePart = req.getPart("executable");
            Path uploadedFile = Files.createTempFile("executable-", null);
            Files.copy(filePart.getInputStream(), uploadedFile, StandardCopyOption.REPLACE_EXISTING);
            if (uploadedFile.toFile().length() > 20*1024*1024)
            {
                uploadedFile.toFile().delete();
                throw new RuntimeException("Executable file too big, limit is 20Mb");
            }
            Executable executable = new Executable(uploadedFile.toFile(), executableTypes.get(req.getParameter("exectype")));
            String buildId = TaskRunnerFarm.INSTANCE.submit(taskManager.getTaskById(taskId), executable);
            resp.getOutputStream().println(
                    "{ \"status\": \"success\", \"buildId\": \"" + buildId + "\"}"
            );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            resp.getOutputStream().println(
                    "{ \"status\": \"error\", \"reason\": \"" + e.getClass().getSimpleName() + " " + e.getMessage() + "\"}"
            );
        }
    }
}
