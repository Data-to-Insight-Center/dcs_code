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

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.dcs.contentdetection.api.ContentDetectionService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.packaging.ingest.api.StatefulBootstrap;
import org.dataconservancy.packaging.ingest.impl.DepositStateManager;
import org.dataconservancy.packaging.ingest.impl.IngestWorkflowStateFactory;
import org.dataconservancy.ui.util.GZipPackageExtractor;
import org.dataconservancy.ui.util.PackageExtractor;
import org.dataconservancy.ui.util.PackageSelector;
import org.dataconservancy.ui.util.PackageSelectorImpl;
import org.dataconservancy.ui.util.TarPackageExtractor;
import org.dataconservancy.ui.util.ZipPackageExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/org/dataconservancy/packaging/config/applicationContext.xml",
    "classpath*:/org/dataconservancy/config/applicationContext.xml"})
public class IngestPackageIT {
    
    private BagItDepositManagerImpl bagitDepositManagerImpl;
    private PackageSelectorImpl packageSelector;
    
    @Autowired
    @Qualifier("org.dataconservancy.packaging.ingest.services.StatefulBootstrapImpl")
    private StatefulBootstrap statefulBootstrap;
    
    @Autowired
    @Qualifier("org.dataconservancy.dcs.contentdetection.impl.droid.DroidContentDetectionServiceImpl")
    private ContentDetectionService contentDetectionService;
    
    @Autowired
    @Qualifier("inMemoryIdService")
    private IdService eventManagerIdService;
    
    @Autowired
    @Qualifier("packageIngestWorkflowStateFactory")
    private IngestWorkflowStateFactory stateFactory;

    @Autowired
    @Qualifier("packageIngestDepositStateManager")
    private DepositStateManager stateManager;
    
    @Before
    public void setup() {
        bagitDepositManagerImpl = new BagItDepositManagerImpl();
        
        packageSelector = new PackageSelectorImpl();
        Map<String, PackageExtractor> extractors = new HashMap<String, PackageExtractor>();
        extractors.put(PackageSelector.ZIP_KEY, new ZipPackageExtractor());
        extractors.put(PackageSelector.GZIP_KEY, new GZipPackageExtractor());
        extractors.put(PackageSelector.TAR_KEY, new TarPackageExtractor());
        
        packageSelector.setExtractors(extractors);
        
        bagitDepositManagerImpl.setPackageSelector(packageSelector);
        
        //Set all of the spring wired beans for the deposit manager.
        bagitDepositManagerImpl.setBootstrap(statefulBootstrap);
        bagitDepositManagerImpl.setContentDetectionService(contentDetectionService);
        bagitDepositManagerImpl.setIdService(eventManagerIdService);
        bagitDepositManagerImpl.setStateFactory(stateFactory);
        bagitDepositManagerImpl.setStateManager(stateManager);
    }
    
    @Test
    public void testDeposit() throws PackageException {
        bagitDepositManagerImpl.deposit(null, null, null, null);
        assertTrue(false);
    }
}