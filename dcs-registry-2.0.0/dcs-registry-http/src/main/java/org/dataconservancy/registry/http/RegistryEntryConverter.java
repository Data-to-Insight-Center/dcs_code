package org.dataconservancy.registry.http;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.registry.api.RegistryEntry;

/**
 * XStream Converter for RegistryEntry objects.
 */
public class RegistryEntryConverter implements Converter {

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        RegistryEntry<?> entry = (RegistryEntry<?>) source;

        writer.startNode("entry");
        writer.addAttribute("id", entry.getId());
        writer.addAttribute("type", entry.getType());

        context.convertAnother(entry.getEntry());

        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // Default method body
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return RegistryEntry.class.isAssignableFrom(type);
    }
}
