package com.frocate.taskrunner;

public class TestMainJar
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Hello world");
        }
        else
        {
            System.out.print(args[0]);
        }
    }
}
