package com.frocate.web;

import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.junit.TaskProgress;
import com.frocate.taskrunner.result.*;
import com.frocate.taskrunner.result.Result;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;


@WebServlet("result")
public class ResultServlet extends HttpServlet
{
    public static final String PARAM_BUILD_ID = "buildId";
    public static final String ANSWER_STATUS = "status";
    public static final String ANSWER_DATA = "data";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (req.getParameter(PARAM_BUILD_ID) == null)
        {
            throw new IllegalArgumentException("Request must contain param: " + PARAM_BUILD_ID);
        }

        String buildId = req.getParameter(PARAM_BUILD_ID);
        int indexInQueue = TaskRunnerFarm.INSTANCE.getIndexInQueue(buildId);
        TaskProgress progress = TaskRunnerFarm.INSTANCE.getProgress(buildId);
        Result result = TaskRunnerFarm.INSTANCE.getResult(buildId);
//        Result result = getMockResult();

        JSONObject answerJson = new JSONObject();

        if (result != null)
        {
            answerJson.put(ANSWER_STATUS, "resultReady");
            answerJson.put(ANSWER_DATA, constructResultJSON(result));
            answerJson.write(resp.getWriter());
        } else if (progress != null)
        {
            answerJson.put(ANSWER_STATUS, "inProgress");
            answerJson.put(ANSWER_DATA, constructProgressJSON(progress));
            answerJson.write(resp.getWriter());
        } else if (indexInQueue != -1)
        {
            answerJson.put(ANSWER_STATUS, "inQueue");
            answerJson.put(ANSWER_DATA, constructInQueue(indexInQueue));
            answerJson.write(resp.getWriter());
        } else
        {
            answerJson.put(ANSWER_STATUS, "unknownBuildId");
            answerJson.write(resp.getWriter());
        }
    }

    private JSONObject constructInQueue(int indexInQueue) throws IOException
    {
        JSONObject progressJson = new JSONObject();
        progressJson.put("indexInQueue", indexInQueue);
        return progressJson;
    }

    private JSONObject constructProgressJSON(TaskProgress progress) throws IOException
    {
        JSONObject progressJson = new JSONObject();
        progressJson.put("totalTests", progress.getTotalTests());
        progressJson.put("finishedTests", progress.getTests().size());
        progressJson.put("currentTest", progress.getCurrentTest());
        return progressJson;
    }

    private JSONObject constructResultJSON(Result result) throws IOException
    {
        int passed = 0;

        List<JSONObject> failures = new ArrayList<>();

        for (TestResult res : result.getTestResults())
        {
            if (res.isSuccess())
            {
                passed++;
            } else
            {
                JSONObject failedResult = new JSONObject();
                failedResult.put("testname", res.getName());
                failedResult.put("errormsg", res.getError());
                failures.add(failedResult);
            }
        }

        List<JSONObject> metrics = new ArrayList<>();
        for (Metric metric : result.getMetrics())
        {
            JSONObject excellentRangeObj = new JSONObject();
            excellentRangeObj.put("start", metric.getExcellentRange().getStartInclusive());
            excellentRangeObj.put("end", metric.getExcellentRange().getEndInclusive());

            JSONObject goodRangeObj = new JSONObject();
            goodRangeObj.put("start", metric.getGoodRange().getStartInclusive());
            goodRangeObj.put("end", metric.getGoodRange().getEndInclusive());

            JSONObject metricObj = new JSONObject();
            metricObj.put("name", metric.getName());
            metricObj.put("desc", metric.getDescription());
            metricObj.put("unit", metric.getUnit());
            metricObj.put("value", metric.getValue());
            metricObj.put("goodRange", goodRangeObj);
            metricObj.put("excellentRange", excellentRangeObj);
            metricObj.put("rating", metric.isError() ? "ERROR" : metric.getRating());
            metrics.add(metricObj);
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("id", result.getId());
        resultJson.put("executableType", result.getExecutableType());
        resultJson.put("executableSize", result.getExecutableSize());
        resultJson.put("startTime", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(result.getStartTime()));
        resultJson.put("success", passed + "/" + result.getTestResults().size());
        resultJson.put("failures", failures);
        resultJson.put("metrics", metrics);
        resultJson.put("ready", true);

        return resultJson;
    }

    private Result getMockResult() throws IOException
    {
        File file = Files.createTempFile("test", "txt").toFile();
        Executable executable = new Executable(file, ExecutableType.JAR);

        TestResult resultPassed = new TestResult("PassedTest", true, null);
        TestResult resultFailed1 = new TestResult("MyTestName", false, "RuntimeException, something went wrong");
        TestResult resultFailed2 = new TestResult("AnotherTest", false, "IllegalStateException, something went very very wrong");

        Metric metric1 = new Metric("Metric1", "Metric of something", -1, "unit", new Range(5, 10), new Range(11, 20), true);
        Metric metric2 = new Metric("Metric1", "Metric of something", 1, "unit", new Range(5, 10), new Range(11, 20), false);
        Metric metric3 = new Metric("Metric1", "Metric of something", 6, "unit", new Range(5, 10), new Range(11, 20), false);
        Metric metric4 = new Metric("Metric1", "Metric of something", 15, "unit", new Range(5, 10), new Range(11, 20), false);

        ResultInfo info = new ResultInfo("1523",
                ExecutableType.JAR,
                new Date(),
                "myTask",
                Arrays.asList(resultPassed, resultFailed1, resultFailed2),
                Arrays.asList(metric1, metric2, metric3, metric4));

        return new ResultImpl(file, executable, file, file, info);
    }
}
