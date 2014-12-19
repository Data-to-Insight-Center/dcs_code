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

import org.dataconservancy.dcs.util.stream.api.StreamSource;
import org.dataconservancy.dcs.util.stream.fs.FilesystemStreamSource;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Trivial base class for tests needing to operate on DcsMetadataScheme instances or serializations.
 */
public abstract class BaseMetadataSchemeTest {

    private static final String SAMPLES_RESOURCE_PATH = "/sample_metadataschemes";

    static final StreamSource streamSource = new FilesystemStreamSource(
            new File(MetadataSchemeMapperTest.class.getResource(SAMPLES_RESOURCE_PATH).getPath()));

    static final Map<String, DcsMetadataScheme> expectedSchemes = new HashMap<String, DcsMetadataScheme>();

    static final Map<String, Dcp> expectedDcps = new HashMap<String, Dcp>();

    static final DcsXstreamStaxModelBuilder mb = new DcsXstreamStaxModelBuilder();

    @BeforeClass
    public static void populateExpectedSchemesAndDcps() throws IOException, InvalidXmlException {
        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName("Some Scheme Name");
        scheme.setSchemaUrl("http://www.google.com");
        scheme.setSource("http://dataconservancy.org/datastream/1234");
        scheme.setSchemaVersion("1.0");

        expectedSchemes.put("0", scheme);

        assertTrue(expectedSchemes.size() > 0);

        for (String streamId : streamSource.streams()) {
            expectedDcps.put(streamId, mb.buildSip(streamSource.getStream(streamId)));
        }

        assertTrue(expectedDcps.size() > 0);
    }
}
