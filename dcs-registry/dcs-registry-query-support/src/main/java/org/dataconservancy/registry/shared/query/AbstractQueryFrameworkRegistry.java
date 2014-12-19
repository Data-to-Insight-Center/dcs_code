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
package org.dataconservancy.registry.shared.query;


import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.api.support.QueryMatchIterator;
import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.support.RegistryEntry;
import org.dataconservancy.registry.shared.query.support.RegistryEntryLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * An abstract registry implementation backed by the Data Conservancy Query Framework.  Provided a
 * {@link org.dataconservancy.dcs.query.api.LookupQueryService} and an {@code entryType}, this implementation queries
 * a Data Conservancy instance for Deliverable Units that contain Registry Entries.
 */
public abstract class AbstractQueryFrameworkRegistry<T> implements Registry<T> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final RegistryEntryLookupService<T> lookupQueryService;
    private final String entryType;
    private final String typeQueryTemplate =
            SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "type", "%s");

    /**
     * Constructs a Registry backed by the DCS Query Framework.  The supplied {@code entryType} is a String that
     * is shared by all entries in this Registry instance.  The {@code entryType} is used to identify the Deliverable
     * Units that contain serialized entries for this Registry; that is to say, a DCS Search Query like
     * {@code entityType:DeliverableUnit AND type:entryType} will retrieve all entries contained in this Registry.
     *
     * @param entryType a String shared by all entries in this Registry, typically a URI stored as the Deliverable Unit
     *                  {@link org.dataconservancy.model.dcs.DcsDeliverableUnit#getType() type}
     * @param lookupQueryService a {@link org.dataconservancy.dcs.query.api.LookupQueryService} that returns {@link RegistryEntry} objects.
     */
    public AbstractQueryFrameworkRegistry(String entryType, RegistryEntryLookupService<T> lookupQueryService) {
        if (lookupQueryService == null) {
            throw new IllegalArgumentException("Lookup Query Service must not be null.");
        }

        if (entryType == null || entryType.trim().length() == 0) {
            throw new IllegalArgumentException("Entry type must not be null or empty.");
        }

        this.lookupQueryService = lookupQueryService;
        this.entryType = entryType;
    }

    @Override
    public boolean containsKey(String id) {
         return get(id) != null;
    }

    @Override
    public String getEntryType() {
        return entryType;
    }

    @Override
    public Set<Map.Entry<String, T>> entrySet() {
        Map<String, T> results = new HashMap<String, T>();
        QueryMatchIterator<RegistryEntry<T>> itr = null;
        try {
            itr = new QueryMatchIterator<RegistryEntry<T>>(lookupQueryService,
                    lookupQueryService.query(String.format(typeQueryTemplate, getEntryType()), 0, Integer.MAX_VALUE));
        } catch (QueryServiceException e) {
            log.error(e.getMessage(), e);
            return Collections.<String, T>emptyMap().entrySet();
        }

        while (itr.hasNext()) {
            QueryMatch<RegistryEntry<T>> match = itr.next();
            RegistryEntry<T> registryEntry = match.getObject();
            results.put(registryEntry.getId(), registryEntry.getEntry());
        }

        return results.entrySet();
    }

    @Override
    public T get(String id) {
        try {
            RegistryEntry<T> e = lookupQueryService.lookup(id);
            if (e != null) {
                return e.getEntry();
            }
        } catch (QueryServiceException e) {
            log.info("Cannot lookup " + id + ": " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Set<String> keySet() {
        QueryMatchIterator<RegistryEntry<T>> itr = null;
        HashSet<String> results = new HashSet<String>();
        try {
            itr = new QueryMatchIterator<RegistryEntry<T>>(lookupQueryService,
                    lookupQueryService.query(String.format(typeQueryTemplate, getEntryType()), 0, Integer.MAX_VALUE));
        } catch (QueryServiceException e) {
            log.error(e.getMessage(), e);
            return Collections.emptySet();
        }

        while (itr.hasNext()) {
            QueryMatch<RegistryEntry<T>> match = itr.next();
            RegistryEntry<T> registryEntry = match.getObject();
            results.add(registryEntry.getId());
        }

        return results;
    }

    @Override
    public Iterator<T> iterator() {
        final QueryMatchIterator<RegistryEntry<T>> matchIterator;
        try {
            matchIterator = new QueryMatchIterator<RegistryEntry<T>>(lookupQueryService,
                    lookupQueryService.query(String.format(typeQueryTemplate, getEntryType()), 0, Integer.MAX_VALUE));
        } catch (QueryServiceException e) {
            log.error(e.getMessage(), e);
            return Collections.<T>emptySet().iterator();
        }

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return matchIterator.hasNext();
            }

            @Override
            public T next() {
                return matchIterator.next().getObject().getEntry();
            }

            @Override
            public void remove() {
                matchIterator.remove();
            }
        };
    }

}
