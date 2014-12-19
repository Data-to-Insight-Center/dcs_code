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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * 
 * Tests for BusinessObjectValidator
 * 
 */
public class BusinessObjectValidatorTest {
    
    private IngestWorkflowState state;
    private BusinessObjectManager businessObjectManager;

    private EventManager eventManager;
    private List<DcsEvent> events;
    private Map<String, BusinessObject> businessObjectMap;
    private org.dataconservancy.packaging.model.Package pkg;
    private BusinessObjectValidator underTest;
    
    private DateTime publishDate = new DateTime(2013, 6, 10, 1, 55, 18);
    private final String collectionName =  "Collection-1";
    private final String subCollectionName = "Collection-2";
    private final String dataItemOneName = "DataItem-1";
    private final String dataFileOneName = "DataFile-1.doc";

    private final String metadataFileOneName = "MetaDataFile-1.txt";

    private final String collectionSummary =  "Test-Big-Collection";
    private final String subCollectionSummary = "Test-Subcollection";
    private final String dataItemOneDescription = "Test-Collection-DataItemOne";

    private PersonName personName1;
    private PersonName personName2;
    private long dataFileOneSize = 132465798L;
    private long metadataFileOneSize = 674987654L;


    private final String dataFileFormat = "test/format1";
    private final String metadataFileFormat = "test/format2";
    
    private String creator2 = "TheJohns Hopkins University";
    
    private final String dataFileOnePayloadPath = "path";
    private final String metadataFileOnePayloadPath = "metadataPath";
    private final String dataFileOneSource = "file source";
    private final String metadataFileOneSource = "metadata file source";

    private Collection collection;
    private Collection subcollection;
    private DataItem dataItemOne;
    private DataFile dataFileOne;
    private DataFile dataFileTwo;
    private MetadataFile metadataFileOne;
    
    private DateTime depositDate;
    private final String userId = "user";
    
    @Before
    public void setUp() {
        underTest = new BusinessObjectValidator();
        
        depositDate = new DateTime();

        pkg = mock(org.dataconservancy.packaging.model.Package.class);
        
        // mocked services and managers
        AttributeSetManager attributeSetManager = mock(AttributeSetManager.class);
        businessObjectManager = mock(BusinessObjectManager.class);
        eventManager = mock(EventManager.class);
        
        // persistence for these
        businessObjectMap = new HashMap<String, BusinessObject>();
        events = new ArrayList<DcsEvent>();
        
        setupMockServices();
        addBusinessObjects();
        
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
    }
    
