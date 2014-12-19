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

import java.util.Set;

/**
 * A registry guaranteed to manage objects of a single type.  For example, the License {@code TypedRegistry} manages
 * objects of type {@code DcsLicense}, while the Transformation {@code TypedRegistry} manages objects of type
 * {@code Mapping}.
 * <p/>
 * The TypedRegistry {@link TypedRegistry#getType type} is used to indicate what type of entries
 * are managed by a particular registry instance.
 *
 * @param <T> the type of domain object type managed by the registry
 */
public interface TypedRegistry<T> extends Registry {

    /**
     * An identifier which represents the type of entries contained in this TypedRegistry instance.  All
     * {@code RegistryEntry} objects managed by this {@code TypedRegistry} will have the same
     * {@link RegistryEntry#getType() type}.  The semantics of "type" are left to the implementation. The string may
     * represent a Java class name, or another non-opaque string such as a URI.  Other implementations may choose to
     * simply return opaque strings.
     *
     * @return the a string that identifies the type of entries contained in this registry, never {@code null}
     */
    public String getType();

    /**
     * Obtain the {@code RegistryEntry} identified by <code>id</code>.
     *
     * @param id the identifier of the entry
     * @return the entry, otherwise {@code null}
     */
    public RegistryEntry<T> retrieve(String id);

    /**
     * Obtain {@code RegistryEntry} objects by keys.
     *
     * @param keys the key
     * @return matching entries, may be empty but never {@code null}
     */
    public Set<RegistryEntry<T>> lookup(String... keys);

}
