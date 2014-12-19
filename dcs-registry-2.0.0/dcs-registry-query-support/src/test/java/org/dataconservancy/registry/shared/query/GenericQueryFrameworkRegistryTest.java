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
package org.dataconservancy.registry.shared.query;

import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.support.DcpUtil;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.license.shared.LicenseProfiler;
import org.dataconservancy.profile.api.DcpMapper;
import org.dataconservancy.registry.shared.query.support.DcpQueryService;
import org.dataconservancy.registry.shared.query.support.RegistryEntryLookupService;
import org.dataconservancy.registry.shared.test.AbstractQueryFrameworkRegistryTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dataconservancy.registry.impl.license.shared.LicenseRegistryConstant.LICENSE_REGISTRY_ENTRY_TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/org/dataconservancy/config/applicationContext.xml", 
    "classpath*:/org/dataconservancy/config/test-applicationContext.xml",
    "classpath*:/org/dataconservancy/access/config/applicationContext.xml",
    "classpath*:/org/dataconservancy/model/config/applicationContext.xml"})
public class GenericQueryFrameworkRegistryTest<DcsLicense> extends AbstractQueryFrameworkRegistryTest {

    @Autowired
    private DcpMapper<RegistryEntry<DcsLicense>> licenseRegistryMapper;

    @Autowired
    private LicenseProfiler licenseProfiler;

    @Override
    protected Collection<Dcp> getDcpsToIndex() {
        final List<Dcp> dcps = new ArrayList<Dcp>();
        final DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();
        final Resource dcpDir = new ClassPathResource("/org/dataconservancy/registry/impl/license/query/sample_dcps");
        final String contentDir = "/org/dataconservancy/registry/impl/license/query/sample_content/";
        assertTrue("DCP directory cannot be found: " + dcpDir.getFilename(), dcpDir.exists());
        try {
            assertTrue("No DCPs were found in directory " + dcpDir.getFile(), dcpDir.getFile().listFiles().length > 0);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            for (File dcpXml : dcpDir.getFile().listFiles()) {
                final Dcp dcp;
                try {
                    dcp = builder.buildSip(new FileInputStream(dcpXml));
                    if (dcp != null && !dcp.getFiles().isEmpty()) {
                        for (DcsFile file : dcp.getFiles()) {
                            String fileName = file.getSource();
                            file.setSource(GenericQueryFrameworkRegistryTest.class.getResource(contentDir + fileName).toString());
                        }
                    }
                } catch (InvalidXmlException e) {
                    final String msg = "Invalid XML in DCP file: " + dcpXml.getAbsolutePath();
                    log.error(msg, e);
                    throw new RuntimeException(msg, e);
                } catch (FileNotFoundException e) {
                    final String msg = "DCP XML file not found: " + dcpXml.getAbsolutePath();
                    log.error(msg, e);
                    throw new RuntimeException(msg, e);
                }
                dcps.add(dcp);
            }
        } catch (IOException e) {
            final String msg = "Error listing DCP XML files from Spring Resource: " + dcpDir;
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        return dcps;
    }

    @Override
    protected TypedRegistry<DcsLicense> getUnderTest() {
        DcsDataModelQueryService dcsQueryService = new DcsDataModelQueryService(new SolrService(getSolrServer()));

        RegistryEntryLookupService<DcsLicense> lookupService =
                new RegistryEntryLookupService<DcsLicense>(
                        new DcpQueryService(dcsQueryService), licenseRegistryMapper, licenseProfiler);


        return new GenericQueryFrameworkRegistry<DcsLicense>(LICENSE_REGISTRY_ENTRY_TYPE, lookupService);
    }

    @Override
    protected Map<String, RegistryEntry<DcsLicense>> getExpectedEntries() {
        assertNotNull("License Registry Mapper must not be null", licenseRegistryMapper);

        final Map<String, RegistryEntry<DcsLicense>> expectedEntries = new HashMap<String, RegistryEntry<DcsLicense>>();
        Dcp registryEntryDcp = new Dcp();
        //Combine the object dcp and the registry entry dcp into one
        for (Dcp dcp : getDcpsToIndex()) {            
            DcpUtil.add(registryEntryDcp, DcpUtil.asList(dcp));
        }
        
        assertTrue("DCP " + registryEntryDcp + " does not conform to profile " + licenseProfiler, licenseProfiler.conforms(registryEntryDcp));
        for (String archiveId : licenseRegistryMapper.discover(registryEntryDcp)) {
            RegistryEntry<DcsLicense> entry = licenseRegistryMapper.from(archiveId, registryEntryDcp, null);   
            if (entry != null) {
                expectedEntries.put(entry.getId(), entry);
            }
        }

        return expectedEntries;
    }
}
