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
package org.dataconservancy.registry.shared.query.support;

import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.support.DcpUtil;
import org.dataconservancy.profile.api.DcpProfile;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.profile.api.DcpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends the Query Framework {@link QueryResult} by wrapping each {@link QueryMatch} in a {@link org.dataconservancy.registry.api.support.RegistryEntry}.
 */
public class RegistryEntryQueryResult<T> extends QueryResult<RegistryEntry<T>> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final QueryResult<Dcp> delegate;
    private final DcpMapper<RegistryEntry<T>> modelMapper;
    private final DcpProfile profile;

    /**
     * Constructs a new result object, initialized with original query objects, and the appropriate
     * <code>modelMapper</code>.
     *
     * @param queryString the original query string
     * @param queryParams the original query parameters
     * @param dcpQueryResult the original query result
     * @param modelMapper the model mapper
     */
    public RegistryEntryQueryResult(String queryString, String[] queryParams,
                                    QueryResult<Dcp> dcpQueryResult, DcpMapper<RegistryEntry<T>> modelMapper, DcpProfile profile) {
        super(dcpQueryResult.getOffset(), dcpQueryResult.getTotal(), queryString, queryParams);
        this.delegate = dcpQueryResult;
        this.modelMapper = modelMapper;
        this.profile = profile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QueryMatch<RegistryEntry<T>>> getMatches() {
        List<QueryMatch<RegistryEntry<T>>> registryMatches = new ArrayList<QueryMatch<RegistryEntry<T>>>();

        for (QueryMatch<Dcp> dcpMatch : delegate.getMatches()) {

            if (!profile.conforms(dcpMatch.getObject())) {
                log.debug("DCP {} did not conform to expected Profile {}", dcpMatch.getObject(), profile);
                continue;
            }

            for (String archiveId : modelMapper.discover(dcpMatch.getObject())) {
                DcsDeliverableUnit du = (DcsDeliverableUnit)DcpUtil.asMap(dcpMatch.getObject()).get(archiveId);
                if (du.getFormerExternalRefs().isEmpty()) {
                    log.debug("DU {} did not contain any formerExternalRefs, unable to obtain business id", du);
                    continue;
                }
                RegistryEntry<T> registryEntry = modelMapper.from(archiveId, dcpMatch.getObject(), null);
                QueryMatch<RegistryEntry<T>> registryMatch = new QueryMatch<RegistryEntry<T>>(registryEntry, dcpMatch.getContext());
                registryMatches.add(registryMatch);
            }
        }

        return registryMatches;
    }

}
