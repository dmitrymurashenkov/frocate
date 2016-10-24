package com.frocate.taskrunner;

import com.google.common.io.Files;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.frocate.taskrunner.VMFile.vmFile;
import static org.junit.Assert.*;

public class DockerVMTest
{
    @Test
    public void constructor_shouldThrowException_ifNameInvalid()
    {
        assertExceptionThrownIfContainerName("!");
        assertExceptionThrownIfContainerName("123^");
        new DockerVM("123");
        new DockerVM("abc");
        new DockerVM("ABC");
        new DockerVM("abc-123");
        new DockerVM("abc_123");
    }

    @Test
    public void start_shouldThrowException_ifContainerAlreadyRunning()
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            vm.start();
            assertTrue(vm.isRunning());
            try
            {
                vm.start();
                fail("Exception expected");
            }
            catch (RuntimeException e)
            {
                //ok
            }
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void stop_mustStopAndRemoveContainer()
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            vm.start();
            assertTrue(vm.isRunning());
            vm.stop();
            assertFalse(vm.isRunning());
            vm.start();
            assertTrue(vm.isRunning());
            assertEquals(0, vm.runCommand("echo 123").exitCode(5, TimeUnit.SECONDS));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void stop_shouldBeIdempotent()
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            vm.start();
            vm.stop();
            vm.stop();
            vm.stop();
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void runCommand_shouldRunCommandInHomeDirInsideContainer() throws IOException
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            vm.start();
            //1.txt means /home/frocate/1.txt
            assertFalse(vm.isFileExists(vmFile("1.txt")));
            //note that command is executed in user home dir - otherwise 1.txt would not be found later
            vm.runCommand("echo -n 123 > 1.txt").exitCode(5, TimeUnit.SECONDS);
            assertEquals("123", FileUtils.readContent(vm.copyToHost(vmFile("1.txt"))));

