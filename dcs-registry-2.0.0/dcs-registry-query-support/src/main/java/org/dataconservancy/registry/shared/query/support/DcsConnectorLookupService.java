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

import org.dataconservancy.access.connector.CountableIterator;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;

public class DcsConnectorLookupService implements LookupQueryService<DcsEntity> {

    private DcsConnector connector;
    
    public DcsConnectorLookupService(DcsConnector connector) {
        this.connector = connector;
    }
    
    @Override
    public QueryResult<DcsEntity> query(String query,
                                        long offset,
                                        int matches,
                                        String... params)
            throws QueryServiceException {
        CountableIterator<DcsEntity> results = null;
        try {
            results = connector.search(query, matches, (int) offset);
        } catch (DcsConnectorFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        QueryResult<DcsEntity> queryResults = new QueryResult<DcsEntity>(0, 0, query, params);
        if (results != null && results.hasNext()) {
            queryResults = new QueryResult<DcsEntity>(0, results.count(), query, params);
            while (results.hasNext()) {
                queryResults.getMatches().add(new QueryMatch<DcsEntity>(results.next(), null));
            }
        }
        return queryResults;
    }


    @Override
    public void shutdown() throws QueryServiceException {
        
    }

    @Override
    public DcsEntity lookup(String id) throws QueryServiceException {
        final String duQuery = SolrQueryUtil.createLiteralQuery("entityType", "DeliverableUnit");
        final String idQuery = SolrQueryUtil.createLiteralQuery("OR", "id", id, "former", id); 
        String query = duQuery + " AND " + idQuery;

        QueryResult<DcsEntity> results = query(query, 0, -1, "");
        DcsEntity result = null;
        if (results.getTotal() > 0) {
            result = results.getMatches().get(0).getObject();
        }
        return result;
    }
    
}