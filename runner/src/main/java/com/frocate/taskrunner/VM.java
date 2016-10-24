package com.frocate.taskrunner;

import java.io.File;

public interface VM
{
    String getName();
    String getIp();

    void start();
    void stop();
    boolean isRunning();

    ProcessFuture runCommand(String command);

    boolean isFileExists(VMFile fileInVm);
    void copyFromHost(File fileOnHost, VMFile fileInVm);
    void copyToHost(VMFile fileInVm, File fileOnHost);
    void copyToHostIfExists(VMFile fileInVm, File fileOnHost);

    /**
     * Copies content to tmp delete-on-exit file
     */
    File copyToHost(VMFile fileInVm);
}
