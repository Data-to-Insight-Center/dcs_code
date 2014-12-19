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
