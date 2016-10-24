package com.frocate.taskrunner.executable;

public enum ExecutableType
{
    JAR(new JarRunner()),
    LINUX_BINARY(new LinuxBinaryRunner());

    private final Runner runner;

    ExecutableType(Runner runner)
    {
        this.runner = runner;
    }

    public Runner getRunner()
    {
        return runner;
    }
}
