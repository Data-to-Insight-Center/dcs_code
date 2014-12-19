/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.*;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.AttributeValueType;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PayloadFileFormatVerifierTest {

    private PayloadFileFormatVerifier underTest;
    private PackageSerialization serialization;
    private AttributeSetManager attributeManager;
    private BusinessObjectManager businessObjectManager;

    private EventManager eventManager;
    private List<DcsEvent> events;

    private File file1Tmp;
    private File file2Tmp;
    private File file3Tmp;
    private String fileOneURI;
    private String fileTwoURI;
    private String fileThreeURI;
    private File nonPayloadFile;
    private String file1FormatURI = "info:pronom/fmt/19";
    private String file1FormatString = "DcsFormat{format ='fmt/19', name='Adobe PDF', schema uri='http://www.nationalarchives.gov.uk/PRONOM/', version='null'}";
    private String file1FormatString2 = "DcsFormat{format ='text/xml', name='XML file', schema uri='http://www.iana.org/assignments/media-types/', version='null'}";

    private String file2FormatURI = "info:pronom/fmt/202";
    private String file2FormatString = "DcsFormat{format ='fmt/202', name='text/xml', schema uri='http://www.nationalarchives.gov.uk/PRONOM/', version='null'}";
    private String file2FormatString2 = "DcsFormat{format ='text/xml', name='text/xml', schema uri='http://www.iana.org/assignments/media-types/', version='null'}";
    private String nonPayloadFileFormatURI = "info:pronom/fmt/202";
    private String nonPayloadFileFormatString = "DcsFormat{format ='fmt/202', name='text/xml', schema uri='http://www.nationalarchives.gov.uk/PRONOM/', version='null'}";
    private String file3FormatURI = "info:pronom/fmt/333";
    private String file3FormatString = "DcsFormat{format ='fmt/333', name='text/xml', schema uri='http://www.nationalarchives.gov.uk/PRONOM/', version='null'}";
    private org.dataconservancy.packaging.model.Package pkg;

    private String depositDir;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        underTest = new PayloadFileFormatVerifier();

        String packageBaseDir = setUpFiles();

        setUpPackage(packageBaseDir);

        businessObjectManager = mock(BusinessObjectManager.class);
        attributeManager = mock(AttributeSetManager.class);

        AttributeSet file1AS = new AttributeSetImpl(AttributeSetName.FILE);
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file1Tmp.getPath()));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString2));

        AttributeSet file2AS = new AttributeSetImpl(AttributeSetName.FILE);
        file2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file2Tmp.getPath()));
        file2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file2FormatString));
        file2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file2FormatString2));

        AttributeSet file1OREReMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        Attribute file1PathAttribute = new AttributeImpl(Metadata.FILE_PATH, "String", fileOneURI);
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, "String", "text/xml"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file1Tmp.getName()));
        file1OREReMAS.getAttributes().add(file1PathAttribute);
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", file1FormatURI));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CREATOR_NAME, "String", "Willard Sirk"));
        Set<AttributeSet> file1ResultSet = new HashSet<AttributeSet>();
        file1ResultSet.add(file1OREReMAS);

        AttributeSet file2OREReMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        Attribute file2PathAttribute = new AttributeImpl(Metadata.FILE_PATH, "String", fileTwoURI);
        file2OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, "String", "text/xml"));
        file2OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file2Tmp.getName()));
        file2OREReMAS.getAttributes().add(file2PathAttribute);
        file2OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", file2FormatURI));
        file2OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CREATOR_NAME, "String", "Willard Sirk"));
        Set<AttributeSet> file2ResultSet = new HashSet<AttributeSet>();
        file2ResultSet.add(file2OREReMAS);

        when(attributeManager.matches(AttributeSetName.ORE_REM_FILE, file1PathAttribute)).thenReturn(file1ResultSet);
        when(attributeManager.matches(AttributeSetName.ORE_REM_FILE, file2PathAttribute)).thenReturn(file2ResultSet);

        when(attributeManager.getAttributeSet(file1Tmp.getPath())).thenReturn(file1AS);
        when(attributeManager.getAttributeSet(file2Tmp.getPath())).thenReturn(file2AS);

        setUpEventManager();
    }
    private String setUpFiles() throws IOException, URISyntaxException {
        depositDir = new File(System.getProperty("java.io.tmpdir"), "999999").getPath();
        String packageBaseDir =  new File(depositDir, "PayloadFileFormatVerifierBag").getPath();
        String payloadDirPath =  new File(packageBaseDir, "data").getPath();
        boolean success  = new File(payloadDirPath).mkdirs();
        File payloadDir = new File(payloadDirPath);

        file1Tmp = java.io.File.createTempFile("testFileOne", ".txt", payloadDir);
        file1Tmp.deleteOnExit();

        PrintWriter fileOneOut = new PrintWriter(file1Tmp);

        fileOneOut.println("This is test file one");
        fileOneOut.close();

        fileOneURI = "file://" + file1Tmp.getPath().substring(depositDir.length()).replace(" ", "%20");


        file2Tmp = java.io.File.createTempFile("testFileTwo", ".txt", payloadDir);
        file2Tmp.deleteOnExit();

        PrintWriter fileTwoOut = new PrintWriter(file2Tmp);

        fileTwoOut.println("This is test file two");
        fileTwoOut.close();

        fileTwoURI = "file://" + file2Tmp.getPath().substring(depositDir.length()).replace(" ", "%20");

        file3Tmp = java.io.File.createTempFile("testFileThree", ".txt", payloadDir);
        file3Tmp.deleteOnExit();

        PrintWriter fileThreeOut = new PrintWriter(file3Tmp);

        fileThreeOut.println("This is test file three");
        fileThreeOut.close();

        fileThreeURI = "file://" + file3Tmp.getPath().substring(depositDir.length()).replace(" ", "%20");

        nonPayloadFile = java.io.File.createTempFile("testFileFour", ".txt");
        nonPayloadFile.deleteOnExit();

        PrintWriter fileFourOut = new PrintWriter(nonPayloadFile);

        fileFourOut.println("This is test file four");
        fileFourOut.close();
        return packageBaseDir;
    }

    private void setUpPackage(String packageBaseDir) {
        List<File> files = new ArrayList<File>();
        files.add(file1Tmp);
        files.add(file2Tmp);

        serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(files);
        when(serialization.getBaseDir()).thenReturn(new File (packageBaseDir));

        pkg = new PackageImpl(null, serialization);

    }

    private void setUpEventManager() {

        eventManager = mock(EventManager.class);

        events = new ArrayList<DcsEvent>();

        eventManager = mock(EventManager.class);
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation)
                    throws Throwable {
                DcsEvent event = (DcsEvent) invocation.getArguments()[1];
                events.add(event);
                return event;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));

        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation)
                    throws Throwable {
                String type = (String) invocation.getArguments()[0];

                DcsEvent event = new DcsEvent();
                event.setDate((new DateTime()).toString());
                event.setEventType(type);
                return event;
            }

        }).when(eventManager).newEvent(anyString());
    }

    /**
     * Tests that when all of the formats are successfully verified, one "FORMAT_VERIFIED" event is emitted.
     * In this test case, one file has MORE THAN ONE detected format.
     *
     * Expected : One successful event is emitted per file examined
     *
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testAllSuccessfulFormatVerifications() throws StatefulIngestServiceException {
        //setup the workflow state
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        assertEquals(0, events.size());
        underTest.execute("depositID", state);

        assertEquals(2, events.size());
        for (DcsEvent event : events) {
            assertEquals(Package.Events.FORMAT_VERIFIED, event.getEventType());
            assertNotNull(event);

            assertEquals(1, event.getTargets().size());
            assertTrue(event.getTargets().iterator().hasNext());
            assertTrue(event.getTargets().iterator().next().getRef().equals(file1Tmp.getPath().substring(depositDir.length())) ||
                    event.getTargets().iterator().next().getRef().equals(file2Tmp.getPath().substring(depositDir.length())));
        }

    }

    /**
     * Tests that when all of the formats are successfully verified, one "FORMAT_VERIFIED" event is emitted.
     *
     * Expected: One successful event is permitted per file examined
     *
     * In this test case, one file has MORE THAN ONE detected format.
     * @throws StatefulIngestServiceException
     * @throws MalformedURLException 
     * @throws URISyntaxException 
     */
    @Test
    public void testVerifyFormatsForWithMultipleDetectedFormats() throws StatefulIngestServiceException {
        AttributeSet file1AS = new AttributeSetImpl(AttributeSetName.FILE);
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file1Tmp.getPath()));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString2));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString.replace("19", "191")));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString.replace("19", "192")));

        when(attributeManager.getAttributeSet(file1Tmp.getPath())).thenReturn(file1AS);
        String fileOneURI = "file://" + file1Tmp.getPath().substring(depositDir.length()).replace(" ", "%20");

        when(attributeManager.getAttributeSet(fileOneURI)).thenReturn(file1AS);
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        assertEquals(0, events.size());
        underTest.execute("depositID", state);

        assertEquals(2, events.size());
        for (DcsEvent event : events) {
            assertEquals(Package.Events.FORMAT_VERIFIED, event.getEventType());
            assertNotNull(event);

            assertEquals(1, event.getTargets().size());
            assertTrue(event.getTargets().iterator().hasNext());
            assertTrue(event.getTargets().iterator().next().getRef().equals(file1Tmp.getPath().substring(depositDir.length())) ||
                    event.getTargets().iterator().next().getRef().equals(file2Tmp.getPath().substring(depositDir.length())));
        }
    }
    /**
     * Tests that when formats are successfully verified, one "FORMAT_VERIFIED" event is emitted per file
     * examined. One "FORMAT_VERIFICATION_FAIL" event is emitted perfile for the formats who fail verification (if any).
     *
     * Event target includes reference to the examined file whose formats were verified.
     *
     * In this test case, one file has MORE THAN ONE asserted format.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testVerifyFormatsForWithMultipleAssertedFormats() throws StatefulIngestServiceException {
        AttributeSet file1OREReMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        Attribute file1PathAttribute = new AttributeImpl(Metadata.FILE_PATH, "String", fileOneURI);
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, "String", "text/xml"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file1Tmp.getName()));
        file1OREReMAS.getAttributes().add(file1PathAttribute);
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", "fmt/666"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", "fmt/2342"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", file1FormatURI));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", "fmt/555"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CREATOR_NAME, "String", "Willard Sirk"));
        Set<AttributeSet> file1ResultSet = new HashSet<AttributeSet>();
        file1ResultSet.add(file1OREReMAS);

        when(attributeManager.matches(AttributeSetName.ORE_REM_FILE, file1PathAttribute)).thenReturn(file1ResultSet);

        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        assertEquals(0, events.size());
        underTest.execute("depositID", state);
        System.out.println(events);
        int successfulEventCount = 0;
        int failureEventCount = 0;
        assertEquals(3, events.size());
        for (DcsEvent event : events) {
            assertEquals(1, event.getTargets().size());
            System.out.println(event);
            if (event.getEventType().equals(Package.Events.FORMAT_VERIFIED)) {
                successfulEventCount++;
                for (DcsEntityReference eventTarget : event.getTargets()) {
                    assertTrue(eventTarget.getRef().equals(file1Tmp.getPath().substring(depositDir.length())) ||
                            eventTarget.getRef().equals(file2Tmp.getPath().substring(depositDir.length())));
                }
            } else {
                assertEquals(Package.Events.FORMAT_VERIFICATION_FAILED, event.getEventType());
                failureEventCount++;
                for (DcsEntityReference eventTarget : event.getTargets()) {
                    assertTrue(eventTarget.getRef().equals(file1Tmp.getPath().substring(depositDir.length())));
                }
            }

        }
        assertEquals(2, successfulEventCount);
        assertEquals(1, failureEventCount);

    }

    /**
     * Test that when a file does not have any verifiable format, no "FORMAT_VERIFIED" event will be emitted for that
     * file
     *
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testVerifyFormatOneFileNoSuccess() throws StatefulIngestServiceException {
        AttributeSet file1AS = new AttributeSetImpl(AttributeSetName.FILE);
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file1Tmp.getPath()));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString.replace("19", "888")));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString.replace("19", "999")));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString.replace("19", "232")));

        when(attributeManager.getAttributeSet(file1Tmp.getPath())).thenReturn(file1AS);
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        assertEquals(0, events.size());
        underTest.execute("depositID", state);

        assertEquals(2, events.size());
        for (DcsEvent event : events) {

            if (event.getEventType().equals(Package.Events.FORMAT_VERIFIED)) {
                assertNotNull(event);
                assertEquals(1, event.getTargets().size());
                assertTrue(event.getTargets().iterator().hasNext());
                assertTrue(event.getTargets().iterator().next().getRef().equals(file2Tmp.getPath().substring(depositDir.length())));

            } else {
                assertEquals(Package.Events.FORMAT_VERIFICATION_FAILED, event.getEventType());
                assertEquals(1, event.getTargets().size());
                assertTrue(event.getTargets().iterator().hasNext());
                assertTrue(event.getTargets().iterator().next().getRef().equals(file1Tmp.getPath().substring(depositDir.length())));
                System.out.println((new DateTime()).toString());
            }
        }
    }

    /**
     * Test that an exception is thrown when a payload file is found without a corresponding File attribute set
     * @throws StatefulIngestServiceException
     */
    @Test (expected = StatefulIngestServiceException.class)
    public void testVerifyFormatMissingFileAttributeSet() throws StatefulIngestServiceException {

        //add an addtional file to package
        List<File> files = new ArrayList<File>();
        files.add(file1Tmp);
        files.add(file2Tmp);
        files.add(file3Tmp);

        when(serialization.getFiles()).thenReturn(files);

        pkg = new PackageImpl(null, serialization);

        //add new ore-rem-file attribute set for additional file to attribute set manager.
        AttributeSet file1OREReMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeImpl file1PathAttribute = new AttributeImpl(Metadata.FILE_PATH, "String", fileOneURI);
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, "String", "text/xml"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file1Tmp.getName()));
        file1OREReMAS.getAttributes().add(file1PathAttribute);
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", "fmt/666"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", "fmt/2342"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", file1FormatURI));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, "String", "fmt/555"));
        file1OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CREATOR_NAME, "String", "Willard Sirk"));
        Set<AttributeSet> file1ResultSet = new HashSet<AttributeSet>();
        file1ResultSet.add(file1OREReMAS);

        when(attributeManager.matches(AttributeSetName.ORE_REM_FILE, file1PathAttribute)).thenReturn(file1ResultSet);

        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        assertEquals(0, events.size());
        underTest.execute("depositID", state);
    }

    /**
     * Test that when a file does not have asserted format, its format verification does not happen and file name does
     * not get reported in either failed or successful event.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testVerifyFormatMissingAssertedFormat() throws StatefulIngestServiceException {

        //add an addtional file to package
        List<File> files = new ArrayList<File>();
        files.add(file1Tmp);
        files.add(file2Tmp);
        files.add(file3Tmp);

        when(serialization.getFiles()).thenReturn(files);
        pkg = new PackageImpl(null, serialization);

        AttributeSet file3AS = new AttributeSetImpl(AttributeSetName.FILE);
        file3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file3Tmp.getPath()));
        file3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString));
        when(attributeManager.getAttributeSet(file3Tmp.getPath())).thenReturn(file3AS);

        //add new ore-rem-file attribute set for additional file to attribute set manager
        // - leaving out conforms-to and file-format attribute.
        AttributeSet file3OREReMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeImpl file3PathAttribute = new AttributeImpl(Metadata.FILE_PATH, "String", fileThreeURI);
        file3OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", file3Tmp.getName()));
        file3OREReMAS.getAttributes().add(file3PathAttribute);
        file3OREReMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_CREATOR_NAME, "String", "Willard Sirk"));
        Set<AttributeSet> file3ResultSet = new HashSet<AttributeSet>();
        file3ResultSet.add(file3OREReMAS);
        when(attributeManager.matches(AttributeSetName.ORE_REM_FILE, file3PathAttribute)).thenReturn(file3ResultSet);

        //make sure file 1 will fail format verificaton
        AttributeSet file1AS = new AttributeSetImpl(AttributeSetName.FILE);
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", fileOneURI));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString.replace("19", "888")));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString.replace("19", "999")));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, file1FormatString.replace("19", "232")));


        when(attributeManager.getAttributeSet(file1Tmp.getPath())).thenReturn(file1AS);

        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        assertEquals(0, events.size());
        underTest.execute("depositID", state);

        System.out.println(events);
        assertEquals(2, events.size());
        for (DcsEvent event : events) {

            if (event.getEventType().equals(Package.Events.FORMAT_VERIFIED)) {
                assertNotNull(event);
                assertEquals(1, event.getTargets().size());
                assertTrue(event.getTargets().iterator().hasNext());
                assertTrue(event.getTargets().iterator().next().getRef().equals(file2Tmp.getPath().substring(depositDir.length())));

            } else {
                assertEquals(Package.Events.FORMAT_VERIFICATION_FAILED, event.getEventType());
                assertEquals(1, event.getTargets().size());
                assertTrue(event.getTargets().iterator().hasNext());
                assertTrue(event.getTargets().iterator().next().getRef().equals(file1Tmp.getPath().substring(depositDir.length())));
            }
        }


    }

    /**
     * Test that service skips over non-payload file.
     * Expects: 2 successful events emitted for two payload files
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testVerifyFormatWithNonPayloadFile() throws StatefulIngestServiceException {
        //add an addtional file to package
        List<File> files = new ArrayList<File>();
        files.add(file1Tmp);
        files.add(file2Tmp);
        files.add(nonPayloadFile);

        when(serialization.getFiles()).thenReturn(files);

        pkg = new PackageImpl(null, serialization);

        //mock attribute manager to return file attribute set for payload file
        AttributeSet nonPayloadFileAS = new AttributeSetImpl(AttributeSetName.FILE);
        nonPayloadFileAS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", nonPayloadFile.getPath()));
        nonPayloadFileAS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, nonPayloadFileFormatString));

        when(attributeManager.getAttributeSet(nonPayloadFile.getPath())).thenReturn(nonPayloadFileAS);

        //setup the workflow state
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        assertEquals(0, events.size());
        underTest.execute("depositID", state);

        System.out.println(events);
        assertEquals(2, events.size());
        for (DcsEvent event : events) {
            assertEquals(Package.Events.FORMAT_VERIFIED, event.getEventType());
            assertNotNull(event);
            assertEquals(1, event.getTargets().size());
            assertTrue(event.getTargets().iterator().hasNext());
            assertTrue(event.getTargets().iterator().next().getRef().equals(file1Tmp.getPath().substring(depositDir.length())) ||
                    event.getTargets().iterator().next().getRef().equals(file2Tmp.getPath().substring(depositDir.length())));
        }
    }

}
