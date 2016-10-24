package com.frocate.taskrunner;

import com.frocate.taskrunner.executable.JarRunner;
import com.frocate.taskrunner.executable.LinuxBinaryRunner;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.frocate.taskrunner.executable.Runner.MB;
import static org.junit.Assert.assertEquals;

public class LinuxBinaryRunnerTest
{
    @Test
    public void test() throws Exception
    {
        String expectedOutput = "abc";
        File executable = FileUtils.createTmpFileWithContent(
                "#!/bin/bash\n" +
                "echo -n $1");
        String command = new LinuxBinaryRunner().buildCommand(executable, 256*MB, new String[] {expectedOutput});
        Process process = new ProcessBuilder().command("/bin/bash", "-c", command).start();
        String output = CharStreams.toString(new InputStreamReader(process.getInputStream()));
        process.waitFor(5, TimeUnit.SECONDS);
        assertEquals(0, process.exitValue());
        assertEquals(expectedOutput, output);
    }
}
