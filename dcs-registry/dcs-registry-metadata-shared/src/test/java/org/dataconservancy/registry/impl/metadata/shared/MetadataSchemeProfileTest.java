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
