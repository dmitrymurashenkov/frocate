package com.frocate.taskrunner;

import java.io.File;

public interface Env extends AutoCloseable
{
    void close();
    String getBuildId();

    /**
     * Name must be unique within this VM
     */
    VM createVM(String name);
    VM createVM(String name, boolean allowDockerControl);
    String getNetworkName();

    /**
     * File is created in tmp dir associated with this environment and will be deleted upon env close.
     */
    File createTmpFile(String name);
    File createTmpFile(String name, String content);

    PropertiesBuilder buildProperties();

    File getJarWithClass(Class clazz);
}
