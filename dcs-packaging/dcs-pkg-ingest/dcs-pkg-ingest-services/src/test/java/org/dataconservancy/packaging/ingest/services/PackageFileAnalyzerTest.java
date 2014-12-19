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

import java.io.File;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.packaging.ingest.api.*;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.contentdetection.impl.droid.DroidContentDetectionServiceImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.util.ChecksumGeneratorVerifier;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.joda.time.DateTime;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

public class PackageFileAnalyzerTest {
    
    private PackageFileAnalyzer packageAnalyzer;
    private org.dataconservancy.packaging.model.Package pkg;
    private PackageSerialization serialization;
    private AttributeSetManager attributeManager;
    private BusinessObjectManager businessObjectManager;
    private Map<String, AttributeSet> attributeMap;
    
    private EventManager eventManager;
    private Set<DcsEvent> eventSet;
    private File fileOneTmp;
    private File fileTwoTmp;
    
    @Before
    public void setup() throws Exception {
        packageAnalyzer = new PackageFileAnalyzer();
        packageAnalyzer.setContentDetectionService(new DroidContentDetectionServiceImpl());
        packageAnalyzer.setChecksumGenerator(new ChecksumGeneratorVerifier());
        fileOneTmp = java.io.File.createTempFile("testFile", ".txt");
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
        
        pkg = new PackageImpl(null, serialization);    
        
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

        businessObjectManager = mock(BusinessObjectManager.class);
    }
    
