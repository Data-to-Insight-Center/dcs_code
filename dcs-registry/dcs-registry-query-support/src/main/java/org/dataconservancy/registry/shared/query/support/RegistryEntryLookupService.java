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

import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.profile.api.DcpProfile;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.api.support.RegistryEntry;
import org.dataconservancy.profile.api.DcpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Retrieves {@link RegistryEntry} objects from the DCS.
 */
public class RegistryEntryLookupService<T> implements LookupQueryService<RegistryEntry<T>> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DcpQueryService dcpQueryService;
    private final DcpMapper<T> modelMapper;
    private final DcpProfile profile;
    private final String entryType;

    public RegistryEntryLookupService(DcpQueryService dcpQueryService, DcpMapper<T> modelMapper, DcpProfile profile) {
        this.dcpQueryService = dcpQueryService;
        this.modelMapper = modelMapper;
        this.profile = profile;
        this.entryType = profile.getType();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation queries the DCS for Deliverable Units that either have an entity ID or formerExternalRef
     * that matches {@code id}.
     *
     * @param id the identifier or former external ref for the Deliverable Unit
     * @return the {@code RegistryEntry} represented by the Deliverable Unit and its graph of objects
     * @throws QueryServiceException {@inheritDoc}
     */
    @Override
    public RegistryEntry<T> lookup(String id) throws QueryServiceException {
        final String duQuery = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "type", entryType);
        final String idQuery = SolrQueryUtil.createLiteralQuery("OR", "id", id, "former", id);
        final String query = duQuery + " AND " + idQuery;

        Dcp dcp;

        try {
            QueryResult<Dcp> result = dcpQueryService.query(query, 0, Integer.MAX_VALUE);

            if (result.getTotal() < 1) {
                log.debug("Query {} returned no results", query);
                return null;
            }

            if (result.getTotal() > 1) {
                log.debug("Query {} returned {} results. Taking the first result.",
                        query, result.getTotal());
            }

            dcp = result.getMatches().get(0).getObject();

        } catch (QueryServiceException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        if (!profile.conforms(dcp)) {
            log.debug("DCP returned by query {} did not conform to the expected profile {}", query, profile);
            return null;
        }

        // TODO: it would be nice to have a List of model mappers so this implementation can be instantiated once instead of one instance per mapper.


        // Find the archive identifier and the business identifier from the package
        String archiveId = null;
        String businessId = null;

        // if the supplied 'id' is an archival identifier, then the mapper will discover it
        if (modelMapper.discover(dcp).contains(id)) {
            archiveId = id;
            for (DcsDeliverableUnit du : dcp.getDeliverableUnits()) {
                if (du.getId().equals(archiveId)) {
                    if (du.getFormerExternalRefs().isEmpty()) {
                        log.debug("Could not extract a business identifier for DCP {}", dcp);
                    } else {
                        businessId = du.getFormerExternalRefs().iterator().next();
                    }
                }
            }
        } else {
            // otherwise the 'id' is a business identifier
            businessId = id;
            for (DcsDeliverableUnit du : dcp.getDeliverableUnits()) {
                if (du.getFormerExternalRefs().contains(id)) {
                    archiveId = du.getId();
                }
            }
        }

        if (archiveId != null && businessId != null) {
            return new BasicRegistryEntryImpl<T>(businessId, modelMapper.from(archiveId, dcp, null), entryType);
        }

        log.debug("Unable to map DCP {} returned by query {} to a registry entry (Mapper: {}, Profile {})",
                new Object[] { dcp, query, modelMapper, profile } );

        if (archiveId == null && businessId == null) {
            log.debug("Unable to find the business id or the archive id for the registry entry {}", dcp);
        } else if (businessId == null) {
            log.debug("Unable to find the business id for the DU with archive id {} for registry entry",
                    archiveId, dcp);
        } else {
            log.debug("Unable to find the archive id for the DU with business id {} for registry entry",
                    businessId, dcp);
        }

        return null;
    }

    @Override
    public QueryResult<RegistryEntry<T>> query(String query, long offset, int matches, String... params)
            throws QueryServiceException {
        QueryResult<Dcp> results = dcpQueryService.query(query, offset, matches, params);
        if (results != null) {
            return new RegistryEntryQueryResult<T>(query, params, results, modelMapper, profile);
        }

        log.debug("Query {} (offset {}, matches {}, params {}) returned no results.",
                new Object[] { query, offset, matches, params } );

        return null;
    }

    @Override
    public void shutdown() throws QueryServiceException {
        dcpQueryService.shutdown();
    }
}
