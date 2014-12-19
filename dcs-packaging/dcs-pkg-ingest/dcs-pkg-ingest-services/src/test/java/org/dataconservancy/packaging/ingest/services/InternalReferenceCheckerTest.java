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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.when;

public class InternalReferenceCheckerTest extends BaseReferenceCheckerTest {
    
    private InternalReferenceChecker underTest;
        
    private final String packageId = "00235EAG2";
    private final String projectId = "FGW2351G2";
    private final String collectionId = "423i8GTA";
    private final String dataItemId = "GF236SGIW";
    private final String fileId = "683RHES58903";
    private final String subCollectionId = "GHEARGHA2389";
    private final String versionDataItemId = "2352GAE236";
    private final String collectionOnlyFileId = "HJWI32523GES";
    private final String dataItemOnlyFileId = "PEIW4358BMA";
    private final String fileCollectionMetadataId = "AGHEWA2839";
    private final String fileDataItemMetadataId = "MWQP28902ZYX";

    @Before
    public void setup() throws Exception {
        super.setup();
        underTest = new InternalReferenceChecker();
        
        Attribute packageAggregatesProject = new AttributeImpl("Package-Aggregates-Project", "String", projectId);
        packageAttributeSet.getAttributes().add(packageAggregatesProject);
        Attribute packageAggregatesCollection = new AttributeImpl("Package-Aggregates-Collection", "String",
                collectionId);
        packageAttributeSet.getAttributes().add(packageAggregatesCollection);
        Attribute packageAggregatesDataItem = new AttributeImpl("Package-Aggregates-DataItem", "String", dataItemId);
        packageAttributeSet.getAttributes().add(packageAggregatesDataItem);
        Attribute packageAggregatesFile = new AttributeImpl("Package-Aggregates-File", "String", fileId);
        packageAttributeSet.getAttributes().add(packageAggregatesFile);
        
        Attribute projectAggregatesCollection = new AttributeImpl("Project-Aggregates-Collection", "String",
                collectionId);
        projectAttributeSet.getAttributes().add(projectAggregatesCollection);
        Attribute projectAggregatesFile = new AttributeImpl("Project-Aggregates-File", "String", fileId);
        projectAttributeSet.getAttributes().add(projectAggregatesFile);
        
        Attribute collectionAggregatesCollection = new AttributeImpl("Collection-Aggregates-Collection", "String",
                subCollectionId);
        collectionAttributeSet.getAttributes().add(collectionAggregatesCollection);
        Attribute collectionAggregatesDataItem = new AttributeImpl("Collection-Aggregates-DataItem", "String",
                dataItemId);
        collectionAttributeSet.getAttributes().add(collectionAggregatesDataItem);
        Attribute collectionAggregatesFile = new AttributeImpl("Collection-Aggregates-File", "String", fileId);
        collectionAttributeSet.getAttributes().add(collectionAggregatesFile);
        Attribute collectionAggregatedByProject = new AttributeImpl("Collection-Aggregated-By-Project", "String",
                projectId);
        collectionAttributeSet.getAttributes().add(collectionAggregatedByProject);
        Attribute collectionOnlyAggregatedFile = new AttributeImpl("Collection-Aggregates-File", "String",
                collectionOnlyFileId);
        collectionAttributeSet.getAttributes().add(collectionOnlyAggregatedFile);
        
        Attribute collectionIsPartOfCollection = new AttributeImpl("Collection-IsPartOf-Collection", "String",
                collectionId);
        subCollectionAttributeSet.getAttributes().add(collectionIsPartOfCollection);
        
        Attribute dataItemAggregatesFile = new AttributeImpl("DataItem-Aggregates-File", "String", fileId);
        dataItemAttributeSet.getAttributes().add(dataItemAggregatesFile);
        Attribute dataItemAggregatedByCollection = new AttributeImpl("DataItem-IsPartOf-Collection", "String",
                collectionId);
        dataItemAttributeSet.getAttributes().add(dataItemAggregatedByCollection);
        Attribute dataItemOnlyAggregatedFile = new AttributeImpl("DataItem-Aggregates-File", "String",
                dataItemOnlyFileId);
        dataItemAttributeSet.getAttributes().add(dataItemOnlyAggregatedFile);
        
        Attribute dataItemIsVersionOf = new AttributeImpl("DataItem-IsVersionOf-DataItem", "String", dataItemId);
        versionedDataItemAttributeSet.getAttributes().add(dataItemIsVersionOf);
        
        Attribute fileIsMetadataForProject = new AttributeImpl("File-IsMetadata-For", "String", projectId);
        fileAttributeSet.getAttributes().add(fileIsMetadataForProject);
        Attribute fileIsMetadataForCollection = new AttributeImpl("File-IsMetadata-For", "String",
                fileCollectionMetadataId);
        fileAttributeSet.getAttributes().add(fileIsMetadataForCollection);
        Attribute fileIsMetadataForDataItem = new AttributeImpl("File-IsMetadata-For", "String", fileDataItemMetadataId);
        fileAttributeSet.getAttributes().add(fileIsMetadataForDataItem);
        
        
        
    }

