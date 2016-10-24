package com.frocate.taskrunner;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.frocate.taskrunner.FileUtils.createTmpFile;
import static com.frocate.taskrunner.FileUtils.createTmpFileWithContent;
import static com.frocate.taskrunner.FileUtils.deleteDirRecursive;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileUtilsTest
{
    @Test
    public void deleteDirRecursive_shouldRemoveFile()
    {
        File file = createTmpFileWithContent("abc");
        assertTrue(file.exists());
        assertTrue(file.isFile());

        deleteDirRecursive(file);

        assertFalse(file.exists());
    }

    @Test
    public void deleteDirRecursive_shouldRemoveEmptyDir()
    {
        File dir = createTmpFile();
        dir.delete();
        dir.mkdirs();
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        deleteDirRecursive(dir);

        assertFalse(dir.exists());
    }

    @Test
    public void deleteDirRecursive_shouldRemoveDirWithFiles() throws IOException
    {
        File dir = createTmpFile();
        dir.delete();
        dir.mkdirs();

        File f1 = new File(dir, "f1.txt");
        Files.write(f1.toPath(), "abc".getBytes());
        assertTrue(f1.exists());
        assertTrue(f1.isFile());

        File f2 = new File(dir, "f2");
        f2.delete();
        f2.mkdirs();
        assertTrue(f2.exists());
        assertTrue(f2.isDirectory());

        deleteDirRecursive(dir);

        assertFalse(dir.exists());
        assertFalse(f1.exists());
        assertFalse(f2.exists());
    }

    @Test
    public void deleteDirRecursive_shouldRemoveInnerNonEmptyDirs() throws IOException
    {
        File dir = createTmpFile();
        dir.delete();
        dir.mkdirs();

        File f1 = new File(dir, "f1");
        f1.delete();
        f1.mkdirs();
        assertTrue(f1.exists());
        assertTrue(f1.isDirectory());

        File f2 = new File(f1, "f2.txt");
        Files.write(f2.toPath(), "abc".getBytes());
        assertTrue(f2.exists());
        assertTrue(f2.isFile());

        deleteDirRecursive(dir);

        assertFalse(dir.exists());
        assertFalse(f1.exists());
        assertFalse(f2.exists());
    }
}
