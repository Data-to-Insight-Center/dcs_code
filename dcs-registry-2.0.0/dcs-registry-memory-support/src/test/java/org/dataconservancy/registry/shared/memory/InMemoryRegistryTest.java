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
package org.dataconservancy.registry.shared.memory;

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class InMemoryRegistryTest {

    /**
     * The identifier of the sample registry
     */
    private static final String REGISTRY_ID = "dataconservancy:id:sample-registry";

    /**
     * Textual description of the sample registry
     */
    private static final String REGISTRY_DESC = "A sample TypedRegistry instance for testing.";

    /**
     * The type string for all entries in the sample registry
     */
    private static final String ENTRY_TYPE = "dataconservancy:types:registry-entry:sample";

    /**
     * A Map containing the registry entries.  It is populated in {@link #populateTestEntries()}.
     * {@link #newEmptyRegistryInstance()} will clear this map before returning a {@code InMemoryRegistry} instance.
     */
    private Map<String, BasicRegistryEntryImpl<String>> testEntries =
            new HashMap<String, BasicRegistryEntryImpl<String>>();

    /**
     * Populates the sample registry with entries for testing.
     */
    @Before
    public void populateTestEntries() {
        for (int i = 1; i < 5; i++) {
            BasicRegistryEntryImpl<String> entry = new BasicRegistryEntryImpl<String>();
            entry.setId("id" + i);
            entry.setEntry("Entry Object " + i);
            entry.setEntryType(ENTRY_TYPE);
            HashSet<String> keys = new HashSet<String>();
            keys.add("key/" + i * 19);
            keys.add("key/" + i * 31);
            entry.setKeys(keys);
            entry.setDescription("Sample TypedRegistry Entry " + i);
            testEntries.put(entry.getId(), entry);
        }
    }

    /**
     * Insures that the entries which were put in can be retrieved.
     *
     * @throws Exception
     */
    @Test
    public void testGetEntries() throws Exception {
        final InMemoryRegistry underTest = newRegistryInstance();

        assertNotNull(underTest.getEntries());
        assertEquals(testEntries, underTest.getEntries());
    }

    /**
     * Insures that, as expected, setting an empty set of entries on the registry will clear the backing map.
     *
     * @throws Exception
     */
    @Test
    public void testGetEntriesEmptyRegistry() throws Exception {
        final InMemoryRegistry underTest = newRegistryInstance();
        underTest.setEntries(Collections.<String, RegistryEntry>emptyMap());

        assertNotNull(underTest.getEntries());
        assertEquals(0, underTest.getEntries().size());
    }

    /**
     * Insures that adding an entry to the backing map adds the entry to the TypedRegistry.
     *
     * @throws Exception
     */
    @Test
    public void testModifyEntries() throws Exception {
        final InMemoryRegistry underTest = newRegistryInstance();

        testEntries.put("foo", new BasicRegistryEntryImpl<String>("anotherId", "the entry", ENTRY_TYPE,
                Collections.<String>emptySet(), "Another Test Entry."));

        assertEquals(testEntries, underTest.getEntries());
    }

    /**
     * You can't set a null backing map.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetEntriesWithNull() throws Exception {
        final InMemoryRegistry underTest = newRegistryInstance();

        underTest.setEntries(null);
    }

    /**
     * Asserts that an existing entry can be retrieved from the registry, and that a non-existent entry is
     * not found (returning null).
     *
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception {
        final InMemoryRegistry underTest = newRegistryInstance();

        assertTrue(testEntries.containsKey("id1"));
        assertNotNull(underTest.retrieve("id1"));
        assertEquals(testEntries.get("id1"), underTest.retrieve("id1"));

        assertFalse(testEntries.containsKey("foo"));
        assertNull(underTest.retrieve("foo"));
    }

    /**
     * Asserts that non-existent entries in the registry return null.
     *
     * @throws Exception
     */
    @Test
    public void testGetEmptyRegistry() throws Exception {
        final InMemoryRegistry underTest = newEmptyRegistryInstance();

        assertFalse(underTest.getEntries().containsKey("id1"));
        assertFalse(underTest.getEntries().containsKey("foo"));
        assertNull(underTest.retrieve("id1"));
        assertNull(underTest.retrieve("foo"));
    }

    /**
     * Asserts that the entry type is returned properly.
     *
     * @throws Exception
     */
    @Test
    public void testGetEntryType() throws Exception {
        final InMemoryRegistry underTest = newRegistryInstance();
        assertEquals(ENTRY_TYPE, underTest.getType());
    }

    /**
     * Asserts that the lookup method will return the appropriate entry.
     *
     * @throws Exception
     */
    @Test
    public void testLookup() throws Exception {
        final RegistryEntry expected = testEntries.get("id1");
        assertNotNull(expected);

        final InMemoryRegistry underTest = newRegistryInstance();

        assertEquals(expected, underTest.lookup("key/19").iterator().next());
        assertEquals(expected, underTest.lookup("key/31").iterator().next());
    }

    /**
     * Asserts that the lookup method for a non-existent key returns an empty Set.
     *
     * @throws Exception
     */
    @Test
    public void testLookupNonExistentKey() throws Exception {
        final InMemoryRegistry underTest = newRegistryInstance();

        assertEquals(0, underTest.lookup("fookey").size());
    }

    /**
     * Asserts that the lookup method can return multiple RegistryEntries if there are multiple entries
     * that share the same key.
     *
     * @throws Exception
     */
    @Test
    public void testLookupMultipleMatches() throws Exception {
        final RegistryEntry expectedExisting = testEntries.get("id2");

        // Add an entry to the backing map, which shares a key with an existing entry
        final Collection<String> entryKeys = new HashSet<String>();
        entryKeys.addAll(expectedExisting.getKeys());
        final BasicRegistryEntryImpl<String> entry = new BasicRegistryEntryImpl<String>("id", "", ENTRY_TYPE, entryKeys,
                "An entry that shares keys with another entry");
        testEntries.put(entry.getId(), entry);

        // Lookup by key, insuring that both expected entries come back.
        final InMemoryRegistry underTest = newRegistryInstance();

        Set<RegistryEntry> actualEntries = underTest.lookup("key/62");
        assertNotNull(actualEntries);
        assertEquals(2, actualEntries.size());

        assertTrue(actualEntries.contains(expectedExisting));
        assertTrue(actualEntries.contains(entry));
    }
    
    /**
     * Tests that a lookup with multiple keys works in any order
     */
    @Test
    public void testLookupMultipleKeys() {
        final RegistryEntry expected = testEntries.get("id1");
        assertNotNull(expected);

        final InMemoryRegistry underTest = newRegistryInstance();

        assertEquals(expected, underTest.lookup("key/19", "key/31").iterator().next());
        
        //Test that order doesn't matter
        assertEquals(expected, underTest.lookup("key/31", "key/19").iterator().next());
    }
    
    /**
     * Tests that if an object only has partial match to the keys provided then it is not returned.
     */
    @Test
    public void testMultipleKeysWithNonExistentKey() {
        final RegistryEntry expected = testEntries.get("id1");
        assertNotNull(expected);

        final InMemoryRegistry underTest = newRegistryInstance();

        assertEquals(0, underTest.lookup("key/19", "key/foo").size());
        
        assertEquals(0, underTest.lookup("key/31", "key/19", "key/foo").size());
    }

    @Test
    public void testMultipleKeysReturnMultipleEntries() {
        final RegistryEntry expectedExisting = testEntries.get("id2");

        // Add an entry to the backing map, which shares a key with an existing entry
        final Collection<String> entryKeys = new HashSet<String>();
        entryKeys.addAll(expectedExisting.getKeys());
        entryKeys.add("key/5");
        final BasicRegistryEntryImpl<String> entry = new BasicRegistryEntryImpl<String>("id", "", ENTRY_TYPE, entryKeys,
                "An entry that shares keys with another entry");
        testEntries.put(entry.getId(), entry);

        // Lookup by key, insuring that both expected entries come back.
        final InMemoryRegistry underTest = newRegistryInstance();

        Set<RegistryEntry> actualEntries = underTest.lookup("key/62", "key/38");
        assertNotNull(actualEntries);
        assertEquals(2, actualEntries.size());

        assertTrue(actualEntries.contains(expectedExisting));
        assertTrue(actualEntries.contains(entry));
        
        Set<RegistryEntry> singleEntry = underTest.lookup("key/62", "key/38", "key/5");
        assertNotNull(singleEntry);
        assertEquals(1, singleEntry.size());

        assertTrue(singleEntry.contains(entry));        
    }
    
    /**
     * Returns a new instance of {@code InMemoryRegistry}, populated by the {@link #testEntries} set up in the
     * {@link #populateTestEntries()} method.
     *
     * @return a populated, new InMemoryRegistry instance
     */
    InMemoryRegistry newRegistryInstance() {
        return new InMemoryRegistry(REGISTRY_ID, ENTRY_TYPE, testEntries, REGISTRY_DESC);
    }

    /**
     * Clears the map {@link #testEntries} map, and then returns a new {@code InMemoryRegistry} instance.
     *
     * @return an empty, new, InMemoryRegistry instance
     */
    InMemoryRegistry newEmptyRegistryInstance() {
        testEntries.clear();
        return newRegistryInstance();
    }
}
