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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.model.dcs.support.DcpUtil;
import org.dataconservancy.profile.api.DcpProfile;
import org.dataconservancy.registry.api.support.BasicRegistryEntryMapper;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.profile.api.DcpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Retrieves {@link RegistryEntry} objects from the DCS.
 */
public class RegistryEntryLookupService<T> implements LookupQueryService<RegistryEntry<T>> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DcpQueryService dcpQueryService;
    private final DcpMapper<RegistryEntry<T>> modelMapper;
    private final DcpProfile profile;
    private final String entryType;

    public RegistryEntryLookupService(DcpQueryService dcpQueryService, DcpMapper<RegistryEntry<T>> modelMapper, DcpProfile profile) {
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
        final String entryDuQuery = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "type", BasicRegistryEntryMapper.REGISTRY_ENTRY_DU_TYPE);
        final String idQuery = SolrQueryUtil.createLiteralQuery("OR", "id", id, "former", id);
        final String entryQuery = entryDuQuery + " AND " + idQuery;

        Dcp registryEntryDcp = new Dcp();
        try {
            QueryResult<Dcp> result = dcpQueryService.query(entryQuery, 0, Integer.MAX_VALUE);

            if (result.getTotal() < 1) {
                log.debug("Query {} returned no results", entryQuery);
                return null;
            }

            if (result.getTotal() > 1) {
                log.debug("Query {} returned {} results. Taking the first result.",
                        entryQuery, result.getTotal());
            }
            
            Dcp entryDcp = result.getMatches().get(0).getObject();
            Dcp objectDcp = null;
            Iterator<DcsDeliverableUnit> duIter = entryDcp.getDeliverableUnits().iterator();
            boolean objectDuFound = false;
            
            //There should only be one du in the dcp but just to be safe loop through all of them and find the relationship with the object du
            while (duIter.hasNext() && !objectDuFound) {
                DcsDeliverableUnit du = duIter.next();
                //Loop through all the relationships until we find the registry entry relationship
                for (DcsRelation relation : du.getRelations()) {
                    if (relation.getRelUri().equals(DcsRelationship.IS_REGISTRY_ENTRY_FOR.asString())) {
                        String objectDuId = relation.getRef().getRef();
                        objectDuFound = true;
                        
                        //Query for the object du that is referenced by the registry entry
                        String objectIdQuery = SolrQueryUtil.createLiteralQuery("OR", "id", objectDuId, "former", objectDuId);
                        String objectQuery = duQuery + " AND " + objectIdQuery;
                        QueryResult<Dcp> objectResult = dcpQueryService.query(objectQuery, 0, Integer.MAX_VALUE);
                        if (objectResult.getTotal() >= 1) {
                            if (objectResult.getTotal() > 1) {
                                log.debug("Query {} returned {} results. Taking the first result.",
                                          objectQuery, objectResult.getTotal());
                            }
                            
                            objectDcp = objectResult.getMatches().get(0).getObject();
                           
                        } else {
                            log.debug("Query {} returned no results", duQuery);
                        }
                        
                        break;
                    }                
                }
            }
            
            //If we couldn't find the object dcp then there is no sense returning just the registry entry.
            if (objectDcp == null) {
                log.error("Unable to retrieve object dcp");
                return null;
            }
            
            DcpUtil.add(registryEntryDcp, DcpUtil.asList(entryDcp));
            DcpUtil.add(registryEntryDcp, DcpUtil.asList(objectDcp));

        } catch (QueryServiceException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        if (!profile.conforms(registryEntryDcp)) {
            log.error("DCP returned by query {} did not conform to the expected profile {}", entryQuery, profile);
            return null;
        }

        // TODO: it would be nice to have a List of model mappers so this implementation can be instantiated once instead of one instance per mapper.


        // Find the archive identifier and the business identifier from the package
        String archiveId = null;
        String businessId = null;

        // if the supplied 'id' is an archival identifier, then the mapper will discover it
        if (modelMapper.discover(registryEntryDcp).contains(id)) {
            archiveId = id;
            for (DcsDeliverableUnit du : registryEntryDcp.getDeliverableUnits()) {
                if (du.getId().equals(archiveId)) {
                    if (du.getFormerExternalRefs().isEmpty()) {
                        log.debug("Could not extract a business identifier for DCP {}", registryEntryDcp);
                    } else {
                        businessId = du.getFormerExternalRefs().iterator().next();
                    }
                }
            }
        } else {
            // otherwise the 'id' is a business identifier
            businessId = id;
            for (DcsDeliverableUnit du : registryEntryDcp.getDeliverableUnits()) {
                if (du.getFormerExternalRefs().contains(id)) {
                    archiveId = du.getId();
                }
            }
        }
        
        if (archiveId != null && businessId != null) {
            return modelMapper.from(archiveId, registryEntryDcp, null);
        }

        log.debug("Unable to map DCP {} returned by query {} to a registry entry (Mapper: {}, Profile {})",
                new Object[] { registryEntryDcp, entryQuery, modelMapper, profile } );

        if (archiveId == null && businessId == null) {
            log.debug("Unable to find the business id or the archive id for the registry entry {}", registryEntryDcp);
        } else if (businessId == null) {
            log.debug("Unable to find the business id for the DU with archive id {} for registry entry",
                    archiveId, registryEntryDcp);
        } else {
            log.debug("Unable to find the archive id for the DU with business id {} for registry entry",
                    businessId, registryEntryDcp);
        }

        return null;
    }
    

    public Set<RegistryEntry<T>> lookupMultipleKeys(String...keys) {
        final String entryDuQuery = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit");
        String query = entryDuQuery;
        for (String key : keys) {
            query += " AND " + SolrQueryUtil.createLiteralQuery("resourceValue", key);
        }
        
        Set<Dcp> registryEntryDcps = new HashSet<Dcp>();
        try {
            QueryResult<Dcp> result = dcpQueryService.query(query, 0, Integer.MAX_VALUE);
    
            if (result.getTotal() == 0) {
                log.error("Query {} returned no results", query);
            } else {
                for (int i = 0; i < result.getTotal(); i++) {
                    Dcp dcp = result.getMatches().get(i).getObject();
                    Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();
                    
                    boolean objectDuFound = false;
                    
                    //There should only be one du in the dcp but just to be safe loop through all of them and find the relationship with the object du
                    Iterator<DcsDeliverableUnit> duIter = dus.iterator();
                    while (duIter.hasNext() && !objectDuFound) {
                        DcsDeliverableUnit du = duIter.next();
                        if (du.getRelations().size() > 0 ) {
                            
                            //Loop through all the relationships until we find the registry entry relationship
                            for (DcsRelation relation : du.getRelations()) {
                                if (relation.getRelUri().equals(DcsRelationship.IS_REGISTRY_ENTRY_FOR.asString())) {
                                    String objectDuId = relation.getRef().getRef();
                                    objectDuFound = true;
                                    Dcp registryEntryDcp = new Dcp();
    
                                    //Query for the object du that is referenced by the registry entry
                                    String duQuery = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "id", objectDuId);
                                    QueryResult<Dcp> objectResult = dcpQueryService.query(duQuery, 0, Integer.MAX_VALUE);
                                    if (objectResult.getTotal() >= 1) {
                                        if (objectResult.getTotal() > 1) {
                                            log.error("Query {} returned {} results. Taking the first result.",
                                                      duQuery, objectResult.getTotal());
                                        }
                                        
                                        Dcp objectDcp = objectResult.getMatches().get(0).getObject();
                                        if (profile.conforms(objectDcp)) {
                                            DcpUtil.add(registryEntryDcp, DcpUtil.asList(dcp));  
                                            DcpUtil.add(registryEntryDcp, DcpUtil.asList(objectDcp));
                                            registryEntryDcps.add(registryEntryDcp);
    
                                        } else {
                                            log.error("DCP returned by query {} did not conform to the expected profile {}", duQuery, profile);
                                        }
                                    } else {
                                        log.error("Query {} returned no results", duQuery);
                                    }
                                    
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (QueryServiceException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        
        //Now that we've build all of the dcps map them to registry entries
        //We've already checked that the object dcps conform to the profile at this point
        Set<RegistryEntry<T>> registryEntries = new HashSet<RegistryEntry<T>>();
        for (Dcp dcp : registryEntryDcps) {
            //Registry entry mapper doesn't use id so we won't bother with discover for now.
            RegistryEntry<T> entry = modelMapper.from("", dcp, null);
            if (entry != null) {
                registryEntries.add(entry);
            } else {
                log.debug("Unable to map DCP {} returned by query {} to a registry entry (Mapper: {}, Profile {})",
                          new Object[] { dcp, query, modelMapper, profile } );
            }            
        }

        return registryEntries;        
    }

    @Override
    public QueryResult<RegistryEntry<T>> query(String query, long offset, int matches, String... params)
            throws QueryServiceException {
        QueryResult<Dcp> results = dcpQueryService.query(query, offset, matches, params);
        
        //This query call will find the object dcps we need to add the registry entry dcps
        if (results != null) {
            for (int i = 0; i < results.getTotal(); i++) {
                Dcp dcp = results.getMatches().get(i).getObject();
                Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();
                
                boolean entryDuFound = false;
                
                //There should only be one du in the dcp but just to be safe loop through all of them and find the relationship with the object du
                Iterator<DcsDeliverableUnit> duIter = dus.iterator();
                while (duIter.hasNext() && !entryDuFound) {
                    DcsDeliverableUnit du = duIter.next();
                    if (du.getRelations().size() > 0 ) {
                        
                        //Loop through all the relationships until we find the registry entry relationship
                        for (DcsRelation relation : du.getRelations()) {
                            if (relation.getRelUri().equals(DcsRelationship.HAS_REGISTRY_ENTRY.asString())) {
                                String entryDuId = relation.getRef().getRef();
                                entryDuFound = true;

                                //Query for the registry entry du that is referenced by the registry entry relationship
                                String duQuery = SolrQueryUtil.createLiteralQuery("AND", "entityType", "DeliverableUnit", "id", entryDuId);
                                QueryResult<Dcp> entryResult = dcpQueryService.query(duQuery, 0, Integer.MAX_VALUE);
                                if (entryResult.getTotal() >= 1) {
                                    if (entryResult.getTotal() > 1) {
                                        log.debug("Query {} returned {} results. Taking the first result.",
                                                  duQuery, entryResult.getTotal());
                                    }
                                    
                                    Dcp entryDcp = entryResult.getMatches().get(0).getObject();
                                    
                                    DcpUtil.add(dcp, DcpUtil.asList(entryDcp));  

                                } else {
                                    log.debug("Query {} returned no results", duQuery);
                                }
                                
                                break;
                            }
                        }
                    }
                }
            }
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
