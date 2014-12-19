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
 * A Registry provides mechanisms and semantics for retrieving objects in a domain of interest to the client.
 */
public interface Registry extends Iterable {

    /**
     * Provides a short, human-readable, plain text description of this {@code Registry}.
     *
     * @return the description, may be {@code null}
     */
    public String getDescription();

    /**
     * Obtain the {@code RegistryEntry} identified by {@code id}.  Identifiers are immutable, opaque, strings.  They
     * are global.  For example, the identifier for the FGDC 1998 XML format will be the same for any instance of a
     * Data Conservancy format registry.  An identifier is guaranteed to resolve to at most one {@code RegistryEntry}.
     *
     * @param id the identifier of the entry
     * @return the entry, otherwise {@code null}
     */
    public RegistryEntry retrieve(String id);

    /**
     * Obtain {@code RegistryEntry} objects by keys.  Keys differ from identifiers in that a single key may resolve to
     * one or more {@code RegistryEntry} instances.  For example, a lookup using the format identifier for the FGDC 1998
     * XML format may return multiple {@code RegistryEntry}s for known transformations, format entries, etc.
     * 
     * If more than one key is specified, an entry must contain all of the supplied keys in order to be consider a match. 
     * If an entry has more keys than what are supplied it will still be considered a match if the provided keys all match.
     * For example if an entry A has keys 5, 6, 7 and keys 6, 7 are supplied to the lookup function entry A will still be returned.
     * However if keys 7, 8 are provided it will not. 
     *
     * @param <T> the type of domain object contained in the {@code RegistryEntry}
     * @param keys the keys That the entry must contain
     * @return matching entries, may be empty but never {@code null}
     */
    public <T> Set<RegistryEntry<T>> lookup(String... keys);

}
