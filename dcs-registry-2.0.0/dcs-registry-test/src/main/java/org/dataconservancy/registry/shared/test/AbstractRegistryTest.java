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
package org.dataconservancy.registry.shared.test;

import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.RegistryEntry;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Base test harness for TypedRegistry functional tests.
 */
public abstract class AbstractRegistryTest<T> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The instance under test.
     */
    protected TypedRegistry<T> underTest;

    /**
     * Obtain the instance of the TypedRegistry that is under test.
     *
     * @return the registry instance
     */
    protected abstract TypedRegistry<T> getUnderTest();

    /**
     * Obtain the entries that are expected to be contained in the registry under test.  The map is keyed by
     * strings that identify the entry.  It is possible to have multiple identifiers for the same entry.
     * 
     * @return the expected registry entries, keyed by registry entry identifier
     */
    protected abstract Map<String, RegistryEntry<T>> getExpectedEntries();

    /**
     * Asserts that each {@link #getExpectedEntries() expected entry} is present in the
     * registry under test.  This exercises {@link org.dataconservancy.registry.api.TypedRegistry#containsKey(String)} and
     * {@link org.dataconservancy.registry.api.TypedRegistry#get(String)}.
     */
    @Test
    public void verifyRegistryDomainObjectsByIdLookup() {
        final Map<String, RegistryEntry<T>> expectedEntries = getExpectedEntries();
        assertTrue("No expected entries were found!", expectedEntries.size() > 0);

        for (Map.Entry<String, RegistryEntry<T>> e : expectedEntries.entrySet()) {
            final String expectedId = e.getKey();
            final RegistryEntry<T> expectedEntry = e.getValue();
           
            assertNotNull("Null object returned for entry " + expectedId,
                    underTest.retrieve(expectedId));
            assertEquals(expectedEntry.getEntry(), underTest.retrieve(expectedId).getEntry());
        }
    }

    /**
     * Asserts that each {@link #getExpectedEntries() expected entry} is present in the
     * registry under test.  This exercises {@link org.dataconservancy.registry.api.TypedRegistry#iterator()}.
     */
    @Test
    public void verifyRegistryDomainObjectsByIterator() {
        final Map<String, RegistryEntry<T>> expectedEntries = getExpectedEntries();
        assertTrue("No expected entries were found!", expectedEntries.size() > 0);

        final Set<RegistryEntry<T>> actualEntries = new HashSet<RegistryEntry<T>>();
        Iterator<RegistryEntry<T>> iter = underTest.iterator();
        while (iter.hasNext()) {
            actualEntries.add(iter.next());
        }
        assertTrue("No actual entries were found!", actualEntries.size() > 0);
        assertEquals("Expected " + expectedEntries.size() + " but found " + actualEntries.size() + " instead!",
                expectedEntries.size(), actualEntries.size());

        // Populate a set of expected and actual ids
        final Set<String> expectedIds = expectedEntries.keySet();
        final Set<String> actualIds = new HashSet<String>();

        for (RegistryEntry<T> actualEntry : actualEntries) {
            actualIds.add(actualEntry.getId());
        }

        // Each expected registry entry should be in the actual entry set
        for (String expectedId : expectedIds) {
            assertTrue("TypedRegistry should contain entry " + expectedId,
                    actualIds.contains(expectedId));
        }

        // Each actual entry should be in the expected entries
        for (String expectedId : actualIds) {
            assertTrue("Unexpected entry in the registry: " + expectedId, expectedIds.contains(expectedId));
        }

        //Test domain objects for equality
        Set<T> expectedObjects = new HashSet<T>();
        Iterator<Map.Entry<String, RegistryEntry<T>>> expectedIter = expectedEntries.entrySet().iterator();
        while (expectedIter.hasNext()) {
            expectedObjects.add(expectedIter.next().getValue().getEntry());
        }
        
        for (RegistryEntry<T> entry : actualEntries) {
            assertTrue(expectedObjects.contains(entry.getEntry()));
            expectedObjects.remove(entry.getEntry());
        }
        
        assertEquals("Expected entries contained more objects then actual.", 0, expectedObjects.size());
    }
    
    /**
     * Asserts that each {@link #getExpectedEntries() expected entry} is present in the
     * registry under test.  This exercises {@link org.dataconservancy.registry.api.TypedRegistry#lookup()}.
     */
    @Test
    public void verifyRegistryDomainObjectsByKey() {
        final Map<String, RegistryEntry<T>> expectedEntries = getExpectedEntries();
        assertTrue("No expected entries were found!", expectedEntries.size() > 0);

        //First find all entries with just a single key
        for (Map.Entry<String, RegistryEntry<T>> e : expectedEntries.entrySet()) {
            Collection<String> keys = e.getValue().getKeys();
            assertTrue("Expected there to be at least one key on expected entry.", keys.size() > 0);
            final String expectedKey = keys.iterator().next();
            final RegistryEntry<T> expectedEntry = e.getValue();

            Set<RegistryEntry<T>> results = underTest.lookup(expectedKey);
            assertEquals("Expected only one result for key: " + expectedKey, 1, results.size());
            assertEquals(expectedEntry.getEntry(), results.iterator().next().getEntry());
        }
        
        //Now try to find all entries with multiple keys
        for (Map.Entry<String, RegistryEntry<T>> e : expectedEntries.entrySet()) {
            String[] keys = new String[e.getValue().getKeys().size()];
            e.getValue().getKeys().toArray(keys);
            assertTrue("Expected there to be more than one key on expected entry.", keys.length > 1);
            final RegistryEntry<T> expectedEntry = e.getValue();

            Set<RegistryEntry<T>> results = underTest.lookup(keys);
            assertEquals("Expected only one result for key: " + keys, 1, results.size());
            assertEquals(expectedEntry.getEntry(), results.iterator().next().getEntry());
        }
    }

}
