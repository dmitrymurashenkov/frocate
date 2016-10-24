package com.frocate.taskrunner;

import com.google.common.io.CharStreams;
import com.frocate.taskrunner.executable.JarRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.frocate.taskrunner.executable.Runner.MB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JarRunnerTest
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        //default value for case when frocate is run from IDE, but jar must be built by maven previously
        File jarPath = new File(System.getProperty("mock-executable-jar-path", JarRunnerTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "../mock-jar/mock-executable.jar"));
        if (!jarPath.exists())
        {
            throw new AssertionError("Mock jar not found at location '" + jarPath.getAbsolutePath() + "' it is built by maven before starting tests (are you running frocate from IDE?)");
        }
        String expectedOutput = "abc";
        String command = new JarRunner().buildCommand(jarPath, 256*MB, new String[] {expectedOutput});
        Process process = new ProcessBuilder().command("/bin/bash", "-c", command).start();
        String output = CharStreams.toString(new InputStreamReader(process.getInputStream()));
        process.waitFor(5, TimeUnit.SECONDS);
        assertEquals(0, process.exitValue());
        assertEquals(expectedOutput, output);
    }
}
