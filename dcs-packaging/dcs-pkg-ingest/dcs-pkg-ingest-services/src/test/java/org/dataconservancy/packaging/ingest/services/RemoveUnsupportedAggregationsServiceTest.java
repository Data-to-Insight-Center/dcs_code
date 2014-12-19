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

import java.io.File;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.AttributeValueType;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.packaging.model.Package;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemoveUnsupportedAggregationsServiceTest {
    private final String projectId = "projectId";
    private final String collectionId = "collectionId";
    private final String projectKey = AttributeSetName.ORE_REM_PROJECT + "_" + projectId;
    private final String collectionKey = AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId;
    
    private final String projectAggregatedFileName = "projectAggregatedFile";
    private final String collectionAggregatedFileName = "unAggregatedFile";
    
    private String projectAggregatedFileUri;
    
    private String projectAggregatedFileRemKey;
    private String projectAggregatedFileKey;
    
    private String collectionAggregatedFileRemKey;
    private String collectionAggregatedFileKey;
    
    AttributeSetManager attributeSetManager;
    EventManager eventManager;
    
    private final String bagName = "BusinessObjectBuilderBag";
    
    private Map<String, AttributeSet> attributeMap;
    private Set<DcsEvent> events;
    private Package pkg;
    
    private IngestWorkflowState state;
    
    private RemoveUnsupportedAggregationsService underTest;
    
    @Before
    public void setup() {
        attributeMap = new HashMap<String, AttributeSet>();
        events = new HashSet<DcsEvent>();
        
        attributeSetManager = mock(AttributeSetManager.class);
        eventManager = mock(EventManager.class);
        BusinessObjectManager businessObjectManager = mock(BusinessObjectManager.class);
        setMockManagers();
        
        PackageDescription description = new DescriptionImpl();
        PackageSerialization serialization = new SerializationImpl();
        
        File extractDir = new File("/tmp/package-extraction");
        extractDir.mkdir();
        extractDir.deleteOnExit();

        File baseDir = new File("deposit1", bagName);
        baseDir.mkdir();
        baseDir.deleteOnExit();
        
        serialization.setBaseDir(baseDir);
        serialization.setExtractDir(extractDir);
        
        pkg = new PackageImpl(description, serialization);
        
        setupAttributeSets();
        
        state = mock(IngestWorkflowState.class);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getIngestUserId()).thenReturn("user");
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        
        underTest = new RemoveUnsupportedAggregationsService();
    }
    
    @Test
    public void testProjectAggregatedFileIsRemoved() throws StatefulIngestServiceException {
        underTest.execute("foo", state);
        AttributeSet projectAttributeSet = attributeSetManager.getAttributeSet(projectKey);
        assertNotNull(projectAttributeSet);
        
        assertTrue(projectAttributeSet.getAttributesByName(Metadata.PROJECT_AGGREGATES_FILE).isEmpty());
        
        assertNull(attributeSetManager.getAttributeSet(projectAggregatedFileRemKey));
        assertNull(attributeSetManager.getAttributeSet(projectAggregatedFileKey));
        
        assertEquals(1, events.size());
        DcsEvent event = events.iterator().next();
        assertEquals(projectAggregatedFileUri, event.getTargets().iterator().next().getRef());
    }
    
    @Test
    public void testCollectionAggregatedFileAreKept() throws StatefulIngestServiceException {
        underTest.execute("foo", state);
        AttributeSet collectionAttributeSet = attributeSetManager.getAttributeSet(collectionKey);
        assertNotNull(collectionAttributeSet);
        
        assertEquals(1, collectionAttributeSet.getAttributesByName(Metadata.COLLECTION_AGGREGATES_FILE).size());
        
        assertNotNull(attributeSetManager.getAttributeSet(collectionAggregatedFileRemKey));
        assertNotNull(attributeSetManager.getAttributeSet(collectionAggregatedFileKey));
    }
    
    private void setMockManagers() {
        doAnswer(new Answer<Set<AttributeSet>>() {

            @Override
            public Set<AttributeSet> answer(InvocationOnMock invocation)
                    throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the name of the attribute set and the matching attribute", args);
                assertEquals("Expected two arguments: the name of the attribute set and the matching attribute",
                        2, args.length);
                assertTrue("Expected argument to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument two to be of type attribute",
                           args[1] instanceof Attribute);
                
                String name = (String) args[0];
                Attribute matchingAttribute = (Attribute) args[1];

                //Brute force matching method for the test could be cleaner    
                Set<AttributeSet> matchingSets = new HashSet<AttributeSet>();
                for (AttributeSet potentialSet : attributeMap.values()) {
                    if (potentialSet.getName().equalsIgnoreCase(name)) {
                        for (Attribute attr : potentialSet.getAttributes()) {
                            if (matchingAttribute.getName() != null) {
                                if (!matchingAttribute.getName().equalsIgnoreCase(attr.getName())) {
                                    continue;
                                }
                            }
                            
                            if (matchingAttribute.getType() != null) {
                                if (!matchingAttribute.getType().equalsIgnoreCase(attr.getType())) {
                                    continue;
                                }
                            }
                            
                            if (matchingAttribute.getValue() != null) {
                                if (!matchingAttribute.getValue().equalsIgnoreCase(attr.getValue())) {
                                    continue;
                                }
                            }
                            
                            matchingSets.add(potentialSet);
                        }
                    }
                }
                
                return matchingSets;
            }
             
        }).when(attributeSetManager).matches(anyString(), any(Attribute.class));
         
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
        }).when(attributeSetManager).addAttributeSet(anyString(), any(AttributeSet.class));

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
         }).when(attributeSetManager).updateAttributeSet(anyString(), any(AttributeSet.class));

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
         }).when(attributeSetManager).getAttributeSet(anyString());
         
         doAnswer(new Answer<AttributeSet>() {

            @Override
            public AttributeSet answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the key of the attribute set to be removed", args);
                assertEquals("Expected one argument: the key of the attribute set to be removed",
                        1, args.length);
                assertTrue("Expected argument to be of type string",
                        args[0] instanceof String);
                String key = (String) args[0];

                return attributeMap.remove(key);
            }
             
         }).when(attributeSetManager).removeAttributeSet(anyString());
         
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
                 event.setEventType(type);
                 return event;
             }
        
         }).when(eventManager).newEvent(anyString());
         
         doAnswer(new Answer<Collection<DcsEvent>>() {

            @Override
            public Collection<DcsEvent> answer(InvocationOnMock invocation)
                    throws Throwable {
                return events;
            }
             
         }).when(eventManager).getEvents(anyString(), anyString());

    }
    
    private void setupAttributeSets() {
        File payloadFileBaseDir =  new File( pkg.getSerialization().getExtractDir(),
                                             pkg.getSerialization().getBaseDir().getPath());
                                   
        File dataDir = new File(payloadFileBaseDir, "data");
       
        String projectAggregatedFilePath = new File("data", projectAggregatedFileName).getPath();
        projectAggregatedFileUri = "file:///" + bagName + "/" + projectAggregatedFilePath.replace("\\", "/");
        File file1 = new File(dataDir, projectAggregatedFileName);
        projectAggregatedFileKey = file1.getPath();  
        projectAggregatedFileRemKey = AttributeSetName.ORE_REM_FILE + "_" + projectAggregatedFileUri;

        String collectionAggregatedFilePath = new File("data", collectionAggregatedFileName).getPath();
        String collectionAggregatedFileUri = "file:///" + bagName + "/" + collectionAggregatedFilePath.replace("\\", "/");
        File file2 = new File(dataDir, collectionAggregatedFileName);
        collectionAggregatedFileKey = file2.getPath();  
        collectionAggregatedFileRemKey = AttributeSetName.ORE_REM_FILE + "_" + collectionAggregatedFileUri;

        AttributeSet projectAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_PROJECT);
        AttributeSet collectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        AttributeSet projectAggregatedFileRemAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet projectAggregatedFileAttributeSet = new AttributeSetImpl(AttributeSetName.FILE);
        
        AttributeSet collectionAggregatedFileRemAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet collectionAggregatedFileAttributeSet = new AttributeSetImpl(AttributeSetName.FILE);

        projectAttributeSet.getAttributes().add(new AttributeImpl(Metadata.PROJECT_RESOURCEID, AttributeValueType.STRING, projectId));
        projectAttributeSet.getAttributes().add(new AttributeImpl(Metadata.PROJECT_AGGREGATES_FILE, AttributeValueType.STRING, projectAggregatedFileUri));
        attributeSetManager.addAttributeSet(projectKey, projectAttributeSet);
        
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, collectionId));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_AGGREGATES_FILE, AttributeValueType.STRING, collectionAggregatedFileUri));
        attributeSetManager.addAttributeSet(collectionKey, collectionAttributeSet);
        
        projectAggregatedFileRemAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, projectAggregatedFileName));
        projectAggregatedFileRemAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, projectAggregatedFileUri));
        attributeSetManager.addAttributeSet(projectAggregatedFileRemKey, projectAggregatedFileRemAttributeSet);

        collectionAggregatedFileRemAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, collectionAggregatedFileName)); 
        collectionAggregatedFileRemAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, collectionAggregatedFileUri));
        attributeSetManager.addAttributeSet(collectionAggregatedFileRemKey, collectionAggregatedFileRemAttributeSet);
        
        projectAggregatedFileAttributeSet = new AttributeSetImpl(AttributeSetName.FILE);
        projectAggregatedFileAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(100l)));
        projectAggregatedFileAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is a project aggregated file"));
        projectAggregatedFileAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, "project file format"));

        attributeSetManager.addAttributeSet(projectAggregatedFileKey, projectAggregatedFileAttributeSet);

        collectionAggregatedFileAttributeSet = new AttributeSetImpl(AttributeSetName.FILE);
        collectionAggregatedFileAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(200l)));
        collectionAggregatedFileAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is a file that should survive the service"));
        collectionAggregatedFileAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, "foo"));
        attributeSetManager.addAttributeSet(collectionAggregatedFileKey, collectionAggregatedFileAttributeSet);       
    }
}