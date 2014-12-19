package org.dataconservancy.packaging.ingest.services;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.*;
import org.dataconservancy.packaging.model.Package;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class OrphanResourceCheckerTest {

    /**
     * Base directory on the classpath which contains our sample ReMs used for testing.
     */
    private static final String ORE_REM_BASE_RESOURCE = "/OrphanResourceChecker";

    /**
     * A single, standalone, ReM file which contains a single orphan resource.
     * See the contents of the file for additional comments
     */
    private static final String STANDALONE_REM_WITH_ORPHANED_RESOURCE = "test-standalone-rem.xml";

    /**
     * A single ReM file which uses the &lt;ore:isDescribedBy .../> predicate to include additional ReM files.  It
     * contains no orphans (that is, there is a single root to the graph).
     * See the contents of the file for additional comments.
     */
    private static final String REM_WITH_MULTIPLE_RESOURCES_SINGLE_ROOT = "test-multiple-rems.xml";

    /**
     * A single ReM file which uses the &lt;ore:isDescribedBy .../> predicate to include additional ReM files.  It
     * contains two orphans.
     * See the contents of the file for additional comments.
     */
    private static final String REM_WITH_MULTIPLE_RESOURCES_MULTIPLE_ROOTS = "test-multiple-rems-with-orphans.xml";


    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Tests that the {@code OrphanResourceChecker} can detect when there are multiple roots in a single ORE ReM file.
     *
     * @throws Exception
     */
    @Test
    public void testStandaloneRemWithTwoRoots() throws Exception {
        final File remFile = resolveRemResource(STANDALONE_REM_WITH_ORPHANED_RESOURCE);
        final String depositId = "foo";
        final IngestWorkflowState state = MockUtil.mockState();
        final AttributeSetManager asm = state.getAttributeSetManager();

        final Attribute pkgOreRemAttrib = mock(Attribute.class);
        when(pkgOreRemAttrib.getName()).thenReturn(Metadata.PKG_ORE_REM);
        when(pkgOreRemAttrib.getValue()).thenReturn("file:///" + remFile.getName());


        final AttributeSet bagItProfileAs = mock(AttributeSet.class);
        when(bagItProfileAs.getAttributesByName(Metadata.PKG_ORE_REM)).thenReturn(Arrays.asList(pkgOreRemAttrib));

        when(asm.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(bagItProfileAs);

        final Package thePackage = state.getPackage();
        final PackageSerialization ser = thePackage.getSerialization();
        when(ser.getExtractDir()).thenReturn(remFile.getParentFile().getParentFile());
        when(ser.getBaseDir()).thenReturn(new File(ORE_REM_BASE_RESOURCE.substring(1) + "/" + STANDALONE_REM_WITH_ORPHANED_RESOURCE));

        final List<Resource> roots = new ArrayList<Resource>();
        final OrphanResourceChecker underTest = new OrphanResourceChecker(roots);
        try {
            underTest.execute(depositId, state);
            fail("Expected a StatefulIngestServiceException to be thrown, because there are multiple roots defined" +
                    " in the ReM");
        } catch (StatefulIngestServiceException e) {
            // expected
        }

        verify(asm).getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        verify(pkgOreRemAttrib).getValue();

        assertEquals(2, roots.size());
        assertTrue(roots.contains(ResourceFactory.createResource("urn:uuid:1")));
        assertTrue(roots.contains(ResourceFactory.createResource("urn:uuid:4")));
    }

    /**
     * Tests that the {@code OrphanResourceChecker} can detect when there is a single root after parsing multiple
     * ORE ReM files.
     *
     * @throws Exception
     */
    @Test
    public void testMultipleRemsWithASingleRoot() throws Exception {
        final File remFile = resolveRemResource(REM_WITH_MULTIPLE_RESOURCES_SINGLE_ROOT);
        final String depositId = "foo";
        final IngestWorkflowState state = MockUtil.mockState();
        final AttributeSetManager asm = state.getAttributeSetManager();

        final Attribute pkgOreRemAttrib = mock(Attribute.class);
        when(pkgOreRemAttrib.getName()).thenReturn(Metadata.PKG_ORE_REM);
        when(pkgOreRemAttrib.getValue()).thenReturn("file:///" + remFile.getName());


        final AttributeSet bagItProfileAs = mock(AttributeSet.class);
        when(bagItProfileAs.getAttributesByName(Metadata.PKG_ORE_REM)).thenReturn(Arrays.asList(pkgOreRemAttrib));

        when(asm.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(bagItProfileAs);

        final Package thePackage = state.getPackage();
        final PackageSerialization ser = thePackage.getSerialization();
        when(ser.getExtractDir()).thenReturn(remFile.getParentFile().getParentFile());
        when(ser.getBaseDir()).thenReturn(new File(ORE_REM_BASE_RESOURCE.substring(1) + "/" + REM_WITH_MULTIPLE_RESOURCES_SINGLE_ROOT));

        final List<Resource> roots = new ArrayList<Resource>();
        final OrphanResourceChecker underTest = new OrphanResourceChecker(roots);
            underTest.execute(depositId, state);

        verify(asm).getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        verify(pkgOreRemAttrib).getValue();

        assertEquals(1, roots.size());
        assertTrue(roots.contains(ResourceFactory.createResource("urn:uuid:1")));
    }

    /**
     * Tests that the {@code OrphanResourceChecker} can detect when there are multiple roots spread across multiple
     * ORE ReM files.
     *
     * @throws Exception
     */
    @Test
    public void testMultipleRemsWithMultipleRoots() throws Exception {
        final File remFile = resolveRemResource(REM_WITH_MULTIPLE_RESOURCES_MULTIPLE_ROOTS);
        final String depositId = "foo";
        final IngestWorkflowState state = MockUtil.mockState();
        final AttributeSetManager asm = state.getAttributeSetManager();

        final Attribute pkgOreRemAttrib = mock(Attribute.class);
        when(pkgOreRemAttrib.getName()).thenReturn(Metadata.PKG_ORE_REM);
        when(pkgOreRemAttrib.getValue()).thenReturn("file:///" + remFile.getName());


        final AttributeSet bagItProfileAs = mock(AttributeSet.class);
        when(bagItProfileAs.getAttributesByName(Metadata.PKG_ORE_REM)).thenReturn(Arrays.asList(pkgOreRemAttrib));

        when(asm.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(bagItProfileAs);

        final Package thePackage = state.getPackage();
        final PackageSerialization ser = thePackage.getSerialization();
        when(ser.getExtractDir()).thenReturn(remFile.getParentFile().getParentFile());
        when(ser.getBaseDir()).thenReturn(new File(ORE_REM_BASE_RESOURCE.substring(1) + "/" + REM_WITH_MULTIPLE_RESOURCES_MULTIPLE_ROOTS));

        final List<Resource> roots = new ArrayList<Resource>();
        final OrphanResourceChecker underTest = new OrphanResourceChecker(roots);
        try {
            underTest.execute(depositId, state);
            fail("Expected a StatefulIngestServiceException to be thrown, because there are multiple roots to the " +
                    "graph.");
        } catch (StatefulIngestServiceException e) {
            // expected
        }

        verify(asm).getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        verify(pkgOreRemAttrib).getValue();

        assertEquals(3, roots.size());
        assertTrue(roots.contains(ResourceFactory.createResource("urn:uuid:1")));
        assertTrue(roots.contains(ResourceFactory.createResource("urn:uuid:5")));
        assertTrue(roots.contains(ResourceFactory.createResource("urn:uuid:6")));
    }

    /**
     * Composes and returns a File representing the classpath resource identified by {@code rem}, by first resolving
     * the {@link #ORE_REM_BASE_RESOURCE} to a File (a directory) and concatenating the supplied {@code rem}.
     *
     * @param rem
     * @return
     */
    private File resolveRemResource(String rem) {
        URL base = this.getClass().getResource(ORE_REM_BASE_RESOURCE);
        assertNotNull("Could not resolve " + ORE_REM_BASE_RESOURCE + " from the classpath.");
        File remFile = new File(base.getPath(), rem);
        assertTrue("ReM resource " + rem + " was not found under " + base.getPath(), remFile.exists());
        return remFile;
    }
}
