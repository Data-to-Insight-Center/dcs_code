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
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link QueryService} that returns DCP packages in response to queries.
 * <p/>
 * Because the Registry Framework design calls for mapping a domain object to a DCP package and back (i.e. domain
 * objects are serialized as DCP packages), it needs to operate on entire packages (not just individual entities)
 * when performing certain operations. Specifically, the {@link org.dataconservancy.registry.profile.support profile support}
 * classes and {@link org.dataconservancy.registry.api.support mapper interfaces} require DCP packages to perform their
 * tasks.
 * <p/>
 * This implementation delegates queries to an implementation of {@link LookupQueryService<DcsEntity>}.  When the
 * response is received (e.g. a {@link DcsEntity}), this implementation will use that entity as the basis for building a
 * "full" DCP package.
 * </p>
 * <strong>This implementation depends heavily on how <em>ancestry</em> is defined and indexed by the Query
 * Framework.</strong>  Specifically, this implementation does not provide guarantees as to what is contained in a "full"
 * DCP package: it relies on the behavior of Solr <em>ancestry</em> search index behavior.
 */
public class DcpQueryService implements LookupQueryService<Dcp> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private LookupQueryService<DcsEntity> entityQueryService;

    public DcpQueryService(LookupQueryService<DcsEntity> entityQueryService) {
        if (entityQueryService == null) {
            throw new IllegalArgumentException("DcsDataModelQueryService must not be null.");
        }
        this.entityQueryService = entityQueryService;
    }

    @Override
    public QueryResult<Dcp> query(String query, long offset, int matches, String... params) throws QueryServiceException {
        QueryResult<DcsEntity> entityResults = entityQueryService.query(query, offset, matches, params);
        DcpQueryResult dcpResults = new DcpQueryResult(entityResults.getOffset(), entityResults.getTotal(), query, params);

        // For each entity in the results, we build a DCP package
        // Each entity will have a corresponding DCP package
        for (QueryMatch<DcsEntity> match : entityResults.getMatches()) {
            DcsEntity e = match.getObject();
            Dcp dcp = buildPackage(e);

            // and then re-wrap the DCP package in a new QueryResult
            dcpResults.addMatch(dcp, match.getContext());
        }

        return dcpResults;
    }

    @Override
    public void shutdown() throws QueryServiceException {
        entityQueryService.shutdown();
    }

    private Dcp buildPackage(DcsEntity entity) {
        Dcp dcp = new Dcp();
        if (entity instanceof DcsDeliverableUnit) {
            buildPackage((DcsDeliverableUnit) entity, dcp);
        } else if (entity instanceof DcsCollection) {
            buildPackage((DcsCollection) entity, dcp);
        } else if (entity instanceof DcsManifestation) {
            buildPackage((DcsManifestation) entity, dcp);
        } else if (entity instanceof DcsFile) {
            buildPackage((DcsFile) entity, dcp);
        } else if (entity instanceof DcsEvent) {
            buildPackage((DcsEvent) entity, dcp);
        } else {
            final String msg = "Unable to determine sub-type of DcsEntity: " + entity;
            RuntimeException e = new RuntimeException(msg);
            log.error(msg, e);
            throw e;
        }

        return dcp;
    }

    @Override
    public Dcp lookup(String id) throws QueryServiceException {
        DcsEntity e = entityQueryService.lookup(id);
        if (e != null) {
            return buildPackage(e);
        }

        return null;
    }

    /**
     * Builds a package based on the collection by finding and de-referencing all parent collections, and by
     * finding every DcsEntity that has <code>c</code> as an ancestor.
     *
     * @param c the collection to build a package around
     * @return the package
     */
    private void buildPackage(DcsCollection c, Dcp dcp) {
        dcp.addCollection(c);

        // lookup parent collections
        while (c.getParent() != null) {
            try {
                DcsCollection parent = (DcsCollection) entityQueryService.lookup(c.getParent().getRef());
                dcp.addCollection(parent);
            } catch (QueryServiceException e) {
                log.warn("Unable to obtain parent collection {}: {}", c.getParent().getRef(), e.getMessage());
            } catch (ClassCastException e) {
                log.warn("Expected a DcsCollection but got some other DcsEntity.", e);
            }
        }

        for (DcsEntity e : getDescendants(c.getId())) {
            addEntityToPackage(e, dcp);
        }
    }

    private void buildPackage(DcsDeliverableUnit du, Dcp dcp) {
        dcp.addDeliverableUnit(du);

        // lookup references for all parent dus
        Set<DcsDeliverableUnit> ancestors = new HashSet<DcsDeliverableUnit>();
        getAncestorDus(du, ancestors);
        for (DcsDeliverableUnit ancestorDu : ancestors) {
            dcp.addDeliverableUnit(ancestorDu);
        }

        // lookup descendants of the du (everything that has this du as an ancestor)
        for (DcsEntity e : getDescendants(du.getId())) {
            addEntityToPackage(e, dcp);
        }
    }

    private void buildPackage(DcsManifestation man, Dcp dcp) {
        dcp.addManifestation(man);

        // lookup reference for parent du, then build the package
        DcsDeliverableUnit du = null;
        try {
            du = (DcsDeliverableUnit) entityQueryService.lookup(man.getDeliverableUnit());
        } catch (QueryServiceException e) {
            log.warn("Unable to obtain deliverable unit {}: {}", man.getDeliverableUnit(), e.getMessage());
            return;
        } catch (ClassCastException e) {
            log.warn("Expected a DcsDeliverableUnit but got some other DcsEntity.", e);
            return;
        }

        dcp.addDeliverableUnit(du);

        buildPackage(du, dcp);
    }

    private void buildPackage(DcsFile file, Dcp dcp) {
        dcp.addFile(file);

        // lookup reference for parent manifestations, and build the package
        String query = SolrQueryUtil.createLiteralQuery("AND", "entityType", "Manifestation", "fileRef", file.getId());
        QueryResult<DcsEntity> result = null;
        try {
            result = entityQueryService.query(query, 0, Integer.MAX_VALUE);
        } catch (QueryServiceException e) {
            log.warn("Unable to obtain manifestations for file {}: {}", file.getId(), e.getMessage());
            return;
        }

        for (QueryMatch<DcsEntity> match : result.getMatches()) {
            DcsManifestation man = null;
            try {
                man = (DcsManifestation) match.getObject();
            } catch (ClassCastException e) {
                log.warn("Expected a DcsManifestation but got some other DcsEntity.", e);
                return;
            }
            buildPackage(man, dcp);
        }
    }

    /**
     * TODO: not implemented.  Currently returns null.
     *
     * @param event the event
     * @return null
     */
    private void buildPackage(DcsEvent event, Dcp dcp) {
        // TODO: not implemented
    }

    private void addEntityToPackage(DcsEntity entity, Dcp dcp) {
        if (entity instanceof DcsDeliverableUnit) {
            dcp.addDeliverableUnit((DcsDeliverableUnit) entity);
        } else if (entity instanceof DcsCollection) {
            dcp.addCollection((DcsCollection) entity);
        } else if (entity instanceof DcsManifestation) {
            dcp.addManifestation((DcsManifestation) entity);
        } else if (entity instanceof DcsFile) {
            dcp.addFile((DcsFile) entity);
        } else if (entity instanceof DcsEvent) {
            dcp.addEvent((DcsEvent) entity);
        } else {
            final String msg = "Unable to determine sub-type of DcsEntity: " + entity;
            RuntimeException e = new RuntimeException(msg);
            log.error(msg, e);
            throw e;
        }
    }

    /**
     * Get all references to ancestors of the provided <code>du</code>.  This includes parents, grandparents,
     * great-grandparents, etc.  The supplied Set will be populated with references to the ancestors.
     *
     * @param du        the DcsDeliverableUnit
     * @param ancestors ancestors of <code>du</code>, populated by this method
     */
    private void getAncestorDus(DcsDeliverableUnit du, Set<DcsDeliverableUnit> ancestors) {
        for (DcsDeliverableUnitRef ref : du.getParents()) {
            try {
                DcsDeliverableUnit ancestorDu = (DcsDeliverableUnit) entityQueryService.lookup(ref.getRef());
                ancestors.add(ancestorDu);
                getAncestorDus(ancestorDu, ancestors);
            } catch (QueryServiceException e) {
                log.warn("Unable to obtain ancestor deliverable unit {}: {}", ref.getRef(), e.getMessage());
            } catch (ClassCastException e) {
                log.warn("Expected a DcsDeliverableUnit but got some other DcsEntity.", e);
            }
        }
    }

    /**
     * Obtain all entities that have the specified <code>entityId</code> as an ancestor.  Effectively this obtains
     * all the descendants of <code>entityId</code>.
     *
     * @param entityId the entity id
     * @return a Set containing descendant entities
     */
    private Set<DcsEntity> getDescendants(String entityId) {
        final String descendantsQuery = SolrQueryUtil.createLiteralQuery("ancestry", entityId);
        final Set<DcsEntity> descendants = new HashSet<DcsEntity>();

        try {
            QueryResult<DcsEntity> queryResult = entityQueryService.query(descendantsQuery, 0, Integer.MAX_VALUE);
            for (QueryMatch<DcsEntity> match : queryResult.getMatches()) {
                descendants.add(match.getObject());
            }
        } catch (QueryServiceException e) {
            log.warn("Unable to build descendants for DCS Entity {}: {}", entityId, e.getMessage());
        }

        return descendants;
    }

    private class DcpQueryResult extends QueryResult<Dcp> {

        private List<QueryMatch<Dcp>> matches = new ArrayList<QueryMatch<Dcp>>();

        private DcpQueryResult(long offset, long total, String query, String... params) {
            super(offset, total, query, params);
        }

        private void addMatch(Dcp dcp, String context) {
            QueryMatch<Dcp> match = new QueryMatch<Dcp>(dcp, context);
            matches.add(match);
            super.getMatches().add(match);
        }

        @Override
        public List<QueryMatch<Dcp>> getMatches() {
            return this.matches;
        }
    }
}
