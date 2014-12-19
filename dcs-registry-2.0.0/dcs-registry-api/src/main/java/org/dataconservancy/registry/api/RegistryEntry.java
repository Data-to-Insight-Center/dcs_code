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
package org.dataconservancy.registry.api;

import java.util.Collection;

/**
 * An interface encapsulating properties common to all registry entries.
 *
 * @param <T> the type of domain object type managed by the registry
 */
public interface RegistryEntry<T> {

    /**
     * The identifier for the entry, which can be used to retrieve the entry from the
     * {@link Registry}.
     *
     * @return the identifier for the entry, never {@code null}
     */
    public String getId();


    /**
     * The keys for the entry, which can be used to lookup the entry from the
     * {@link Registry}.
     *
     * @return the keys for the entry, may be empty, but never {@code null}
     */
    public Collection<String> getKeys();

    /**
     * Retrieve the underlying object identified by this {@code RegistryEntry}.
     *
     * @return the registry entry object
     */
    public T getEntry();

    /**
     * Retrieve the entry type.  Should be the same as {@link TypedRegistry#getType()}.
     * All entries in a single registry must have the same type.  The semantics of "type" are left to the
     * implementation. The string may represent a Java class name, or another non-opaque string such as a URI.  Other
     * implementations may choose to simply return opaque strings.
     *
     * @return the a string representing the registry entry type, never {@code null}
     */
    public String getType();

    /**
     * Provides a short, human-readable, plain text description of this {@code RegistryEntry}.
     *
     * @return the description, may be {@code null}
     */
    public String getDescription();
}
