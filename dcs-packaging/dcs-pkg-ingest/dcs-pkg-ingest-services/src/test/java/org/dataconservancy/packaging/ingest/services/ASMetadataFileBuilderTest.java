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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.impl.IdentifierImpl;
import org.dataconservancy.dcs.ingest.EventManager;
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
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.builder.AttributeSetBuilder;
import org.dataconservancy.packaging.model.builder.xstream.XstreamAttributeSetBuilder;
import org.dataconservancy.packaging.model.builder.xstream.XstreamAttributeSetFactory;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.Pair;
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
 * Test class for ASMetadataFileBuilder
 */
public class ASMetadataFileBuilderTest {


    private IngestWorkflowState state;
    private AttributeSetManager attributeSetManager;
    private BusinessObjectManager businessObjectManager;
    private EventManager eventManager;
    private ASMetadataFileBuilder underTest;

    private List<DcsEvent> events;
    private String[] expectedKeys;
    private Map<String, AttributeSet> attributeMap;
    private Map<String, BusinessObject> businessObjectMap;
    private IdService idService;
    private org.dataconservancy.packaging.model.Package pkg;
    private int i;//counter for idService

    private DateTime publishDate = new DateTime(2013, 6, 10, 1, 55, 18);
    private String collectionName;
    private String subcollectionName;
    private String dataItemOneName;
    private String dataItemTwoName;
    private String dataItemThreeName;
    private String dataFileOneName;
    private String dataFileTwoName;
    private String dataFileThreeName;
    private String dataFileFourName;
    private String metadataFileOneName;
    private String metadataFileTwoName;
    private String metadataFileThreeName;

    private String collectionSummary;
    private String subcollectionSummary;
    private String dataItemOneDescription;
    private String dataItemTwoDescription;
    private String dataItemThreeDescription;

    private PersonName personName1;
    private PersonName personName2;
    private long dataFileOneSize = 132465798L;
    private long dataFileTwoSize = 564321321L;
    private long dataFileThreeSize = 556323213L;
    private long dataFileFourSize = 78765465461L;
    private long metadataFileOneSize = 674987654L;
    private long metadataFileTwoSize = 564798765L;
    private long metadataFileThreeSize = 658798465L;


    private String file1FileASKey;
    private String file2FileASKey;
    private String file3FileASKey;
    private String file4FileASKey;
    private String metadataFile1FileASKey;
    private String metadataFile2FileASKey;
    private String metadataFile3FileASKey;
    private String file1URI;
    private String file2URI;
    private String file3URI;
    private String file4URI;
    private String metadataFile1URI;
    private String metadataFile2URI;
    private String metadataFile3URI;

    private String dataFileFormat;
    private String metadataFileFormat;

    private String creator2 = "TheJohns Hopkins University";

    private String dataFileOnePayloadPath;
    private String dataFileTwoPayloadPath;
    private String dataFileThreePayloadPath;
    private String dataFileFourPayloadPath;
    private String metadataFileOnePayloadPath;
    private String metadataFileTwoPayloadPath;
    private String metadataFileThreePayloadPath;


    private Collection collection;
    private Collection subcollection;
    private DataItem dataItemOne;
    private DataItem dataItemTwo;
    private DataItem dataItemThree;
    private DataFile dataFileOne;
    private DataFile dataFileTwo;
    private DataFile dataFileThree;
    private DataFile dataFileFour;
    private MetadataFile metadataFileOne;
    private MetadataFile metadataFileTwo;
    private MetadataFile metadataFileThree;

    private String depositId = "ASMetadataFileBuilder-1";
    private String bagName = "ASMetadata-Bag";

    private AttributeSet collectionAttributeSet;
    private AttributeSet subcollectionAttributeSet;
    private AttributeSet dataItemOneAttributeSet;
    private AttributeSet dataItemTwoAttributeSet;
    private AttributeSet dataItemThreeAttributeSet;
    private AttributeSet dataFileOneAttributeSet;
    private AttributeSet dataFileTwoAttributeSet;
    private AttributeSet dataFileThreeAttributeSet;
    private AttributeSet dataFileFourAttributeSet;
    private AttributeSet metadataFileOneAttributeSet;
    private AttributeSet metadataFileTwoAttributeSet;
    private AttributeSet metadataFileThreeAttributeSet;

