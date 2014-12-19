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
package org.dataconservancy.registry.impl.metadata.shared;

import org.dataconservancy.model.dcp.Dcp;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A trivial test for DcsMetadataScheme
 */
public class MetadataSchemeProfileTest extends BaseMetadataSchemeTest {

    @Test
    public void testConforms() throws Exception {
        for (Dcp candidatePackage : expectedDcps.values()) {
            assertTrue(new MetadataSchemeProfile().conforms(candidatePackage));
        }
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE,
                new MetadataSchemeProfile().getType());
    }

    @Test
    public void testGetVersion() throws Exception {
        assertEquals(MetadataRegistryConstant.METADATASCHEME_VERSION_ONE, new MetadataSchemeProfile().getVersion());
    }
}