            vm.runCommand("echo -n abc &> 2.txt").exitCode(5, TimeUnit.SECONDS);
            assertEquals("abc", FileUtils.readContent(vm.copyToHost(vmFile("2.txt"))));

        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void getIp_shouldReturnNull_ifNotAttachedToNetwork() throws IOException
    {
        DockerVM vm = new DockerVM("test");
        try
        {
            vm.start();
            assertNull(vm.getIp());
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void getIp_shouldReturnIp_ifAttachedToNetwork() throws IOException
    {
        DockerVM vm = new DockerVM("test", "bridge");
        try
        {
            vm.start();
            assertTrue(vm.getIp().startsWith("172."));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void start_shouldAttachContainerToNetworkStackOfAnotherContainer() throws IOException, InterruptedException
    {
        DockerVM vm1 = new DockerVM("test1");
        DockerVM vm2 = new DockerVM("test2", "container:" + vm1.getName());
        try
        {
            vm1.start();
            vm2.start();
            assertFalse(vm1.isFileExists(vmFile("1.txt")));
            ProcessFuture server = vm1.runCommand("nc -l -p 10000 > 1.txt");
            //need to repeat command several times if server not yet started
            vm2.runCommand("until echo abc | nc -q 5 127.0.0.1 10000; do sleep 0.1s; done").exitCode(5, TimeUnit.SECONDS);
            server.exitCode(5, TimeUnit.SECONDS);
            assertTrue(vm1.isFileExists(vmFile("1.txt")));
            assertEquals("abc", readFile(vm1.copyToHost(vmFile("1.txt"))));
        }
        finally
        {
            vm1.stop();
            vm2.stop();
        }
    }

    @Test
    public void start_shouldAttachContainerToSpecifiedNetwork() throws IOException, InterruptedException
    {
        DockerVM vm1 = new DockerVM("test1", "bridge");
        DockerVM vm2 = new DockerVM("test2", "bridge");
        try
        {
            vm1.start();
            vm2.start();
            ProcessFuture server = vm1.runCommand("nc -l -p 10000");
            //need to repeat command several times if server not yet started
            vm2.runCommand("until echo abc | nc -q 5 " + vm1.getIp() + "  10000; do sleep 0.1s; done").exitCode(5, TimeUnit.SECONDS);
            assertEquals("abc", server.stdout(3, TimeUnit.SECONDS));
        }
        finally
        {
            vm1.stop();
            vm2.stop();
        }
    }

    @Test
    public void start_shouldLimitPhysicalMemory()
    {
        int memoryLimitBytes = 20*1024*1024;
        DockerVM limitedMemory = new DockerVM("test1", null, false, memoryLimitBytes);
        DockerVM unlimitedMemory = new DockerVM("test2");
        try
        {
            limitedMemory.start();
            long maxMemory = Long.parseLong(limitedMemory.runCommand("cat /sys/fs/cgroup/memory/memory.limit_in_bytes").stdout(3, TimeUnit.SECONDS));
            assertEquals(memoryLimitBytes, maxMemory);

            //double check by trying to allocate file in tmpfs and looking at swap usage before and after
            //then perform same operation in container with unlimited memory and check that effect is different
            int swapBefore = Integer.parseInt(limitedMemory.runCommand("swapon --show=USED --noheadings --bytes").stdout(3, TimeUnit.SECONDS).trim());
            assertEquals(0, limitedMemory.runCommand("dd if=/dev/zero of=/dev/shm/fill bs=1024k count=50").exitCode(3, TimeUnit.SECONDS));
            int swapAfter = Integer.parseInt(limitedMemory.runCommand("swapon --show=USED --noheadings --bytes").stdout(3, TimeUnit.SECONDS).trim());
            assertTrue(swapAfter - swapBefore >= 30*1024*1024);
            limitedMemory.stop();

            unlimitedMemory.start();
            maxMemory = Long.parseLong(unlimitedMemory.runCommand("cat /sys/fs/cgroup/memory/memory.limit_in_bytes").stdout(3, TimeUnit.SECONDS));
            assertTrue(maxMemory > memoryLimitBytes*5);

            swapBefore = Integer.parseInt(unlimitedMemory.runCommand("swapon --show=USED --noheadings --bytes").stdout(3, TimeUnit.SECONDS).trim());
            assertEquals(0, unlimitedMemory.runCommand("dd if=/dev/zero of=/dev/shm/fill bs=1024k count=50").exitCode(3, TimeUnit.SECONDS));
            swapAfter = Integer.parseInt(unlimitedMemory.runCommand("swapon --show=USED --noheadings --bytes").stdout(3, TimeUnit.SECONDS).trim());
            assertTrue(swapAfter - swapBefore < 30*1024*1024);
        }
        finally
        {
            limitedMemory.stop();
            unlimitedMemory.stop();
        }
    }

    @Test
    public void start_shouldAllowDockerControlFromInsideVM_ifFlagSpecified() throws IOException, InterruptedException
    {
        DockerVM vm = new DockerVM("test1", null, true);
        try
        {
            vm.start();
            ProcessFuture result = vm.runCommand("docker ps | grep test1");
            assertTrue(result.stdout(3, TimeUnit.SECONDS).contains(DockerVM.DOCKER_IMAGE_FOR_TESTS));
            assertEquals(0, result.exitCode(3, TimeUnit.SECONDS));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void start_shouldNotAllowDockerControlFromInsideVM_ifFlagNotSpecified() throws IOException, InterruptedException
    {
        DockerVM vm = new DockerVM("test1");
        try
        {
            vm.start();
            ProcessFuture result = vm.runCommand("docker ps | grep test1");
            assertEquals("", result.stdout(3, TimeUnit.SECONDS));
            assertEquals(1, result.exitCode(3, TimeUnit.SECONDS));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void runCommand_shouldReturnProcessFuture() throws IOException, InterruptedException
    {
        DockerVM vm = new DockerVM("test");
        try
        {
            vm.start();
            ProcessFuture future = vm.runCommand("sleep 3");
            assertFalse(future.waitFor(1, TimeUnit.SECONDS));
            assertFalse(future.waitFor(1, TimeUnit.SECONDS));
            assertTrue(future.waitFor(5, TimeUnit.SECONDS));
            assertEquals(0, future.exitCode(1,TimeUnit.SECONDS));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void runCommand_shouldSaveStdout() throws IOException, InterruptedException
    {
        DockerVM vm = new DockerVM("test");
        try
        {
            vm.start();
            ProcessFuture future = vm.runCommand("echo -n 123");
            assertEquals("123", future.stdout(3, TimeUnit.SECONDS));
            assertEquals("", future.stderr(3, TimeUnit.SECONDS));
            assertEquals(0, future.exitCode(1,TimeUnit.SECONDS));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void runCommand_shouldSaveFirst10KbytesOfStdout() throws IOException, InterruptedException
    {
        DockerVM vm = new DockerVM("test");
        try
        {
            vm.start();
            ProcessFuture future = vm.runCommand("for i in `seq 1 20000`; do echo $i; done");
            String stdout = future.stdout(3, TimeUnit.SECONDS);
            assertEquals(10316, stdout.length());
            assertTrue(stdout.endsWith("Too much output! 10240 bytes of output received - ignoring further output"));
            assertEquals("", future.stderr(3, TimeUnit.SECONDS));
            assertEquals(0, future.exitCode(1,TimeUnit.SECONDS));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void runCommand_shouldSaveStderr() throws IOException, InterruptedException
    {
        DockerVM vm = new DockerVM("test");
        try
        {
            vm.start();
            ProcessFuture future = vm.runCommand("echo -n 123 >&2");
            assertEquals("", future.stdout(3, TimeUnit.SECONDS));
            assertEquals("123", future.stderr(3, TimeUnit.SECONDS));
            assertEquals(0, future.exitCode(1,TimeUnit.SECONDS));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void copyToHost_shouldCreateTmpFile() throws IOException
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            vm.start();
            vm.runCommand("echo -n abc > 1.txt").exitCode(5, TimeUnit.SECONDS);
            File fileOnHost = vm.copyToHost(vmFile("1.txt"));
            assertEquals("abc", readFile(fileOnHost));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test(expected = RuntimeException.class)
    public void copyToHost_shouldThrowException_ifFileNotExists() throws IOException
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            vm.start();
            vm.copyToHost(vmFile("1.txt"));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void copyToHostIfExists_shoulddoNothing_ifFileNotExists() throws IOException
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            vm.start();
            vm.copyToHostIfExists(vmFile("1.txt"), new File(""));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void copyToHost_shouldCopyToSpecifiedFile() throws IOException
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            vm.start();
            vm.runCommand("echo -n abc > 1.txt").exitCode(5, TimeUnit.SECONDS);
            File fileOnHost = createTmpFile();
            vm.copyToHost(vmFile("1.txt"), fileOnHost);
            assertEquals("abc", readFile(fileOnHost));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void copyFromHost_shouldSetOwnerToDefaultVMUser() throws IOException
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            File fileOnHost = createTmpFileWithContent("abc");
            VMFile fileInVm = vmFile("1.txt");

            vm.start();
            vm.copyFromHost(fileOnHost, fileInVm);
            assertEquals(0, vm.runCommand("ls -l 1.txt | grep \"test test\"").exitCode(5, TimeUnit.SECONDS));
        }
        finally
        {
            vm.stop();
        }
    }

    @Test
    public void copyFromHost_shouldCopyFileToUserHomeInsideContainer() throws IOException
    {
        DockerVM vm = new DockerVM("frocate");
        try
        {
            File fileOnHost = createTmpFileWithContent("abc");
            VMFile fileInVm = vmFile("1.txt");

            vm.start();
            assertFalse(vm.isFileExists(fileInVm));

            vm.copyFromHost(fileOnHost, fileInVm);
            assertTrue(vm.isFileExists(fileInVm));

            assertEquals("abc", readFile(vm.copyToHost(fileInVm)));
        }
        finally
        {
            vm.stop();
        }
    }

    private void assertExceptionThrownIfContainerName(String name)
    {
        try
        {
            new DockerVM(name);
            fail("Exception was expected");
        }
        catch (IllegalArgumentException e)
        {
            //ok
        }
    }

    private String readFile(File file)
    {
        try
        {
            return Files.readLines(file, Charset.defaultCharset()).stream().collect(Collectors.joining());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private File createTmpFile()
    {
        try
        {
            File tmp = File.createTempFile("tmp", null);
            tmp.deleteOnExit();
            return tmp;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private File createTmpFileWithContent(String content)
    {
        try
        {
            File tmp = File.createTempFile("tmp", null);
            tmp.deleteOnExit();
            Files.write(content.getBytes(), tmp);
            return tmp;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