    /**
     * Tests that events and attribute sets are correctly added to the state object. 
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testGenerateAllFileAttributes() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        packageAnalyzer.execute("ingest:1", state);
        
        assertEquals(2, attributeMap.size());
       
        for (AttributeSet attributeSet : attributeMap.values()) {
            assertEquals(9, attributeSet.getAttributes().size()); 
            int detectionToolNameCount = 0;
            int detectionToolVersionCount = 0;
            int fileNameCount = 0;
            int fileSizeCount = 0;
            int fileIngestCount = 0;
            int fileFormatCount = 0;
            int checksumCount = 0;
            for (Attribute attr : attributeSet.getAttributes()) {
                if (attr.getName().equalsIgnoreCase("File-Format-Detection-Tool-Name")) {
                    detectionToolNameCount++;
                } else if (attr.getName().equalsIgnoreCase("File-Format-Detection-Tool-Version")) {
                    detectionToolVersionCount++;
                } else if (attr.getName().equalsIgnoreCase("File-Name")) {
                    fileNameCount++;
                } else if (attr.getName().equalsIgnoreCase("File-Size")) {
                    fileSizeCount++;
                } else if (attr.getName().equalsIgnoreCase("File-Imported-Date")) {
                    fileIngestCount++;
                } else if (attr.getName().equalsIgnoreCase("File-Format")) {
                    fileFormatCount++;
                } else if (attr.getName().equalsIgnoreCase("Calculated-Checksum")) {
                    checksumCount++;
                }
            }
            
            assertEquals(1, detectionToolNameCount);
            assertEquals(1, detectionToolVersionCount);
            assertEquals(1, fileNameCount);
            assertEquals(1, fileSizeCount);
            assertEquals(1, fileIngestCount);
            assertEquals(2, fileFormatCount);
            assertEquals(2, checksumCount);
            
        }
        
        assertEquals(10, eventSet.size());
        
        assertEquals(0, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(4, state.getEventManager().getEvents("", Package.Events.FIXITY_CALCULATED).size());
        assertEquals(2, state.getEventManager().getEvents("", Package.Events.METADATA_GENERATED).size());
        assertEquals(4, state.getEventManager().getEvents("", Package.Events.CHARACTERIZATION_FORMAT).size());
    }    
    
    /**
     * Test to ensure the code path that updates metadata attribute sets instead of adding new ones works correctly.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testGenerateFileAttributesExisingAttributeSet() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet fileOneAttributeSet = new AttributeSetImpl("File");
        AttributeSet fileTwoAttributeSet = new AttributeSetImpl("File");
        
        fileOneAttributeSet.getAttributes().add(new AttributeImpl("Test", "String", "ExistingAttribute"));
        fileTwoAttributeSet.getAttributes().add(new AttributeImpl("Test", "String", "ExistingAttributeTwo"));
        
        attributeManager.addAttributeSet(fileOneTmp.getPath(), fileOneAttributeSet);
        attributeManager.addAttributeSet(fileTwoTmp.getPath(), fileTwoAttributeSet);
        
        packageAnalyzer.execute("ingest:1", state);
        
        assertEquals(2, attributeMap.size());
       
        for (AttributeSet attributeSet : attributeMap.values()) {
            assertEquals(10, attributeSet.getAttributes().size()); 
        }
        
        assertEquals(10, eventSet.size());
        
        assertEquals(0, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(4, state.getEventManager().getEvents("", Package.Events.FIXITY_CALCULATED).size());
        assertEquals(2, state.getEventManager().getEvents("", Package.Events.METADATA_GENERATED).size());
        assertEquals(4, state.getEventManager().getEvents("", Package.Events.CHARACTERIZATION_FORMAT).size());
    }
    
    /**
     * Test that a null package results in an IllegalStateException being thrown by the service.
     */
    @Test(expected = IllegalStateException.class)
    public void testNullPackageFiresFailureEvent() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getPackage()).thenReturn(null);
        
        packageAnalyzer.execute("ingest:1", state);
    }
    
    /**
     * Tests that if the state arrives with a null event manager the service throws an IllegalStateException.
     * @throws StatefulIngestServiceException
     */    
    @Test(expected = IllegalStateException.class)
    public void testNullEventManagerThrowsException() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        when(state.getEventManager()).thenReturn(null);
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(state.getPackage()).thenReturn(pkg);
        
        packageAnalyzer.execute("ingest:1", state);
    }
    
    /**
     * Tests that if the service recieves a state with a null attribute set manager a IllegalStateException is thrown
     * @throws StatefulIngestServiceException
     */
    @Test(expected = IllegalStateException.class)
    public void testNullAttributeSetManagerFiresFailureEvent() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getAttributeSetManager()).thenReturn(null);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        when(state.getPackage()).thenReturn(pkg);
        
        packageAnalyzer.execute("ingest:1", state);
    }
    /**
     * Test that a null file fires the correct failure event
     */
    @Test
    public void testNullFileFiresEvent() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        List<File> files = new ArrayList<File>();
        files.add(fileOneTmp);
        files.add(fileTwoTmp);
        files.add(null);       
        when(serialization.getFiles()).thenReturn(files);

        packageAnalyzer.execute("ingest:1", state);
        
        assertEquals(2, attributeMap.size());
        
        for (AttributeSet attributeSet : attributeMap.values()) {
            assertEquals(9, attributeSet.getAttributes().size()); 
        }
        
        assertEquals(11, eventSet.size());
        
        assertEquals(1, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(4, state.getEventManager().getEvents("", Package.Events.FIXITY_CALCULATED).size());
        assertEquals(2, state.getEventManager().getEvents("", Package.Events.METADATA_GENERATED).size());
        assertEquals(4, state.getEventManager().getEvents("", Package.Events.CHARACTERIZATION_FORMAT).size());
    }
    
    /**
     * Tests that directories are correctly ignored by the file analyzer.
     */
    @Test
    public void testDirectoryGeneratesNoAttributes() throws StatefulIngestServiceException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        List<File> files = new ArrayList<File>();
        files.add(fileOneTmp);
        files.add(fileTwoTmp);
        files.add(tempDir);       
        when(serialization.getFiles()).thenReturn(files);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        
        packageAnalyzer.execute("ingest:1", state);
        
        assertEquals(2, attributeMap.size());
        
        for (AttributeSet attributeSet : attributeMap.values()) {
            assertEquals(9, attributeSet.getAttributes().size()); 
        }
        
        assertEquals(10, eventSet.size());
        
        assertEquals(0, state.getEventManager().getEvents("", Package.Events.INGEST_FAIL).size());
        assertEquals(4, state.getEventManager().getEvents("", Package.Events.FIXITY_CALCULATED).size());
        assertEquals(2, state.getEventManager().getEvents("", Package.Events.METADATA_GENERATED).size());
        assertEquals(4, state.getEventManager().getEvents("", org.dataconservancy.packaging.ingest.api.Package.Events.CHARACTERIZATION_FORMAT).size());
    }
    
    /**
     * Tests that if the service is passed a null workflow state an IllegalArgumentException is thrown.
     * @throws StatefulIngestServiceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullStateThrowsAnException() throws StatefulIngestServiceException {
        
        packageAnalyzer.execute("ingest:1", null);
    }
}