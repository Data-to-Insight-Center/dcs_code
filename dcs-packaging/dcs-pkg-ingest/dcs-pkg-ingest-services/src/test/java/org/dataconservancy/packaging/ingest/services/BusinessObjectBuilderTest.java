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

import java.net.URL;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.impl.IdentifierImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.packaging.ingest.api.*;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.*;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.ui.model.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.dataconservancy.dcs.id.api.Types;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the BusinessObjectBuilder ingest service
 */
public class BusinessObjectBuilderTest {

    private IngestWorkflowState state;
    private AttributeSetManager attributeSetManager;
    private BusinessObjectManager businessObjectManager;

    private EventManager eventManager;
    private BusinessObjectBuilder businessObjectBuilder;
    private List<DcsEvent> events;
    private String[] expectedKeys;
    private Map<String, AttributeSet> attributeMap;
    private Map<Key, BusinessObject> businessObjectMap;
    private IdService idService;
    private org.dataconservancy.packaging.model.Package pkg;
    private int i;//counter for idService

    private final String collectionName =  "Collection-1";
    private final String subCollectionName = "Collection-2";
    private final String dataItemOneName = "DataItem-1";
    private final String dataItemTwoName = "DataItem-2";
    private final String dataItemThreeName = "DataItem-3";
    private final String dataFileOneName = "DataFile-1.doc";
    private final String dataFileTwoName = "DataFile-2.pdf";
    private final String dataFileThreeName = "DataFile-3.xls";
    private final String dataFileFourName = "DataFile-4.pdf";
    private final String metadataFileOneName = "MetaDataFile-1.txt";
    private final String metadataFileTwoName = "MetaDataFile-2.jpg";
    private final String metadataFileThreeName = "MetaDataFile-3.txt";
    private final String randomFileName =  "fileawownonero";
    private final String externalProjectId = "http://dataconservancy.org/project/1";
    private final String aggregatedCollectionName = "aggregatedCollectionName";
    private DateTime publishDate = new DateTime(2013, 6, 10, 1, 55, 18, DateTimeZone.UTC);

    private final String randomResourceId = "vbtg35w6356bq3a45b344s3566b";

    private final String externalCollectionId = "dc:id:collection:1";
    
    private final String collectionSummary =  "Test-Big-Collection";
    private final String subCollectionSummary = "Test-Subcollection";
    private final String dataItemOneDescription = "Test-Collection-DataItemOne";
    private final String dataItemTwoDescription = "Test-Collection-DataItemTwo";
    private final String dataItemThreeDescription = "Test-Collection-DataItemThree";
    
    private final String bagName = "BusinessObjectBuilderBag";
    private final String ingestUser = "user:cookiemonster";
    private long dataFileOneSize = 132465798L;
    private long dataFileTwoSize = 564321321L;
    private long dataFileThreeSize = 556323213L;
    private long dataFileFourSize = 78765465461L;
    private long metadataFileOneSize = 674987654L;
    private long metadataFileTwoSize = 564798765L;
    private long metadataFileThreeSize = 658798465L;

    private String dataFileOneUri;
    private String dataFileTwoUri;
    private String dataFileThreeUri;
    private String dataFileFourUri;
    private String metadataFileOneUri;
    private String metadataFileTwoUri;
    private String metadataFileThreeUri;

    private String file1FileASKey;
    private String file2FileASKey;
    private String file3FileASKey;
    private String file4FileASKey;
    private String metadataFile1FileASKey;
    private String metadataFile2FileASKey;
    private String metadataFile3FileASKey;
    
    private String fileTwoSource;
    private String fileThreeSource;
    private String fileFourSource;
    private String metadataFileOneSource;
    private String metadataFileTwoSource;
    private String metadataFileThreeSource;

    private PersonName personName1;
    private PersonName personName2;

    private final String pronomFormatUri = "info:pronom/fmt/19";
    //When parsed by the builder this should produce the pronomFormatUri given above
    private final String dataFileFormat = "DcsFormat{format ='fmt/19', name='text/xml', schema uri='http://www.nationalarchives.gov.uk/PRONOM/', version='null'}";
    
    //When parsed by the builder this should remain the same
    private final String assertedDataFileFormat = "text/plain";
    
    //When parsed by the builder this should produce metadataFileMimeType
    private final String metadataFileFormat = "DcsFormat{format ='text/xml', name='text/xml', schema uri='http://www.iana.org/assignments/media-types/', version='null'}";

    private final String dataFileMimeType = "application/pdf";
    private final String metadataFileMimeType = "text/xml";

    private String creator2 = " TheJohns Hopkins University";
    
