package org.dataconservancy.registry.impl.metadata.shared;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * XStream converter for DcsMetadataScheme
 */
public class MetadataSchemeConverter implements Converter {

    public static final String E_SCHEME = "scheme";
    private final String E_NAME = "name";
    private final String E_SCHEMAVERSION = "schemaVersion";
    private final String E_SCHEMAURL = "schemaUrl";
    private final String E_SOURCE = "source";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        DcsMetadataScheme metadataScheme = (DcsMetadataScheme) source;

        writer.startNode(E_NAME);
        writer.setValue(metadataScheme.getName());
        writer.endNode();

        writer.startNode(E_SCHEMAVERSION);
        writer.setValue(metadataScheme.getSchemaVersion());
        writer.endNode();

        writer.startNode(E_SCHEMAURL);
        writer.setValue(metadataScheme.getSchemaUrl());
        writer.endNode();

        writer.startNode(E_SOURCE);
        writer.setValue(metadataScheme.getSource());
        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        DcsMetadataScheme metadataScheme = new DcsMetadataScheme();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            String nodeValue = reader.getValue();
            if (nodeName.equals(E_NAME)) {
                metadataScheme.setName(nodeValue);
            }

            if (nodeName.equals(E_SCHEMAVERSION)) {
                metadataScheme.setSchemaVersion(nodeValue);
            }

            if (nodeName.equals(E_SCHEMAURL)) {
                metadataScheme.setSchemaUrl(nodeValue);
            }

            if (nodeName.equals(E_SOURCE)) {
                metadataScheme.setSource(nodeValue);
            }
            reader.moveUp();
        }

        return metadataScheme;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsMetadataScheme.class == type;
    }
}