    @Test
    public void testSuccess() throws StatefulIngestServiceException {
        underTest.execute("foo", state);
        
        Assert.assertEquals(1, events.size());
        Assert.assertTrue(events.get(0).getDetail().contains("Successfully validated"));
        Assert.assertTrue(events.get(0).getOutcome().equalsIgnoreCase("Successfully validated a total number of: 5 BusinessObjects."));
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testCollectionMissingTitle() throws StatefulIngestServiceException {
        Collection titleLessCollection = new Collection();
        titleLessCollection.setId("collection-id");
        titleLessCollection.setSummary("summary");
        titleLessCollection.getCreators().add(personName2);
        titleLessCollection.setPublicationDate(publishDate);
        titleLessCollection.setParentId(collection.getId());
        businessObjectManager.add("", titleLessCollection, Collection.class);
        
        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testCollectionMissingSummary() throws StatefulIngestServiceException {
        Collection collectionMissingSummary = new Collection();
        collectionMissingSummary.setId("collection-id");
        
        collectionMissingSummary.setSummary("");
        collectionMissingSummary.getCreators().add(personName2);
        collectionMissingSummary.setPublicationDate(publishDate);
        collectionMissingSummary.setParentId(collection.getId());
        businessObjectManager.add("", collectionMissingSummary, Collection.class);
        
        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testCollectionMissingParent() throws StatefulIngestServiceException {
        Collection collectionMissingParent = new Collection();
        collectionMissingParent.setId("collection-id");
        collectionMissingParent.setTitle("title");
        collectionMissingParent.setSummary("summary");
        collectionMissingParent.getCreators().add(personName2);
        collectionMissingParent.setPublicationDate(publishDate);
        businessObjectManager.add("", collectionMissingParent, Collection.class);
        
        underTest.execute("foo", state);
    }
    
    @Test
    public void testCollectionWithParentProject() throws StatefulIngestServiceException {
        Collection collectionWithParentProject = new Collection();
        collectionWithParentProject.setId("collection-id");
        collectionWithParentProject.setTitle("title");
        collectionWithParentProject.setSummary("summary");
        collectionWithParentProject.getCreators().add(personName2);
        collectionWithParentProject.setPublicationDate(publishDate);
        collectionWithParentProject.setParentProjectId("project");
        businessObjectManager.add("", collectionWithParentProject, Collection.class);
        
        underTest.execute("foo", state);
        
        Assert.assertEquals(1, events.size());
        Assert.assertTrue(events.get(0).getDetail().contains("Successfully validated"));
        Assert.assertTrue(events.get(0).getOutcome().equalsIgnoreCase("Successfully validated a total number of: 6 BusinessObjects."));
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testDataItemMissingName() throws StatefulIngestServiceException {
        DataItem dataItemMissingName = new DataItem();
        dataItemMissingName.setId("dataitem-id");
        dataItemMissingName.setDescription(dataItemOneDescription);
        dataItemMissingName.getFiles().add(dataFileOne);
        dataItemMissingName.getFiles().add(dataFileTwo);
        dataItemMissingName.setDepositDate(depositDate);
        dataItemMissingName.setDepositorId(userId);
        businessObjectManager.add("BadDataItem", dataItemMissingName, DataItem.class);

        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testDataItemMissingDepositor() throws StatefulIngestServiceException {
        DataItem dataItemMissingDepositor = new DataItem();
        dataItemMissingDepositor.setName("dataitem");
        dataItemMissingDepositor.setId("dataitem-id");
        dataItemMissingDepositor.setDescription(dataItemOneDescription);
        dataItemMissingDepositor.getFiles().add(dataFileOne);
        dataItemMissingDepositor.getFiles().add(dataFileTwo);
        dataItemMissingDepositor.setDepositDate(depositDate);
        businessObjectManager.add("BadDataItem", dataItemMissingDepositor, DataItem.class);

        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testDataItemMissingDepositDate() throws StatefulIngestServiceException {
        DataItem dataItemMissingName = new DataItem();
        dataItemMissingName.setId("dataitem-id");
        dataItemMissingName.setName("dataitem");
        dataItemMissingName.setDescription(dataItemOneDescription);
        dataItemMissingName.getFiles().add(dataFileOne);
        dataItemMissingName.getFiles().add(dataFileTwo);
        dataItemMissingName.setDepositorId(userId);
        businessObjectManager.add("BadDataItem", dataItemMissingName, DataItem.class);

        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testDataFileMissingRequiredField() throws StatefulIngestServiceException {
        DataFile badDataFile = new DataFile();
        businessObjectManager.add("BadDataFile", badDataFile, DataFile.class);
        
        underTest.execute("foo", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testMetadataFileMissingRequiredField() throws StatefulIngestServiceException {
        MetadataFile badMetadataFile = new MetadataFile();
        businessObjectManager.add("badMetadataFile", badMetadataFile, MetadataFile.class);
        
        underTest.execute("foo", state);
    }

    @SuppressWarnings("unchecked")
    private void setupMockServices() {
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
                event.setDate(new DateTime().toString());
                event.setEventType(type);
                return event;
            }
            
        }).when(eventManager).newEvent(anyString());
        
        doAnswer(new Answer<BusinessObject>() {
            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull(
                        "Expected two arguments: the local id of the business object to be retrieved, and the class of the business object",
                        args);
                assertEquals(
                        "Expected one argument: the class of the business object to be retrieved, and the class of the business object",
                        2, args.length);
                assertTrue("Expected argument to be of type string", args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject class", args[1] instanceof Class);
                String key = (String) args[0];
                return businessObjectMap.get(key);
            }
        }).when(businessObjectManager).get(anyString(), any(Class.class));
        
        doAnswer(new Answer<BusinessObject>() {
            
            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected three arguments: the local id of the business object to be retrieved,"
                        + " the instance of the business object, and the class of the business object", args);
                assertEquals("Expected three arguments: the class of the business object to be retrieved,"
                        + " the instance of the business object, and the class of the business object", 3, args.length);
                assertTrue("Expected argument to be of type string", args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject", args[1] instanceof BusinessObject);
                assertTrue("Expected argument to be of type BusinessObject class", args[2] instanceof Class);
                String key = (String) args[0];
                BusinessObject bo = (BusinessObject) args[1];
                businessObjectMap.put(key, bo);
                return null;
            }
        }).when(businessObjectManager).add(anyString(), any(BusinessObject.class), any(Class.class));
        
        doAnswer(new Answer<BusinessObject>() {
            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {
                
                Object[] args = invocation.getArguments();
                assertNotNull("Expected three arguments: the local id of the business object to be retrieved,"
                        + " the instance of the business object, and the class of the business object", args);
                assertEquals("Expected three arguments: the class of the business object to be retrieved,"
                        + " the instance of the business object, and the class of the business object", 3, args.length);
                assertTrue("Expected argument to be of type string", args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject", args[1] instanceof BusinessObject);
                assertTrue("Expected argument to be of type BusinessObject class", args[2] instanceof Class);
                String key = (String) args[0];
                BusinessObject bo = (BusinessObject) args[1];
                businessObjectMap.put(key, bo);
                return null;
            }
        }).when(businessObjectManager).update(anyString(), any(BusinessObject.class), any(Class.class));
        
        doAnswer(new Answer<Set<BusinessObject>>() {
            @Override
            public Set<BusinessObject> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one arguments: the the class of the business object", args);
                assertEquals("Expected one argument: the class of the business objects to be retrieved", 1, args.length);
                assertTrue("Expected argument to be of type BusinessObject class", args[0] instanceof Class);
                
                Set<MetadataFile> metadataFileSet = new HashSet<MetadataFile>();
                Set<DataFile> dataFileSet = new HashSet<DataFile>();
                Set<Collection> collectionSet = new HashSet<Collection>();
                Set<DataItem> dataItemSet = new HashSet<DataItem>();
                
                for (String key : businessObjectMap.keySet()) {
                    if (businessObjectMap.get(key) instanceof MetadataFile) {
                        metadataFileSet.add((MetadataFile) businessObjectMap.get(key));
                    }
                    else if (businessObjectMap.get(key) instanceof DataFile) {
                        dataFileSet.add((DataFile) businessObjectMap.get(key));
                    }
                    else if (businessObjectMap.get(key) instanceof Collection) {
                        collectionSet.add((Collection) businessObjectMap.get(key));
                    }
                    else if (businessObjectMap.get(key) instanceof DataItem) {
                        dataItemSet.add((DataItem) businessObjectMap.get(key));
                    }
                }
                
                Set<BusinessObject> result = new HashSet<BusinessObject>();
                
                if (args[0].equals(MetadataFile.class)) {
                    result.addAll(metadataFileSet);
                }
                else if (args[0].equals(DataFile.class)) {
                    result.addAll(dataFileSet);
                }
                else if (args[0].equals(Collection.class)) {
                    result.addAll(collectionSet);
                }
                else if (args[0].equals(DataItem.class)) {
                    result.addAll(dataItemSet);
                }
                return result;
            }
        }).when(businessObjectManager).getInstancesOf(any(Class.class));

    }
    
    private void addBusinessObjects() {
        personName1 = new PersonName();
        personName1.setPrefixes("Dr.");
        personName1.setGivenNames("Robert");
        personName1.setMiddleNames("Moses");
        personName1.setFamilyNames("Kildare");
        
        personName2 = new PersonName();
        personName2.setFamilyNames(creator2);
        
        collection = new Collection();
        collection.setId("id:collection");
        collection.setTitle(collectionName);
        collection.setSummary(collectionSummary);
        collection.getCreators().add(personName1);
        collection.setPublicationDate(publishDate);
        collection.getChildrenIds().add("id:subcollection");
        collection.setParentId("project:foo");
        businessObjectManager.add(collectionName, collection, Collection.class);
        
        subcollection = new Collection();
        subcollection.setId("id:subcollection");
        subcollection.setTitle(subCollectionName);
        subcollection.setSummary(subCollectionSummary);
        subcollection.getCreators().add(personName2);
        subcollection.setPublicationDate(publishDate);
        subcollection.setParentId(collection.getId());
        businessObjectManager.add(subCollectionName, subcollection, Collection.class);
        
        dataFileOne = new DataFile();
        dataFileOne.setId("dataFile:1");
        dataFileOne.setName(dataFileOneName);
        dataFileOne.setPath(dataFileOnePayloadPath);
        dataFileOne.setFormat(dataFileFormat);
        dataFileOne.setSize(dataFileOneSize);
        dataFileOne.setSource(dataFileOneSource);
        businessObjectManager.add(dataFileOneName, dataFileOne, DataFile.class);
        
        metadataFileOne = new MetadataFile();
        metadataFileOne.setId("metadataFile:1");
        metadataFileOne.setName(metadataFileOneName);
        metadataFileOne.setPath(metadataFileOnePayloadPath);
        metadataFileOne.setSize(metadataFileOneSize);
        metadataFileOne.setFormat(metadataFileFormat);
        metadataFileOne.setSource(metadataFileOneSource);
        businessObjectManager.add(metadataFileOneName, metadataFileOne, MetadataFile.class);
        
        dataItemOne = new DataItem();
        dataItemOne.setId("dataItem:1");
        dataItemOne.setName(dataItemOneName);
        dataItemOne.setDescription(dataItemOneDescription);
        dataItemOne.getFiles().add(dataFileOne);
        dataItemOne.getFiles().add(dataFileTwo);
        dataItemOne.setDepositDate(depositDate);
        dataItemOne.setDepositorId(userId);
        businessObjectManager.add(dataItemOneName, dataItemOne, DataItem.class);
    }

}