    private File baseDir;
    private File extractDir;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws IdentifierNotFoundException{
         businessObjectBuilder = new BusinessObjectBuilder();

         PackageDescription description = new DescriptionImpl();
         PackageSerialization serialization = new SerializationImpl();
         
         extractDir = new File("/tmp/package-extraction");
         extractDir.mkdir();
         extractDir.deleteOnExit();

         baseDir = new File("deposit1", bagName);
         baseDir.mkdir();
         baseDir.deleteOnExit();
         
         serialization.setBaseDir(baseDir);
         serialization.setExtractDir(extractDir);
         
         pkg = new PackageImpl(description, serialization);

         //mocked services and managers
         attributeSetManager = mock(AttributeSetManager.class);
         businessObjectManager= mock(BusinessObjectManager.class);
         eventManager = mock(EventManager.class);
         idService = mock(IdService.class);

         businessObjectBuilder.setIdService(idService);

         //persistence for these
         attributeMap = new HashMap<String, AttributeSet>();
         businessObjectMap = new HashMap<Key,BusinessObject>();
         events = new ArrayList<DcsEvent>();
         
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
         
         doAnswer(new Answer<Identifier>() {
                   @Override
                   public Identifier answer(InvocationOnMock invocation) throws Throwable {
                         // Extract the Attribute set and key from the InvocationOnMock
                         Object[] args = invocation.getArguments();
                         assertNotNull("Expected one argument: the event type to be added", args);
                         Assert.assertEquals("Expected one argument: the event type to be added",
                                   1, args.length);
                         assertTrue("Expected argument to be of type String",
                                           args[0] instanceof String);
                         String eventType = (String) args[0];
                         return new IdentifierImpl(eventType, String.valueOf(i++));
                   }
         }).when(idService).create(anyString());
         
         doAnswer(new Answer<Identifier>() {

            @Override
            public Identifier answer(InvocationOnMock invocation)
                    throws Throwable {
             // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the url of the id to be retrieved", args);
                Assert.assertEquals("Expected one argument: the url of the id to be retrieved",
                          1, args.length);
                assertTrue("Expected argument to be of type URL",
                                  args[0] instanceof URL);
                URL idURL = (URL) args[0];
                Identifier id = null;
                if (idURL.toExternalForm().contains("project")) {
                    id = new IdentifierImpl(Types.PROJECT.name(), idURL.toString());
                } else if (idURL.toExternalForm().contains("collection")){
                    id = new IdentifierImpl(Types.COLLECTION.name(), idURL.toString());
                }
                    
                return id;
            }
             
         }).when(idService).fromUrl(any(URL.class));
         
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
         
         doAnswer(new Answer<Set<AttributeSet>>() {

            @Override
            public Set<AttributeSet> answer(InvocationOnMock invocation)
                    throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the matcher to be used", args);
                assertEquals("Expected one argument: the matcher to be used",
                        1, args.length);
                assertTrue("Expected argument to be of type AttributeMatcher",
                        args[0] instanceof AttributeMatcher);
                
                AttributeMatcher matcher = (AttributeMatcher) args[0];
                
                //Brute force matching method for the test could be cleaner    
                final Set<AttributeSet> results = new HashSet<AttributeSet>();
                for (AttributeSet candidate : attributeMap.values()) {
                    for (Attribute candidateAttr : candidate.getAttributes()) {
                        if (matcher.matches(candidate.getName(), candidateAttr)) {
                            results.add(candidate);
                        }
                    }
                }

                return results;
            }
             
         }).when(attributeSetManager).matches(any(AttributeMatcher.class));

        doAnswer(new Answer<Set<String>>() {
            @Override
            public Set<String> answer(InvocationOnMock invocation) throws Throwable {
          // Extract the Attribute set and key from the InvocationOnMock
                return attributeMap.keySet();
            }
        }).when(attributeSetManager).getKeys();

