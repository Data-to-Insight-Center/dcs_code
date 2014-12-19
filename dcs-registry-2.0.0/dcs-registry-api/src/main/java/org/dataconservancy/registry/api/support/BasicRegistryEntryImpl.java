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
package org.dataconservancy.registry.api.support;

import org.dataconservancy.registry.api.RegistryEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A convenience implementation of {@link org.dataconservancy.registry.api.RegistryEntry}.
 *
 * @param <T> the type of domain object wrapped by the entry
 */
public class BasicRegistryEntryImpl<T> implements RegistryEntry<T> {

    private String id;
    private T entry;
    private String entryType;
    private String description;
    private Set<String> keys = new HashSet<String>(1);

    public BasicRegistryEntryImpl() {

    }

    public BasicRegistryEntryImpl(String id, T entry, String entryType, Collection<String> keys, String description) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("Entry ID must not be empty or null.");
        }

        if (entryType == null || entryType.trim().length() == 0) {
            throw new IllegalArgumentException("Entry type must not be empty or null.");
        }

        if (keys == null) {
            throw new IllegalArgumentException("Entry keys must not be null.");
        }

        if (entry == null) {
            throw new IllegalArgumentException("Entry must not be null.");
        }

        this.id = id;
        this.entry = entry;
        this.entryType = entryType;
        this.keys.addAll(keys);
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<String> getKeys() {
        return this.keys;
    }

    @Override
    public T getEntry() {
        return entry;
    }

    @Override
    public String getType() {
        return entryType;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setId(String id) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("ID must not be empty or null.");
        }
        this.id = id;
    }

    public void setKeys(Collection<String> keys) {
        if (keys == null) {
            throw new IllegalArgumentException("Keys must not be null.");
        }

        this.keys.addAll(keys);
    }

    public void setEntry(T entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry must not be null.");
        }
        this.entry = entry;
    }

    public void setEntryType(String entryType) {
        if (entryType == null || entryType.trim().length() == 0) {
            throw new IllegalArgumentException("Entry type must not be empty or null.");
        }
        this.entryType = entryType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicRegistryEntryImpl that = (BasicRegistryEntryImpl) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (entry != null ? !entry.equals(that.entry) : that.entry != null) return false;
        if (entryType != null ? !entryType.equals(that.entryType) : that.entryType != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (keys != null ? !keys.equals(that.keys) : that.keys != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (entry != null ? entry.hashCode() : 0);
        result = 31 * result + (entryType != null ? entryType.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (keys != null ? keys.hashCode() : 0);
        return result;
    }
}
