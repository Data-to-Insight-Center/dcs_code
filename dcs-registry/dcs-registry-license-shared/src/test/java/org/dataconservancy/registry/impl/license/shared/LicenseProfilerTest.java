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
import org.dataconservancy.profile.support.ProfileStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class LicenseProfilerTest {

    private Dcp conformingDcp;

    private Dcp missingFileDcp;

    private Dcp missingDuDcp;

    private Dcp missingManifestationDcp;

    private Dcp missingTechenvDcp;

    private Dcp missingDataModelDcp;

    private Dcp incorrectTypeDcp;

    private LicenseProfiler underTest;
    
    @Before
    public void setUp() throws Exception {
        conformingDcp = Dcps.getConformingDcp();
        missingFileDcp = Dcps.getMissingFileDcp();
        missingDuDcp = Dcps.getMissingDuDcp();
        missingManifestationDcp = Dcps.getMissingManifestationDcp();
        missingTechenvDcp = Dcps.getMissingTechenvDcp();
        missingDataModelDcp = Dcps.getMissingDataModelDcp();
        incorrectTypeDcp = Dcps.getIncorrectDuTypeDcp();
        
        underTest = new LicenseProfiler();
        underTest.setDeliverableUnitProfile(ProfileStatement.equals(LicenseRegistryConstant.LICENSE_REGISTRY_ENTRY_TYPE));
        underTest.setFileProfile(ProfileStatement.equals("java-entry-serialization"));
        final Set<ProfileStatement> techEnv = new HashSet<ProfileStatement>();
        techEnv.add(ProfileStatement.equals("DCS Data Model http://dataconservancy.org/schemas/dcp/1.0"));
        techEnv.add(ProfileStatement.equals("XStream 1.3.1"));
        techEnv.add(ProfileStatement.startsWith("Java Version: 1.6"));
        underTest.setTechnicalEnvironmentProfile(techEnv);
    }

    @Test
    public void testConformsTo() throws Exception {   
        assertTrue(underTest.conformsTo(conformingDcp));
    }
    
    @Test
    public void testMissingFile() throws Exception {   
        assertFalse(underTest.conformsTo(missingFileDcp));
    }
    
    @Test
    public void testMissingDu() throws Exception {   
        assertFalse(underTest.conformsTo(missingDuDcp));
    }
    
    @Test
    public void testMissingManifestation() throws Exception {
        assertFalse(underTest.conformsTo(missingManifestationDcp));
    }

    @Test
    public void testMissingTechenv() throws Exception {
        assertFalse(underTest.conformsTo(missingTechenvDcp));
    }

    @Test
    public void testMissingDataModel() throws Exception {
        assertFalse(underTest.conformsTo(missingDataModelDcp));
    }

    @Test
    public void testIncorrectType() throws Exception {
        assertFalse(underTest.conformsTo(incorrectTypeDcp));
    }

}
