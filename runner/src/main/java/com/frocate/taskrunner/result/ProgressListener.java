package com.frocate.taskrunner.result;

import com.frocate.taskrunner.junit.TaskProgress;

public interface ProgressListener
{
    public static final ProgressListener STUB = (buildId, progress) -> {
        System.out.println("Build: " + buildId + " progress: " + progress.getTests().size() + "/" + progress.getTotalTests() + " current test: " + progress.getCurrentTest());
    };

    public void onProgress(String buildId, TaskProgress progress);
}
