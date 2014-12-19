package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Metadata;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dataconservancy.packaging.ingest.services.AttributeSetUtil.composeKey;
import static org.dataconservancy.packaging.model.AttributeSetName.*;
import static org.dataconservancy.packaging.model.Metadata.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 *
 */
public class OreRelationshipConstraintCheckerTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private IngestWorkflowState state;

    private AttributeSetManager asm;

    @Before
    public void setUp() throws Exception {
        state = MockUtil.mockState();
        asm = new AttributeSetManagerImpl();
        when(state.getAttributeSetManager()).thenReturn(asm);
    }

    @Test
    public void testDataItemAggregatedByPackage() throws Exception {
        final String diId = "id:dataitem";
        AttributeSetImpl diAs = new AttributeSetImpl(ORE_REM_DATAITEM,
                Arrays.<Attribute>asList(new AttributeImpl(DATAITEM_RESOURCEID, "String", diId),
                                         new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION, "String", "collId")));

        final String packageId = "id:package";
        AttributeSetImpl pkgAs = new AttributeSetImpl(ORE_REM_PACKAGE,
                Arrays.<Attribute>asList(new AttributeImpl(PACKAGE_RESOURCEID, "String", packageId),
                                         new AttributeImpl(PACKAGE_AGGREGATES_DATAITEM, "String", diId)));

        asm.addAttributeSet(composeKey(ORE_REM_DATAITEM, diId), diAs);
        asm.addAttributeSet(composeKey(ORE_REM_PACKAGE, packageId), pkgAs);

        List<String> errors = new ArrayList<String>();
        OreRelationshipConstraintChecker underTest = new OreRelationshipConstraintChecker(errors);

        underTest.execute("depositId", state);

        assertEquals(0, errors.size());
    }

    @Test
    public void testDataItemAggregatedByPackageMissingIsPartOf() throws Exception {
        final String diId = "id:dataitem";
        AttributeSetImpl diAs = new AttributeSetImpl(ORE_REM_DATAITEM,
                Arrays.<Attribute>asList(new AttributeImpl(DATAITEM_RESOURCEID, "String", diId)));

        final String packageId = "id:package";
        AttributeSetImpl pkgAs = new AttributeSetImpl(ORE_REM_PACKAGE,
                Arrays.<Attribute>asList(new AttributeImpl(PACKAGE_RESOURCEID, "String", packageId),
                        new AttributeImpl(PACKAGE_AGGREGATES_DATAITEM, "String", diId)));

        asm.addAttributeSet(composeKey(ORE_REM_DATAITEM, diId), diAs);
        asm.addAttributeSet(composeKey(ORE_REM_PACKAGE, packageId), pkgAs);

        List<String> errors = new ArrayList<String>();
        OreRelationshipConstraintChecker underTest = new OreRelationshipConstraintChecker(errors);

        try {
            underTest.execute("depositId", state);
            fail("Expected a StatefulIngestServiceException to be thrown.");
        } catch (StatefulIngestServiceException e) {
            // expected
        }

        assertEquals(1, errors.size());
    }

    @Test
    public void testDataItemAggregatedByPackageMultipleIsPartOf() throws Exception {
        final String diId = "id:dataitem";
        AttributeSetImpl diAs = new AttributeSetImpl(ORE_REM_DATAITEM,
                Arrays.<Attribute>asList(new AttributeImpl(DATAITEM_RESOURCEID, "String", diId),
                        new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION, "String", "collId1"),
                        new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION, "String", "collId2")));

        final String packageId = "id:package";
        AttributeSetImpl pkgAs = new AttributeSetImpl(ORE_REM_PACKAGE,
                Arrays.<Attribute>asList(new AttributeImpl(PACKAGE_RESOURCEID, "String", packageId),
                        new AttributeImpl(PACKAGE_AGGREGATES_DATAITEM, "String", diId)));

        asm.addAttributeSet(composeKey(ORE_REM_DATAITEM, diId), diAs);
        asm.addAttributeSet(composeKey(ORE_REM_PACKAGE, packageId), pkgAs);

        List<String> errors = new ArrayList<String>();
        OreRelationshipConstraintChecker underTest = new OreRelationshipConstraintChecker(errors);

        try {
            underTest.execute("depositId", state);
            fail("Expected a StatefulIngestServiceException to be thrown.");
        } catch (StatefulIngestServiceException e) {
            // expected
        }

        assertEquals(1, errors.size());
    }

    @Test
    public void testDataItemAggregatedByCollection() throws Exception {
        final String diId = "id:dataitem";
        AttributeSetImpl diAs = new AttributeSetImpl(ORE_REM_DATAITEM,
                Arrays.<Attribute>asList(new AttributeImpl(DATAITEM_RESOURCEID, "String", diId)));

        final String collId = "id:collection";
        AttributeSetImpl collAs = new AttributeSetImpl(ORE_REM_COLLECTION,
                Arrays.<Attribute>asList(new AttributeImpl(COLLECTION_RESOURCEID, "String", collId),
                        new AttributeImpl(COLLECTION_AGGREGATES_DATAITEM, "String", diId)));

        final String packageId = "id:package";
                AttributeSetImpl pkgAs = new AttributeSetImpl(ORE_REM_PACKAGE,
                        Arrays.<Attribute>asList(new AttributeImpl(COLLECTION_RESOURCEID, "String", collId)));

        asm.addAttributeSet(composeKey(ORE_REM_DATAITEM, diId), diAs);
        asm.addAttributeSet(composeKey(ORE_REM_COLLECTION, collId), collAs);
        asm.addAttributeSet(composeKey(ORE_REM_PACKAGE, packageId), pkgAs);

        List<String> errors = new ArrayList<String>();
        OreRelationshipConstraintChecker underTest = new OreRelationshipConstraintChecker(errors);

        underTest.execute("depositId", state);

        assertEquals(0, errors.size());
    }

    @Test
    public void testDataItemAggregatedByCollectionWithIsPartOf() throws Exception {
        final String diId = "id:dataitem";
        AttributeSetImpl diAs = new AttributeSetImpl(ORE_REM_DATAITEM,
                Arrays.<Attribute>asList(new AttributeImpl(DATAITEM_RESOURCEID, "String", diId),
                        new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION, "String", "fooId")));

        final String collId = "id:collection";
        AttributeSetImpl collAs = new AttributeSetImpl(ORE_REM_COLLECTION,
                Arrays.<Attribute>asList(new AttributeImpl(COLLECTION_RESOURCEID, "String", collId),
                        new AttributeImpl(COLLECTION_AGGREGATES_DATAITEM, "String", diId)));

        final String packageId = "id:package";
                AttributeSetImpl pkgAs = new AttributeSetImpl(ORE_REM_PACKAGE,
                        Arrays.<Attribute>asList(new AttributeImpl(COLLECTION_RESOURCEID, "String", collId)));

        asm.addAttributeSet(composeKey(ORE_REM_DATAITEM, diId), diAs);
        asm.addAttributeSet(composeKey(ORE_REM_COLLECTION, collId), collAs);
        asm.addAttributeSet(composeKey(ORE_REM_PACKAGE, packageId), pkgAs);

        List<String> errors = new ArrayList<String>();
        OreRelationshipConstraintChecker underTest = new OreRelationshipConstraintChecker(errors);

        try {
            underTest.execute("depositId", state);
            fail("Expected a StatefulIngestServiceException to be thrown.");
        } catch (StatefulIngestServiceException e) {
            // expected
        }

        assertEquals(1, errors.size());
    }

    @Test
    public void testDataItemAggregatedByCollectionWithCorrectIsPartOf() throws Exception {
        final String diId = "id:dataitem";
        final String collId = "id:collection";

        AttributeSetImpl diAs = new AttributeSetImpl(ORE_REM_DATAITEM,
                Arrays.<Attribute>asList(new AttributeImpl(DATAITEM_RESOURCEID, "String", diId),
                        new AttributeImpl(DATA_ITEM_IS_PART_OF_COLLECTION, "String", collId)));

        AttributeSetImpl collAs = new AttributeSetImpl(ORE_REM_COLLECTION,
                Arrays.<Attribute>asList(new AttributeImpl(COLLECTION_RESOURCEID, "String", collId),
                        new AttributeImpl(COLLECTION_AGGREGATES_DATAITEM, "String", diId)));

        final String packageId = "id:package";
                AttributeSetImpl pkgAs = new AttributeSetImpl(ORE_REM_PACKAGE,
                        Arrays.<Attribute>asList(new AttributeImpl(COLLECTION_RESOURCEID, "String", collId)));

        asm.addAttributeSet(composeKey(ORE_REM_DATAITEM, diId), diAs);
        asm.addAttributeSet(composeKey(ORE_REM_COLLECTION, collId), collAs);
        asm.addAttributeSet(composeKey(ORE_REM_PACKAGE, packageId), pkgAs);

        List<String> errors = new ArrayList<String>();
        OreRelationshipConstraintChecker underTest = new OreRelationshipConstraintChecker(errors);

        underTest.execute("depositId", state);

        assertEquals(0, errors.size());
    }
}
