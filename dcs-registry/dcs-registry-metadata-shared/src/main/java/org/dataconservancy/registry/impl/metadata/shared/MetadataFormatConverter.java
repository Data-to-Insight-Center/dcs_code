package org.dataconservancy.registry.impl.metadata.shared;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 */
public class MetadataFormatConverter implements Converter {
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        DcsMetadataFormat metadataFormat = (DcsMetadataFormat) source;

        writer.startNode("name");
        writer.setValue(metadataFormat.getName());
        writer.endNode();

        writer.startNode("version");
        writer.setValue(metadataFormat.getVersion());
        writer.endNode();

        writer.startNode("scheme");
        context.convertAnother(metadataFormat.getScheme());
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // Default method body
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return MetadataFormatConverter.class == type;
    }
}
