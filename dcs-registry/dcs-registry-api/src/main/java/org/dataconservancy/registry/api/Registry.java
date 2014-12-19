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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A Registry provides shared mechanisms and semantics for retrieving and enumerating objects in a domain of interest
 * to the client.
 * <p/>
 * A Data Conservancy instance may have multiple registries, with each individual registry instance managing objects of
 * a single type.  For example, the License Registry manages objects of type <code>DcsLicense</code>, while the
 * Transformation Registry manages objects of type <code>Mapping</code>.
 * <p/>
 * The {@link org.dataconservancy.registry.api.Registry#getEntryType()} is used to indicate what type of entries are
 * managed by a particular registry instance.
 *
 * @param <T> the domain object type managed by this registry
 */
public interface Registry<T> extends Iterable<T> {

    /**
     * Determine whether or not this registry contains an entry identified or named by <code>id</code>.
     *
     * @param id the identifier or name of the entry
     * @return <code>true</code> if the registry contains the entry, <code>false</code> otherwise.
     */
    public boolean containsKey(String id);

    /**
     * Obtain a <code>Set</code> of all entries in this registry.  Entries are keyed by their identifier(s).  Entries
     * that have multiple identifiers will have multiple entries.
     *
     * @return a set of all entries in the registry.  May be empty, but never <code>null</code>.
     */
    public Set<Map.Entry<String, T>> entrySet();

    /**
     * Obtain the domain object identified or named by <code>id</code>.
     *
     * @param id the identifier or name of the entry
     * @return the entry, otherwise <code>null</code>
     */
    public T get(String id);

    /**
     * Obtain a <code>Set</code> of all entry identifiers in this registry.
     *
     * @return a set of all entry identifiers in the registry.  May be empty, but never <code>null</code>.
     */
    public Set<String> keySet();

    /**
     * An identifier which represents the type of entries contained in this Registry instance.  All
     * <code>RegistryEntry</code>s managed by this <code>Registry</code> will have the same
     * {@link org.dataconservancy.registry.api.support.RegistryEntry#getEntryType() type}.  The semantics of "type" are
     * left to the implementation. The string may represent a Java class name, or another non-opaque string such as a
     * URI.  Other implementations may choose to simply return opaque strings.
     * 
     * @return the a string that identifies the type of data contained in this registry
     */
    public String getEntryType();

    /**
     * {@inheritDoc}
     * <p/>
     * Provides an iterator over the domain objects held in this registry.
     *
     * @return the iterator
     */
    public Iterator<T> iterator();

}
