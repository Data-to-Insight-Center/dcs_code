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
package org.dataconservancy.registry.impl.license.shared;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.support.FieldFilter;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/org/dataconservancy/config/applicationContext.xml", "classpath*:/org/dataconservancy/config/test-applicationContext.xml"})
public class DcpLicenseMapperTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private FieldFilter filter;

    @Autowired
    private LicenseProfiler licenseProfiler;

    @Autowired
    private DcpLicenseMapper underTest;

    @Before
    public void setUp() throws NoSuchFieldException, IOException {
        filter = new FieldFilter()
                .addField(DcsEntity.class.getDeclaredField("id"))
                .addField(DcsManifestation.class.getDeclaredField("dateCreated"))
                .addField(DcsManifestation.class.getDeclaredField("deliverableUnit"))
                .addField(DcsManifestation.class.getDeclaredField("manifestationFiles"))
                .addField(DcsFile.class.getDeclaredField("source"));
    }
    
    @Test
    public void testRegistryEntryRoundTrip() {
        final DcsLicense domainObject = Dcps.getConformingLicense();
        Set<String> keys = new HashSet<String>();
        keys.add(domainObject.getName());
        
        RegistryEntry<DcsLicense> licenseEntry = new BasicRegistryEntryImpl<DcsLicense>(domainObject.getTag(), domainObject, 
                LicenseRegistryConstant.LICENSE_REGISTRY_ENTRY_TYPE, keys, domainObject.getSummary());
        
        Dcp dcp = underTest.to(licenseEntry, null);
        assertNotNull(dcp);
        
        RegistryEntry<DcsLicense> resultEntry = underTest.from("", dcp, null);
        assertNotNull(resultEntry);
        
        assertEquals(domainObject, resultEntry.getEntry());
        assertEquals(licenseEntry.getId(), resultEntry.getId());
        assertEquals(licenseEntry.getDescription(), resultEntry.getDescription());
        assertEquals(licenseEntry.getType(), resultEntry.getType());
        assertEquals(licenseEntry.getKeys(), resultEntry.getKeys());        
    }
    
    @Test
    public void testRegistryEntryDcpConformsToProfile() {
        final DcsLicense domainObject = Dcps.getConformingLicense();
        Set<String> keys = new HashSet<String>();
        keys.add(domainObject.getName());
        
        RegistryEntry<DcsLicense> licenseEntry = new BasicRegistryEntryImpl<DcsLicense>(domainObject.getTag(), domainObject, 
                LicenseRegistryConstant.LICENSE_REGISTRY_ENTRY_TYPE, keys, domainObject.getSummary());
        
        Dcp dcp = underTest.to(licenseEntry, null);
        assertNotNull(dcp);
        assertTrue(licenseProfiler.conformsTo(dcp));
    }
    
    @Test
    public void testFromDomainObject() throws Exception {
        final DcsLicense domainObject = Dcps.getConformingLicense();

        Dcp actualDcp = new Dcp();

        underTest.serializeObjectState(domainObject, actualDcp);
        
        final Dcp expectedDcp = Dcps.getConformingDcp();

        assertTrue(licenseProfiler.conformsTo(expectedDcp));
        assertTrue(licenseProfiler.conformsTo(actualDcp));

        final DcsDeliverableUnit expectedDu = licenseProfiler.selectDeliverableUnit(expectedDcp);
        final DcsDeliverableUnit actualDu = licenseProfiler.selectDeliverableUnit(actualDcp);
        assertTrue("Expected " + actualDu + " to be equal to " + expectedDu,
                expectedDu.equals(actualDu, filter));

        final DcsFile expectedFile = licenseProfiler.selectFile(expectedDcp);
        final DcsFile actualFile = licenseProfiler.selectFile(actualDcp);
        assertTrue("Expected " + actualFile + " to be equal to " + expectedFile,
                expectedFile.equals( actualFile, filter));


        // TODO: hard to test technical environment using equals.  Would be nice to have an equality test based on ProfileStatements
//        assertTrue(licenseProfiler.selectManifestation(expectedDcp).equals(
//                licenseProfiler.selectManifestation(actualDcp), filter));
        final DcsManifestation expectedManifestation = licenseProfiler.selectManifestation(expectedDcp);
        final DcsManifestation actualManifestation = licenseProfiler.selectManifestation(actualDcp);
        assertNotNull(expectedManifestation);
        assertNotNull(actualManifestation);
    }

    @Test
    public void testFromDomainObjectFails() throws Exception {
        final DcsLicense domainObject = Dcps.getMalformedLicense();
        final Dcp expectedDcp = Dcps.getConformingDcp();
        //final Dcp malformedDcp = underTest.to(domainObject, null);

        assertTrue(licenseProfiler.conformsTo(expectedDcp));
        //assertFalse(licenseProfiler.conformsTo(malformedDcp));
    }

    @Test
    public void testToDomainObject() throws Exception {
        final DcsLicense expectedLicence = Dcps.getConformingLicense();
        final Dcp template = Dcps.getConformingDcp();

        assertTrue(licenseProfiler.conformsTo(template));

        // Modify the file id so it can be retrieved by our ResourceStreamSource
        // This means we have to re-link the DU and the File with a new Manifestation
        // Use the template DCP to build a new DCP with the modified file ID and associated objects.
        final DcsManifestation man = licenseProfiler.selectManifestation(template);
        final DcsManifestationFile manFile = man.getManifestationFiles().iterator().next();
        final DcsFile file = licenseProfiler.selectFile(template);
        final DcsFileRef fileRef = new DcsFileRef();
        file.setId("serialized_license.xml");
        fileRef.setRef(file.getId());
        manFile.setRef(fileRef);
        Set<DcsManifestationFile> mf = new HashSet<DcsManifestationFile>();
        mf.add(manFile);
        man.setManifestationFiles(mf);

        final Dcp dcp = new Dcp();
        dcp.addDeliverableUnit(licenseProfiler.selectDeliverableUnit(template));
        dcp.addManifestation(man);
        dcp.addFile(file);

        assertTrue(licenseProfiler.conformsTo(dcp));

        final DcsLicense actualLicense = underTest.deserializeObjectState(dcp);
        assertEquals(expectedLicence, actualLicense);
    }


}
