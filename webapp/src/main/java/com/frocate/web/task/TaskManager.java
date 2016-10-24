package com.frocate.web.task;


import com.frocate.taskrunner.Task;

import java.util.Map;

public interface TaskManager
{
    public Task getTaskById(String id);

    public Map<String, Task> getTasks();

}
