/*
 * Copyright 2013 Johns Hopkins University
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
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.*;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.Pair;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for the PackageFileChecksumVerifier ingest service
 */
public class PackageFileChecksumVerifierTest {

    private PackageFileChecksumVerifier checksumVerifier;
    private org.dataconservancy.packaging.model.Package pkg;
    private AttributeSetManager attributeManager;
    private BusinessObjectManager businessObjectManager;
    private Map<String, AttributeSet> attributeMap;
    private PackageSerialization serialization;

    private EventManager eventManager;
    private Set<DcsEvent> eventSet;

    private File fileOneTmp;
    private File fileTwoTmp;

    @Before
    public void setup() throws Exception {
        checksumVerifier = new PackageFileChecksumVerifier();

        fileOneTmp = java.io.File.createTempFile("testFileOne", ".txt");
        fileOneTmp.deleteOnExit();

        PrintWriter fileOneOut = new PrintWriter(fileOneTmp);

        fileOneOut.println("This is test file one");
        fileOneOut.close();

        fileTwoTmp = java.io.File.createTempFile("testFileTwo", ".txt");
        fileTwoTmp.deleteOnExit();

        PrintWriter fileTwoOut = new PrintWriter(fileTwoTmp);

        fileTwoOut.println("This is test file two");
        fileTwoOut.close();

        List<File> files = new ArrayList<File>();
        files.add(fileOneTmp);
        files.add(fileTwoTmp);

        serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(files);
        when(serialization.getExtractDir()).thenReturn(new File(System.getProperty("java.io.tmpdir")));
        
        pkg = new PackageImpl(null, serialization);

        businessObjectManager = mock(BusinessObjectManager.class);
        attributeManager = mock(AttributeSetManager.class);

        attributeMap = new HashMap<String, AttributeSet>();

        doAnswer(new Answer<AttributeSet>() {
            @Override
            public AttributeSet answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the key and the attribute set to be added", args);
                assertEquals("Expected two arguments: the key and the attribute set to be added",
                        2, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument to be of type AttributeSet",
                        args[1] instanceof AttributeSet);
                String key = (String) args[0];
                AttributeSet attrSet = (AttributeSet) args[1];
                attributeMap.put(key, attrSet);
                return null;
            }
        }).when(attributeManager).addAttributeSet(anyString(), any(AttributeSet.class));

