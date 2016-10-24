package com.frocate.web;

import com.frocate.taskrunner.Task;
import com.frocate.taskrunner.TaskRunner;
import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.junit.TaskProgress;
import com.frocate.taskrunner.result.ProgressListener;
import com.frocate.taskrunner.result.Result;
import com.frocate.taskrunner.result.ResultStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

import static com.frocate.web.TaskRunnerParams.MAX_EXECUTABLE_LOG_SIZE;
import static com.frocate.web.TaskRunnerParams.MAX_TEST_LOG_SIZE;

public class TaskRunnerFarm
{
    public static final Logger log = LoggerFactory.getLogger(TaskRunnerFarm.class);

    public static final String RESULT_DIR = System.getProperty("com.frocate.RESULT_DIR", "/tmp");

    //todo inject
    public static final TaskRunnerFarm INSTANCE = new TaskRunnerFarm();

    private final BlockingQueue<Build> builds = new ArrayBlockingQueue<>(100);
    private final Thread executor = new Thread(this::processBuildQueue);
    private final ResultStorage storage = new ResultStorage(new File(RESULT_DIR));
    private final TaskRunner runner = new TaskRunner(MAX_TEST_LOG_SIZE, MAX_EXECUTABLE_LOG_SIZE, this::onTaskProgress);
    private final Map<String, TaskProgress> progress = Collections.synchronizedMap(new HashMap<>());

    private TaskRunnerFarm()
    {
        executor.start();
    }

    public String submit(Task task, Executable executable)
    {
        String buildId = TaskRunner.generateBuildId(task.getId(), new Date());
        progress.put(buildId, null);
        if (!builds.offer(new Build(buildId, task, executable)))
        {
            progress.remove(buildId);
            throw new RuntimeException("Cannot schedule build - queue is already full");
        }
        return buildId;
    }

    private void onTaskProgress(String buildId, TaskProgress taskProgress)
    {
        log.info("Build '{}' progress reported: {}/{}", buildId, taskProgress.getTests().size(), taskProgress.getTotalTests());
        progress.put(buildId, taskProgress);
    }

    public Result getResult(String buildId)
    {
        return storage.get(buildId);
    }

    public TaskProgress getProgress(String buildId)
    {
        return progress.get(buildId);
    }

    private void processBuildQueue()
    {
        while (true)
        {
            try
            {
                builds.take().run();
            }
            catch (Throwable e)
            {
                log.error("Error during build", e);
            }
        }
    }

    public int getIndexInQueue(String buildId)
    {
        int index = 0;
        for (Build build : builds)
        {
            if (buildId.equals(build.buildId))
            {
                return index;
            }
            index++;
        }
        if (progress.containsKey(buildId))
        {
            //if build just remove from the queue, but first progress not yet reported
            return 0;
        }
        return -1;
    }

    class Build implements Runnable
    {
        private final String buildId;
        private final Task task;
        private final Executable executable;

        public Build(String buildId, Task task, Executable executable)
        {
            this.buildId = buildId;
            this.task = task;
            this.executable = executable;
        }

        @Override
        public void run()
        {
            Result result = null;
            try
            {
                result = runner.run(buildId, executable, task);
                storage.add(result);
            }
            catch (Throwable e)
            {
                //todo move this into TaskRunnner?
                //todo mark results someway so that client won't wait for them
                log.error("Build '{}' failed with exception: {}", buildId, e.getMessage(), e);
            }
            finally
            {
                progress.remove(buildId);
                if (result != null && !result.getFile().delete())
                {
                    log.warn("Failed to cleanup tmp result file: " + result.getFile().getAbsolutePath());
                }
                if (!executable.getFile().delete())
                {
                    log.warn("Failed to cleanup tmp executable file: " + result.getFile().getAbsolutePath());
                }
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

            Build build = (Build) o;

            return buildId != null ? buildId.equals(build.buildId) : build.buildId == null;

        }

        @Override
        public int hashCode()
        {
            return buildId != null ? buildId.hashCode() : 0;
        }
    }
}
