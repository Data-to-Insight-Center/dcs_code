package org.dataconservancy.registry.impl.metadata.shared;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class XstreamMetadataFormatSerializer implements MetadataSerializer<DcsMetadataFormat> {

    private final XStream xstream;

    public XstreamMetadataFormatSerializer() {
        this.xstream = new XStream(new StaxDriver());

        this.xstream.alias(MetadataFormatConverter.E_FORMAT, DcsMetadataFormat.class);
        this.xstream.registerConverter(new MetadataFormatConverter());

        this.xstream.alias("metadataScheme", DcsMetadataScheme.class);
        this.xstream.registerConverter(new MetadataSchemeConverter());
    }

    @Override
    public DcsMetadataFormat deserialize(InputStream in) {
        return (DcsMetadataFormat) xstream.fromXML(in);
    }

    @Override
    public void serialize(DcsMetadataFormat metadataFormat, OutputStream out) {
        xstream.toXML(metadataFormat, out);
    }

}
