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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 */
public class MetadataFormatConverter implements Converter {
    
    public final static String E_FORMAT = "metadataFormat";
    private final static String E_NAME = "name";
    private final static String E_VERSION = "version";
    private final static String E_ID = "id";
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        DcsMetadataFormat metadataFormat = (DcsMetadataFormat) source;

        writer.startNode(E_NAME);
        writer.setValue(metadataFormat.getName());
        writer.endNode();

        writer.startNode(E_ID);
        writer.setValue(metadataFormat.getId());
        writer.endNode();
        
        writer.startNode(E_VERSION);
        writer.setValue(metadataFormat.getVersion());
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        DcsMetadataFormat metadataFormat = new DcsMetadataFormat();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            String nodeValue = reader.getValue();
            if (nodeName.equals(E_NAME)) {
                metadataFormat.setName(nodeValue);
            }

            if (nodeName.equals(E_ID)) {
                metadataFormat.setId(nodeValue);
            }
            
            if (nodeName.equals(E_VERSION)) {
                metadataFormat.setVersion(nodeValue);
            }

            reader.moveUp();
        }
        
        return metadataFormat;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsMetadataFormat.class == type;
    }
}
