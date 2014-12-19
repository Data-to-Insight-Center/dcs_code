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

import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.support.RegistryEntry;

import javax.print.attribute.standard.NumberUp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple registry implementation backed by a mutable Map of entries.
 */
public class InMemoryRegistry<T> implements Registry<T> {

    private String entryType;
    private Map<String, RegistryEntry<T>> entries = new HashMap<String, RegistryEntry<T>>();

    /**
     * Constructs a {@code Registry} containing entries of the specified type.  Type is specified
     * by an opaque String, but it is expected that clients of the Registry would use URIs to type
     * Registry instances.
     *
     * @param entryType an opaque string which types the entries contained in the registry
     */
    public InMemoryRegistry(String entryType) {
        if (entryType == null || entryType.trim().length() == 0) {
            throw new IllegalArgumentException("Registry entry type must not be empty or null.");
        }

        this.entryType = entryType;
    }

    /**
     * Returns the bare (not defensively copied) map of registry entries, keyed by their identifier.
     *
     * @return the registry entries
     */
    public Map<String, RegistryEntry<T>> getEntries() {
        return entries;
    }

    /**
     * Set the registry entries that are managed by this instance.
     *
     * @param entries the registry entries, keyed by their string identifier
     * @throws IllegalArgumentException if <code>entries</code> is <code>null</code>
     */
    public void setEntries(Map<String, RegistryEntry<T>> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("Entries must not be null.");
        }
        this.entries = entries;
    }

    @Override
    public boolean containsKey(String id) {
        return entries.containsKey(id);
    }

    @Override
    public Set<Map.Entry<String, T>> entrySet() {
        Map<String, T> bareEntries = new HashMap<String, T>(entries.size());
        for (Map.Entry<String, RegistryEntry<T>> e : entries.entrySet()) {
            bareEntries.put(e.getKey(), e.getValue().getEntry());
        }
        return bareEntries.entrySet();
    }

    @Override
    public T get(String id) {
        if (entries.containsKey(id)) {
            return entries.get(id).getEntry();
        }

        return null;
    }

    @Override
    public Set<String> keySet() {
        return entries.keySet();
    }

    @Override
    public String getEntryType() {
        return entryType;
    }

    @Override
    public Iterator<T> iterator() {
        if (entries.values().isEmpty()) {
            return Collections.<T>emptyList().iterator();
        }

        return new Iterator<T>() {
            final Iterator<RegistryEntry<T>> entryIterator = entries.values().iterator();

            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public T next() {
                return entryIterator.next().getEntry();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is unsupported by this implementation.");
            }
        };
    }
}
