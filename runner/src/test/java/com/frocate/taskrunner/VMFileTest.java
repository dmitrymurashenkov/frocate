package com.frocate.taskrunner;

import org.junit.Test;

import static com.frocate.taskrunner.VMFile.vmFile;
import static org.junit.Assert.assertEquals;

public class VMFileTest
{
    @Test
    public void vmFile_shouldReturnFileWithExpectedPath()
    {
        assertEquals("/home/test/1.txt", vmFile("1.txt").getAbsolutePath());
        assertEquals("/home/test/./1.txt", vmFile("./1.txt").getAbsolutePath());
        assertEquals("/1.txt", vmFile("/1.txt").getAbsolutePath());
    }

}
