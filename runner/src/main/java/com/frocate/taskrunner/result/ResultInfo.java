package com.frocate.taskrunner.result;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.frocate.taskrunner.executable.ExecutableType;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResultInfo
{
    private String id;
    private ExecutableType executableType;
    private Date startTime;
    private String taskId;
    private List<TestResult> tests = new ArrayList<>();
    private List<Metric> metrics = new ArrayList<>();

    ResultInfo() {}

    public ResultInfo(String id, ExecutableType executableType, Date startTime, String taskId, List<TestResult> tests, List<Metric> metrics)
    {
        this.id = id;
        this.executableType = executableType;
        this.startTime = startTime;
        this.taskId = taskId;
        this.tests = tests;
        this.metrics = metrics;
    }

    public String getId()
    {
        return id;
    }

    public ExecutableType getExecutableType()
    {
        return executableType;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public List<TestResult> getTests()
    {
        return tests;
    }

    public List<Metric> getMetrics()
    {
        return metrics;
    }

    public String toJson()
    {
        return new Gson().toJson(this);
    }

    public void save(File file)
    {
        try
        {
            Files.write(toJson().getBytes(), file);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static ResultInfo load(InputStream stream)
    {
        try
        {
            return new Gson().fromJson(new InputStreamReader(stream), ResultInfo.class);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static ResultInfo load(File file)
    {
        try
        {
            return new Gson().fromJson(new FileReader(file), ResultInfo.class);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ResultInfo that = (ResultInfo) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (executableType != that.executableType)
        {
            return false;
        }
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null)
        {
            return false;
        }
        if (taskId != null ? !taskId.equals(that.taskId) : that.taskId != null)
        {
            return false;
        }
        if (tests != null ? !tests.equals(that.tests) : that.tests != null)
        {
            return false;
        }
        return metrics != null ? metrics.equals(that.metrics) : that.metrics == null;

    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (executableType != null ? executableType.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
        result = 31 * result + (tests != null ? tests.hashCode() : 0);
        result = 31 * result + (metrics != null ? metrics.hashCode() : 0);
        return result;
    }
}
