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
package org.dataconservancy.packaging.ingest.impl;

import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.NonexistentBusinessObjectException;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class BusinessObjectManagerImplTest {

    BusinessObjectManager underTest;

    Collection collection1;
    String collection1LocalID = "local.id:collection:1";
    Collection collection2;
    String collection2LocalID = "local.id:collection:2";

    Collection collection3;
    String collection3LocalID = "local.id:collection:3";

    DataItem dataItem1;
    String dataItem1LocalID = "local.id:dataitem:1";
    DataItem dataItem2;
    String dataItem2LocalID = "local.id:dataitem:2";
    DataItem dataItem3;
    String dataItem3LocalID = "local.id:dataitem:3";

    DataFile dataFile1;
    String dataFile1LocalID = "local.id:datafile:1";
    DataFile dataFile2;
    String dataFile2LocalID = "local.id:datafile:2";
    DataFile dataFile3;
    String dataFile3LocalID = "local.id:datafile:3";

    MetadataFile metadataFile1;
    String metadataFile1LocalID = "local.id:metadatafile:1";
    MetadataFile metadataFile2;
    String metadataFile2LocalID = "local.id:metadatafile:2";
    MetadataFile metadataFile3;
    String metadataFile3LocalID = "local.id:metadatafile:3";


    @Before
    public void setUp() {
        underTest = new BusinessObjectManagerImpl();

        collection1 = new Collection();
        collection1.setId("biz.id:collection:1");
        collection2 = new Collection();
        collection2.setId("biz.id:collection:2");
        collection3 = new Collection();
        collection3.setId("biz.id:collection:3");

        dataItem1 = new DataItem();
        dataItem1.setId("biz.id:dataItem:1");
        dataItem2 = new DataItem();
        dataItem2.setId("biz.id:dataItem:2");
        dataItem3 = new DataItem();
        dataItem3.setId("biz.id:dataItem:3");

        dataFile1 = new DataFile();
        dataFile1.setId("biz.id:dataFile:1");
        dataFile2 = new DataFile();
        dataFile2.setId("biz.id:dataFile:3");
        dataFile3 = new DataFile();
        dataFile1.setId("biz.id:dataFile:3");

        metadataFile1 = new MetadataFile();
        metadataFile1.setId("biz.id:metadataFile:1");
        metadataFile2 = new MetadataFile();
        metadataFile2.setId("biz.id:metadataFile:2");
        metadataFile3 = new MetadataFile();
        metadataFile3.setId("biz.id:metadataFile:3");
    }

    /**
     * Add BOs to the BOManager. Test that BOs added can be retrieved, and BOs that had not been added don't exist in the
     * manager.
     */
    @Test
    public void testAddAndRetrieveBusinessObjectByIdAndType() {
        //add collections
        underTest.add(collection1LocalID, collection1, Collection.class);
        underTest.add(collection2LocalID, collection2, Collection.class);

        //Add data item
        underTest.add(dataItem1LocalID, dataItem1, DataItem.class);

        //Add data files
        underTest.add(dataFile1LocalID, dataFile1, DataFile.class);
        underTest.add(dataFile2LocalID, dataFile2, DataFile.class);
        underTest.add(dataFile3LocalID, dataFile3, DataFile.class);

        underTest.add(metadataFile2LocalID, metadataFile2, MetadataFile.class);

        assertEquals(collection1, underTest.get(collection1LocalID, Collection.class));
        assertEquals(metadataFile2, underTest.get(metadataFile2LocalID, MetadataFile.class));

        assertNull(underTest.get(collection3LocalID,Collection.class));
        assertNull(underTest.get(dataItem2LocalID, DataItem.class));
        assertNull(underTest.get(metadataFile1LocalID, MetadataFile.class));
    }


    /**
     * Test updating existing business objects
     */
    @Test
    public void testUpdateBusinessObjects() {
        //add collections
        underTest.add(collection1LocalID, collection1, Collection.class);
        underTest.add(collection2LocalID, collection2, Collection.class);

        //Add data item
        underTest.add(dataItem1LocalID, dataItem1, DataItem.class);

        assertEquals(collection1, underTest.get(collection1LocalID, Collection.class));
        assertEquals(dataItem1, underTest.get(dataItem1LocalID, DataItem.class));

        //Update collection 1
        Collection newCollection1 = new Collection();
        newCollection1.setId("biz.id:collection:1");
        newCollection1.setSummary("This is an updated version of Collection 1.");
        underTest.update(collection1LocalID, newCollection1, Collection.class);
        assertEquals(newCollection1, underTest.get(collection1LocalID, Collection.class));

        //Update dataItem1
        DataItem newDataItem1 = new DataItem();
        newDataItem1.setId("biz.id:dataItem:1");
        newDataItem1.setName("Data Item 1");
        newDataItem1.setDescription("This is an updated version of DataItem 1.");
        underTest.update(dataItem1LocalID, newDataItem1, DataItem.class);
        assertEquals(newDataItem1, underTest.get(dataItem1LocalID, DataItem.class));
    }

    /**
     * Test removal of BOs
     */
    @Test
    public void testAddAndRemoveBOs () {
        //add collections
        underTest.add(collection1LocalID, collection1, Collection.class);
        underTest.add(collection2LocalID, collection2, Collection.class);

        //Add data item
        underTest.add(dataItem1LocalID, dataItem1, DataItem.class);

        //Add data files
        underTest.add(dataFile1LocalID, dataFile1, DataFile.class);
        underTest.add(dataFile2LocalID, dataFile2, DataFile.class);
        underTest.add(dataFile3LocalID, dataFile3, DataFile.class);

        underTest.add(metadataFile2LocalID, metadataFile2, MetadataFile.class);

        assertEquals(collection1, underTest.get(collection1LocalID, Collection.class));
        assertEquals(collection2, underTest.get(collection2LocalID, Collection.class));

        assertEquals(dataItem1, underTest.get(dataItem1LocalID, DataItem.class));

        assertEquals(dataFile1, underTest.get(dataFile1LocalID, DataFile.class));
        assertEquals(dataFile2, underTest.get(dataFile2LocalID, DataFile.class));
        assertEquals(dataFile3, underTest.get(dataFile3LocalID, DataFile.class));

        assertEquals(metadataFile2, underTest.get(metadataFile2LocalID, MetadataFile.class));

        underTest.remove(dataFile1LocalID, DataFile.class);
        underTest.remove(collection2LocalID, Collection.class);
        underTest.remove(metadataFile2LocalID, MetadataFile.class);

        assertEquals(collection1, underTest.get(collection1LocalID, Collection.class));
        assertEquals(dataItem1, underTest.get(dataItem1LocalID, DataItem.class));
        assertEquals(dataFile2, underTest.get(dataFile2LocalID, DataFile.class));
        assertEquals(dataFile3, underTest.get(dataFile3LocalID, DataFile.class));

        assertNull(underTest.get(dataFile1LocalID, DataFile.class));
        assertNull(underTest.get(collection2LocalID, Collection.class));
        assertNull(underTest.get(metadataFile2LocalID, Collection.class));
    }

    /**
     * Test that getInstances method works: returns all instances of a specified typed.
     */
    @Test
    public void testGetInstances() {
        //add collections
        underTest.add(collection1LocalID, collection1, Collection.class);
        underTest.add(collection2LocalID, collection2, Collection.class);

        //Add data item
        underTest.add(dataItem1LocalID, dataItem1, DataItem.class);

        //Add data files
        underTest.add(dataFile1LocalID, dataFile1, DataFile.class);
        underTest.add(dataFile2LocalID, dataFile2, DataFile.class);
        underTest.add(dataFile3LocalID, dataFile3, DataFile.class);

        underTest.add(metadataFile2LocalID, metadataFile2, MetadataFile.class);

        Set<DataFile> allDataFiles = underTest.getInstancesOf(DataFile.class);
        Set<DataFile> expectedDataFiles = new HashSet<DataFile>();
        expectedDataFiles.add(dataFile1);
        expectedDataFiles.add(dataFile2);
        expectedDataFiles.add(dataFile3);

        assertTrue(expectedDataFiles.containsAll(allDataFiles));
        assertTrue(allDataFiles.containsAll(expectedDataFiles));

    }
    
    @Test
    public void testGetObjectsByBusinessId() {
        underTest.add(collection1LocalID, collection1, Collection.class);
        underTest.add(dataItem1LocalID, dataItem1, DataItem.class);
        underTest.add(dataFile1LocalID, dataFile1, DataFile.class);
        underTest.add(metadataFile1LocalID, metadataFile1, MetadataFile.class);
        
        Collection testCollection = (Collection) underTest.get(collection1.getId());
        assertNotNull(testCollection);
        assertEquals(collection1, testCollection);
        
        DataItem testDataItem = (DataItem) underTest.get(dataItem1.getId());
        assertNotNull(testDataItem);
        assertEquals(dataItem1, testDataItem);
        
        DataFile testDataFile = (DataFile) underTest.get(dataFile1.getId());
        assertNotNull(testDataFile);
        assertEquals(dataFile1, testDataFile);
        
        MetadataFile testMetadataFile = (MetadataFile) underTest.get(metadataFile1.getId());
        assertNotNull(testMetadataFile);
        assertEquals(metadataFile1, testMetadataFile);
        
        Collection nonExistentCollection = (Collection) underTest.get("foo");
        assertNull(nonExistentCollection);
    }

    /**
     * Test behavior when updaing non existing BOs
     */
    @Test (expected = NonexistentBusinessObjectException.class)
    public void testUpdatingNonExistingBO() {
        underTest.update(collection1LocalID, collection1, Collection.class);
    }

    @Test (expected = NonexistentBusinessObjectException.class)
    public void testRemovingNonExistingBO() {
        underTest.remove(collection1LocalID, Collection.class);

    }
    
    /**
     * Make sure a map can be created.
     */
    @Test
    public void testCreateMap() {
        underTest.add(collection1LocalID, collection1, Collection.class);
        underTest.add(dataItem1LocalID, dataItem1, DataItem.class);
        underTest.add(dataFile1LocalID, dataFile1, DataFile.class);
        
        Map<BusinessObject, String> map = underTest.createMap();
     
        Map<BusinessObject, String> expected = new HashMap<BusinessObject, String>();
        expected.put(collection1, collection1LocalID);
        expected.put(dataItem1, dataItem1LocalID);
        expected.put(dataFile1, dataFile1LocalID);

        assertEquals(expected, map);
    }
}

