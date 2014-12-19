package org.dataconservancy.packaging.ingest.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;

import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class RDFXMLSyntaxCheckerTest {
    private final static String[] VALID_ORE_REM_FILES = new String[] {
            "/SampleOreRemMetadata/ORE-REM/243A4C7A-CA89-4BA5-99D3-1E26D7C31DD8-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/3ae15e08-e282-4435-9c95-1f65a6345ecf-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/61b29cfc-f687-4652-975a-bacd48f0d766-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/ad20517e-029a-4e05-aae7-f316f256fd64-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/d09dcb2d-9ec3-4b3d-a3cb-c133d936c599-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/ae15fc6a-3f92-4e58-9297-d254d81359f7-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/de72493d-d12b-11e2-aee4-e6917b6eedd8-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/de724d8b-d12b-11e2-aee4-a439acceb8fc-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/de7256a4-d12b-11e2-aee4-d574cd660a8f-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/de725faa-d12b-11e2-aee4-87b85d7ccd1c-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/de726233-d12b-11e2-aee4-a0b4b916629f-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/de726e77-d12b-11e2-aee4-ff82d6dab447-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/de72713d-d12b-11e2-aee4-de599a44523d-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/e7b6cb09-2795-418d-967d-1802d7c0882a-ReM.xml",
            "/SampleOreRemMetadata/ORE-REM/de7243bc-d12b-11e2-aee4-90a790b0b17a-ReM.xml" };
    private final static String[] BAD_XML_ORE_REM_FILES = new String[] {
            "/SampleOreRemMetadata/BAD-XML-ORE-REM/BadXMLFile1.xml",
            "/SampleOreRemMetadata/BAD-XML-ORE-REM/BadXMLFile2.xml",
            "/SampleOreRemMetadata/BAD-XML-ORE-REM/BadXMLFile3.xml" };

    private final static String[] BAD_RDF_ORE_REM_FILES = new String[] {
            "/SampleOreRemMetadata/BAD-RDF-SYNTAX/BadRDF-NonRDF-XML.xml",
            "/SampleOreRemMetadata/BAD-RDF-SYNTAX/BadRDF-IllegalNameUsed.xml",
            "/SampleOreRemMetadata/BAD-RDF-SYNTAX/BadRDF-IncompleteSyntax.xml",
            "/SampleOreRemMetadata/BAD-RDF-SYNTAX/BadRDF-EmptyPropertyElement.xml" };

    private final static String ROOT_RESOURCE_MAP_NAME = "8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml";

    private static final String SAMPLE_BAG_NAME = "ELOKA002.bag";

    private AttributeSetManager attributeSetManager;
    private BusinessObjectManager businessObjectManager;
    private EventManager eventManager;
    private IngestWorkflowState state;
    private List<DcsEvent> events;

    RDFXMLSyntaxChecker underTest;

    @Before
    public void setUp() throws IOException {
        attributeSetManager = new AttributeSetManagerImpl();
        eventManager = mock(EventManager.class);
        businessObjectManager = mock(BusinessObjectManager.class);

        events = new ArrayList<DcsEvent>();

        eventManager = mock(EventManager.class);
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {
                DcsEvent event = (DcsEvent) invocation.getArguments()[1];
                events.add(event);
                return event;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));

        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {
                String type = (String) invocation.getArguments()[0];
                DcsEvent event = new DcsEvent();
                event.setEventType(type);
                return event;
            }

        }).when(eventManager).newEvent(anyString());

        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);


        AttributeSetImpl bagit = new AttributeSetImpl(
                AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> atts = bagit.getAttributes();
        atts.add(new AttributeImpl("PKG-ORE-REM", "String",
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        attributeSetManager
                .addAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA, bagit);

        underTest = new RDFXMLSyntaxChecker();
    }

    @Test
    public void testSyntaxCheckGoodPackage() throws StatefulIngestServiceException, IOException {
        // Create a package with all valid files
        org.dataconservancy.packaging.model.Package pkg = createPackage(VALID_ORE_REM_FILES);
        when(state.getPackage()).thenReturn(pkg);
        assertEquals(0, events.size());
        underTest.execute("blah", state);
        assertEquals(0, events.size());
    }

    @Test
    public void testSyntaxCheckBadXMLFiles() throws StatefulIngestServiceException, IOException {
        // Check packages whose xml resource maps are not well formed and should fail

        for (String testfile : BAD_XML_ORE_REM_FILES) {
            org.dataconservancy.packaging.model.Package pkg = createPackage(testfile);
            when(state.getPackage()).thenReturn(pkg);

            boolean failed = false;

            try {
                underTest.execute("blah", state);
            } catch (StatefulIngestServiceException e) {
                //e.printStackTrace();
                failed = true;
            }

            assertTrue("Expected to fail: " + testfile, failed);
        }
    }

    @Test
    public void testSyntaxCheckBadRDFFiles() throws StatefulIngestServiceException, IOException {
        // Check packages whose resource maps violate rdf/xml syntax and should fail

        for (String testfile : BAD_RDF_ORE_REM_FILES) {
            org.dataconservancy.packaging.model.Package pkg = createPackage(testfile);
            when(state.getPackage()).thenReturn(pkg);

            boolean failed = false;

            try {
                underTest.execute("blah", state);
            } catch (StatefulIngestServiceException e) {
                //e.printStackTrace();
                failed = true;
            }

            assertTrue("Expected to fail: " + testfile, failed);
        }
    }

    private org.dataconservancy.packaging.model.Package createPackage(String... paths)
            throws IOException {
        File top = File.createTempFile("testbag", null);
        top.delete();
        top.mkdir();
        top.deleteOnExit();

        File bagdir = new File(top, SAMPLE_BAG_NAME);

        bagdir.mkdir();
        bagdir.deleteOnExit();

        File oreRemDir = new File(bagdir, "ORE-REM");
        oreRemDir.mkdir();
        oreRemDir.deleteOnExit();

        for (String path : paths) {
            URL url = this.getClass().getResource(path);

            String name = new File(path).getName();

            // If only one file, ensure it has the root name
            if (paths.length == 1) {
                name = ROOT_RESOURCE_MAP_NAME;
            }

            File file = new File(oreRemDir, name);

            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(file);
            copy(is, os);
            is.close();
            os.close();

            file.deleteOnExit();
        }

        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getExtractDir()).thenReturn(top);
        when(serialization.getBaseDir()).thenReturn(new File(SAMPLE_BAG_NAME));

        org.dataconservancy.packaging.model.Package pkg = new PackageImpl(null, serialization);

        return pkg;
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[16 * 1024];
        int n = 0;

        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
    }
}
