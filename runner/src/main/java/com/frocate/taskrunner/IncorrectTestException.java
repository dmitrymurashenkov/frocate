package com.frocate.taskrunner;

public class IncorrectTestException extends RuntimeException
{
    public IncorrectTestException(String message)
    {
        super(message);
    }
}
