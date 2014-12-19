package org.dataconservancy.registry.impl.metadata.shared;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
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

        Dcp actualDcp = underTest.to(scheme, null);
        Dcp expectedDcp = expectedDcps.values().iterator().next();

        assertEquals(expectedDcp, actualDcp);
    }

}