    /**
     * Tests that attribute sets with no missing references passes through service with no events generated.
     */
    @Test
    public void testInternalReferenceCheckerPass() throws StatefulIngestServiceException {    
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(0, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    } 
    
    @Test
    public void testExternalReferencePassesThrough() throws StatefulIngestServiceException {        
        Attribute packageAggregatesProject = new AttributeImpl("Package-Aggregates-Project", "String", "http://dataconservancy.org/project/1");
        AttributeSet packageAttributeSet = new AttributeSetImpl("Ore-Rem-Package");
        packageAttributeSet.getAttributes().add(packageAggregatesProject);
        
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(0, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the package attribute set can't find a project it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testPackageMissingProject() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the package attribute set can't find a collection it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testPackageMissingCollection() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the package attribute set can't find a data item it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testPackageMissingDataItem() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the package attribute set can't find a file it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testPackageMissingFile() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the project attribute set can't find a collection it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testProjectMissingCollection() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the project attribute set can't find a file it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testProjectMissingFile() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the collection attribute set can't find the project that aggregates it an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testCollectionMissingProject() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the collection attribute set can't find a collection that it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testCollectionMissingSubCollection() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the collection attribute set can't find a data item it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testCollectionMissingDataItem() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);

        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the collection attribute set can't find a file it aggregates it an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testCollectionMissingFile() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the collection attribute set can't find the collection that aggregates it an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testCollectionMissingParent() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the data item attribute set can't find a file it aggregates an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testDataItemMissingFile() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the data item attribute set can't find the collection that aggregates it an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testDataItemMissingParentCollection() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the data item attribute set can't find the data item it's a version of an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testDataItemMissingVersion() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the file attribute set can't find the project it's metadata for an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testFileMissingMetadataProject() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the file attribute set can't find the collection it's metadata for an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testFileMissingMetadataCollection() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + fileDataItemMetadataId, fileDataItemMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that if the file attribute set can't find the data item it's metadata for an error is generated.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testFileMissingMetadataDataItem() throws StatefulIngestServiceException {

        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + packageId, packageAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + projectId, projectAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + collectionId, collectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + subCollectionId, subCollectionAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + dataItemId, dataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + versionDataItemId, versionedDataItemAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + collectionOnlyFileId, collectionOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + dataItemOnlyFileId, dataItemOnlyFileAttributeSet);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_" + fileCollectionMetadataId, fileCollectionMetadataAttributeSet);
        
        underTest.execute("ingest:1", state);
        
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
    
    /**
     * Tests that a file path that can't be resolved fails the reference check.
     * @throws StatefulIngestServiceException
     */
    @Test
    public void testFileMissingPath() throws StatefulIngestServiceException {
        Attribute fileMissingPath = new AttributeImpl(Metadata.FILE_PATH, "String", "foo");
        fileAttributeSet.getAttributes().clear();
        fileAttributeSet.getAttributes().add(fileMissingPath);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        
        underTest.execute("ingest:1", state);
        assertEquals(1, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());

    }
    
    /**
     * Tests that a file with a good path that can be resolved passes ingest.
     * @throws StatefulIngestServiceException
     * @throws IOException
     */
    @Test
    public void testFilePathFound() throws StatefulIngestServiceException, IOException {
        File baseDir = new File("bagFoo");
        
        File fileOneTmp = java.io.File.createTempFile("testFile", ".txt");
        fileOneTmp.deleteOnExit();

        pkg.getSerialization().setExtractDir(fileOneTmp.getParentFile());
        pkg.getSerialization().setBaseDir(baseDir);
        
        Attribute fileMissingPath = new AttributeImpl(Metadata.FILE_PATH, "String", "file:///" + fileOneTmp.getName().replace("\\", "/"));
        fileAttributeSet.getAttributes().clear();
        fileAttributeSet.getAttributes().add(fileMissingPath);
        attributeManager.addAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + fileId, fileAttributeSet);
        
        underTest.execute("ingest:1", state);
        assertEquals(0, eventManager.getEvents("ingest:1", org.dataconservancy.packaging.ingest.api.Package.Events.INGEST_FAIL).size());
    }
}