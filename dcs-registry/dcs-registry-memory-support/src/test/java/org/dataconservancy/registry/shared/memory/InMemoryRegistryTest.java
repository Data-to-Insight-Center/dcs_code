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

import org.dataconservancy.registry.api.support.RegistryEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class InMemoryRegistryTest {

    private static final String ENTRY_TYPE = "dataconservancy:types:registry-entry:sample";
    final TestEntry testEntry1 = new TestEntry("Test Registry Entry 1", "id1", "Registry Entry Value 1", ENTRY_TYPE);

    private Map<String, RegistryEntry<String>> testEntries = new HashMap<String, RegistryEntry<String>>();

    @Before
    public void populateTestEntries() {
        testEntries.put("id1", testEntry1);
        testEntries.put("id2", new TestEntry("Test Registry Entry 2", "id2", "Registry Entry Value 2", ENTRY_TYPE));
        testEntries.put("id3", new TestEntry("Test Registry Entry 3", "id3", "Registry Entry Value 3", ENTRY_TYPE));
        testEntries.put("id4", new TestEntry("Test Registry Entry 4", "id4", "Registry Entry Value 4", ENTRY_TYPE));
        testEntries.put("id5", new TestEntry("Test Registry Entry 5", "id5", "Registry Entry Value 5", ENTRY_TYPE));
    }

    @Test
    public void testGetEntries() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);
        underTest.setEntries(testEntries);

        assertNotNull(underTest.getEntries());
        assertEquals(testEntries, underTest.getEntries());
    }

    @Test
    public void testGetEntriesEmptyRegistry() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);

        assertNotNull(underTest.getEntries());
        assertEquals(0, underTest.getEntries().size());
    }

    @Test
    public void testModifyEntries() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);
        underTest.setEntries(testEntries);
        testEntries.put("foo", new TestEntry("Foo Entry", "foo", "Entry Foo", ENTRY_TYPE));

        assertEquals(testEntries, underTest.getEntries());
    }

    @Test
    public void testModifyEntriesEmptyRegistry() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);

        assertNotNull(underTest.getEntries());
        assertTrue(underTest.getEntries().isEmpty());

        underTest.getEntries().put(testEntry1.id, testEntry1);

        assertEquals(1, underTest.getEntries().size());
        assertEquals(testEntry1.getEntry(), underTest.get(testEntry1.id));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEntriesWithNull() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);

        underTest.setEntries(null);
    }

    @Test
    public void testContainsKey() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);
        underTest.setEntries(testEntries);

        assertTrue(underTest.containsKey("id1"));
        assertFalse(underTest.containsKey("foo"));
    }

    @Test
    public void testEntrySet() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);
        underTest.setEntries(testEntries);

        assertEquals(testEntries.size(), underTest.getEntries().size());
        assertEquals(testEntries.entrySet().size(), underTest.getEntries().entrySet().size());

        final Set<Map.Entry<String, String>> entriesUnderTest = underTest.entrySet();

        for (final Map.Entry<String, RegistryEntry<String>> expectedRegistryEntry : testEntries.entrySet()) {
            // The expected Map.Entry with its value unwrapped from from RegistryEntry
            final Map.Entry expectedBareEntry = new Map.Entry() {
                @Override
                public Object getKey() {
                    return expectedRegistryEntry.getValue().getId();
                }

                @Override
                public Object getValue() {
                    return expectedRegistryEntry.getValue().getEntry();
                }

                @Override
                public Object setValue(Object o) {
                    // Default method body
                    return null;
                }
            };
            assertTrue(entriesUnderTest.contains(expectedBareEntry));

        }
    }

    @Test
    public void testEntrySetEmptyRegistry() {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);

        assertNotNull(underTest.entrySet());
        assertTrue(underTest.entrySet().isEmpty());
    }

    @Test
    public void testGet() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);
        underTest.setEntries(testEntries);

        assertTrue(underTest.containsKey("id1"));
        assertNotNull(underTest.get("id1"));
        assertEquals(testEntry1.getEntry(), underTest.get("id1"));

        assertFalse(underTest.containsKey("foo"));
        assertNull(underTest.get("foo"));
    }

    @Test
    public void testGetEmptyRegistry() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);

        assertFalse(underTest.containsKey("id1"));
        assertFalse(underTest.containsKey("foo"));
        assertNull(underTest.get("id1"));
        assertNull(underTest.get("foo"));
    }

    @Test
    public void testKeySet() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);
        underTest.setEntries(testEntries);

        Set<String> expectedKeys = testEntries.keySet();
        Set<String> actualKeys = underTest.keySet();

        assertEquals(expectedKeys, actualKeys);
    }

    @Test
    public void testKeySetEmptyRegistry() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);

        assertNotNull(underTest.keySet());
        assertTrue(underTest.keySet().isEmpty());
    }

    @Test
    public void testGetEntryType() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);
        assertEquals(ENTRY_TYPE, underTest.getEntryType());
    }

    @Test
    public void testIterator() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);
        underTest.setEntries(testEntries);

        final Set<String> expectedValues = new HashSet<String>();
        for (RegistryEntry<String> expectedEntry : testEntries.values()) {
            expectedValues.add(expectedEntry.getEntry());
        }

        int count = 0;
        final Iterator<String> itrUnderTest = underTest.iterator();
        assertNotNull(itrUnderTest);
        while (itrUnderTest.hasNext()) {
            count++;
            assertTrue(expectedValues.contains(itrUnderTest.next()));
        }

        assertEquals(testEntries.keySet().size(), count);
    }

    @Test
    public void testIteratorEmptyRegistry() throws Exception {
        final InMemoryRegistry<String> underTest = new InMemoryRegistry<String>(ENTRY_TYPE);

        final Iterator<String> itrUnderTest = underTest.iterator();
        assertNotNull(itrUnderTest);
        assertFalse(itrUnderTest.hasNext());
        try {
            itrUnderTest.next();
            fail("expected a nosuchelementexception");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    private static class TestEntry implements RegistryEntry<String> {
        private String name;
        private String id;
        private String value;
        private String type;

        private TestEntry(String name, String id, String value, String type) {
            this.name = name;
            this.id = id;
            this.value = value;
            this.type = type;
        }

//        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getEntry() {
            return value;
        }

        @Override
        public String getEntryType() {
            return type;
        }
    }
}