    private AttributeSet file1AS;
    private AttributeSet file2AS;
    private AttributeSet file3AS;
    private AttributeSet file4AS;

    private AttributeSet metadataFile1AS;
    private AttributeSet metadataFile2AS;
    private AttributeSet metadataFile3AS;


    private Pair<String, Checksum> dataFileOnePair;
    private Pair<String, Checksum> dataFileTwoPair;
    private Pair<String, Checksum> dataFileThreePair;
    private Pair<String, Checksum> dataFileFourPair;
    private Pair<String, Checksum> metadataFileOnePair;
    private Pair<String, Checksum> metadataFileTwoPair;
    private Pair<String, Checksum> metadataFileThreePair;
    
    private File extractDir;
    private File baseDir;

    @Before
    public void setup(){
        underTest = new ASMetadataFileBuilder();

        extractDir = new File("/tmp/package-extraction");
        extractDir.mkdir();
        extractDir.deleteOnExit();

        baseDir = new File(depositId, bagName);
        baseDir.mkdir();
        baseDir.deleteOnExit();

        PackageDescription description = new DescriptionImpl();
        PackageSerialization serialization = new SerializationImpl();
        serialization.setExtractDir(extractDir);
        serialization.setBaseDir(baseDir);
        pkg = new PackageImpl(description, serialization);

        //mocked services and managers
        attributeSetManager = mock(AttributeSetManager.class);
        businessObjectManager= mock(BusinessObjectManager.class);
        eventManager = mock(EventManager.class);
        idService = mock(IdService.class);

        underTest.setIdService(idService);

        //persistence for these
        attributeMap = new HashMap<String, AttributeSet>();
        businessObjectMap = new HashMap<String,BusinessObject>();
        events = new ArrayList<DcsEvent>();

        setupMockServices();
        initializeStrings();
        addAttributeSets();
        addFileAttributeSet();
        addBagitAttributeSet();
        addBusinessObjects();
    }

