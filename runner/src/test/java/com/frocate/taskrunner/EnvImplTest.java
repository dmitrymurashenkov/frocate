package com.frocate.taskrunner;

import com.google.common.io.Files;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnvImplTest
{
    private final EnvImpl env = new EnvImpl("build1");;

    @After
    public void tearDown()
    {
        env.close();
    }

    @Test
    public void constructor_shouldCreateDir()
    {
        assertTrue(env.getDir().exists());
        assertTrue(env.getDir().isDirectory());
    }

    @Test
    public void getBuildId_shouldReturnBuildId()
    {
        assertEquals("build1", env.getBuildId());
    }

    @Test
    public void close_shouldRemoveDir()
    {
        assertTrue(env.getDir().exists());
        env.close();
        assertFalse(env.getDir().exists());
    }

    @Test
    public void close_shouldRemoveNonEmptyDir() throws IOException
    {
        File file = env.createTmpFile("1.txt");
        Files.write("abc".getBytes(), file);
        assertTrue(file.exists());
        env.close();
        assertFalse(env.getDir().exists());
        assertFalse(file.exists());
    }

    @Test
    public void createVM_shouldAddVMToTmpInternalNetwork()
    {
        VM vm = env.createVM("test");
        vm.start();
        assertTrue(vm.getIp().startsWith("172."));
        assertEquals(1, vm.runCommand("ping -w 2 8.8.8.8").exitCode(5, TimeUnit.SECONDS));
    }

    @Test
    public void createVM_shouldAllowDockerControlFromInsideVM_ifFlagSpecified()
    {
        VM vm = env.createVM("test1", true);
        vm.start();
        ProcessFuture result = vm.runCommand("docker ps | grep test1");
        assertTrue(result.stdout(3, TimeUnit.SECONDS).contains(DockerVM.DOCKER_IMAGE_FOR_TESTS));
        assertEquals(0, result.exitCode(3, TimeUnit.SECONDS));
    }

    @Test
    public void createVM_shouldNotAllowDockerControlFromInsideVM_ifFlagNotSpecified()
    {
        VM vm = env.createVM("test1");
        vm.start();
        ProcessFuture result = vm.runCommand("docker ps | grep test1");
        assertEquals("", result.stdout(3, TimeUnit.SECONDS));
        assertEquals(1, result.exitCode(3, TimeUnit.SECONDS));
    }

    @Test
    public void createTmpFile_shouldCreateTmpFileInsideDir()
    {
        File tmpFile = env.createTmpFile("1.txt");
        assertEquals(env.getDir(), tmpFile.getParentFile());
    }

    @Test
    public void createTmpFile_shouldCreateFileWithContent() throws IOException
    {
        File tmpFile = env.createTmpFile("1.txt", "abc");
        assertEquals(env.getDir(), tmpFile.getParentFile());
        assertEquals("abc", new String(Files.toByteArray(tmpFile)));
    }

    @Test
    public void createVM_shouldPrependBuildIdForUniqueness()
    {
        VM vm = env.createVM("frocate");
        assertEquals("build1-frocate", vm.getName());
    }

    @Test
    public void close_shouldBeIdempotent()
    {
        env.close();
        env.close();
        env.close();
    }

    @Test
    public void close_shouldRemoveVMs()
    {
        VM vm = env.createVM("frocate");
        vm.start();
        assertTrue(vm.isRunning());
        env.close();
        assertFalse(vm.isRunning());
    }

    @Test
    public void buildProperties_shouldStorePropertiesToTmpFile() throws IOException
    {
        File file = env.buildProperties()
                .add("key1", "value1")
                .add("key2", "value2")
        .toFile(env.createTmpFile("frocate.properties"));
        assertEquals(env.getDir(), file.getParentFile());
        List<String> lines = Files.readLines(file, Charset.defaultCharset());
        assertEquals(Arrays.asList("key1=value1", "key2=value2"), lines);
        env.close();
        assertFalse(file.exists());
    }

    @Test
    public void getJarWithClass_shouldReturnJarFromClasspathWhichContainsSpecifiedClass()
    {
        File junitJar = env.getJarWithClass(JUnitCore.class);
        //difficult to assert exact path because of dependency version so we check that it points to .m2
        assertTrue(junitJar.getAbsolutePath().contains(".m2/repository/junit/junit"));
    }
}
