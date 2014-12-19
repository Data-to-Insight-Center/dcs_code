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

import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple registry implementation backed by a mutable Map of entries.
 */
public class InMemoryRegistry implements TypedRegistry {

    private String entryType;

    private String description;

    private String id;

    private Map<String, ? extends RegistryEntry> entries = new HashMap<String, BasicRegistryEntryImpl>();

    public InMemoryRegistry(String id, String entryType, Map<String, ? extends RegistryEntry> entries, String description) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("TypedRegistry ID must not be empty or null.");
        }

        if (entryType == null || entryType.trim().length() == 0) {
            throw new IllegalArgumentException("TypedRegistry entry type must not be empty or null.");
        }

        if (entries == null) {
            throw new IllegalArgumentException("TypedRegistry entries must not be null.");
        }

        checkType(entryType, entries);

        this.description = description;
        this.entries = entries;
        this.entryType = entryType;
        this.id = id;
    }


    /**
     * Constructs a {@code TypedRegistry} containing entries of the specified type.  Type is specified
     * by an opaque String, but it is expected that clients of the TypedRegistry would use URIs to type
     * TypedRegistry instances.
     *
     * @param entryType an opaque string which types the entries contained in the registry
     * @deprecated use {@link InMemoryRegistry#InMemoryRegistry(String, String, java.util.Map, String)} instead
     */
    public InMemoryRegistry(String entryType) {
        if (entryType == null || entryType.trim().length() == 0) {
            throw new IllegalArgumentException("TypedRegistry entry type must not be empty or null.");
        }

        this.entryType = entryType;
    }

    /**
     * Returns the bare (not defensively copied) map of registry entries, keyed by their identifier.
     *
     * @return the registry entries
     */
    public Map<String, ? extends RegistryEntry> getEntries() {
        return entries;
    }

    /**
     * Set the registry entries that are managed by this instance.
     *
     * @param entries the registry entries, keyed by their string identifier
     * @throws IllegalArgumentException if <code>entries</code> is <code>null</code>
     */
    public void setEntries(Map<String, ? extends RegistryEntry> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("Entries must not be null.");
        }

        checkType(this.entryType, entries);

        this.entries = entries;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public RegistryEntry retrieve(String id) {
        return entries.get(id);
    }

    @Override
    public Set<RegistryEntry> lookup(String... keys) {
        Set<RegistryEntry> result = new HashSet<RegistryEntry>();
        for (RegistryEntry entry : entries.values()) {
            if (entry.getKeys().contains(keys[0])) {
                result.add(entry);
                //If we have more than one key make sure that all the other keys are also on the entry
                if (keys.length > 1) {
                    for (int i = 1; i < keys.length; i++) {
                        //If the key isn't on the entry remove it from the result and move to the next entry
                        if (!entry.getKeys().contains(keys[i])) {
                            result.remove(entry);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public String getType() {
        return entryType;
    }

    @Override
    public Iterator iterator() {
        return entries.values().iterator();
    }

    /**
     * Asserts that every RegistryEntry in the {@code entries Set} is equal to {@code entryType}.
     *
     * @param entryType
     * @param entries
     * @throws IllegalArgumentException if the {@code entryType} doesn't match the {@code RegistryEntry} type
     */
    private void checkType(String entryType, Map<String, ? extends RegistryEntry> entries) {
        for (RegistryEntry entry : entries.values()) {
            if (!entryType.equals(entry.getType())) {
                throw new IllegalArgumentException("TypedRegistry entries must all be of type " + entryType + " (entry " +
                        entry + " was of type " + entry.getType());
            }
        }
    }

}
