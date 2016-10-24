package com.frocate.taskrunner;

public class ProcessTimeoutException extends RuntimeException
{
    public ProcessTimeoutException(String message)
    {
        super(message);
    }
}
