package com.frocate.taskrunner;

import java.io.File;

import static com.frocate.taskrunner.DockerVM.DOCKER_USER;

public class VMFile extends File
{
    private VMFile(String pathname)
    {
        super(pathname);
    }

    public static VMFile vmFile(String pathInVm)
    {
        if (pathInVm.startsWith("/"))
        {
            return new VMFile(pathInVm);
        }
        else
        {
            return new VMFile("/home/" + DOCKER_USER + "/" + pathInVm);
        }
    }
}
