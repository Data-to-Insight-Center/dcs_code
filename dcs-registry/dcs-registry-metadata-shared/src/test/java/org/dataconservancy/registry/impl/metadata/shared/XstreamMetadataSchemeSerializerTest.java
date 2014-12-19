package org.dataconservancy.registry.impl.metadata.shared;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class XstreamMetadataSchemeSerializerTest {

    private static final String SOURCE = "http://www.google.com";
    private static final String SCHEMA_VERSION = "1.0";
    private static final String SCHEMA_NAME = "FGDC";
    private static final String SCHEMA_URL = "http://dataconservancy.org";

    private static final String SERIALIZED_METADATASCHEME = "<metadataScheme>\n" +
            "  <name>FGDC</name>\n" +
            "  <schemaVersion>1.0</schemaVersion>\n" +
            "  <schemaUrl>http://dataconservancy.org</schemaUrl>\n" +
            "  <source>http://www.google.com</source>\n" +
            "</metadataScheme>";

    private XstreamMetadataSchemeSerializer underTest = new XstreamMetadataSchemeSerializer();

    private DcsMetadataScheme metadataScheme;

    @Before
    public void setUp() throws Exception {
        metadataScheme = new DcsMetadataScheme();

        metadataScheme.setSchemaUrl(SCHEMA_URL);
        metadataScheme.setName(SCHEMA_NAME);
        metadataScheme.setSchemaVersion(SCHEMA_VERSION);
        metadataScheme.setSource(SOURCE);
    }

    @Test
    public void testDeserialize() throws Exception {
        DcsMetadataScheme actual = underTest.deserialize(IOUtils.toInputStream(SERIALIZED_METADATASCHEME));
        assertEquals(metadataScheme, actual);
    }

    @Test
    public void testSerialize() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        underTest.serialize(metadataScheme, out);
        XMLAssert.assertXMLEqual(SERIALIZED_METADATASCHEME, new String(out.toByteArray()));
    }
}
