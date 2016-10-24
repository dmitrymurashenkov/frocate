package com.frocate.taskrunner;

import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.result.ProgressListener;

import java.util.Collection;

public interface Task
{
    TaskResult run(Executable executable, Env env, ProgressListener listener);
    String getId();
    String getName();
    String getShortDescription();
    String getDescription();
    Collection<String> getTags();
}
