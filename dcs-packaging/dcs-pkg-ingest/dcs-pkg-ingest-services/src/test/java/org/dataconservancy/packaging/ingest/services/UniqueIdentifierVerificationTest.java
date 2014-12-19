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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.joda.time.DateTime;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UniqueIdentifierVerificationTest {
    
    private UniqueIdentifierVerificationService underTest;
    private Package pkg;

    private AttributeSetManager attributeManager;
    private BusinessObjectManager businessObjectManager;

    private EventManager eventManager;
    private Set<DcsEvent> eventSet;
    private AttributeSet attrSetOne;
    private AttributeSet attrSetTwo;
    
    @Before
    public void setup() throws Exception {
        underTest = new UniqueIdentifierVerificationService();        
      
        pkg = mock(Package.class);    
        businessObjectManager = mock(BusinessObjectManager.class);

        attributeManager = new AttributeSetManagerImpl();
 
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
        
        attrSetOne = new AttributeSetImpl("Set One");
        attrSetTwo = new AttributeSetImpl("Set Two");
        
        Attribute projectIdOne = new AttributeImpl("Project-Identifier", "String", "project:dc:1");
        Attribute projectIdTwo = new AttributeImpl("Project-Identifier", "String", "project:dc:2");
        attrSetOne.getAttributes().add(projectIdOne);
        attrSetTwo.getAttributes().add(projectIdTwo);
        
        Attribute collectionIdOne = new AttributeImpl("Collection-Identifier", "String", "collection:dc:1");
        Attribute collectionIdTwo = new AttributeImpl("Collection-Identifier", "String", "collection:dc:2");
        attrSetOne.getAttributes().add(collectionIdOne);
        attrSetTwo.getAttributes().add(collectionIdTwo);
        
        Attribute dataItemIdOne = new AttributeImpl("DataItem-Identifier", "String", "dataItem:dc:1");
        Attribute dataItemIdTwo = new AttributeImpl("DataItem-Identifier", "String", "dataItem:dc:2");
        attrSetOne.getAttributes().add(dataItemIdOne);
        attrSetTwo.getAttributes().add(dataItemIdTwo);
        
        Attribute dataFileIdOne = new AttributeImpl("File-Identifier", "String", "dataFile:dc:1");
        Attribute dataFileIdTwo = new AttributeImpl("File-Identifier", "String", "dataFile:dc:2");
        attrSetOne.getAttributes().add(dataFileIdOne);
        attrSetTwo.getAttributes().add(dataFileIdTwo);
    }
    
    /**
     * Tests that attribute sets with no duplicates pass with no problem.
     */
    @Test
    public void testUniqueIdentifierPass() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        attributeManager.addAttributeSet("attr:2", attrSetTwo);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(0, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }      
    
    /**
     * Tests that attribute sets with the same project id attribute fails
     */
    @Test
    public void testDuplicateProjectIdInDifferentAttributeSetsFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute projectDuplicate = new AttributeImpl("Project-Identifier", "String", "project:dc:1");
        attrSetTwo.getAttributes().add(projectDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        attributeManager.addAttributeSet("attr:2", attrSetTwo);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that attribute sets with the same collection id attribute fails
     */
    @Test
    public void testDuplicateCollectionIdInDifferentAttributeSetsFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute collectionDuplicate = new AttributeImpl("Collection-Identifier", "String", "collection:dc:1");
        attrSetTwo.getAttributes().add(collectionDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        attributeManager.addAttributeSet("attr:2", attrSetTwo);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that attribute sets with the same data item id attribute fails
     */
    @Test
    public void testDuplicateDataItemIdInDifferentAttributeSetsFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute dataItemDuplicate = new AttributeImpl("DataItem-Identifier", "String", "dataItem:dc:1");
        attrSetTwo.getAttributes().add(dataItemDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        attributeManager.addAttributeSet("attr:2", attrSetTwo);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that attribute set with the same file id attribute fails
     */
    @Test
    public void testDuplicateFileIdInDifferentAttributeSetsFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute fileDuplicate = new AttributeImpl("File-Identifier", "String", "dataItem:dc:1");
        attrSetTwo.getAttributes().add(fileDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        attributeManager.addAttributeSet("attr:2", attrSetTwo);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that attribute set with project duplicate fails
     */
    @Test
    public void testDuplicateProjectIdInAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute projectDuplicate = new AttributeImpl("Project-Identifier", "String", "project:dc:1");
        attrSetOne.getAttributes().add(projectDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that attribute set with collection duplicate fails
     */
    @Test
    public void testDuplicateCollectionIdInAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute collectionDuplicate = new AttributeImpl("Collection-Identifier", "String", "collection:dc:1");
        attrSetOne.getAttributes().add(collectionDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that attribute set with data item duplicate fails
     */
    @Test
    public void testDuplicateDataItemIdInAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute dataItemDuplicate = new AttributeImpl("DataItem-Identifier", "String", "dataItem:dc:1");
        attrSetOne.getAttributes().add(dataItemDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that attribute set with file duplicate fails
     */
    @Test
    public void testDuplicateFileIdInAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute fileDuplicate = new AttributeImpl("File-Identifier", "String", "dataFile:dc:1");
        attrSetOne.getAttributes().add(fileDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    } 
    
    /**
     * Tests that different attribute names with the same id fails
     */
    @Test
    public void testDuplicateProjectCollectionIdInAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute projectDuplicate = new AttributeImpl("Collection-Identifier", "String", "project:dc:1");
        attrSetOne.getAttributes().add(projectDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
       
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that different attribute names with the same id fails
     */
    @Test
    public void testDuplicateCollectionProjectIdInAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute projectDuplicate = new AttributeImpl("Project-Identifier", "String", "collection:dc:1");
        attrSetOne.getAttributes().add(projectDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
       
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that different attribute names with the same id fails
     */
    @Test
    public void testDuplicateDataItemDataFileIdInAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute dataItemDuplicate = new AttributeImpl("DataItem-Identifier", "String", "dataFile:dc:1");
        attrSetOne.getAttributes().add(dataItemDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);

        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that different attribute names with the same id fails
     */
    @Test
    public void testDuplicateFileDataItemIdInAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute fileDuplicate = new AttributeImpl("File-Identifier", "String", "dataItem:dc:1");
        attrSetOne.getAttributes().add(fileDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }   
     
    /**
     * Tests that different attribute names with the same id fails
     */
    @Test
    public void testDuplicateIdWithDifferentNameInFirstAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute projectDuplicate = new AttributeImpl("Collection-Identifier", "String", "project:dc:1");
        attrSetTwo.getAttributes().add(projectDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        attributeManager.addAttributeSet("attr:2", attrSetTwo);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }       
    
    /**
     * Tests that different attribute names with the same id fails
     */
    @Test
    public void testDuplicateIdWithDifferentNameInSecondAttributeSetFails() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        
        Attribute fileDuplicate = new AttributeImpl("Project-Identifier", "String", "collection:dc:2");
        attrSetOne.getAttributes().add(fileDuplicate);
        
        attributeManager.addAttributeSet("attr:1", attrSetOne);
        attributeManager.addAttributeSet("attr:2", attrSetTwo);
        
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }    
    
}