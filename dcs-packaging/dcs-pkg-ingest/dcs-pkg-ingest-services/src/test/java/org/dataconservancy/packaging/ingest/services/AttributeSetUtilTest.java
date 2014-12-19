package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.dataconservancy.packaging.ingest.services.AttributeSetUtil.composeKey;
import static org.dataconservancy.packaging.ingest.services.AttributeSetUtil.decomposeKey;
import static org.dataconservancy.packaging.ingest.services.AttributeSetUtil.values;
import static org.dataconservancy.packaging.model.AttributeSetName.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class AttributeSetUtilTest {

    @Test
    public void testComposeKey() throws Exception {
        assertEquals("foo_bar", composeKey("foo", "bar"));

        // id with "special" chars
        String id = "file:///sample%20bag/path/to/a/file";
        assertEquals("foo_" + id, composeKey("foo", id));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComposeKeyIllegalChars() throws Exception {
        // id with illegal char
        String illegal = "foo" + AttributeSetUtil.CONCAT_CHAR + "bar";
        composeKey("unf", illegal);
    }

    @Test
    public void testDecomposeKey() throws Exception {
        final String[] keyParts = new String[] { "foo", "bar" };
        final String key = keyParts[0] + "_" + keyParts[1];

        assertEquals(keyParts[0], decomposeKey(key)[0]);
        assertEquals(keyParts[1], decomposeKey(key)[1]);
    }

    @Test
    public void testComposeDecomposeRoundTrip() throws Exception {
        final String component1 = "foo";
        final String component2 = "bar";

        final String key = composeKey(component1, component2);

        assertEquals(component1, decomposeKey(key)[0]);
        assertEquals(component2, decomposeKey(key)[1]);
    }

    /**
     * Documents that the matchers in AttributeSetUtil won't match empty attribute sets.  This is actually logic that
     * is not controlled by the matchers, but by the AttributeSetManagerImpl.
     *
     * @throws Exception
     */
    @Test
    public void testMatchersWontMatchEmptyAttributeSets() throws Exception {
        final AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("foo", new AttributeSetImpl(ORE_REM_PACKAGE));
        assertEquals(0, asm.matches(AttributeSetUtil.ORE_PACKAGE_MATCHER).size());
    }

    /**
     * Insures that the ORE_PACKAGE_MATCHER works as expected.  It won't match empty AttributeSets, so we have to
     * populate them.
     *
     * @throws Exception
     */
    @Test
    public void testOrePackageMatcher() throws Exception {
        final AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("foo", as(ORE_REM_PACKAGE, attr("foo", "bar")));
        asm.addAttributeSet("baz", as(ORE_REM_DATAITEM, attr("buz", "zub")));
        asm.addAttributeSet("biz", as(ORE_REM_PACKAGE, attr("baz", "biz")));

        assertEquals(2, asm.matches(AttributeSetUtil.ORE_PACKAGE_MATCHER).size());
    }

    /**
     * Insures that the ORE_PROJECT_MATCHER works as expected.  It won't match empty AttributeSets, so we have to
     * populate them.
     *
     * @throws Exception
     */
    @Test
    public void testOreProjectMatcher() throws Exception {
        final AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("foo", as(ORE_REM_PROJECT, attr("foo", "bar")));
        asm.addAttributeSet("bar", as(ORE_REM_COLLECTION, attr("biz", "boo")));
        asm.addAttributeSet("baz", as(ORE_REM_PROJECT, attr("pee", "wee")));

        assertEquals(2, asm.matches(AttributeSetUtil.ORE_PROJECT_MATCHER).size());
    }

    /**
     * Insures that the ORE_COLLECTION_MATCHER works as expected.  It won't match empty AttributeSets, so we have to
     * populate them.
     *
     * @throws Exception
     */
    @Test
    public void testOreCollectionMatcher() throws Exception {
        final AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("foo", as(ORE_REM_COLLECTION, attr("foo", "bar")));
        asm.addAttributeSet("baz", as(ORE_REM_PROJECT, attr("pee", "wee")));
        asm.addAttributeSet("bar", as(ORE_REM_COLLECTION, attr("biz", "boo")));

        assertEquals(2, asm.matches(AttributeSetUtil.ORE_COLLECTION_MATCHER).size());
    }

    /**
     * Insures that the ORE_DATAITEM_MATCHER works as expected.  It won't match empty AttributeSets, so we have to
     * populate them.
     *
     * @throws Exception
     */
    @Test
    public void testOreDataItemMatcher() throws Exception {
        final AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("foo", as(ORE_REM_DATAITEM, attr("foo", "bar")));
        asm.addAttributeSet("baz", as(ORE_REM_PROJECT, attr("pee", "wee")));
        asm.addAttributeSet("bar", as(ORE_REM_DATAITEM, attr("biz", "boo")));

        assertEquals(2, asm.matches(AttributeSetUtil.ORE_DATAITEM_MATCHER).size());
    }

    /**
     * Insures that the ORE_DATAITEM_MATCHER works as expected.  It won't match empty AttributeSets, so we have to
     * populate them.
     *
     * @throws Exception
     */
    @Test
    public void testOreFileMatcher() throws Exception {
        final AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("foo", as(ORE_REM_FILE, attr("foo", "bar")));
        asm.addAttributeSet("baz", as(ORE_REM_PROJECT, attr("pee", "wee")));
        asm.addAttributeSet("bar", as(ORE_REM_FILE, attr("biz", "boo")));

        assertEquals(2, asm.matches(AttributeSetUtil.ORE_FILE_MATCHER).size());
    }

    @Test
    public void testValues() throws Exception {
        final AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        asm.addAttributeSet("foo", as(ORE_REM_FILE, attr("foo", "bar"), attr("foo", "boo")));
        asm.addAttributeSet("baz", as(ORE_REM_PROJECT, attr("pee", "wee")));
        asm.addAttributeSet("bar", as(ORE_REM_FILE, attr("foo", "boo")));


        Set<String> results = AttributeSetUtil.values(asm.getAttributeSet("foo"), "foo");
        assertEquals(2, results.size());
        assertTrue(results.contains("bar"));
        assertTrue(results.contains("boo"));

        results = AttributeSetUtil.values(asm.getAttributeSet("bar"), "foo");
        assertEquals(1, results.size());
        assertTrue(results.contains("boo"));

        results = AttributeSetUtil.values(asm.getAttributeSet("baz"), "non-existent");
        assertEquals(0, results.size());
    }

    /**
     * Insures that the values() method obtains the value of attributes in an AttributeSet properly.
     * @throws Exception
     */
    @Test
    public void testValuesTwo() throws Exception {
        AttributeSetImpl as = new AttributeSetImpl("foo");
        AttributeImpl attr1 = new AttributeImpl("attr1", "String", "attr1Value");
        AttributeImpl attr2a = new AttributeImpl("attr2", "String", "attr2Value2");
        AttributeImpl attr2b = new AttributeImpl("attr2", "String", "attr2Value1");
        as.addAttribute(attr1, attr2a, attr2b);

        Set<String> values = values(as, attr1.getName());
        assertEquals(1, values.size());
        assertTrue(values.contains(attr1.getValue()));

        values = values(as, attr2a.getName());
        assertEquals(2, values.size());
        assertTrue(values.contains(attr2a.getValue()));
        assertTrue(values.contains(attr2b.getValue()));

        values = values(as, "non-existent-attribute-name");
        assertEquals(0, values.size());
    }


    /**
     * Convenience method which creates an AttributeSet with the supplied name (with name being a proxy for the type of
     * AttributeSet - see javadoc on OreRelationshipTypeChecker) and Attributes.
     *
     * @param type
     * @param attributes
     * @return
     */
    private AttributeSetImpl as(String type, Attribute... attributes) {
        return new AttributeSetImpl(type, Arrays.asList(attributes));
    }

    /**
     * Convenience method which creates a new "String" attribute with the supplied name and value.
     *
     * @param name
     * @param value
     * @return
     */
    private Attribute attr(String name, String value) {
        return new AttributeImpl(name, "String", value);
    }
}
