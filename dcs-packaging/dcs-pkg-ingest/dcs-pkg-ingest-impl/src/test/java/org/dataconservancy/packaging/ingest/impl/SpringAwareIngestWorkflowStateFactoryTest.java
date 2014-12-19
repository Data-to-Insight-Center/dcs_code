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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/org/dataconservancy/packaging/config/applicationContext.xml",
    "classpath*:/org/dataconservancy/config/applicationContext.xml"})
public class SpringAwareIngestWorkflowStateFactoryTest implements ApplicationContextAware {

    private ApplicationContext appContext;
    private SpringAwareIngestWorkflowStateFactory springIngestStateFactory;
    
    @Before
    public void setup() {
        springIngestStateFactory = new SpringAwareIngestWorkflowStateFactory();
        springIngestStateFactory.setApplicationContext(appContext);
        springIngestStateFactory.setIngestWorkflowStateBeanName("packageIngestWorkflowState");
    }

    @Test
    public void testStateNotNull() {
        IngestWorkflowState state = springIngestStateFactory.newInstance();
        assertNotNull(state);
    }
    
    @Test
    public void testEventManagerNotNull() {
        IngestWorkflowState state = springIngestStateFactory.newInstance();
        assertNotNull(state);
        
        assertNotNull(state.getEventManager());
    }
    
    @Test
    public void testAttributeSetManagerNotNull() {
        IngestWorkflowState state = springIngestStateFactory.newInstance();
        assertNotNull(state);
        
        assertNotNull(state.getAttributeSetManager());
    }

    @Test
    public void testBusinessObjectManagerNotNull() {
        IngestWorkflowState state = springIngestStateFactory.newInstance();
        assertNotNull(state);

        assertNotNull(state.getBusinessObjectManager());
    }

    @Test
    public void testPackageNotNull() {
        IngestWorkflowState state = springIngestStateFactory.newInstance();
        assertNotNull(state);
        
        assertNotNull(state.getPackage());
    }

    @Test
    public void testPrototypeIsWorkingProperly() throws Exception {
        IngestWorkflowState state1 = springIngestStateFactory.newInstance();
        IngestWorkflowState state2 = springIngestStateFactory.newInstance();
        assertFalse(state1 == state2);
        assertFalse(state1.getBusinessObjectManager() == state2.getBusinessObjectManager());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        appContext = applicationContext;        
    }
    
}