         doAnswer(new Answer<BusinessObject>() {
            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the local id of the business object to be retrieved, and the class of the business object", args);
                assertEquals("Expected one argument: the class of the business object to be retrieved, and the class of the business object",
                        2, args.length);
                assertTrue("Expected argument to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject class",
                        args[1] instanceof Class);
                String id = (String) args[0];
                Key key = new Key(id, (Class)args[1]);
                return businessObjectMap.get(key);
            }
         }).when(businessObjectManager).get(anyString(), any(Class.class));
         
         doAnswer(new Answer<Set<BusinessObject>>() {

            @Override
            public Set<BusinessObject> answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected oen arguments: the class of the business objects to be retrieved", args);
                assertEquals("Expected one argument: the class of the business objects to be retrieved",
                        1, args.length);
                assertTrue("Expected argument to be of type BusinessObject class",
                        args[0] instanceof Class);

                Set<BusinessObject> objects = new HashSet<BusinessObject>();
                for (Key key : businessObjectMap.keySet()) {
                    if (key.getBoClass().equals(args[0])) {
                        objects.add(businessObjectMap.get(key));
                    }
                }
                return objects;
            }             
         }).when(businessObjectManager).getInstancesOf(any(Class.class));
         
         doAnswer(new Answer<Class>() {
             @Override
             public Class answer(InvocationOnMock invocation) throws Throwable {
                 Object[] args = invocation.getArguments();
                 assertNotNull("Expected one argument: the local id of the business object type to be retrieved.", args);
                 assertEquals("Expected one argument: the local id of the business object type to be retrieved.", 1, args.length);
                 assertTrue("Expected argument to be of type string",
                         args[0] instanceof String);
                 String id = (String)args[0];
                 for (Key key : businessObjectMap.keySet()) {
                     if (key.getLocalId().equalsIgnoreCase(id)) {
                         return key.getBoClass();
                     }
                 }
                 
                 return null;
             }
         }).when(businessObjectManager).getType(anyString());

         doAnswer(new Answer<BusinessObject>() {

            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected three arguments: the local id of the business object to be retrieved," +
                        " the instance of the business object, and the class of the business object", args);
                                assertEquals("Expected three arguments: the class of the business object to be retrieved," +
                                        " the instance of the business object, and the class of the business object",
                                        3, args.length);
                assertTrue("Expected argument to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject",
                        args[1] instanceof BusinessObject);
                assertTrue("Expected argument to be of type BusinessObject class",
                        args[2] instanceof Class);
                String id = (String) args[0];
                Key key = new Key(id, (Class)args[2]);
                BusinessObject bo = (BusinessObject) args[1];
                businessObjectMap.put(key, bo);
                return null;
            }
         }).when(businessObjectManager).add(anyString(), any(BusinessObject.class), any(Class.class));

         doAnswer(new Answer<BusinessObject>() {
            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {

                Object[] args = invocation.getArguments();
                assertNotNull("Expected three arguments: the local id of the business object to be retrieved," +
                        " the instance of the business object, and the class of the business object", args);
                                assertEquals("Expected three arguments: the class of the business object to be retrieved," +
                                        " the instance of the business object, and the class of the business object",
                                        3, args.length);
                assertTrue("Expected argument to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject",
                        args[1] instanceof BusinessObject);
                assertTrue("Expected argument to be of type BusinessObject class",
                        args[2] instanceof Class);
                String id = (String) args[0];
                Key key = new Key(id, (Class)args[2]);
                BusinessObject bo = (BusinessObject) args[1];
                if (businessObjectMap.get(key) == null) {
                    throw new NonexistentBusinessObjectException("Business object with type " + args[2] + " and " +
                            "localID \"" + id + "\" could not be found. Cannot update nonexistent business object.");
                }
                businessObjectMap.put(key, bo);
                return null;
            }
         }).when(businessObjectManager).update(anyString(), any(BusinessObject.class), any(Class.class));
         
         initializeStrings();
         addAttributeSets();
         addFileAttributeSet();
         
         state = mock(IngestWorkflowState.class);
         when(state.getEventManager()).thenReturn(eventManager);
         when(state.getPackage()).thenReturn(pkg);
         when(state.getIngestUserId()).thenReturn(ingestUser);
         when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
         when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
    }


    /**
     * Verifies that an exception is thrown if there are AttributeSets representing business
     * objects, but the objects are not created or able to be updated
     * @throws StatefulIngestServiceException
     */
    @Test(expected = IllegalStateException.class)
    public void testFailBadBusinessObjectManager() throws StatefulIngestServiceException {       
        when(state.getBusinessObjectManager()).thenReturn(null);

        businessObjectBuilder.execute("businessObjectBuilder:1", state);
    }

    /**
     * Simulates an ASM with no AttributeSets representing business objects
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testBadAttributeSetManager() throws StatefulIngestServiceException {
        AttributeSetManager badAttributeSetManager = mock(AttributeSetManager.class);
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);

        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        assertEquals(1, events.size());
        assertEquals(Package.Events.BUSINESS_OBJECT_BUILT, events.get(0).getEventType());
        assertEquals(0, events.get(0).getTargets().size());
        assertEquals(BusinessObjectBuilder.SUCCESS_EVENT_DETAIL, events.get(0).getDetail());
        assertEquals(BusinessObjectBuilder.SUCCESS_EVENT_OUTCOME + "0 objects created.", events.get(0).getOutcome());
    }

    /**
     * Verify that the correct event is emitted upon success.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testSuccessEvent() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        assertEquals(1, events.size());
        assertEquals(Package.Events.BUSINESS_OBJECT_BUILT, events.get(0).getEventType());
        assertEquals(13, events.get(0).getTargets().size());
        assertEquals(BusinessObjectBuilder.SUCCESS_EVENT_DETAIL, events.get(0).getDetail());
        assertEquals(BusinessObjectBuilder.SUCCESS_EVENT_OUTCOME + "13 objects created.", events.get(0).getOutcome());
    }

    /**
     * Verifies that the project id is set on the collection
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testCollectionWithParentProject() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        Collection collection = (Collection) businessObjectManager.get(collectionName, Collection.class);
        assertNotNull(collection);
        
        assertNotNull(collection.getParentProjectId());
        assertEquals(externalProjectId, collection.getParentProjectId());
    }
    /**
     *  Verify that the collection business object is created for the corresponding attribute set.  We check that
     *  relationships kept on the business object are present.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testCollectionBuild() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        Collection collection = (Collection) businessObjectManager.get(collectionName, Collection.class);
        Collection subcollection =  (Collection) businessObjectManager.get(subCollectionName, Collection.class);
        Collection aggregatedCollection = (Collection) businessObjectManager.get(aggregatedCollectionName, Collection.class);
        
        assertNotNull(collection);
        assertNotNull(subcollection);
        assertNotNull(aggregatedCollection);

        assertEquals(collectionName, collection.getTitle());
        assertEquals(collectionSummary, collection.getSummary());
        assertEquals(1, collection.getCreators().size());
        assertEquals(personName1, collection.getCreators().get(0));
        assertEquals(1, collection.getChildrenIds().size());

        assertTrue(collection.getChildrenIds().contains(aggregatedCollection.getId()));
        assertEquals(publishDate, collection.getPublicationDate());
        
        assertEquals(collection.getId(), subcollection.getParentId());
        assertEquals(collection.getId(), aggregatedCollection.getParentId());
        assertNull(aggregatedCollection.getParentProjectId());

        //Check the data item parent was correctly set
        DataItem dataItem = (DataItem) businessObjectManager.get(dataItemOneName, DataItem.class);
        assertNotNull(dataItem);
        assertEquals(collection.getId(), dataItem.getParentId());
    }

    /**
     *  Verify that the  subcollection business object is created for the corresponding attribute set.  We check that
     *  relationships kept on the business object are present.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testSubcollectionBuild() throws StatefulIngestServiceException{
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        Collection subcollection =  (Collection) businessObjectManager.get(subCollectionName, Collection.class);
        Collection collection = (Collection) businessObjectManager.get(collectionName, Collection.class);
        assertEquals(subCollectionName, subcollection.getTitle());
        assertEquals(subCollectionSummary, subcollection.getSummary());
        assertEquals(1, subcollection.getCreators().size());
        assertEquals(personName2, subcollection.getCreators().get(0));
        assertEquals(0, subcollection.getChildrenIds().size());
        assertEquals(publishDate, subcollection.getPublicationDate());
        assertEquals(collection.getId(), subcollection.getParentId());
    }
    
    @Test
    public void testCollectionIsPartOfProject() throws StatefulIngestServiceException {
        AttributeSet collectionIsPartOfProject = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        collectionIsPartOfProject.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, "collectionIsPartOfExternalProject"));
        collectionIsPartOfProject.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, "summary"));
        collectionIsPartOfProject.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_IS_PART_OF_COLLECTION, "String", externalProjectId));
        collectionIsPartOfProject.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, "collectionIsPartOfExternalProject"));
        attributeSetManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + "collectionIsPartOfExternalProject", collectionIsPartOfProject);
        
        businessObjectBuilder.execute("businessObjectBuilder:1", state);
        
        Collection collection = (Collection) businessObjectManager.get("collectionIsPartOfExternalProject", Collection.class);
        assertEquals(externalProjectId, collection.getParentProjectId());
        assertNull(collection.getParentId());
    }

    /**
     *  Verify that the  data item  business object is created for the corresponding attribute set.  We check that
     *  relationships kept on the business object are present.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testDataItemOneBuildWithMultipleDataFiles() throws StatefulIngestServiceException{
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        DataItem dataItem = (DataItem) businessObjectManager.get(dataItemOneName, DataItem.class);
        assertNotNull(dataItem);
        assertEquals(dataItemOneName, dataItem.getName());
        assertEquals(dataItemOneDescription, dataItem.getDescription());
        assertEquals(ingestUser, dataItem.getDepositorId());
        assertNotNull(dataItem.getDepositDate());
        assertEquals(2, dataItem.getFiles().size());

        DataFile dataFileOne = (DataFile) businessObjectManager.get(dataFileThreeUri, DataFile.class);
        assertNotNull(dataFileOne);
        DataFile dataFileTwo = (DataFile) businessObjectManager.get(dataFileTwoUri, DataFile.class);
        assertNotNull(dataFileTwo);

        assertTrue(dataItem.getFiles().contains(dataFileOne));
        assertTrue(dataItem.getFiles().contains(dataFileTwo));
        
        assertEquals(dataItem.getId(), dataFileOne.getParentId());
        assertEquals(dataItem.getId(), dataFileTwo.getParentId());

    }

    @Test
    public void testDataItemWithExternalParent() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);
        
        DataItem dataItem = (DataItem) businessObjectManager.get(dataItemTwoName, DataItem.class);
        assertNotNull(dataItem);
        
        assertEquals(externalCollectionId, dataItem.getParentId());        
    }
    
    /**
     *  Verify that the  data file  business object is created for the corresponding attribute set.
     *  Case: detected and asserted format exist and matching.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testDataFileBuild() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        //Data file with detected format
        DataFile dataFile = (DataFile) businessObjectManager.get(dataFileTwoUri, DataFile.class);
        assertEquals(dataFile.getName(), dataFileTwoName);
        assertEquals(pronomFormatUri, dataFile.getFormat());
        assertEquals(fileTwoSource, dataFile.getSource());
        assertEquals(dataFileTwoSize, dataFile.getSize());
    }
    /**
     *  Verify that the  data file  business object is created for the corresponding attribute set.
     *  Case: asserted format exists, detected format missing.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testDataFileBuildMissingDetectedFormat() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        //DataFile with missing detected format
        DataFile dataFile = (DataFile) businessObjectManager.get(dataFileThreeUri, DataFile.class);
        assertEquals(dataFile.getName(), dataFileThreeName);
        assertEquals(assertedDataFileFormat, dataFile.getFormat());
        assertEquals(fileThreeSource, dataFile.getSource());
        assertEquals(dataFileThreeSize, dataFile.getSize());
    }
    
    /**
     *  Verify that the  data file  business object is created for the corresponding attribute set.
     *  Case: detected and asserted format exists and not matching.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testDataFileBuildNonMatchingFormats() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        DataFile dataFile = (DataFile) businessObjectManager.get(dataFileFourUri, DataFile.class);
        assertEquals(dataFile.getName(), dataFileFourName);
        assertEquals(pronomFormatUri, dataFile.getFormat());
        assertEquals(fileFourSource, dataFile.getSource());
        assertEquals(dataFileFourSize, dataFile.getSize());
    }

    /**
     *  Verify that the  data item  business object is created for the corresponding attribute set.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testMetaDataFileBuild() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        MetadataFile metadataFile = (MetadataFile) businessObjectManager.get(metadataFileTwoUri, MetadataFile.class);
        assertEquals(metadataFileTwoName, metadataFile.getName());
        assertEquals(metadataFileMimeType, metadataFile.getFormat());
        assertEquals(metadataFileTwoSource, metadataFile.getSource());
        assertEquals(metadataFileTwoSize, metadataFile.getSize());
        Collection collection = (Collection) businessObjectManager.get(collectionName, Collection.class);
        assertEquals(collection.getId(), metadataFile.getParentId());

        metadataFile = (MetadataFile) businessObjectManager.get(metadataFileOneUri, MetadataFile.class);
        assertEquals(metadataFileOneName, metadataFile.getName());
        assertEquals(metadataFileMimeType, metadataFile.getFormat());
        assertEquals(metadataFileOneSource, metadataFile.getSource());
        assertEquals(metadataFileOneSize, metadataFile.getSize());
        MetadataFile metadataFile1 = (MetadataFile) businessObjectManager.get(metadataFileTwoUri, MetadataFile.class);
        assertEquals(metadataFile1.getId(), metadataFile.getParentId());

        metadataFile = (MetadataFile) businessObjectManager.get(metadataFileThreeUri, MetadataFile.class);
        assertEquals(metadataFileThreeName, metadataFile.getName());
        assertEquals(metadataFileMimeType, metadataFile.getFormat());
        assertEquals(metadataFileThreeSource, metadataFile.getSource());
        assertEquals(metadataFileThreeSize, metadataFile.getSize());
        DataItem dataItem = (DataItem) businessObjectManager.get(dataItemOneName, DataItem.class);
        assertEquals(dataItem.getId(), metadataFile.getParentId());
    }

    @Test (expected = StatefulIngestServiceException.class)
    public void testHandlingMalformedFileURIInOREReMFileAS() throws StatefulIngestServiceException {
        AttributeSet fileOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);

        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileOneName));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, dataFileOneUri));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileFormat));
        fileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileOneUri)) ;
        attributeSetManager.addAttributeSet(fileOneAttributeSet.getName() + "_" + dataFileOneName, fileOneAttributeSet);

        businessObjectBuilder.execute("businessObjectBuilder:1", state);

    }

    /**
     * Tests that the key and the resourceId can be completely unrelated
     *
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testKeyAndResourceIdCanBeUnrelated() throws StatefulIngestServiceException {
        businessObjectBuilder.execute("businessObjectBuilder:1", state);

        DataFile dataFile = (DataFile) businessObjectManager.get(randomResourceId, DataFile.class);
        assertEquals(dataFile.getName(), randomFileName);
    }

    private void initializeStrings(){
        try {    
            File payloadFileBaseDir =  new File( pkg.getSerialization().getExtractDir(),
                      pkg.getSerialization().getBaseDir().getPath());
            
            File dataDir = new File(payloadFileBaseDir, "data");
            
            String dataFileOnePayloadPath = new File("data", dataFileOneName).getPath(); 
            File file1 = new File(dataDir, dataFileOneName);
            file1FileASKey = file1.getPath();  
            dataFileOneUri = "file:///lkfjsdlkfj#$!#@" + bagName + "/" + dataFileOnePayloadPath.replace("\\", "/");
    
            String dataFileTwoPayloadPath = new File("data", dataFileTwoName).getPath();
            File file2 = new File(dataDir, dataFileTwoName);
            file2FileASKey = file2.getPath();
            fileTwoSource = file2.toURI().toURL().toExternalForm();
            dataFileTwoUri = "file:///" + bagName + "/" + dataFileTwoPayloadPath.replace("\\", "/");
            
            String dataFileThreePayloadPath = new File("data", dataFileThreeName).getPath();
            File file3 = new File(dataDir, dataFileThreeName);
            file3FileASKey = file3.getPath();
            fileThreeSource = file3.toURI().toURL().toExternalForm();
            dataFileThreeUri = "file:///" + bagName + "/" + dataFileThreePayloadPath.replace("\\", "/");
            
            String dataFileFourPayloadPath = new File("data", dataFileFourName).getPath();
            File file4 = new File(dataDir, dataFileFourName);
            file4FileASKey = file4.getPath();
            fileFourSource = file4.toURI().toURL().toExternalForm();
            dataFileFourUri = "file:///" + bagName + "/" + dataFileFourPayloadPath.replace("\\", "/");
            
            String metadataFileOnePayloadPath = new File("data", metadataFileOneName).getPath();
            File metadataFile1 =  new File(dataDir, metadataFileOneName);
            metadataFile1FileASKey = metadataFile1.getPath();
            metadataFileOneSource = metadataFile1.toURI().toURL().toExternalForm();
            metadataFileOneUri = "file:///" + bagName + "/" + metadataFileOnePayloadPath.replace("\\", "/");
            
            String metadataFileTwoPayloadPath = new File("data", metadataFileTwoName).getPath();
            File metadataFile2 = new File(dataDir, metadataFileTwoName);
            metadataFile2FileASKey = metadataFile2.getPath();
            metadataFileTwoSource = metadataFile2.toURI().toURL().toExternalForm();
            metadataFileTwoUri = "file:///" + bagName + "/" + metadataFileTwoPayloadPath.replace("\\", "/");
            
            String metadataFileThreePayloadPath = new File("data", metadataFileThreeName).getPath();
            File metadataFile3 = new File(dataDir, metadataFileThreeName);
            metadataFile3FileASKey = metadataFile3.getPath();
            metadataFileThreeSource = metadataFile3.toURI().toURL().toExternalForm();
            metadataFileThreeUri = "file:///" + bagName + "/" + metadataFileThreePayloadPath.replace("\\", "/");
        } catch( Exception e) {
            throw new RuntimeException("Failed to generate file sources", e);
        }

        personName1 = new PersonName();
        personName1.setPrefixes("Dr.");
        personName1.setGivenNames("Robert");
        personName1.setMiddleNames("Moses");
        personName1.setFamilyNames("Kildare");

        personName2 = new PersonName();
        personName2.setFamilyNames(creator2);
    }

    private void addAttributeSets(){
        AttributeSet projectAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_PROJECT);
        AttributeSet collectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        AttributeSet subCollectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        AttributeSet dataItemOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        AttributeSet dataItemTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        AttributeSet dataItemThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        AttributeSet fileTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet fileThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet fileFourAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet metadataFileOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet metadataFileTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet metadataFileThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet randomKeyAttributeSet = new  AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        AttributeSet aggregatedCollectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        
        String creationDate =  "2013-06-10T01:55:18Z";

        String creator1 =   "Dr. Robert Moses Kildare";

        String randomKey = "rybdrVB3456b54bMNWb4656WN$b66N34b63bv365b6N&WB";

        expectedKeys = new String[]{
                collectionAttributeSet.getName() + "_" + collectionName,
                subCollectionAttributeSet.getName() + "_" + subCollectionName,
                dataItemOneAttributeSet.getName() + "_" + dataItemOneName,
                dataItemTwoAttributeSet.getName() + "_" + dataItemTwoName,
                dataItemThreeAttributeSet.getName() + "_" + dataItemThreeName,
                fileTwoAttributeSet.getName() + "_" + dataFileTwoName,
                fileThreeAttributeSet.getName() + "_" + dataFileThreeName,
                fileFourAttributeSet.getName() + "_" + dataFileFourName,
                metadataFileOneAttributeSet.getName() + "_" + metadataFileOneName,
                metadataFileTwoAttributeSet.getName() + "_" + metadataFileTwoName,
                metadataFileThreeAttributeSet.getName() + "_" + metadataFileThreeName,
                randomKey,
                aggregatedCollectionAttributeSet.getName() + "_" + aggregatedCollectionName,
        };

        projectAttributeSet.getAttributes().add(new AttributeImpl(Metadata.PROJECT_RESOURCEID, AttributeValueType.STRING, externalProjectId));
        projectAttributeSet.getAttributes().add(new AttributeImpl(Metadata.PROJECT_AGGREGATES_COLLECTION, AttributeValueType.STRING, collectionName));
        attributeSetManager.addAttributeSet( projectAttributeSet.getName() + "_" + externalProjectId, projectAttributeSet);
        
        //Create attribute sets and put them in the manager
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, collectionName));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, collectionSummary));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, creator1));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_AGGREGATES_COLLECTION, AttributeValueType.STRING, aggregatedCollectionName));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_AGGREGATES_DATAITEM, AttributeValueType.STRING, dataItemOneName));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATED, "DateTime", creationDate));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, collectionName));
        attributeSetManager.addAttributeSet(expectedKeys[0], collectionAttributeSet);
        
        aggregatedCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, aggregatedCollectionName));
        aggregatedCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, collectionSummary));
        aggregatedCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, creator1));
        aggregatedCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATED, "DateTime", creationDate));
        aggregatedCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_IS_PART_OF_COLLECTION, "String", externalProjectId));
        aggregatedCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, aggregatedCollectionName));
        attributeSetManager.addAttributeSet(expectedKeys[12], aggregatedCollectionAttributeSet);

        subCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, subCollectionName));
        subCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, subCollectionSummary));
        subCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, creator2));
        subCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_IS_PART_OF_COLLECTION, AttributeValueType.STRING, collectionName));
        subCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATED, "DateTime", creationDate));
        subCollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, subCollectionName));
        attributeSetManager.addAttributeSet(expectedKeys[1], subCollectionAttributeSet);

        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemOneName));
        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemOneDescription));
        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileThreeUri));
        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileTwoUri));
        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[2], dataItemOneAttributeSet);

        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemTwoName));
        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemTwoDescription));
        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileFourUri));
        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemTwoName));
        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_IS_PART_OF_COLLECTION, AttributeValueType.STRING, externalCollectionId));
        attributeSetManager.addAttributeSet(expectedKeys[3], dataItemTwoAttributeSet);

        dataItemThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemThreeName));
        dataItemThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemThreeDescription));
        dataItemThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileFourUri));
        dataItemThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemThreeName));
        attributeSetManager.addAttributeSet(expectedKeys[4], dataItemThreeAttributeSet);

        fileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, dataFileTwoName));
        fileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, dataFileTwoUri));
        fileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, AttributeValueType.STRING, dataFileFormat));
        fileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileMimeType));
        fileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileTwoUri));
        attributeSetManager.addAttributeSet(expectedKeys[5], fileTwoAttributeSet);

        fileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, dataFileThreeName));
        fileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, dataFileThreeUri));
        fileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, AttributeValueType.STRING, dataFileMimeType));
        fileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, assertedDataFileFormat));
        fileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileThreeUri));
        attributeSetManager.addAttributeSet(expectedKeys[6], fileThreeAttributeSet);

        fileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, dataFileFourName));
        fileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, dataFileFourUri));
        fileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, AttributeValueType.STRING, assertedDataFileFormat));
        fileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileMimeType));
        fileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileFourUri));
        attributeSetManager.addAttributeSet(expectedKeys[7], fileFourAttributeSet);

        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, metadataFileOneName));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFileOneUri));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, AttributeValueType.STRING, metadataFileFormat));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileMimeType));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileOneUri));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, metadataFileTwoUri));
        attributeSetManager.addAttributeSet(expectedKeys[8], metadataFileOneAttributeSet);

        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, metadataFileTwoName));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFileTwoUri));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, AttributeValueType.STRING, metadataFileFormat));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileMimeType));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileTwoUri));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, collectionName));
        attributeSetManager.addAttributeSet(expectedKeys[9], metadataFileTwoAttributeSet);

        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, metadataFileThreeName));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFileThreeUri));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, AttributeValueType.STRING, metadataFileFormat));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileMimeType));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileThreeUri));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[10], metadataFileThreeAttributeSet);

        randomKeyAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, randomFileName));
        randomKeyAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_CONFORMS_TO, AttributeValueType.STRING, dataFileFormat));
        randomKeyAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileMimeType));
        randomKeyAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, randomResourceId));

        attributeSetManager.addAttributeSet(expectedKeys[11], randomKeyAttributeSet);
    }

    private void addFileAttributeSet() {

        AttributeSet file1AS = new AttributeSetImpl(AttributeSetName.FILE);
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileOneSize)));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is file one"));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileFormat));

        attributeSetManager.addAttributeSet(file1FileASKey, file1AS);

        AttributeSet file2AS = new AttributeSetImpl(AttributeSetName.FILE);
        file2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileTwoSize)));
        file2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is file two"));
        file2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileFormat));
        attributeSetManager.addAttributeSet(file2FileASKey, file2AS);

        AttributeSet file3AS = new AttributeSetImpl(AttributeSetName.FILE);
        file3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileThreeSize)));
        file3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is file three"));
        attributeSetManager.addAttributeSet(file3FileASKey, file3AS);

        AttributeSet file4AS = new AttributeSetImpl(AttributeSetName.FILE);
        file4AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileFourSize)));
        file4AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is file four"));
        file4AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileFormat));
        attributeSetManager.addAttributeSet(file4FileASKey, file4AS);

        AttributeSet metadataFile1AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(metadataFileOneSize)));
        metadataFile1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is metadata file one"));
        metadataFile1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileFormat));
        attributeSetManager.addAttributeSet(metadataFile1FileASKey, metadataFile1AS);

        AttributeSet metadataFile2AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG,Long.toString(metadataFileTwoSize)));
        metadataFile2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is metadata file two"));
        metadataFile2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileFormat));
        attributeSetManager.addAttributeSet(metadataFile2FileASKey, metadataFile2AS);

        AttributeSet metadataFile3AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG,Long.toString(metadataFileThreeSize)));
        metadataFile3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is metadata file three"));
        metadataFile3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileFormat));
        attributeSetManager.addAttributeSet(metadataFile3FileASKey, metadataFile3AS);
    }

    /**
     * Test to make sure that bytestreams aggregated for a Project are built into metadata files for that project.
     */
    @Test
    public void testProjectsBytestreamsBecomeMdF() throws StatefulIngestServiceException {
        String projectResourceId = "urn:dc:project";
        String collectionResourceId = "urn:dc:collection";
        String collectionTitle = "collection title";
        String fileTitle = "someFile.txt";
        String fileResourceId = "file:///" + bagName + "/data/" + fileTitle;
        String fileFormat = "text/plain";
        String fileSize = "33333";

        //Set up AS for project
        AttributeSet projectREMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_PROJECT);
        projectREMAS.getAttributes().add(new AttributeImpl(Metadata.PROJECT_RESOURCEID, AttributeValueType.STRING, projectResourceId));
        projectREMAS.getAttributes().add(new AttributeImpl(Metadata.PROJECT_AGGREGATES_COLLECTION, AttributeValueType.STRING, collectionResourceId));
        //*****Set the project up to aggregates stand alone files.
        projectREMAS.getAttributes().add(new AttributeImpl(Metadata.PROJECT_AGGREGATES_FILE, AttributeValueType.STRING, fileResourceId));
        attributeSetManager.addAttributeSet(projectREMAS.getName() + "_" + projectResourceId, projectREMAS);

        //Set up AS for collection
        AttributeSet collectionREMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, collectionResourceId));
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, "creator name"));
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, collectionTitle));
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, "collection description"));
        attributeSetManager.addAttributeSet(collectionREMAS.getName() + "_" + collectionTitle, collectionREMAS);

        File payloadFileBaseDir =  new File( pkg.getSerialization().getExtractDir(), pkg.getSerialization().getBaseDir().getPath());
        File dataDir = new File(payloadFileBaseDir, "data");

        File file = new File(dataDir, fileTitle);
        String fileFileASKey = file.getPath();

        //Set up AS for file
        AttributeSet fileREMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        fileREMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, fileResourceId));
        fileREMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, fileTitle));
        attributeSetManager.addAttributeSet(fileREMAS.getName() + "_" + fileTitle, fileREMAS);

        //Set up FILE-AS for file
        AttributeSet fileFILEAS = new AttributeSetImpl(AttributeSetName.FILE);
        DcsFormat projectfileFormat = new DcsFormat();
        projectfileFormat.setFormat(fileFormat);
        projectfileFormat.setSchemeUri("http://www.iana.org/assignments/media-types/");
        projectfileFormat.setName("Plain text");
        fileFILEAS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, projectfileFormat.toString()));
        fileFILEAS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, fileSize));
        attributeSetManager.addAttributeSet(fileFileASKey, fileFILEAS);

        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getIngestUserId()).thenReturn(ingestUser);

        businessObjectBuilder.execute("depositId", state);

        //Make sure that a metadata file was created from the project-aggregated file. Verify that file was built correctly
        MetadataFile resultedFile = (MetadataFile) businessObjectMap.get(new Key(fileResourceId,  MetadataFile.class));
        assertNotNull(resultedFile);
        assertEquals(fileTitle, resultedFile.getName());
        assertEquals(fileFormat, resultedFile.getFormat());
        assertEquals(Long.parseLong(fileSize), resultedFile.getSize());
        assertEquals(projectResourceId, resultedFile.getParentId());
    }

    /**
     * Test to make sure that bytestreams aggregated by collection are built into metadata files for that collection.
     */
    @Test
    public void testCollectionsBytestreamsBecomeMdF() throws StatefulIngestServiceException {
        String projectResourceId = "urn:dc:project";
        String collectionResourceId = "urn:dc:collection";
        String collectionTitle = "collection title";
        String fileTitle = "someFile.txt";
        String fileResourceId = "file:///" + bagName + "/data/" + fileTitle;
        String fileFormat = "text/plain";
        String fileSize = "33333";

        //Set up AS for project
        AttributeSet projectREMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_PROJECT);
        projectREMAS.getAttributes().add(new AttributeImpl(Metadata.PROJECT_RESOURCEID, AttributeValueType.STRING, projectResourceId));
        projectREMAS.getAttributes().add(new AttributeImpl(Metadata.PROJECT_AGGREGATES_COLLECTION, AttributeValueType.STRING, collectionResourceId));
        attributeSetManager.addAttributeSet(projectREMAS.getName() + "_" + projectResourceId, projectREMAS);

        //Set up AS for collection
        AttributeSet collectionREMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, collectionResourceId));
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, "creator name"));
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, collectionTitle));
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, "collection description"));
        collectionREMAS.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_AGGREGATES_FILE, AttributeValueType.STRING, fileResourceId));

        attributeSetManager.addAttributeSet(collectionREMAS.getName() + "_" + collectionTitle, collectionREMAS);

        File payloadFileBaseDir =  new File( pkg.getSerialization().getExtractDir(), pkg.getSerialization().getBaseDir().getPath());
        File dataDir = new File(payloadFileBaseDir, "data");

        File file = new File(dataDir, fileTitle);
        String fileFileASKey = file.getPath();

        //Set up AS for file
        AttributeSet fileREMAS = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        fileREMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, fileResourceId));
        fileREMAS.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, fileTitle));
        attributeSetManager.addAttributeSet(fileREMAS.getName() + "_" + fileTitle, fileREMAS);

        //Set up FILE-AS for file
        AttributeSet fileFILEAS = new AttributeSetImpl(AttributeSetName.FILE);
        DcsFormat projectfileFormat = new DcsFormat();
        projectfileFormat.setFormat(fileFormat);
        projectfileFormat.setSchemeUri("http://www.iana.org/assignments/media-types/");
        projectfileFormat.setName("Plain text");
        fileFILEAS.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, projectfileFormat.toString()));
        fileFILEAS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, fileSize));
        attributeSetManager.addAttributeSet(fileFileASKey, fileFILEAS);

        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getIngestUserId()).thenReturn(ingestUser);

        businessObjectBuilder.execute("depositId", state);

        //Get the collection built by BOB
        Collection resultedCollection = (Collection) businessObjectMap.get(new Key(collectionResourceId, Collection.class));

        //Make sure that a metadata file was created from the project-aggregated file. Verify that file was built correctly
        MetadataFile resultedFile = (MetadataFile) businessObjectMap.get(new Key(fileResourceId,  MetadataFile.class));
        assertNotNull(resultedFile);
        assertEquals(fileTitle, resultedFile.getName());
        assertEquals(fileFormat, resultedFile.getFormat());
        assertEquals(Long.parseLong(fileSize), resultedFile.getSize());
        assertEquals(resultedCollection.getId(), resultedFile.getParentId());
    }
    
    private class Key {
        private String localId;
        private Class boClass;

        public <T extends BusinessObject> Key (String localId, Class<T> boClass) {
            this.localId = localId;
            this.boClass = boClass;
        }

        private String getLocalId() {
            return this.localId;
        }

        private <T extends BusinessObject> Class<T> getBoClass() {
            return this.boClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;

            Key key = (Key) o;

            if (boClass != null ? !boClass.equals(key.boClass) : key.boClass != null) return false;
            if (localId != null ? !localId.equals(key.localId) : key.localId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = localId != null ? localId.hashCode() : 0;
            result = 31 * result + (boClass != null ? boClass.hashCode() : 0);
            return result;
        }
    }


}