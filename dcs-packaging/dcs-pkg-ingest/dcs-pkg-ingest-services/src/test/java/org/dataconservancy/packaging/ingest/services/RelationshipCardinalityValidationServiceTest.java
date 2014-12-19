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

import org.junit.Test;

import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;

import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.STRING;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATED_BY_PROJECT;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_IDENTIFIER;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_IS_PART_OF_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_RESOURCEID;
import static org.dataconservancy.packaging.model.Metadata.DATA_ITEM_AGGREGATES_FILE;
import static org.dataconservancy.packaging.model.Metadata.DATA_ITEM_IDENTIFIER;
import static org.dataconservancy.packaging.model.Metadata.DATA_ITEM_IS_PART_OF_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATES_DATAITEM;
import static org.dataconservancy.packaging.model.Metadata.FILE_RESOURCEID;
import static org.dataconservancy.packaging.model.Metadata.IS_METADATA_FOR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RelationshipCardinalityValidationServiceTest {

    private static final RelationshipCardinalityVerificationService service =
            new RelationshipCardinalityVerificationService();

    @Test
    public void validCollectionsTest() throws StatefulIngestServiceException {

        MetadataAttributeSet coll1 = new MetadataAttributeSet("arbitraryName1");
        coll1.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATED_BY_PROJECT,
                                             STRING,
                                             "SomeProject"));

        MetadataAttributeSet coll2 = new MetadataAttributeSet("arbitraryName2");
        coll2.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 2"));
        coll2.addAttribute(new AttributeImpl(COLLECTION_IS_PART_OF_COLLECTION,
                                             STRING,
                                             "SomeOtherProject"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", coll1);
        asm.addAttributeSet("arbitraryKey2", coll2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);

    }

    @Test(expected = StatefulIngestServiceException.class)
    public void tooManyCollectionAggregatedByTest()
            throws StatefulIngestServiceException {
        MetadataAttributeSet coll1 = new MetadataAttributeSet("arbitraryName1");
        coll1.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATED_BY_PROJECT,
                                             STRING,
                                             "SomeProject"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATED_BY_PROJECT,
                                             STRING,
                                             "Oops!  Bad!"));

        MetadataAttributeSet coll2 = new MetadataAttributeSet("arbitraryName2");
        coll2.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 2"));
        coll2.addAttribute(new AttributeImpl(COLLECTION_IS_PART_OF_COLLECTION,
                                             STRING,
                                             "SomeOtherProject"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", coll1);
        asm.addAttributeSet("arbitraryKey2", coll2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);

    }

    @Test(expected = StatefulIngestServiceException.class)
    public void tooManyCollectionIsPartOfTest()
            throws StatefulIngestServiceException {
        MetadataAttributeSet coll1 = new MetadataAttributeSet("arbitraryName1");
        coll1.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATED_BY_PROJECT,
                                             STRING,
                                             "SomeProject"));

        MetadataAttributeSet coll2 = new MetadataAttributeSet("arbitraryName2");
        coll2.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 2"));
        coll2.addAttribute(new AttributeImpl(COLLECTION_IS_PART_OF_COLLECTION,
                                             STRING,
                                             "SomeCollection"));
        coll2.addAttribute(new AttributeImpl(COLLECTION_IS_PART_OF_COLLECTION,
                                             STRING,
                                             "OOps! Bad!"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", coll1);
        asm.addAttributeSet("arbitraryKey2", coll2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void collectionExclusivityTest()
            throws StatefulIngestServiceException {
        MetadataAttributeSet coll1 = new MetadataAttributeSet("arbitraryName1");
        coll1.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATED_BY_PROJECT,
                                             STRING,
                                             "SomeProject"));

        MetadataAttributeSet coll2 = new MetadataAttributeSet("arbitraryName2");
        coll2.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 2"));
        coll2.addAttribute(new AttributeImpl(COLLECTION_AGGREGATED_BY_PROJECT,
                                             STRING,
                                             "SomeProject"));
        coll2.addAttribute(new AttributeImpl(COLLECTION_IS_PART_OF_COLLECTION,
                                             STRING,
                                             "SomeOtherProject"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", coll1);
        asm.addAttributeSet("arbitraryKey2", coll2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void collectionWithoutRequiredRelationshipTest()
            throws StatefulIngestServiceException {
        MetadataAttributeSet coll1 = new MetadataAttributeSet("arbitraryName1");
        coll1.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATED_BY_PROJECT,
                                             STRING,
                                             "SomeProject"));

        MetadataAttributeSet coll2 = new MetadataAttributeSet("arbitraryName2");
        coll2.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 2"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", coll1);
        asm.addAttributeSet("arbitraryKey2", coll2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test
    public void validDataItemTest() throws StatefulIngestServiceException {
        MetadataAttributeSet di1 = new MetadataAttributeSet("arbitraryName1");
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IDENTIFIER, STRING, "DI 1"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_AGGREGATES_FILE,
                                           STRING,
                                           "File 1"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION,
                                           STRING,
                                           "SomeCollection"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", di1);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void validDataItemAggregatedByOneCollection() 
            throws StatefulIngestServiceException {
        MetadataAttributeSet coll1 = new MetadataAttributeSet("arbitraryName1");
        coll1.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATES_DATAITEM,
                                             STRING,
                                             "DataItem 1"));
        
        MetadataAttributeSet dataItem2 = new MetadataAttributeSet("arbitraryName2");
        dataItem2.addAttribute(new AttributeImpl(DATA_ITEM_IDENTIFIER,
                                             STRING,
                                             "DataItem 1"));
        dataItem2.addAttribute(new AttributeImpl(DATA_ITEM_AGGREGATES_FILE,
                                           STRING,
                                           "File 1"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", coll1);
        asm.addAttributeSet("arbitraryKey2", dataItem2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void dataItemNoCollectionTest()
            throws StatefulIngestServiceException {
        MetadataAttributeSet di1 = new MetadataAttributeSet("arbitraryName1");
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IDENTIFIER, STRING, "DI 1"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_AGGREGATES_FILE,
                                           STRING,
                                           "File 1"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", di1);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void dataItemTwoCollectionsTest()
            throws StatefulIngestServiceException {
        MetadataAttributeSet di1 = new MetadataAttributeSet("arbitraryName1");
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IDENTIFIER, STRING, "DI 1"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_AGGREGATES_FILE,
                                           STRING,
                                           "File 1"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION,
                                           STRING,
                                           "SomeCollection"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION,
                                           STRING,
                                           "SomeOtherCollection"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", di1);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void dataItemAggregatedByTwoCollectionsTest() 
            throws StatefulIngestServiceException {
        MetadataAttributeSet coll1 = new MetadataAttributeSet("arbitraryName1");
        coll1.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATES_DATAITEM,
                                             STRING,
                                             "SomeDataItem"));

        MetadataAttributeSet coll2 = new MetadataAttributeSet("arbitraryName2");
        coll2.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 2"));
        coll2.addAttribute(new AttributeImpl(COLLECTION_AGGREGATES_DATAITEM,
                                             STRING,
                                             "SomeDataItem"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", coll1);
        asm.addAttributeSet("arbitraryKey2", coll2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void dataItemAggregatedByOneCollectionIsPartOfAnotherCollectionTest() 
            throws StatefulIngestServiceException {
        MetadataAttributeSet coll1 = new MetadataAttributeSet("arbitraryName1");
        coll1.addAttribute(new AttributeImpl(COLLECTION_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        coll1.addAttribute(new AttributeImpl(COLLECTION_AGGREGATES_DATAITEM,
                                             STRING,
                                             "SomeDataItem"));

        MetadataAttributeSet dataItem2 = new MetadataAttributeSet("arbitraryName2");
        dataItem2.addAttribute(new AttributeImpl(DATA_ITEM_IDENTIFIER,
                                             STRING,
                                             "Coll 1"));
        dataItem2.addAttribute(new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION,
                                             STRING,
                                             "Coll 2"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", coll1);
        asm.addAttributeSet("arbitraryKey2", dataItem2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }
    
    
    @Test
    public void dataItemMultipleFilesTest()
            throws StatefulIngestServiceException {
        MetadataAttributeSet di1 = new MetadataAttributeSet("arbitraryName1");
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IDENTIFIER, STRING, "DI 1"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_AGGREGATES_FILE,
                                           STRING,
                                           "File 1"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_AGGREGATES_FILE,
                                           STRING,
                                           "File 2"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION,
                                           STRING,
                                           "SomeCollection"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", di1);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void dataItemNoFilesTest() throws StatefulIngestServiceException {
        MetadataAttributeSet di1 = new MetadataAttributeSet("arbitraryName1");
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IDENTIFIER, STRING, "DI 1"));
        di1.addAttribute(new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION,
                                           STRING,
                                           "SomeCollection"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", di1);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test
    public void metadataFileNoIsMetadataForTest() throws Exception {
        MetadataAttributeSet mdfAs = new MetadataAttributeSet("arbitraryName1");
        mdfAs.addAttribute(new AttributeImpl(FILE_RESOURCEID, STRING, "MdF 1"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("arbitraryKey1", mdfAs);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test
    public void metadataFileSingleIsMetadataForTest() throws Exception {
        MetadataAttributeSet collAs = new MetadataAttributeSet("arbitraryName1");
        collAs.addAttribute(new AttributeImpl(COLLECTION_RESOURCEID, STRING, "collId1"));

        MetadataAttributeSet mdfAs = new MetadataAttributeSet("arbitraryName2");
        mdfAs.addAttribute(new AttributeImpl(FILE_RESOURCEID, STRING, "mdfId1"));
        mdfAs.addAttribute(new AttributeImpl(IS_METADATA_FOR, STRING, "collId1"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("mdfKey", mdfAs);
        asm.addAttributeSet("collKey", collAs);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void metadataFileMultipleIsMetadataForTest() throws Exception {
        MetadataAttributeSet collAs1 = new MetadataAttributeSet("arbitraryName1");
        collAs1.addAttribute(new AttributeImpl(COLLECTION_RESOURCEID, STRING, "collId1"));

        MetadataAttributeSet collAs2 = new MetadataAttributeSet("arbitraryName2");
        collAs2.addAttribute(new AttributeImpl(COLLECTION_RESOURCEID, STRING, "collId2"));

        MetadataAttributeSet mdfAs = new MetadataAttributeSet("arbitraryName3");
        mdfAs.addAttribute(new AttributeImpl(FILE_RESOURCEID, STRING, "mdfId1"));
        mdfAs.addAttribute(new AttributeImpl(IS_METADATA_FOR, STRING, "collId1"));
        // illegal: an mdf can only describe one object
        mdfAs.addAttribute(new AttributeImpl(IS_METADATA_FOR, STRING, "collId2"));

        AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("mdfKey", mdfAs);
        asm.addAttributeSet("collKey1", collAs1);
        asm.addAttributeSet("collKey2", collAs2);

        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(asm);

        service.execute("depositId", state);
    }


}
