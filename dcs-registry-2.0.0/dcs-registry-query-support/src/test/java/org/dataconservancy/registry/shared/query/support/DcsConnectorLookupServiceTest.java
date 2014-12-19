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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dataconservancy.access.connector.CountableIterator;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;

public class DcsConnectorLookupServiceTest {
    
    private DcsConnectorLookupService underTest;
    private DcsConnector connector;
    
    @Before
    public void setup() {
        connector = mock(DcsConnector.class);
        underTest = new DcsConnectorLookupService(connector);
    }
    
    @Test
    public void testQuery() throws DcsConnectorFault, QueryServiceException {
        Set<DcsEntity> entities = new HashSet<DcsEntity>();
        DcsDeliverableUnit testDu = new DcsDeliverableUnit();
        testDu.setId("foo");
        
        DcsDeliverableUnit testTwoDu = new DcsDeliverableUnit();
        testTwoDu.setId("bar");
        
        entities.add(testDu);
        entities.add(testTwoDu);
        
        when(connector.search("foo", -1, 0)).thenReturn(new TestIterator(entities));
        
        QueryResult<DcsEntity> results = underTest.query("foo", 0, -1, "");
        assertNotNull(results);
        
        assertEquals(2, results.getTotal());
        assertEquals(2, results.getMatches().size());
    }
    
    @Test
    public void testLookup() throws DcsConnectorFault, QueryServiceException {
        Set<DcsEntity> entities = new HashSet<DcsEntity>();
        DcsDeliverableUnit testDu = new DcsDeliverableUnit();
        testDu.setId("foo");

        entities.add(testDu);
        
        when(connector.search("entityType:\"DeliverableUnit\" AND (id:\"foo\" OR former:\"foo\")", -1, 0)).thenReturn(new TestIterator(entities));
        
        DcsEntity resultEntity = underTest.lookup("foo");
        assertNotNull(resultEntity);
        
        assertEquals(resultEntity, testDu);

    }
    
    private class TestIterator implements CountableIterator<DcsEntity> {

        private Set<DcsEntity> results;
        private Iterator<DcsEntity> iter;
        public TestIterator(Set<DcsEntity> results) {
            this.results = results;
            iter = results.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public DcsEntity next() {
            return iter.next();
        }

        @Override
        public void remove() {
            iter.remove();
        }

        @Override
        public long count() {
            return results.size();
        }
        
    }
}