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
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.support.DcpUtil;
import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.api.support.RegistryEntry;
import org.dataconservancy.registry.impl.license.shared.LicenseProfiler;
import org.dataconservancy.profile.api.DcpMapper;
import org.dataconservancy.profile.api.Profile;
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
@ContextConfiguration({"classpath*:/org/dataconservancy/config/applicationContext.xml", "classpath*:/org/dataconservancy/config/test-applicationContext.xml"})
public class GenericQueryFrameworkRegistryTest<DcsLicense> extends AbstractQueryFrameworkRegistryTest {

    @Autowired
    private DcpMapper<DcsLicense> licenseRegistryMapper;

    @Autowired
    private LicenseProfiler licenseProfiler;

    @Override
    protected Collection<Dcp> getDcpsToIndex() {
        final List<Dcp> dcps = new ArrayList<Dcp>();
        final DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();
        final Resource dcpDir = new ClassPathResource("/org/dataconservancy/registry/impl/license/query/sample_dcps");
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
    protected Registry<DcsLicense> getUnderTest() {
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
        for (Dcp dcp : getDcpsToIndex()) {
            assertTrue("DCP " + dcp + " does not conform to profile " + licenseProfiler, licenseProfiler.conforms(dcp));
            for (String archiveId : licenseRegistryMapper.discover(dcp)) {
                String businessId = ((DcsDeliverableUnit)DcpUtil.asMap(dcp).get(archiveId))
                        .getFormerExternalRefs().iterator().next();
                DcsLicense license = licenseRegistryMapper.from(archiveId, dcp, null);
                BasicRegistryEntryImpl<DcsLicense> entry = new BasicRegistryEntryImpl<DcsLicense>(businessId, license,
                        licenseProfiler.getType() + ":" + licenseProfiler.getVersion());
                expectedEntries.put(businessId, entry);
            }
        }

        return expectedEntries;
    }
}
