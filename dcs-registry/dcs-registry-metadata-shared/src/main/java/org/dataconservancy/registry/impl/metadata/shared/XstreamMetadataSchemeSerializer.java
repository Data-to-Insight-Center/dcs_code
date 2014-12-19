package org.dataconservancy.registry.impl.metadata.shared;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class XstreamMetadataSchemeSerializer implements MetadataSchemeSerializer {

    private final XStream xstream;

    public XstreamMetadataSchemeSerializer() {
        this.xstream = new XStream(new StaxDriver());
        this.xstream.alias("metadataScheme", DcsMetadataScheme.class);
        this.xstream.registerConverter(new MetadataSchemeConverter());
    }


    public XstreamMetadataSchemeSerializer(XStream xstream) {
        this.xstream = xstream;
    }

    @Override
    public DcsMetadataScheme deserialize(InputStream in) {
        return (DcsMetadataScheme) xstream.fromXML(in);
    }

    @Override
    public void serialize(DcsMetadataScheme metadataScheme, OutputStream out) {
        xstream.toXML(metadataScheme, out);
    }
}
