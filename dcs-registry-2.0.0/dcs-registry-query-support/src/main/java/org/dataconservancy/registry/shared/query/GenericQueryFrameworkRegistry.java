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

import org.dataconservancy.registry.shared.query.support.RegistryEntryLookupService;

/**
 * Concrete {@link org.dataconservancy.registry.api.Registry} implementation backed by the DCS Query Framework.
 */
public class GenericQueryFrameworkRegistry<T> extends AbstractQueryFrameworkRegistry<T> {

    /**
     * {@inheritDoc}
     * @param entryType {@inheritDoc}
     * @param lookupQueryService {@inheritDoc}
     */
    public GenericQueryFrameworkRegistry(String entryType, RegistryEntryLookupService<T> lookupQueryService) {
        super(entryType, lookupQueryService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "GenericQueryFrameworkRegistry " + super.getType();
    }    
}
