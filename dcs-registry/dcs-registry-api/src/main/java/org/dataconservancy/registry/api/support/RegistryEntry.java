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
 * An interface encapsulating properties common to all registry entries.
 *
 * @param <T> the domain object type managed by the registry
 */
public interface RegistryEntry<T> {

//    /**
//     * Returns a human-readable name of the registry entry.
//     *
//     * @return a human-readable string, naming the entry
//     */
//    public String getName();

    /**
     * The identifier for the entry, which can be used to retrieve the
     * entry from the {@link org.dataconservancy.registry.api.Registry}.
     *
     * @return the identifier for the entry
     */
    public String getId();

    /**
     * Retrieve the underlying object.
     *
     * @return the registry entry object
     */
    public T getEntry();

    /**
     * Retrieve the entry type.  Should be the same as {@link org.dataconservancy.registry.api.Registry#getEntryType()}.
     * All entries in a single registry must have the same type.  The semantics of "type" are left to the implementation.
     * The string may represent a Java class name, or another non-opaque string such as a URI.  Other implementations
     * may choose to simply return opaque strings.
     *
     * @return the a string representing the registry entry type
     */
    public String getEntryType();
}
