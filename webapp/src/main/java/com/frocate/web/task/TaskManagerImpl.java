package com.frocate.web.task;

import com.frocate.money.transfer.test.MoneyTransferTask;
import com.frocate.shorturl.test.ShortUrlTask;
import com.frocate.taskrunner.Task;
import com.frocate.sumtwonumbers.test.SumTwoNumbersTask;

import java.util.*;

public class TaskManagerImpl implements TaskManager
{
    private final LinkedHashMap<String, Task> tasks = new LinkedHashMap<String, Task>();

    public TaskManagerImpl()
    {
        tasks.put(SumTwoNumbersTask.TASK_ID, new SumTwoNumbersTask());
        tasks.put(MoneyTransferTask.TASK_ID, new MoneyTransferTask());
        tasks.put(ShortUrlTask.TASK_ID, new ShortUrlTask());
    }

    public Task getTaskById(String id)
    {
        try
        {
            return tasks.get(id).getClass().getConstructor().newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Task> getTasks()
    {
        return Collections.unmodifiableMap(tasks);
    }
}
