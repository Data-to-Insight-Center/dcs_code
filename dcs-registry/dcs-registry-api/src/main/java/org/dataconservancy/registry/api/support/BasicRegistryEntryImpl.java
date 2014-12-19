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

/**
 * A convenience implementation of {@link RegistryEntry}.
 *
 * @param <T> the domain object wrapped by the entry
 */
public class BasicRegistryEntryImpl<T> implements RegistryEntry<T> {

    private String name;
    private String id;
    private T entry;
    private String entryType;

    public BasicRegistryEntryImpl() {

    }

    public BasicRegistryEntryImpl(String id, T entry, String entryType) {
        this.id = id;
        this.entry = entry;
        this.entryType = entryType;
    }

//    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public T getEntry() {
        return entry;
    }

    @Override
    public String getEntryType() {
        return entryType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEntry(T entry) {
        this.entry = entry;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }
}
