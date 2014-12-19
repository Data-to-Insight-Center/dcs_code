package org.dataconservancy.packaging.ingest.shared;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.packaging.model.*;
import org.dataconservancy.packaging.model.Package;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class BagUtilTest {

    @Test
    public void testSanitizeUrlForFile() throws Exception {
        final String toSanitize = "http://localhost:8080/deposit/1234";
        final String expected = "httplocalhost8080deposit1234";

        final File file = BagUtil.sanitizeStringForFile(toSanitize);
        assertEquals(expected, file.getName());
        assertFalse(file.isAbsolute());
        assertFalse(file.isDirectory());
    }

    @Test
    public void testSanitizeUppercaseForFile() throws Exception {
        final String toSanitize = "HTTP://LOCALHOST:8080/DEPOSIT/1234";
        final String expected = "httplocalhost8080deposit1234";

        final File file = BagUtil.sanitizeStringForFile(toSanitize);
        assertEquals(expected, file.getName());
        assertFalse(file.isAbsolute());
        assertFalse(file.isDirectory());
    }

    @Test
    public void testSanitizeNumbersForFile() throws Exception {
        final String toSanitize = "1234";
        final String expected = "1234";

        final File file = BagUtil.sanitizeStringForFile(toSanitize);
        assertEquals(expected, file.getName());
        assertFalse(file.isAbsolute());
        assertFalse(file.isDirectory());
    }

    @Test
    public void testSanitizePunctuationAndSpaces() throws Exception {
        final String toSanitize = "- 12 /34.`";
        final String expected = "1234";

        final File file = BagUtil.sanitizeStringForFile(toSanitize);
        assertEquals(expected, file.getName());
        assertFalse(file.isAbsolute());
        assertFalse(file.isDirectory());
    }

    @Test
    public void testDeriveBasedirWithSimpleFile() throws Exception {
        final String depositId = "1";
        final String bagName = "bag";
        final String bagExt = ".tar";
        final File bagFile = new File(bagName + bagExt);
        final File expectedBaseDir = new File(new File("1"), bagName);

        final File baseDir = BagUtil.deriveBaseDirectory(depositId, bagFile);
        assertEquals(expectedBaseDir, baseDir);
        assertFalse(baseDir.isAbsolute());
    }

    @Test
    public void testDeriveBasedirWithSlash() throws Exception {
        final String depositId = "1";
        final String extraDir = "foo";
        final String bagName = "bag";
        final String bagExt = ".tar";
        final File bagFile = new File(extraDir + File.separator + bagName + bagExt);
        final File expectedBaseDir = new File(new File("1"), bagName);

        final File baseDir = BagUtil.deriveBaseDirectory(depositId, bagFile);
        assertEquals(expectedBaseDir, baseDir);
        assertFalse(baseDir.isAbsolute());
    }

    @Test
    public void testDeriveBasedirWithTarGz() throws Exception {
        final String depositId = "1";
        final String bagName = "bag";
        final String bagExt = ".tar.gz";
        final File bagFile = new File(bagName + bagExt);
        final File expectedBaseDir = new File(new File("1"), bagName);

        final File baseDir = BagUtil.deriveBaseDirectory(depositId, bagFile);
        assertEquals(expectedBaseDir, baseDir);
        assertFalse(baseDir.isAbsolute());
    }

    @Test
    public void testDeriveBasedirWithPeriodsInBagName() throws Exception {
        final String depositId = "1";
        final String bagName = "a.bag.with.periods.in.foo";
        final String bagExt = ".tar.gz";
        final File bagFile = new File(bagName + bagExt);
        final File expectedBaseDir = new File(new File("1"), bagName);

        final File baseDir = BagUtil.deriveBaseDirectory(depositId, bagFile);
        assertEquals(expectedBaseDir, baseDir);
        assertFalse(baseDir.isAbsolute());
    }

    @Test
    public void testStripExtensionWithTarGz() throws Exception {
        final String bagName = "bag";
        final String bagExt = ".tar.gz";
        final File bagFile = new File(bagName + bagExt);
        final File expectedFileNoGz = new File(bagName + ".tar");
        final File expectedFileNoTar = new File(bagName);

        assertEquals(expectedFileNoGz, BagUtil.stripLastExtension(bagFile));
        assertEquals(expectedFileNoTar, BagUtil.stripLastExtension(expectedFileNoGz));
    }

    @Test
    public void testDetectAndStripExtensionsWithTarGz() throws Exception {
        final String bagName = "bag";
        final String bagExt = ".tar.gz";
        final File bagFile = new File(bagName + bagExt);
        final File expectedFile = new File(bagName);

        assertEquals(expectedFile, BagUtil.detectAndStripExtensions(bagFile));
    }

    @Test
    public void testDetectAndStripExtensionsWithTarGzAndPeriodsInName() throws Exception {
        final String bagName = "bag.with.periods.in.name";
        final String bagExt = ".tar.gz";
        final File bagFile = new File(bagName + bagExt);
        final File expectedFile = new File(bagName);

        assertEquals(expectedFile, BagUtil.detectAndStripExtensions(bagFile));
    }

    @Test
    public void testDeleteFilesNullPackage() throws Exception {
        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        assertFalse(BagUtil.deleteExtractedFiles(null));

        assertTrue(toDelete.exists());

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteFilesNullSerialization() throws Exception {
        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        when(thePackage.getSerialization()).thenReturn(null);

        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        assertFalse(BagUtil.deleteExtractedFiles(thePackage));

        assertTrue(toDelete.exists());
        verify(thePackage).getSerialization();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteFilesNullExtractDir() throws Exception {
        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(null);

        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        assertFalse(BagUtil.deleteExtractedFiles(thePackage));

        assertTrue(toDelete.exists());
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteFilesNullBaseDir() throws Exception {
        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(toDelete.getParentFile());
        when(serialization.getBaseDir()).thenReturn(null);


        assertFalse(BagUtil.deleteExtractedFiles(thePackage));

        assertTrue(toDelete.exists());
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();
        verify(serialization, atLeastOnce()).getBaseDir();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteFilesNotAbsoluteExtractDir() throws Exception {
        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(new File("foo"));
        when(serialization.getBaseDir()).thenReturn(null);


        assertFalse(BagUtil.deleteExtractedFiles(thePackage));

        assertTrue(toDelete.exists());
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();
        verify(serialization, atLeastOnce()).getBaseDir();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testNonExistantExtractDir() throws Exception {
        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(toDelete.getParentFile());
        final File bubba = new File("bubba");
        when(serialization.getBaseDir()).thenReturn(bubba);

        assertFalse(new File(toDelete.getParentFile(), bubba.getName()).exists());

        assertFalse(BagUtil.deleteExtractedFiles(thePackage));

        assertTrue(toDelete.exists());
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();
        verify(serialization, atLeastOnce()).getBaseDir();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteFilesSuccess() throws Exception {
        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(toDelete.getParentFile());
        when(serialization.getBaseDir()).thenReturn(new File(toDelete.getName()));

        assertTrue(BagUtil.deleteExtractedFiles(thePackage));

        assertFalse(toDelete.exists());
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();
        verify(serialization, atLeastOnce()).getBaseDir();
    }

    @Test
    public void testDeleteDepositDirNullPackage() throws Exception {
        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        assertFalse(BagUtil.deleteDepositDirectory("foo", null));

        assertTrue(toDelete.exists());

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteDepositDirNullDepositId() throws Exception {
        final File toDelete = createTmpFile();
        assertTrue(toDelete.exists());

        assertFalse(BagUtil.deleteDepositDirectory(null, mock(Package.class)));

        assertTrue(toDelete.exists());

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteDepositDirNullSerialization() throws Exception {
        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        when(thePackage.getSerialization()).thenReturn(null);

        final String depositId = "foo";
        final File toDelete = createTmpFile(depositId);
        assertTrue(toDelete.exists());

        assertFalse(BagUtil.deleteDepositDirectory(depositId, thePackage));

        assertTrue(toDelete.exists());
        verify(thePackage).getSerialization();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteDepositDirNullExtractDir() throws Exception {
        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(null);

        final String depositId = "foo";
        final File toDelete = createTmpFile(depositId);
        assertTrue(toDelete.exists());

        assertFalse(BagUtil.deleteDepositDirectory(depositId, thePackage));

        assertTrue(toDelete.exists());
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteDepositDirNotAbsoluteExtractDir() throws Exception {
        final String depositId = "foo";
        final File toDelete = createTmpFile(depositId);
        assertTrue(toDelete.exists());

        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(new File("bar"));


        assertFalse(BagUtil.deleteDepositDirectory(depositId, thePackage));

        assertTrue(toDelete.exists());
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteDepositDirNonExistentExtractDir() throws Exception {
        final String depositId = "foo";
        final File toDelete = createTmpFile(depositId);
        assertTrue(toDelete.exists());
        FileUtils.forceDelete(toDelete);
        assertFalse(toDelete.exists());

        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(toDelete.getParentFile());

        assertFalse(BagUtil.deleteDepositDirectory(depositId, thePackage));

        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();

        // cleanup
        toDelete.delete();
    }

    @Test
    public void testDeleteDepositDirSuccess() throws Exception {
        final String depositId = "foo";
        final File toDelete = createTmpFile(depositId);
        assertTrue(toDelete.exists());

        final org.dataconservancy.packaging.model.Package thePackage = mock(Package.class);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(toDelete.getParentFile());

        assertTrue(BagUtil.deleteDepositDirectory(depositId, thePackage));

        assertFalse("Expected that '" + toDelete + "' would be deleted.", toDelete.exists());
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization, atLeastOnce()).getExtractDir();
    }


    /**
     * Creates a new directory inside of the Java tmp dir, and creates a single empty file, foo.txt inside of the
     * newly created directory.
     *
     * @return a directory that contains a single file, foo.txt
     * @throws IOException
     */
    private static File createTmpFile() throws IOException {
        File dir = File.createTempFile("BagUtilTest-", ".tmp");
        dir.delete();
        FileUtils.forceMkdir(dir);

        FileUtils.touch(new File(dir, "foo.txt"));
        return dir;
    }

    /**
     * Creates a new directory based on the deposit id inside of the Java tmp dir, and creates a single empty file,
     * foo.txt inside of the newly created directory.
     *
     * @return a directory that contains a single file, foo.txt
     * @throws IOException
     */
    private static File createTmpFile(String depositId) throws IOException {
        File extractDir = File.createTempFile("BagUtilTest-", ".tmp");
        extractDir.delete();
        FileUtils.forceMkdir(extractDir);
        final File depositDir = new File(extractDir, BagUtil.sanitizeStringForFile(depositId).getPath());
        FileUtils.forceMkdir(depositDir);

        FileUtils.touch(new File(depositDir, "foo.txt"));
        return depositDir;
    }
}
