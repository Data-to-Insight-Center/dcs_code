package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.dataconservancy.packaging.ingest.services.AttributeSetUtil.composeKey;
import static org.dataconservancy.packaging.ingest.services.AttributeSetUtil.values;
import static org.dataconservancy.packaging.model.AttributeSetName.ORE_REM_COLLECTION;
import static org.dataconservancy.packaging.model.AttributeSetName.ORE_REM_PACKAGE;
import static org.dataconservancy.packaging.model.AttributeSetName.ORE_REM_PROJECT;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_IDENTIFIER;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_RESOURCEID;
import static org.dataconservancy.packaging.model.Metadata.PACKAGE_AGGREGATES_PROJECT;
import static org.dataconservancy.packaging.model.Metadata.PROJECT_IDENTIFIER;
import static org.dataconservancy.packaging.model.Metadata.PROJECT_RESOURCEID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 *
 */
public class OreRelationshipTypeCheckerTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private IngestWorkflowState state;

    private AttributeSetManager asm;

    @Before
    public void setUp() throws Exception {
        state = MockUtil.mockState();
        asm = new AttributeSetManagerImpl();
        when(state.getAttributeSetManager()).thenReturn(asm);
    }

    /**
     * A successful run of the OreRelationshipTypeChecker will execute without any errors.  A simple graph is made:
     * an ORE_REM_PACKAGE AttributeSet referencing a ORE_REM_PROJECT AttributeSet using the PACKAGE_AGGREGATES_PROJECT
     * Attribute whose value is the resource ID of the aggregated Project.
     *
     * @throws Exception
     */
    @Test
    public void testCheckPackageAggregatesProject() throws Exception {
        final String remUri = "urn:uri:aRem";
        final String projectId = "urn:uri:projectId";

        final AttributeSetImpl rem = createAs(ORE_REM_PACKAGE);
        rem.addAttribute(createAttr(PACKAGE_AGGREGATES_PROJECT, "String", projectId));
        addAs(asm, composeKey(ORE_REM_PACKAGE, remUri), rem);

        final AttributeSetImpl projectAs = createAs(ORE_REM_PROJECT);
        projectAs.addAttribute(createAttr(PROJECT_RESOURCEID, "String", projectId));
        addAs(asm, composeKey(ORE_REM_PROJECT, projectId), projectAs);

        final List<String> errors = new ArrayList<String>();
        OreRelationshipTypeChecker underTest = new OreRelationshipTypeChecker(errors);
        underTest.execute("foo", state);

        for (String err : errors) {
            log.info(err);
        }

        assertTrue(errors.isEmpty());
    }

    /**
     * Insures that the logic behind the findAttributeSetContainingId(...) method is correct.  The method is supposed
     * to find any attribute set that contains the supplied identifier.  It does this by searching for attribute sets
     * that contain ResourceId Attributes that match a particular value.
     * @throws Exception
     */
    @Test
    public void testFindAttributeSetContainingId() throws Exception {
        AttributeSetImpl noIdentifierAttr = new AttributeSetImpl("no-identifier");
        final String projectId = "projectId";
        AttributeSetImpl withIdentifierAttr = new AttributeSetImpl("with-identifier");
        AttributeImpl identifierAttr = new AttributeImpl(PROJECT_RESOURCEID, "String", projectId);
        withIdentifierAttr.addAttribute(identifierAttr);

        asm.addAttributeSet("anyKey", withIdentifierAttr);
        asm.addAttributeSet("anyOtherKey", noIdentifierAttr);

        Set<AttributeSet> results = OreRelationshipTypeChecker.findAttributeSetContainingId(asm, projectId);
        assertEquals(1, results.size());
        assertTrue(results.contains(withIdentifierAttr));

        results = OreRelationshipTypeChecker.findAttributeSetContainingId(asm, "non-existent-id");
        assertEquals(0, results.size());

        AttributeSetImpl duplicateIdentifier = new AttributeSetImpl("with-the-same-identifier");
        duplicateIdentifier.addAttribute(new AttributeImpl(PROJECT_RESOURCEID, "String", projectId));
        asm.addAttributeSet("duplicateSet", duplicateIdentifier);

        results = OreRelationshipTypeChecker.findAttributeSetContainingId(asm, projectId);
        assertEquals(2, results.size());
        assertTrue(results.contains(withIdentifierAttr));
        assertTrue(results.contains(duplicateIdentifier));
    }



    /**
     * A test looking up the types of a projectId that has exactly one corresponding attribute set
     * @throws Exception
     */
    @Test
    public void testTypesSimple() throws Exception {
        final String projectId = "projectId";
        AttributeSetImpl project1 = new AttributeSetImpl(ORE_REM_PROJECT);
        project1.addAttribute(new AttributeImpl(PROJECT_RESOURCEID, "String", projectId));
        asm.addAttributeSet(composeKey(ORE_REM_PROJECT, projectId), project1);

        final Map<String, Set<String>> types = OreRelationshipTypeChecker.types(Arrays.asList(projectId), asm);
        assertEquals(1, types.size());
        assertTrue(types.containsKey(projectId));

        final Set<String> projectTypes = types.get(projectId);

        assertEquals(1, projectTypes.size());
        assertEquals(ORE_REM_PROJECT, projectTypes.iterator().next());
    }

    /**
     * A test looking up the types of a projectId that doesn't have a corresponding attribute set
     * @throws Exception
     */
    @Test
    public void testTypesMissingIdReference() throws Exception {
        final String projectId = "projectId";

        final Map<String, Set<String>> types = OreRelationshipTypeChecker.types(Arrays.asList(projectId), asm);
        assertEquals(1, types.size());
        assertTrue(types.containsKey(projectId));

        final Set<String> projectTypes = types.get(projectId);

        assertEquals(0, projectTypes.size());
    }

    /**
     * A test looking up the types of multiple project ids; each project id has exactly one corresponding attribute set.
     * @throws Exception
     */
    @Test
    public void testTypesWithMultipleProjectIds() throws Exception {
        final String projectId1 = "projectId1";
        final String projectId2 = "projectId2";

        final AttributeSetImpl project1 = new AttributeSetImpl(ORE_REM_PROJECT);
        final AttributeSetImpl project2 = new AttributeSetImpl(ORE_REM_PROJECT);

        project1.addAttribute(new AttributeImpl(PROJECT_RESOURCEID, "String", projectId1));
        project2.addAttribute(new AttributeImpl(PROJECT_RESOURCEID, "String", projectId2));

        asm.addAttributeSet(composeKey(ORE_REM_PROJECT, projectId1), project1);
        asm.addAttributeSet(composeKey(ORE_REM_PROJECT, projectId2), project2);

        final Map<String, Set<String>> types = OreRelationshipTypeChecker.types(
                Arrays.asList(projectId1, projectId2), asm);

        assertEquals(2, types.size());
        assertTrue(types.containsKey(projectId1));
        assertTrue(types.containsKey(projectId2));

        final Set<String> typesForProject1 = types.get(projectId1);
        final Set<String> typesForProject2 = types.get(projectId2);

        assertEquals(1, typesForProject1.size());
        assertTrue(typesForProject1.contains(ORE_REM_PROJECT));
        assertEquals(1, typesForProject2.size());
        assertTrue(typesForProject2.contains(ORE_REM_PROJECT));
    }

    /**
     * A single project with two types: it has a project attribute set, which is correct.  But it also has a collection
     * attribute set which is incorrect.  Nevertheless, both types should be reported by this method.
     * @throws Exception
     */
    @Test
    public void testTypesWithMultipleAttributeSets() throws Exception {
        final String projectId = "projectId";

        final AttributeSetImpl project1 = new AttributeSetImpl(ORE_REM_PROJECT);
        final AttributeSetImpl collection1 = new AttributeSetImpl(ORE_REM_COLLECTION);

        project1.addAttribute(new AttributeImpl(PROJECT_RESOURCEID, "String", projectId));
        collection1.addAttribute(new AttributeImpl(COLLECTION_RESOURCEID, "String", projectId));

        asm.addAttributeSet(composeKey(ORE_REM_PROJECT, projectId), project1);
        asm.addAttributeSet(composeKey(ORE_REM_COLLECTION, projectId), collection1);

        final Map<String, Set<String>> types = OreRelationshipTypeChecker.types(
                Arrays.asList(projectId), asm);

        assertEquals(1, types.size());
        assertTrue(types.containsKey(projectId));

        final Set<String> typesForProject = types.get(projectId);

        assertEquals(2, typesForProject.size());
        assertTrue(typesForProject.contains(ORE_REM_PROJECT));
        assertTrue(typesForProject.contains(ORE_REM_COLLECTION));
    }

    private AttributeSetImpl createAs(String name) {
        return new AttributeSetImpl(name);
    }

    private AttributeImpl createAttr(String name, String type, String value) {
        return new AttributeImpl(name, type, value);
    }

    private void addAs(AttributeSetManager asm, String key, AttributeSet as) {
        asm.addAttributeSet(key, as);
    }
}
