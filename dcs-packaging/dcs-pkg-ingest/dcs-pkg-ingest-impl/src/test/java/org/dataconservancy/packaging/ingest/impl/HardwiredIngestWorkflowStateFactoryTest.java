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
package org.dataconservancy.packaging.ingest.impl;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class HardwiredIngestWorkflowStateFactoryTest {
    private HardwiredIngestWorkflowStateFactory underTest;
    
    @Before 
    public void setup() {
        underTest = new HardwiredIngestWorkflowStateFactory();
        underTest.setIdCreationService(mock(BulkIdCreationService.class));
    }
    
    @Test
    public void assertStateNotNull() {
        IngestWorkflowState state = underTest.newInstance();
        assertNotNull(state);
    }
    
    @Test
    public void assertPackageNotNull() {
        IngestWorkflowState state = underTest.newInstance();
        assertNotNull(state);
        assertNotNull(state.getPackage());
    }
    
    @Test
    public void assertAttributeSetManagerNotNull() {
        IngestWorkflowState state = underTest.newInstance();
        assertNotNull(state);
        assertNotNull(state.getAttributeSetManager());
    }

    @Test
    public void assertBusinessObjectManagerNotNull() {
        IngestWorkflowState state = underTest.newInstance();
        assertNotNull(state);
        assertNotNull(state.getBusinessObjectManager());
    }
    
    @Test
    public void assertEventManagerNotNull() {
        IngestWorkflowState state = underTest.newInstance();
        assertNotNull(state);
        assertNotNull(state.getEventManager());
    }
}