    /**
     * Tests that a Bagit AttributeSet with the wrong name will cause an exception
     * @throws StatefulIngestServiceException
     */
    @Test(expected = StatefulIngestServiceException.class)
    public void testBadBagitAttributeSetNameFail() throws StatefulIngestServiceException{
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        //Create bad bagit att set name
        AttributeSet badAttributeSet = new AttributeSetImpl("bad attribute set name");
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileOnePair.toString()));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileTwoPair.toString()));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileThreePair.toString()));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileFourPair.toString()));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileOnePair.toString()));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileTwoPair.toString()));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileThreePair.toString()));
        attributeSetManager.updateAttributeSet(AttributeSetName.BAGIT, badAttributeSet);

        underTest.execute(depositId, state);
    }


    /**
     * Tests that a Collection with a missing ResourceId attribute  will cause an exception
     * @throws StatefulIngestServiceException
     */
    @Test(expected = StatefulIngestServiceException.class)
    public void testNoCollectionResourceIdFail() throws StatefulIngestServiceException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

         //Create bad attribute set - no resource id - and put it  in the manager
        AttributeSet badAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, collectionName));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, collectionSummary));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_AGGREGATES_COLLECTION, AttributeValueType.STRING, subcollectionName));

        attributeSetManager.updateAttributeSet(expectedKeys[0], badAttributeSet);

        underTest.execute(depositId, state);
    }


    /**
     * Tests that a DataItem with a missing ResourceId attribute  will cause an exception
     * @throws StatefulIngestServiceException
     */
    @Test(expected = StatefulIngestServiceException.class)
    public void testNoDataItemResourceIdFail() throws StatefulIngestServiceException{
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        //Create bad attribute set - no resource id - and put it  in the manager
        AttributeSet badAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemOneName));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemOneDescription));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_AGGREGATES_COLLECTION, AttributeValueType.STRING, subcollectionName));

        attributeSetManager.updateAttributeSet(expectedKeys[2], badAttributeSet);

        underTest.execute(depositId, state);
    }

    /**
     * Tests that a file (DataFile or MetadataFile) with a missing ResourceId attribute  will cause an exception
     * @throws StatefulIngestServiceException
     */
    @Test(expected = StatefulIngestServiceException.class)
    public void testNoFileResourceIdFail() throws StatefulIngestServiceException{
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        //Create bad attribute set - no resource id - and put it  in the manager
        AttributeSet badAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, dataFileOneName));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, dataFileOnePayloadPath));

        attributeSetManager.updateAttributeSet(expectedKeys[5], badAttributeSet);

        underTest.execute(depositId, state);
    }

    /**
     * Tests that a file (DataFile or MetadataFile) with a missing Path attribute  will cause an exception
     * @throws StatefulIngestServiceException
     */
    @Test(expected = StatefulIngestServiceException.class)
    public void testNoFilePathFail() throws StatefulIngestServiceException{
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        //Create bad attribute set - no resource id - and put it  in the manager
        AttributeSet badAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_TITLE, AttributeValueType.STRING, dataFileOneName));
        badAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, file1URI));

        attributeSetManager.updateAttributeSet(expectedKeys[5], badAttributeSet);

        underTest.execute(depositId, state);
    }


    @Test
    public void testCollectionSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        Collection retrievedCollection = (Collection) businessObjectManager.get(collectionName, Collection.class);

        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
             if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedCollection.getId())){
                 mdf = metadataFile;
                 break;
             }
        }

        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //Collections should have one attribute set
        assertTrue(metadataAttributeSets.contains(collectionAttributeSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testSubCollectionSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);


        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        Collection retrievedSubcollection = (Collection) businessObjectManager.get(subcollectionName, Collection.class);


        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
                    if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedSubcollection.getId())){
                        mdf = metadataFile;
                        break;
                    }
         }

         assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //Collections should have one attribute set
        assertTrue(metadataAttributeSets.contains(subcollectionAttributeSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testDataItemOneSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataItem retrievedDataItem = (DataItem) businessObjectManager.get(dataItemOneName, DataItem.class);

        MetadataFile mdf = null;

        //check the contents of the new MetadataFile
        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedDataItem.getId())){
                mdf = metadataFile;
                break;
            }
        }
        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        //DataItems should have one attribute set
        assertTrue(metadataAttributeSets.contains(dataItemOneAttributeSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testDataItemTwoSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataItem retrievedDataItem = (DataItem) businessObjectManager.get(dataItemTwoName, DataItem.class);

      MetadataFile mdf = null;


       for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
           if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedDataItem.getId())){
               mdf = metadataFile;
                        break;
               }
           }

        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        //DataItems should have one attribute set
        assertTrue(metadataAttributeSets.contains(dataItemTwoAttributeSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testDataItemThreeSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataItem retrievedDataItem = (DataItem) businessObjectManager.get(dataItemThreeName, DataItem.class);

        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedDataItem.getId())){
                mdf = metadataFile;
                break;
             }
         }

        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        //DataItems should have one attribute set
        assertTrue(metadataAttributeSets.contains(dataItemThreeAttributeSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testDataFileOneSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataFile retrievedDataFile = (DataFile) businessObjectManager.get(dataFileOneName, DataFile.class);

        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedDataFile.getId())){
                mdf = metadataFile;
                break;
            }
        }


        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //DataFiles should have three AttributeSets
        assertTrue(metadataAttributeSets.contains(dataFileOneAttributeSet));
        assertTrue(metadataAttributeSets.contains(file1AS));

        AttributeSet bagitSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileOnePair.toString()));

        assertTrue(metadataAttributeSets.contains(bagitSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testDataFileTwoSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataFile retrievedDataFile = (DataFile) businessObjectManager.get(dataFileTwoName, DataFile.class);

        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedDataFile.getId())){
                mdf = metadataFile;
                break;
            }
        }

        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //DataFiles should have three AttributeSets
        assertTrue(metadataAttributeSets.contains(dataFileTwoAttributeSet));
        assertTrue(metadataAttributeSets.contains(file2AS));

        AttributeSet bagitSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileTwoPair.toString()));

        assertTrue(metadataAttributeSets.contains(bagitSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testDataFileThreeSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataFile retrievedDataFile = (DataFile) businessObjectManager.get(dataFileThreeName, DataFile.class);

        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedDataFile.getId())){
                mdf = metadataFile;
                break;
            }
        }

        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //DataFiles should have three AttributeSets
        assertTrue(metadataAttributeSets.contains(dataFileThreeAttributeSet));
        assertTrue(metadataAttributeSets.contains(file3AS));

        AttributeSet bagitSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileThreePair.toString()));

        assertTrue(metadataAttributeSets.contains(bagitSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testDataFileFourSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataFile retrievedDataFile = (DataFile) businessObjectManager.get(dataFileFourName, DataFile.class);

        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedDataFile.getId())){
                mdf = metadataFile;
                break;
            }
        }


        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //DataFiles should have three AttributeSets
        assertTrue(metadataAttributeSets.contains(dataFileFourAttributeSet));
        assertTrue(metadataAttributeSets.contains(file4AS));

        AttributeSet bagitSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileFourPair.toString()));

        assertTrue(metadataAttributeSets.contains(bagitSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testMetadataFileOneSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataFile retrievedMetadataFile = (DataFile) businessObjectManager.get(metadataFileOneName, MetadataFile.class);

        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedMetadataFile.getId())){
                mdf = metadataFile;
                break;
            }
        }


        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //MetadataFiles should have three AttributeSets
        assertTrue(metadataAttributeSets.contains(metadataFileOneAttributeSet));
        assertTrue(metadataAttributeSets.contains(metadataFile1AS));

        AttributeSet bagitSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileOnePair.toString()));

        assertTrue(metadataAttributeSets.contains(bagitSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testMetadataFileTwoSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        DataFile retrievedMetadataFile = (DataFile) businessObjectManager.get(metadataFileTwoName, MetadataFile.class);

        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedMetadataFile.getId())){
                mdf = metadataFile;
                break;
            }
        }

        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //MetadataFiles should have three AttributeSets
        assertTrue(metadataAttributeSets.contains(metadataFileTwoAttributeSet));
        assertTrue(metadataAttributeSets.contains(metadataFile2AS));

        AttributeSet bagitSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileTwoPair.toString()));

        assertTrue(metadataAttributeSets.contains(bagitSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }

    @Test
    public void testMetadataFileThreeSuccess() throws StatefulIngestServiceException, FileNotFoundException, URISyntaxException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);

        //check that we got a new MetadataFile on our business object
        MetadataFile retrievedMetadataFile = (MetadataFile) businessObjectManager.get(metadataFileThreeName, MetadataFile.class);
        MetadataFile mdf = null;

        for(MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)){
            if(metadataFile.getParentId() != null && metadataFile.getParentId().equals(retrievedMetadataFile.getId())){
                mdf = metadataFile;
                break;
            }
        }

        assertNotNull(mdf);

        //check the contents of the new MetadataFile
        FileInputStream fis = new FileInputStream(new URI(mdf.getSource()).getPath());
        AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
        Set<AttributeSet> metadataAttributeSets = builder.buildAttributeSets(fis);

        assertTrue(metadataAttributeSets.size() > 0);

        //MetadataFiles should have three AttributeSets
        assertTrue(metadataAttributeSets.contains(metadataFileThreeAttributeSet));
        assertTrue(metadataAttributeSets.contains(metadataFile3AS));

        AttributeSet bagitSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileThreePair.toString()));

        assertTrue(metadataAttributeSets.contains(bagitSet));

        assertEquals(1, events.size());
        DcsEvent resultEvent = events.iterator().next();
        //12 business objects were set up for this tests. expect 12 metadatafile to be built for these objects.
        assertEquals(12, resultEvent.getTargets().size());
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testBadAttributeSetKeyThrowsException() throws StatefulIngestServiceException {
        pkg.getSerialization().setBaseDir(new File("foo"));
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        underTest.execute(depositId, state);
    }

    private void initializeStrings() {
        collectionName =  "Collection-1";
        subcollectionName = "Collection-2";
        dataItemOneName = "DataItem-1";
        dataItemTwoName = "DataItem-2";
        dataItemThreeName = "DataItem-3";
        dataFileOneName = "DataFile-1.doc";
        dataFileTwoName = "DataFile-2.pdf";
        dataFileThreeName = "DataFile-3.xls";
        dataFileFourName = "DataFile-4.pdf";
        metadataFileOneName = "MetaDataFile-1.txt";
        metadataFileTwoName = "MetaDataFile-2.jpg";
        metadataFileThreeName = "MetaDataFile-3.txt";
        
        dataFileOnePayloadPath = new File("data", dataFileOneName).getPath();
        dataFileTwoPayloadPath = new File("data", dataFileTwoName).getPath();
        dataFileThreePayloadPath = new File("data", dataFileThreeName).getPath();
        dataFileFourPayloadPath = new File("data", dataFileFourName).getPath();
        metadataFileOnePayloadPath = new File("data", metadataFileOneName).getPath();
        metadataFileTwoPayloadPath = new File("data", metadataFileTwoName).getPath();
        metadataFileThreePayloadPath = new File("data", metadataFileThreeName).getPath();

        File payloadFileBaseDir =  new File( pkg.getSerialization().getExtractDir(),
                  pkg.getSerialization().getBaseDir().getPath());
        
        File dataDir = new File(payloadFileBaseDir, "data");
        //AttributeSetKey for File AttributeSet is the path to the file on local system.
        //Test string used here is an example of a typical file path
        file1FileASKey = new File(dataDir, dataFileOneName).getPath();
        file2FileASKey = new File(dataDir, dataFileTwoName).getPath();
        file3FileASKey = new File(dataDir, dataFileThreeName).getPath();
        file4FileASKey = new File(dataDir,dataFileFourName).getPath();
        metadataFile1FileASKey = new File(dataDir, metadataFileOneName).getPath();
        metadataFile2FileASKey = new File(dataDir, metadataFileTwoName).getPath();
        metadataFile3FileASKey = new File(dataDir, metadataFileThreeName).getPath();

        file1URI = "file:///" + bagName + "/" + dataFileOnePayloadPath.replace("\\", "/");
        file2URI = "file:///" + bagName + "/" + dataFileTwoPayloadPath.replace("\\", "/");
        file3URI = "file:///" + bagName + "/" + dataFileThreePayloadPath.replace("\\", "/");
        file4URI = "file:///" + bagName + "/" + dataFileFourPayloadPath.replace("\\", "/");
        metadataFile1URI = "file:///" + bagName + "/" + metadataFileOnePayloadPath.replace("\\", "/");
        metadataFile2URI = "file:///" + bagName + "/" + metadataFileTwoPayloadPath.replace("\\", "/");
        metadataFile3URI = "file:///" + bagName + "/" + metadataFileThreePayloadPath.replace("\\", "/");

        collectionSummary =  "Test-Big-Collection";
        subcollectionSummary = "Test-Subcollection";
        dataItemOneDescription = "Test-Collection-DataItemOne";
        dataItemTwoDescription = "Test-Collection-DataItemTwo";
        dataItemThreeDescription = "Test-Collection-DataItemThree";

        dataFileFormat = "test/format1";
        metadataFileFormat = "test/format2";

        personName1 = new PersonName();
        personName1.setPrefixes("Dr.");
        personName1.setGivenNames("Robert");
        personName1.setMiddleNames("Moses");
        personName1.setFamilyNames("Kildare");

        personName2 = new PersonName();
        personName2.setFamilyNames(creator2);

    }

    private void setupMockServices(){
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
                event.setDate(new DateTime().toString());
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
                String key = (String) args[0];
                return businessObjectMap.get(key);
            }
        }).when(businessObjectManager).get(anyString(), any(Class.class));

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
                assertEquals("Expected one argument: the class of the business objects to be retrieved",
                        1, args.length);
                assertTrue("Expected argument to be of type BusinessObject class",
                        args[0] instanceof Class);

                Set<MetadataFile> metadataFileSet = new HashSet<MetadataFile>();
                Set<DataFile> dataFileSet = new HashSet<DataFile>();
                Set<Collection> collectionSet = new HashSet<Collection>();
                Set<DataItem> dataItemSet = new HashSet<DataItem>();

                for (String key : businessObjectMap.keySet()) {
                    if (businessObjectMap.get(key) instanceof MetadataFile) {
                        metadataFileSet.add((MetadataFile) businessObjectMap.get(key));
                    } else if (businessObjectMap.get(key) instanceof DataFile) {
                        dataFileSet.add((DataFile) businessObjectMap.get(key));
                    } else if (businessObjectMap.get(key) instanceof Collection) {
                        collectionSet.add((Collection) businessObjectMap.get(key));
                    } else if (businessObjectMap.get(key) instanceof DataItem) {
                        dataItemSet.add((DataItem) businessObjectMap.get(key));
                    }
                }

                Set<BusinessObject> result = new HashSet<BusinessObject>();

                if (args[0].equals(MetadataFile.class)) {
                    result.addAll(metadataFileSet);
                } else if (args[0].equals(DataFile.class)) {
                    result.addAll(dataFileSet);
                } else if (args[0].equals(Collection.class)) {
                    result.addAll(collectionSet);
                } else if (args[0].equals(DataItem.class)) {
                    result.addAll(dataItemSet);
                }
                return result;
            }
        }).when(businessObjectManager).getInstancesOf(any(Class.class));


    }

    private void addBusinessObjects(){
        collection = new Collection();
        collection.setId("id:collection");
        collection.setTitle(collectionName);
        collection.setSummary(collectionSummary);
        collection.getCreators().add(personName1);
        collection.setPublicationDate(publishDate);
        collection.getChildrenIds().add("id:subcollection");
       // collection.addMetadataFile("metadataFile:1");
       // collection.addMetadataFile("metadataFile:2");
        businessObjectManager.add(collectionName, collection, Collection.class);

        subcollection = new Collection();
        subcollection.setId("id:subcollection");
        subcollection.setTitle(subcollectionName);
        subcollection.setSummary(subcollectionSummary);
        subcollection.getCreators().add(personName2);
        subcollection.setPublicationDate(publishDate);
        subcollection.setParentId(collection.getId());
        businessObjectManager.add(subcollectionName, subcollection, Collection.class);

        dataFileOne = new DataFile();
        dataFileOne.setId("dataFile:1");
        dataFileOne.setName(dataFileOneName);
        dataFileOne.setPath(dataFileOnePayloadPath);
        dataFileOne.setFormat(dataFileFormat);
        businessObjectManager.add(dataFileOneName, dataFileOne, DataFile.class);

        dataFileTwo = new DataFile();
        dataFileTwo.setId("dataFile:2");
        dataFileTwo.setName(dataFileTwoName);
        dataFileTwo.setPath(dataFileTwoPayloadPath);
        dataFileTwo.setFormat(dataFileFormat);
        businessObjectManager.add(dataFileTwoName, dataFileTwo, DataFile.class);

        dataFileThree = new DataFile();
        dataFileThree.setId("dataFile:3");
        dataFileThree.setName(dataFileThreeName);
        dataFileThree.setPath(dataFileThreePayloadPath);
        dataFileThree.setFormat(dataFileFormat);
        businessObjectManager.add(dataFileThreeName, dataFileThree, DataFile.class);

        dataFileFour = new DataFile();
        dataFileFour.setId("dataFile:4");
        dataFileFour.setName(dataFileFourName);
        dataFileFour.setPath(dataFileFourPayloadPath);
        dataFileFour.setFormat(dataFileFormat);
        businessObjectManager.add(dataFileFourName, dataFileFour, DataFile.class);

        metadataFileOne = new MetadataFile();
        metadataFileOne.setId("metadataFile:1");
        metadataFileOne.setName(metadataFileOneName);
        metadataFileOne.setPath(metadataFileOnePayloadPath);
        metadataFileOne.setSize(metadataFileOneSize);
        businessObjectManager.add(metadataFileOneName, metadataFileOne, MetadataFile.class);

        metadataFileTwo = new MetadataFile();
        metadataFileTwo.setId("metadataFile:2");
        metadataFileTwo.setName(metadataFileTwoName);
        metadataFileTwo.setPath(metadataFileTwoPayloadPath);
        metadataFileTwo.setSize(metadataFileTwoSize);
        businessObjectManager.add(metadataFileTwoName, metadataFileTwo, MetadataFile.class);

        metadataFileThree = new MetadataFile();
        metadataFileThree.setId("metadataFile:3");
        metadataFileThree.setName(metadataFileThreeName);
        metadataFileThree.setPath(metadataFileThreePayloadPath);
        metadataFileThree.setSize(metadataFileThreeSize);
        businessObjectManager.add(metadataFileThreeName, metadataFileThree, MetadataFile.class);

        dataItemOne = new DataItem();
        dataItemOne.setId("dataItem:1");
        dataItemOne.setName(dataItemOneName);
        dataItemOne.setDescription(dataItemOneDescription);
        dataItemOne.getFiles().add(dataFileOne);
        dataItemOne.getFiles().add(dataFileTwo);
        businessObjectManager.add(dataItemOneName, dataItemOne, DataItem.class);

        dataItemTwo = new DataItem();
        dataItemTwo.setId("dataItem:2");
        dataItemTwo.setName(dataItemTwoName);
        dataItemTwo.setDescription(dataItemTwoDescription);
        dataItemTwo.getFiles().add(dataFileThree);
        businessObjectManager.add(dataItemTwoName, dataItemTwo, DataItem.class);

        dataItemThree = new DataItem();
        dataItemThree.setId("dataItem:3");
        dataItemThree.setName(dataItemThreeName);
        dataItemThree.setDescription(dataItemThreeDescription);
        dataItemThree.getFiles().add(dataFileFour);
        businessObjectManager.add(dataItemThreeName, dataItemThree, DataItem.class);
    }

    private void addAttributeSets(){
        collectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        subcollectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        dataItemOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        dataItemTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        dataItemThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        dataFileOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        dataFileTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        dataFileThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        dataFileFourAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        metadataFileOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        metadataFileTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        metadataFileThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);

        String creationDate =  "2013-06-10T01:55:18Z";

        String creator1 =   "Dr. Robert Moses Kildare";

        expectedKeys = new String[]{
                collectionAttributeSet.getName() + "_" + collectionName,
                subcollectionAttributeSet.getName() + "_" + subcollectionName,
                dataItemOneAttributeSet.getName() + "_" + dataItemOneName,
                dataItemTwoAttributeSet.getName() + "_" + dataItemTwoName,
                dataItemThreeAttributeSet.getName() + "_" + dataItemThreeName,
                dataFileOneAttributeSet.getName() + "_" + dataFileOneName,
                dataFileTwoAttributeSet.getName() + "_" + dataFileTwoName,
                dataFileThreeAttributeSet.getName() + "_" + dataFileThreeName,
                dataFileFourAttributeSet.getName() + "_" + dataFileFourName,
                metadataFileOneAttributeSet.getName() + "_" + metadataFileOneName,
                metadataFileTwoAttributeSet.getName() + "_" + metadataFileTwoName,
                metadataFileThreeAttributeSet.getName() + "_" + metadataFileThreeName,
        };

        //Create attribute sets and put them in the manager
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, collectionName));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, collectionSummary));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, creator1));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_AGGREGATES_COLLECTION, AttributeValueType.STRING, subcollectionName));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATED, "DateTime", creationDate));
        collectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, collectionName));
        attributeSetManager.addAttributeSet(expectedKeys[0], collectionAttributeSet);

        subcollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, subcollectionName));
        subcollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, subcollectionSummary));
        subcollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, creator2));
        subcollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_IS_PART_OF_COLLECTION, AttributeValueType.STRING, collectionName));
        subcollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_CREATED, "DateTime", creationDate));
        subcollectionAttributeSet.getAttributes().add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, subcollectionName));
        attributeSetManager.addAttributeSet(expectedKeys[1], subcollectionAttributeSet);

        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemOneName));
        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemOneDescription));
        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileOneName));
        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileTwoName));
        dataItemOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[2], dataItemOneAttributeSet);

        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemTwoName));
        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemTwoDescription));
        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileThreeName));
        dataItemTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemTwoName));
        attributeSetManager.addAttributeSet(expectedKeys[3], dataItemTwoAttributeSet);

        dataItemThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemThreeName));
        dataItemThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemThreeDescription));
        dataItemThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileFourName));
        dataItemThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemThreeName));
        attributeSetManager.addAttributeSet(expectedKeys[4], dataItemThreeAttributeSet);

        dataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileOneName));
        dataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, file1URI));
        dataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileFormat));
        dataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileOneName));
        attributeSetManager.addAttributeSet(expectedKeys[5], dataFileOneAttributeSet);

        dataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileTwoName));
        dataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, file2URI));
        dataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileFormat));
        dataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileTwoName));
        attributeSetManager.addAttributeSet(expectedKeys[6], dataFileTwoAttributeSet);

        dataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileThreeName));
        dataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING,  file3URI));
        dataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileFormat));
        dataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileThreeName));
        attributeSetManager.addAttributeSet(expectedKeys[7], dataFileThreeAttributeSet);

        dataFileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileFourName));
        dataFileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, file4URI));
        dataFileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, dataFileFormat));
        dataFileFourAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileFourName));
        attributeSetManager.addAttributeSet(expectedKeys[8], dataFileFourAttributeSet);

        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, metadataFileOneName));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFile1URI));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileFormat));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileOneName));
        metadataFileOneAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, collectionName));
        attributeSetManager.addAttributeSet(expectedKeys[9], metadataFileOneAttributeSet);

        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, metadataFileTwoName));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFile2URI));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileFormat));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileTwoName));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, collectionName));
        metadataFileTwoAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[10], metadataFileTwoAttributeSet);

        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, metadataFileThreeName));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFile3URI));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.STRING, metadataFileFormat));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileThreeName));
        metadataFileThreeAttributeSet.getAttributes().add(new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[11], metadataFileThreeAttributeSet);
    }

    private void addFileAttributeSet() {
        file1AS = new AttributeSetImpl(AttributeSetName.FILE);
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileOneSize)));
        file1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is file one"));
        attributeSetManager.addAttributeSet(file1FileASKey, file1AS);

        file2AS = new AttributeSetImpl(AttributeSetName.FILE);
        file2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileTwoSize)));
        file2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is file two"));
        attributeSetManager.addAttributeSet(file2FileASKey, file2AS);

        file3AS = new AttributeSetImpl(AttributeSetName.FILE);
        file3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileThreeSize)));
        file3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is file three"));
        attributeSetManager.addAttributeSet(file3FileASKey, file3AS);

        file4AS = new AttributeSetImpl(AttributeSetName.FILE);
        file4AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileFourSize)));
        file4AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is file four"));
        attributeSetManager.addAttributeSet(file4FileASKey, file4AS);

        metadataFile1AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(metadataFileOneSize)));
        metadataFile1AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is metadata file one"));
        attributeSetManager.addAttributeSet(metadataFile1FileASKey, metadataFile1AS);

        metadataFile2AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG,Long.toString(metadataFileTwoSize)));
        metadataFile2AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is metadata file two"));
        attributeSetManager.addAttributeSet(metadataFile2FileASKey, metadataFile2AS);

        metadataFile3AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG,Long.toString(metadataFileThreeSize)));
        metadataFile3AS.getAttributes().add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING,"This is metadata file three"));
        attributeSetManager.addAttributeSet(metadataFile3FileASKey, metadataFile3AS);
    }

    private void addBagitAttributeSet(){

        Checksum checksum1 = new ChecksumImpl("md5", Long.toString(dataFileOneSize));
        Checksum checksum2 = new ChecksumImpl("md5", Long.toString(dataFileTwoSize));
        Checksum checksum3 = new ChecksumImpl("md5", Long.toString(dataFileThreeSize));
        Checksum checksum4 = new ChecksumImpl("md5", Long.toString(dataFileFourSize));
        Checksum checksum5 = new ChecksumImpl("md5", Long.toString(metadataFileOneSize));
        Checksum checksum6 = new ChecksumImpl("md5", Long.toString(metadataFileTwoSize));
        Checksum checksum7 = new ChecksumImpl("md5", Long.toString(metadataFileThreeSize));

        dataFileOnePair = new Pair<String, Checksum>(dataFileOnePayloadPath, checksum1);
        dataFileTwoPair = new Pair<String, Checksum>(dataFileTwoPayloadPath, checksum2);
        dataFileThreePair = new Pair<String, Checksum>(dataFileThreePayloadPath, checksum3);
        dataFileFourPair = new Pair<String, Checksum>(dataFileFourPayloadPath, checksum4);

        metadataFileOnePair = new Pair<String, Checksum>(metadataFileOnePayloadPath, checksum5);
        metadataFileTwoPair = new Pair<String, Checksum>(metadataFileTwoPayloadPath, checksum6);
        metadataFileThreePair = new Pair<String, Checksum>(metadataFileThreePayloadPath, checksum7);

        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileOnePair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileTwoPair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileThreePair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileFourPair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileOnePair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileTwoPair.toString()));
        bagitAttributeSet.getAttributes().add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileThreePair.toString()));

        attributeSetManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);
    }

}

