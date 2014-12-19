package org.dataconservancy.packaging.ingest.services;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.shared.BagUtil;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 *
 */
public class CleanupIngestServiceTest {

    @Test
    public void testDeleteExtractedFiles() throws Exception {
        final File depositDir = createTmpFile("foo");
        assertTrue(depositDir.exists());
        final IngestWorkflowState state = MockUtil.mockState();
        final org.dataconservancy.packaging.model.Package thePackage = state.getPackage();
        final PackageSerialization serialization = thePackage.getSerialization();

        when(serialization.getExtractDir()).thenReturn(depositDir.getParentFile());
        when(serialization.getBaseDir()).thenReturn(new File(depositDir.getName()));

        final CleanupIngestService underTest = new CleanupIngestService();

        underTest.execute("foo", state);

        assertFalse(depositDir.exists());
   }

    /**
     * Creates a new directory inside of the tmp dir, and creates a single empty file, foo.txt inside of the
     * newly created directory.
     *
     * @return a directory that contains a single file, foo.txt
     * @throws java.io.IOException
     */
    private static File createTmpFile(String depositId) throws IOException {
        File extractDir = File.createTempFile("CleanupIngestServiceTest", ".extractDir");
        extractDir.delete();
        FileUtils.forceMkdir(extractDir);
        File depositDir = new File(extractDir, BagUtil.sanitizeStringForFile(depositId).getPath());
        FileUtils.forceMkdir(depositDir);

        FileUtils.touch(new File(depositDir, "foo.txt"));
        return depositDir;
    }

}
