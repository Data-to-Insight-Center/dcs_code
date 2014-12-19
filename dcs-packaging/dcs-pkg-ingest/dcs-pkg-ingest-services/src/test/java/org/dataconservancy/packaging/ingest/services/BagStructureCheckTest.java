package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.BagUtil;
import org.dataconservancy.packaging.model.*;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.ui.util.TarPackageExtractor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class BagStructureCheckTest {

    private final static String BASE_RESOURCE = "/SampleBagStructureTests";

    private final static String CORRECT_RESOURCE = BASE_RESOURCE + "/correct.tar";

    /**
     * Has bagit.txt and the data/ directory at the incorrect level in the directory hierarchy
     */
    private final static String INCORRECT_RESOURCE = BASE_RESOURCE + "/incorrect.tar";

    /**
     * The bag is named 'wrong-name.tar' but ends up being expanded into the directory 'correct'.  The bag should
     * be expanded into the directory 'wrong-name', but it isn't because of the way the bag is constructed.
     */
    private final static String WRONG_NAME_RESOURCE = BASE_RESOURCE + "/wrong-name.tar";

    /**
     * The bag has periods in its name, so when it is exploded, it should explode into a directory that preserves
     * the name with all of the periods.
     */
    private final static String PERIODS_IN_NAME_RESOURCE = BASE_RESOURCE + "/bag.withperiods.inname.tar";

    private static int depositIdCounter = 0;

    private File extractDir;

    private PackageSerialization serialization;

    private org.dataconservancy.packaging.model.Package thePackage;

    private IngestWorkflowState state;

    private TarPackageExtractor extractor;

    private BagStructureCheck underTest;

    @Before
    public void setUp() throws Exception {

        // Create a unique extract directory for each test
        File tmp = File.createTempFile("BagStructureCheckTest", ".dir");
        assertTrue("Unable to delete temporary file " + tmp.getAbsolutePath(), tmp.delete());
        assertTrue("Unable to create temporary directory " + tmp.getAbsolutePath(), tmp.mkdir());
        extractDir = tmp;

        // Create a PackageSerialization configured with the extraction directory
        serialization = mock(PackageSerialization.class);
        when(serialization.getExtractDir()).thenReturn(extractDir);

        // Mock the Package to return our configured PackageSerialization
        thePackage = mock(Package.class);
        when(thePackage.getSerialization()).thenReturn(serialization);

        // Mock an EventManager and a AttributeSetManager
        EventManager em = mock(EventManager.class);
        AttributeSetManager asm = mock(AttributeSetManager.class);
        BusinessObjectManager bom = mock(BusinessObjectManager.class);

        // Create the IngestWorkflowState and configure it with our mocks
        state = mock(IngestWorkflowState.class);
        when(state.getPackage()).thenReturn(thePackage);
        when(state.getAttributeSetManager()).thenReturn(asm);
        when(state.getBusinessObjectManager()).thenReturn(bom);
        when(state.getEventManager()).thenReturn(em);

        // Instantiate our service and configure it with the extract directory.
        underTest = new BagStructureCheck();
        underTest.setExtractDir(extractDir);

        extractor = new TarPackageExtractor();
        extractor.setExtractDirectory(extractDir.getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
//        extractDir.delete();
    }

    private static String nextId() {
        return String.valueOf(depositIdCounter++);
    }

    private File getBag(String resource) {
        URL bag = this.getClass().getResource(resource);
        assertNotNull("Unable to resolve classpath resource " + resource);
        return new File(bag.getPath());
    }

    @Test
    public void testCorrectBag() throws Exception {
        final String depositId = nextId();
        final File bag = getBag(CORRECT_RESOURCE);

        final File baseDir = BagUtil.deriveBaseDirectory(depositId, bag);
        when(serialization.getBaseDir()).thenReturn(baseDir);

        List<File> files = extractor.getFilesFromPackageFile(BagUtil.sanitizeStringForFile(depositId).getPath(), bag);
        when(serialization.getFiles(false)).thenReturn(files);

        underTest.execute(depositId, state);
    }

    @Test
    public void testPeriodsInBagName() throws Exception {
        final String depositId = nextId();
        final File bag = getBag(PERIODS_IN_NAME_RESOURCE);

        final File baseDir = BagUtil.deriveBaseDirectory(depositId, bag);
        when(serialization.getBaseDir()).thenReturn(baseDir);

        List<File> files = extractor.getFilesFromPackageFile(BagUtil.sanitizeStringForFile(depositId).getPath(), bag);
        when(serialization.getFiles(false)).thenReturn(files);

        underTest.execute(depositId, state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void testIncorrectBag() throws Exception {
        final String depositId = nextId();
        final File bag = getBag(INCORRECT_RESOURCE);

        final File baseDir = BagUtil.deriveBaseDirectory(depositId, bag);
        when(serialization.getBaseDir()).thenReturn(baseDir);

        extractor.getFilesFromPackageFile(BagUtil.sanitizeStringForFile(depositId).getPath(), bag);

        underTest.execute(depositId, state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void testIncorrectName() throws Exception {
        final String depositId = nextId();
        final File bag = getBag(WRONG_NAME_RESOURCE);

        final File baseDir = BagUtil.deriveBaseDirectory(depositId, bag);
        when(serialization.getBaseDir()).thenReturn(baseDir);

        extractor.getFilesFromPackageFile(BagUtil.sanitizeStringForFile(depositId).getPath(), bag);

        underTest.execute(depositId, state);
    }
}
