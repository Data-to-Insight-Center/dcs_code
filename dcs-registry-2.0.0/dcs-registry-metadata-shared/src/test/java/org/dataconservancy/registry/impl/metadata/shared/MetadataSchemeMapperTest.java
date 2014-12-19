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

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.support.DcpUtil;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Trivial test class for MetadataSchemeMapper
 */
public class MetadataSchemeMapperTest extends BaseMetadataSchemeTest {

    @Test
    public void testFrom() throws IOException, InvalidXmlException {
        MetadataSchemeMapper underTest = new MetadataSchemeMapper();
        Map<String, DcsMetadataScheme> actualSchemes = new HashMap<String, DcsMetadataScheme>();
        for (String streamId : streamSource.streams()) {
            Dcp dcp = mb.buildSip(streamSource.getStream(streamId));
            for (String archiveId : underTest.discover(dcp)) {
                actualSchemes.put(archiveId, underTest.from(archiveId, dcp, null));
            }
        }

        assertTrue(actualSchemes.keySet().equals(expectedSchemes.keySet()));

        for (Map.Entry<String, DcsMetadataScheme> expected : expectedSchemes.entrySet()) {
            assertEquals(expected.getValue(), actualSchemes.get(expected.getKey()));
        }
    }

    @Test
    public void testTo() throws Exception {
        MetadataSchemeMapper underTest = new MetadataSchemeMapper();

        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName("Some Scheme Name");
        scheme.setSchemaUrl("http://www.google.com");
        scheme.setSource("http://dataconservancy.org/datastream/1234");
        scheme.setSchemaVersion("1.0");

        Dcp actualDcp = underTest.to(scheme, null, "", "");
        
        //Change the source of the file to match what's in the sample dcp
        DcsFile file = (DcsFile) DcpUtil.asMap(actualDcp).get("3");
        file.setSource("http://dataconservancy.org/datastream/5678");
        
        Dcp expectedDcp = expectedDcps.values().iterator().next();

        assertEquals(expectedDcp, actualDcp);
    }

}
