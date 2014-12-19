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
package org.dataconservancy.registry.api.support;

import java.io.IOException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.registry.api.RegistryEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BasicRegistryEntryMapperTest {
    
    private static final String TEST_STRING = "This is the entry object";
    private static final String ID = "Entry:ID";
    private static final String TYPE = "String";
    private static final String KEY_ONE = "key:1";
    private static final String KEY_TWO = "key:2";
    private static final String DESCRIPTION = "This is a string registry entry for BasicRegistryEntryMapperTest";
    
    private BasicRegistryEntryMapper<String> underTest;
    private RegistryEntry<String> testEntry;
    
    @Before
    public void setup() throws IOException {
        underTest = new StringBasicRegistryEntryMapper();
        Set<String> keys = new HashSet<String>();
        keys.add(KEY_ONE);
        keys.add(KEY_TWO);
        testEntry = new BasicRegistryEntryImpl<String>(ID, TEST_STRING, TYPE, keys, DESCRIPTION);
    }
    
    @Test
    public void testRoundTrip() {
        Dcp dcp = underTest.to(testEntry, null);
        assertNotNull(dcp);
        
        RegistryEntry<String> result = underTest.from("", dcp, null);
        assertNotNull(result);
        
        assertEquals(testEntry.getId(), result.getId());
        assertEquals(testEntry.getDescription(), result.getDescription());
        assertEquals(testEntry.getEntry(), result.getEntry());
        assertEquals(testEntry.getType(), result.getType());
        assertEquals(testEntry.getKeys(), result.getKeys());
    }
    
    @Test
    public void testMappingToDcp() {
        Dcp dcp = underTest.to(testEntry, null);
        Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();
        assertEquals(2, dus.size());
        
        boolean entryDuFound = false;
        DcsDeliverableUnit entryDu = null;
        DcsDeliverableUnit objectDu = null;
        for (DcsDeliverableUnit du : dus) {
            if (du.getType() != null && du.getType().equals(BasicRegistryEntryMapper.REGISTRY_ENTRY_DU_TYPE)) {
                entryDuFound = true;
                entryDu = du;
            } else {
                objectDu = du;
            }
            
        }
        assertTrue(entryDuFound);
        assertNotNull(objectDu);
        
        Collection<DcsRelation> relationships = entryDu.getRelations();
        assertEquals(1, relationships.size());
        
        assertTrue(entryDu.getRelations().contains(new DcsRelation(DcsRelationship.IS_REGISTRY_ENTRY_FOR, objectDu.getId())));
       
        Collection<DcsManifestation> mans = dcp.getManifestations();
        assertEquals(2, mans.size());
        boolean entryManFound = false;
        for (DcsManifestation man : mans) {
            if (man.getType().equals(BasicRegistryEntryMapper.REGISTRY_ENTRY_MAN_TYPE)) {
                entryManFound = true;
                break;
            }
        }
        assertTrue(entryManFound);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMappingException() {
        Set<String> keys = new HashSet<String>();
        keys.add(KEY_ONE);
        keys.add(KEY_TWO);
        RegistryEntry<String> testEntry = new BasicRegistryEntryImpl<String>(ID, null, TYPE, keys, DESCRIPTION);
        underTest.to(testEntry, null);
    }
    
    private class StringBasicRegistryEntryMapper extends BasicRegistryEntryMapper<String> {

        @Override
        protected String deserializeObjectState(Dcp dcp) throws IOException {
            return TEST_STRING;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void serializeObjectState(String object, Dcp dcp)
                throws IOException {
            DcsDeliverableUnit du = new DcsDeliverableUnit();
            du.setId("foo");
            DcsManifestation man = new DcsManifestation();
            DcsFile file = new DcsFile();
            
            dcp.addDeliverableUnit(du);
            dcp.addManifestation(man);
            dcp.addFile(file);
            
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected DcsDeliverableUnit retrieveObjectDu(Dcp dcp) {
            DcsDeliverableUnit du = new DcsDeliverableUnit();
            du.setId("foo");
            return du;
        }
        
    }
}