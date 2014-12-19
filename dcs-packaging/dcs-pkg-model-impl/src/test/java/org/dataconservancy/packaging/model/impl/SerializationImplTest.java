package org.dataconservancy.packaging.model.impl;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SerializationImplTest {

    @Test
    public void testRelativize() throws Exception {
        String basePath = "/storage/bags/bag";
        final String relativeDataFile = "data/file1.txt";
        final String extractDirectory = "/storage/bags";
        final String baseDirectory = "bag";
        
        if (System.getProperty("os.name").contains("Windows")) {
            basePath = "C:/temp/storage/bags/bag";
        }

        final File absFile = new File(new File(basePath), relativeDataFile);
        assertTrue(absFile.isAbsolute());
        final File relativeFile = new File(relativeDataFile);
        assertFalse(relativeFile.isAbsolute());

        // Configure the class under test with
        // - an absolute extract directory
        // - a relative base directory
        // - add an absolute file
        SerializationImpl underTest = new SerializationImpl();
        underTest.setExtractDir(new File(extractDirectory));
        underTest.setBaseDir(new File(baseDirectory));
        underTest.addFile(absFile);

        // We expect that the absolute file will be relativized - that is, the the 'basePath' will
        // be stripped off of the absolute file
        assertEquals(relativeFile, underTest.relativize(new File(basePath), absFile));
    }

    @Test
    public void testSetAbsoluteExtractDir() throws Exception {
        String basePath = System.getProperty("java.io.tmpdir");
        final File expected = new File(basePath);
        assertTrue(expected.isAbsolute());

        SerializationImpl underTest = new SerializationImpl();
        underTest.setExtractDir(expected);
        assertEquals(expected, underTest.getExtractDir());
    }

    @Test
    public void testSetRelativeExtractDir() throws Exception {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        final File relativeExtractDir = new File("storage/bags");
        final File expectedExtractDir = new File(tempDir, relativeExtractDir.getPath());

        assertFalse(relativeExtractDir.isAbsolute());
        assertTrue(tempDir.isAbsolute());
        assertTrue(expectedExtractDir.isAbsolute());

        SerializationImpl underTest = new SerializationImpl();
        underTest.setExtractDir(relativeExtractDir);
        assertEquals(expectedExtractDir, underTest.getExtractDir());
    }

    @Test
    public void testRelativeBaseDir() throws Exception {
        final File extractDir = new File(System.getProperty("java.io.tmpdir"));
        final File baseDir = new File("bags");

        assertTrue(extractDir.isAbsolute());
        assertFalse(baseDir.isAbsolute());

        SerializationImpl underTest = new SerializationImpl();
        underTest.setExtractDir(extractDir);
        underTest.setBaseDir(baseDir);

        assertEquals(baseDir, underTest.getBaseDir());
    }

    @Test
    public void testAbsBaseDirStartingWithExtractDir() throws Exception {
        final File extractDir = new File(System.getProperty("java.io.tmpdir"));
        final File relativeBaseDir = new File("bags");
        final File baseDir = new File(extractDir, relativeBaseDir.getPath());

        assertTrue(extractDir.isAbsolute());
        assertTrue(baseDir.isAbsolute());

        SerializationImpl underTest = new SerializationImpl();
        underTest.setExtractDir(extractDir);
        underTest.setBaseDir(baseDir);

        assertEquals(relativeBaseDir, underTest.getBaseDir());
    }

    @Test
    public void testAbsBaseDirNotStartingWithExtractDir() throws Exception {
        final File extractDir = new File(System.getProperty("java.io.tmpdir"));
        final String absBasePath;
        if (System.getProperty("os.name").contains("Windows")) {
            absBasePath = "C:/foo/storage/bags";
        } else {
            absBasePath = "/foo/storage/bags";
        }
        final File absBaseDir = new File(absBasePath);

        assertFalse(extractDir.getAbsolutePath().startsWith(absBasePath));

        assertTrue(extractDir.isAbsolute());
        assertTrue(absBaseDir.isAbsolute());

        SerializationImpl underTest = new SerializationImpl();
        underTest.setExtractDir(extractDir);
        underTest.setBaseDir(absBaseDir);

        assertFalse(underTest.getBaseDir().isAbsolute());
        String expectedPath = "foo" + File.separator + "storage" + File.separator + "bags";
        assertEquals(expectedPath, underTest.getBaseDir().getPath());
    }
}
