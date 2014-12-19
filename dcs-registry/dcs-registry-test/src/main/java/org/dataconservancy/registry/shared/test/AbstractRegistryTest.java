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

import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.support.RegistryEntry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Base test harness for Registry functional tests.
 */
public abstract class AbstractRegistryTest<T> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The instance under test.
     */
    protected Registry<T> underTest;

    /**
     * Obtain the instance of the Registry that is under test.
     *
     * @return the registry instance
     */
    protected abstract Registry<T> getUnderTest();

    /**
     * Obtain the entries that are expected to be contained in the registry under test.  The map is keyed by
     * strings that identify the entry.  It is possible to have multiple identifiers for the same entry.
     * 
     * @return the expected registry entries, keyed by registry entry identifier
     */
    protected abstract Map<String, RegistryEntry<T>> getExpectedEntries();

    /**
     * Asserts that each {@link #getExpectedEntries() expected entry} is present in the
     * registry under test.  This exercises {@link Registry#containsKey(String)} and
     * {@link Registry#get(String)}.
     */
    @Test
    public void verifyRegistryDomainObjectsByIdLookup() {
        final Map<String, RegistryEntry<T>> expectedEntries = getExpectedEntries();
        assertTrue("No expected entries were found!", expectedEntries.size() > 0);

        for (Map.Entry<String, RegistryEntry<T>> e : expectedEntries.entrySet()) {
            final String expectedId = e.getKey();
            final RegistryEntry<T> expectedEntry = e.getValue();

            assertTrue("Registry should contain entry " + expectedId,
                    underTest.containsKey(expectedId));
            assertNotNull("Null object returned for entry " + expectedId,
                    underTest.get(expectedId));
            assertEquals(expectedEntry.getEntry(), underTest.get(expectedId));
        }
    }

    /**
     * Asserts that each {@link #getExpectedEntries() expected entry} is present in the
     * registry under test.  This exercises {@link Registry#entrySet()} and {@link Registry#get(String)}.
     */
    @Test
    public void verifyRegistryDomainObjectsByEntrySet() {
        final Map<String, RegistryEntry<T>> expectedEntries = getExpectedEntries();
        assertTrue("No expected entries were found!", expectedEntries.size() > 0);

        final Set<Map.Entry<String, T>> actualEntries = underTest.entrySet();
        assertTrue("No actual entries were found!", actualEntries.size() > 0);
        assertEquals("Expected " + expectedEntries.size() + " but found " + actualEntries.size() + " instead!",
                expectedEntries.size(), actualEntries.size());

        // Populate a set of expected and actual ids
        final Set<String> expectedIds = expectedEntries.keySet();
        final Set<String> actualIds = new HashSet<String>();

        for (Map.Entry<String, T> actualEntry : actualEntries) {
            actualIds.add(actualEntry.getKey());
        }

        // Each expected registry entry should be in the actual entry set
        for (String expectedId : expectedIds) {
            assertTrue("Registry should contain entry " + expectedId,
                    actualIds.contains(expectedId));
        }

        // Each actual entry should be in the expected entries
        for (String expectedId : actualIds) {
            assertTrue("Unexpected entry in the registry: " + expectedId, expectedIds.contains(expectedId));
        }

        // Test the domain objects for equality
        for (Map.Entry<String, RegistryEntry<T>> e : expectedEntries.entrySet()) {
            final RegistryEntry<T> expectedEntry = e.getValue();
            final String expectedId = e.getKey();
            assertTrue(actualEntries.contains(new Map.Entry<String, T>() {
                @Override
                public String getKey() {
                    return expectedId;
                }

                @Override
                public T getValue() {
                    return expectedEntry.getEntry();
                }

                @Override
                public T setValue(T t) {
                    return null;
                }
            }));
        }
    }

}