        doAnswer(new Answer<AttributeSet>() {
            @Override
            public AttributeSet answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the key and the attribute set to be added", args);
                assertEquals("Expected two arguments: the key and the attribute set to be added",
                        2, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument to be of type AttributeSet",
                        args[1] instanceof AttributeSet);
                String key = (String) args[0];
                AttributeSet attrSet = (AttributeSet) args[1];
                attributeMap.put(key, attrSet);
                return null;
            }
        }).when(attributeManager).updateAttributeSet(anyString(), any(AttributeSet.class));

        doAnswer(new Answer<AttributeSet>() {
            @Override
            public AttributeSet answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the key of the attribute set to be retrieved", args);
                assertEquals("Expected one argument: the key of the attribute set to be retrieved",
                        1, args.length);
                assertTrue("Expected argument to be of type string",
                        args[0] instanceof String);
                String key = (String) args[0];

                return attributeMap.get(key);
            }
        }).when(attributeManager).getAttributeSet(anyString());

        eventSet = new HashSet<DcsEvent>();
        eventManager = mock(EventManager.class);
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {

                // Extract the event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the key and the event to be added", args);
                assertEquals("Expected two arguments: the key and the event to be added",
                        2, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument two to be of type DcsEvent",
                        args[1] instanceof DcsEvent);
                String key = (String) args[0];
                DcsEvent event = (DcsEvent) args[1];
                eventSet.add(event);
                return null;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));

        doAnswer(new Answer<DcsEvent>() {

            @Override
            public DcsEvent answer(InvocationOnMock invocation)
                    throws Throwable {
                // Extract the Event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the type of the event to be generated", args);
                assertEquals("Expected one argument: the type of the event to be retrieved",
                        1, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                String type = (String) args[0];

                DcsEvent dcsEvent = new DcsEvent();
                dcsEvent.setEventType(type);
                dcsEvent.setDate(DateTime.now().toString());
                dcsEvent.setId("foo");
                return dcsEvent;
            }

        }).when(eventManager).newEvent(anyString());

        doAnswer(new Answer<Collection<DcsEvent>>() {
            @Override
            public Collection<DcsEvent> answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the id and the type of the event to be retrieved", args);
                assertEquals("Expected two arguments: the id and the type of the event to be retrieved",
                        2, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument two to be of type string",
                        args[1] instanceof String);
                String key = (String) args[0];
                String type = (String) args[1];

                List<DcsEvent> events = new ArrayList<DcsEvent>();
                for (DcsEvent event : eventSet) {
                    if (event.getEventType().equalsIgnoreCase(type)) {
                        events.add(event);
                    }
                }
                return events;
            }
        }).when(eventManager).getEvents(anyString(), anyString());
    }


    /**
     * Test that a null package generates an IllegalStateException
     */
    @Test(expected = IllegalStateException.class)
    public void testNullPackageFiresFailureEvent() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getAttributeSetManager()).thenReturn(attributeManager);

        when(state.getPackage()).thenReturn(null);

        checksumVerifier.execute("ingest:1", state);
    }

    /**
     * Tests that if the state arrives with a null event manager the service throws an IllegalStateException
     * @throws StatefulIngestServiceException
     */
    @Test(expected = IllegalStateException.class)
    public void testNullEventManagerThrowsException() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(state.getEventManager()).thenReturn(null);
        when(state.getAttributeSetManager()).thenReturn(attributeManager);

        when(state.getPackage()).thenReturn(pkg);

        checksumVerifier.execute("ingest:1", state);
    }

    /**
     * Tests that if the service recieves a state with a null attribute set manager an IllegalStateException is thrown
     * @throws StatefulIngestServiceException
     */
    @Test(expected = IllegalStateException.class)
    public void testNullAttributeSetManagerFiresFailureEvent() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getAttributeSetManager()).thenReturn(null);

        when(state.getPackage()).thenReturn(pkg);

        checksumVerifier.execute("ingest:1", state);
    }

    /**
     * Tests that if the service is passed a null workflow state an IllegalArgumentException is thrown
     * @throws StatefulIngestServiceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullStateThrowsAnException() throws StatefulIngestServiceException {

        checksumVerifier.execute("ingest:1", null);

    }

    /**
     * Tests that if the BagIt-Manifest AttributeSet is missing, an exception would be thrown.
     * @throws StatefulIngestServiceException
     */
    @Test(expected = StatefulIngestServiceException.class)
    public void testMissingManifestAttributeSetThrowsException() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);

        checksumVerifier.execute("ingest:1", state);

        assertEquals(0, attributeMap.size());

        assertEquals(0, eventSet.size());
    }

    /**
     * Tests that a successful checking of checksum does not generate an ingest fail event or
     * a StatefulIngestServiceException
     */
    @Test
    public void testOneVerificationPasses() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(serialization.getBaseDir()).thenReturn(new File("ingest1/bag"));

        Checksum checksum1 = new ChecksumImpl("md5", "101010101010");
        Pair<String, Checksum> fileOnePair = new Pair("testFileOne.txt", checksum1);

        AttributeSet fileOneAttributeSet = new AttributeSetImpl("testFileOne.txt");
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", "testFileOne.txt"));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        File absBaseDir = new File(serialization.getExtractDir(), serialization.getBaseDir().getPath());
        attributeManager.addAttributeSet(new File(absBaseDir, "testFileOne.txt").getAbsolutePath(), fileOneAttributeSet);

        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        attributeManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);

        checksumVerifier.execute("ingest:1", state);

        assertEquals(0, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(2, attributeMap.size());
    }

    /**
     * Tests that a failed checking of checksum does generate an ingest fail event
     */
    @Test
    public void testOneBadVerificationFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(serialization.getBaseDir()).thenReturn(new File("ingest1/bag"));

        Checksum checksum1 = new ChecksumImpl("md5", "101010101010");
        Pair<String, Checksum> fileOnePair = new Pair("testFileOne.txt", checksum1);
        Checksum checksum2 = new ChecksumImpl("md5", "001101010010");
        Pair<String, Checksum> fileOnePair2 = new Pair("testFileOne.txt", checksum2);


        AttributeSet fileOneAttributeSet = new AttributeSetImpl("testFileOne.txt");
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", "testFileOne.txt"));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        File absBaseDir = new File(serialization.getExtractDir(), serialization.getBaseDir().getPath());
        attributeManager.addAttributeSet(new File(absBaseDir, "testFileOne.txt").getAbsolutePath(), fileOneAttributeSet);

        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair2.toString()));
        attributeManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);

        checksumVerifier.execute("ingest:1", state);

        assertEquals(1, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(2, attributeMap.size());
    }

    /**
     * Tests that a successful checking of one checksum and a failure for another generates an ingest fail event
     */
    @Test
    public void testConflictingVerificationsFail() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(serialization.getBaseDir()).thenReturn(new File("ingest1/bag"));

        Checksum checksum1 = new ChecksumImpl("md5", "101010101010");
        Checksum checksum2 = new ChecksumImpl("md5", "001101010010");
        Pair<String, Checksum> fileOnePair = new Pair("testFileOne.txt", checksum1);
        Pair<String, Checksum> fileOnePair2 = new Pair("testFileOne.txt", checksum2);

        AttributeSet fileOneAttributeSet = new AttributeSetImpl("testFileOne.txt");
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", "testFileOne.txt"));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePair2.toString()));
        File absBaseDir = new File(serialization.getExtractDir(), serialization.getBaseDir().getPath());
        attributeManager.addAttributeSet(new File(absBaseDir, "testFileOne.txt").getAbsolutePath(), fileOneAttributeSet);

        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        attributeManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);

        checksumVerifier.execute("ingest:1", state);

        assertEquals(1, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(2, attributeMap.size());
    }

    /**
     * Tests that a successful checking of two checksums does not generate an ingest fail event or
     * a StatefulIngestServiceException
     */
    @Test
    public void testTwoVerificationPass() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(serialization.getBaseDir()).thenReturn(new File("ingest1/bag"));

        Checksum checksum1 = new ChecksumImpl("md5", "101010101010");
        Checksum checksum2 = new ChecksumImpl("sha1", "001101010010");
        Pair<String, Checksum> fileOnePair = new Pair("testFileOne.txt", checksum1);
        Pair<String, Checksum> fileOnePair2 = new Pair("testFileOne.txt", checksum2);

        AttributeSet fileOneAttributeSet = new AttributeSetImpl("testFileOne.txt");

        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", "testFileOne.txt"));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePair2.toString()));
        File absBaseDir = new File(serialization.getExtractDir(), serialization.getBaseDir().getPath());
        attributeManager.addAttributeSet(new File(absBaseDir, "testFileOne.txt").getAbsolutePath(), fileOneAttributeSet);

        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair2.toString()));
        attributeManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);

        checksumVerifier.execute("ingest:1", state);

        assertEquals(0, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(2, attributeMap.size());
    }

    /**
     * Tests that testing multiple files with two 'correct' checksums passes without an ingest fail event or
     * an exception
     */
    @Test
    public void testTwoFilesWithMultipleVerificationPass() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(serialization.getBaseDir()).thenReturn(new File("ingest1/bag"));

        Checksum checksum1 = new ChecksumImpl("md5", "101010101010");
        Checksum checksum2 = new ChecksumImpl("sha1", "001101010010");

        Checksum checksum3 = new ChecksumImpl("md5", "102010101010");
        Checksum checksum4 = new ChecksumImpl("sha1", "002101010010");

        Pair<String, Checksum> fileOnePair = new Pair("testFileOne.txt", checksum1);
        Pair<String, Checksum> fileOnePair2 = new Pair("testFileOne.txt", checksum2);
        Pair<String, Checksum> fileTwoPair = new Pair("testFileTwo.txt", checksum3);
        Pair<String, Checksum> fileTwoPair2 = new Pair("testFileTwo.txt", checksum4);

        AttributeSet fileOneAttributeSet = new AttributeSetImpl("testFileOne.txt");
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", "testFileOne.txt"));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePair2.toString()));
        File absBaseDir = new File(serialization.getExtractDir(), serialization.getBaseDir().getPath());
        attributeManager.addAttributeSet(new File(absBaseDir, "testFileOne.txt").getAbsolutePath(), fileOneAttributeSet);


        AttributeSet fileTwoAttributeSet = new AttributeSetImpl("testFileTwo.txt");
        fileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", "testFileTwo.txt"));
        fileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileTwoPair.toString()));
        fileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileTwoPair2.toString()));
        attributeManager.addAttributeSet(new File(absBaseDir, "testFileTwo.txt").getAbsolutePath(), fileTwoAttributeSet);

        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair2.toString()));

        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileTwoPair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileTwoPair2.toString()));
        attributeManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);

        checksumVerifier.execute("ingest:1", state);

        assertEquals(0, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(3, attributeMap.size());
    }

    /**
     * Tests that checking a provided checksums against a missing calculated checksum creates an ingest fail event
     */
    @Test
    public void testMissingCalculatedValueCreatesIngestFailEvent() throws StatefulIngestServiceException{
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(serialization.getBaseDir()).thenReturn(new File("ingest1/bag"));

        AttributeSet fileOneAttributeSet = new AttributeSetImpl("testFileOne.txt");
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, "String", "testFileOne.txt"));
        File absBaseDir = new File(serialization.getExtractDir(), serialization.getBaseDir().getPath());
        attributeManager.addAttributeSet(new File(absBaseDir, "testFileOne.txt").getAbsolutePath(), fileOneAttributeSet);
        
        Checksum checksum1 = new ChecksumImpl("md5", "101010101010");
        Pair<String, Checksum> fileOnePair = new Pair("testFileOne.txt", checksum1);

        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        attributeManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);

        checksumVerifier.execute("ingest:1", state);

        assertEquals(1, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(2, attributeMap.size());
    }


    /**
     * Tests that chacking a provided checksums against a calculated
     * checksum with the wrong algorithm creates an ingest fail event
     */
    @Test
    public void testWrongAlgorithmCreatesIngestFailEvent() throws StatefulIngestServiceException{
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(serialization.getBaseDir()).thenReturn(new File("ingest1/bag"));

        Checksum checksum1 = new ChecksumImpl("md5", "101010101010");
        Pair<String, Checksum> fileOnePair = new Pair("testFileOne.txt", checksum1);

        Checksum checksum2 = new ChecksumImpl("sha1", "101010101010");
        Pair<String, Checksum> fileOnePairWrongAlg = new Pair("testFileOne.txt", checksum2);

        AttributeSet fileOneAttributeSet = new AttributeSetImpl("testFileOne.txt");
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.CALCULATED_CHECKSUM, "SimpleEntry", fileOnePairWrongAlg.toString()));
        File absBaseDir = new File(serialization.getExtractDir(), serialization.getBaseDir().getPath());
        attributeManager.addAttributeSet(new File(absBaseDir, "testFileOne.txt").getAbsolutePath(), fileOneAttributeSet);


        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, "SimpleEntry", fileOnePair.toString()));
        attributeManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);

        checksumVerifier.execute("ingest:1", state);

        assertEquals(1, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(2, attributeMap.size());
    }


}
