package com.frocate.taskrunner.result;

import com.frocate.taskrunner.executable.Executable;
import com.frocate.taskrunner.executable.ExecutableType;
import com.frocate.taskrunner.Task;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ResultImpl implements Result
{
    public static final String ZIP_ENTRY_EXECUTABLE = "executable";
    public static final String ZIP_ENTRY_EXECUTABLE_LOG = "executable.log";
    public static final String ZIP_ENTRY_TEST_LOG = "test.log";
    public static final String ZIP_ENTRY_INFO = "info.json";

    private final ZipFile zipFile;
    private final ResultInfo info;

    public ResultImpl(File file)
    {
        try
        {
            this.zipFile = new ZipFile(file.getAbsoluteFile());
            info = ResultInfo.load(zipFile.getInputStream(new ZipEntry(ZIP_ENTRY_INFO)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public ResultImpl(File file, Executable executable, File executableLog, File testLog, ResultInfo info)
    {
        try
        {
            file = file.getAbsoluteFile();
            //create empty zip file
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {}

            try (FileSystem zipFs = FileSystems.newFileSystem(file.toPath(), null))
            {
                Files.copy(executable.getFile().toPath(), zipFs.getPath(ZIP_ENTRY_EXECUTABLE));
                Files.copy(executableLog.toPath(), zipFs.getPath(ZIP_ENTRY_EXECUTABLE_LOG));
                Files.copy(testLog.toPath(), zipFs.getPath(ZIP_ENTRY_TEST_LOG));
                Files.copy(new ByteArrayInputStream(info.toJson().getBytes()), zipFs.getPath(ZIP_ENTRY_INFO));
            }

            //ZipFile doesn't refresh itself if data modified, so it must be created when file exists
            this.zipFile = new ZipFile(file);
            this.info = info;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getId()
    {
        return info.getId();
    }

    @Override
    public ExecutableType getExecutableType()
    {
        return info.getExecutableType();
    }

    @Override
    public Date getStartTime()
    {
        return info.getStartTime();
    }

    @Override
    public Task getTask()
    {
        //todo task factory? string2task
        return null;
    }

    @Override
    public List<TestResult> getTestResults()
    {
        return info.getTests();
    }

    @Override
    public List<Metric> getMetrics()
    {
        return info.getMetrics();
    }

    @Override
    public long getExecutableSize()
    {
        return zipFile.getEntry(ZIP_ENTRY_EXECUTABLE).getSize();
    }

    @Override
    public InputStream getExecutable()
    {
        try
        {
            return zipFile.getInputStream(new ZipEntry(ZIP_ENTRY_EXECUTABLE));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getExecutableLog()
    {
        try
        {
            return zipFile.getInputStream(new ZipEntry(ZIP_ENTRY_EXECUTABLE_LOG));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getTestLog()
    {
        try
        {
            return zipFile.getInputStream(new ZipEntry(ZIP_ENTRY_TEST_LOG));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public File getFile()
    {
        return new File(zipFile.getName());
    }
}
