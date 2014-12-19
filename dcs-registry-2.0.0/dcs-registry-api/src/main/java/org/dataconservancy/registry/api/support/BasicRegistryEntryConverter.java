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
package org.dataconservancy.registry.api.support;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts a basic registry entry to and from XML. Note: The converter doesn't handle serializing the object referenced by the entity.
 *
 */
public class BasicRegistryEntryConverter extends AbstractEntityConverter {

    public static final String E_ENTRY = "basicRegistryEntry";
    public static final String E_ID = "entryId";
    public static final String E_KEYS = "keys";
    public static final String E_KEY = "key";
    public static final String E_DESCRIPTION = "description";
    public static final String E_TYPE = "type";
 
    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        final BasicRegistryEntryImpl entry = (BasicRegistryEntryImpl) source;
        if (entry != null) {

            if (!isEmptyOrNull(entry.getId())) {
                writer.startNode(E_ID);
                writer.setValue(entry.getId());
                writer.endNode();
            }

            if (!isEmptyOrNull(entry.getDescription())) {
                writer.startNode(E_DESCRIPTION);
                writer.setValue(entry.getDescription());
                writer.endNode();
            }

            if (!isEmptyOrNull(entry.getType())) {
                writer.startNode(E_TYPE);
                writer.setValue(entry.getType());
                writer.endNode();
            }

            Collection<String> keys = entry.getKeys();
            if (keys != null && !keys.isEmpty()) {
                writer.startNode(E_KEYS);
                for (String key : keys) {
                    if (key != null) {
                        writer.startNode(E_KEY);
                        writer.setValue(key);
                        writer.endNode();
                    }
                }
                writer.endNode();
            }  
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        BasicRegistryEntryImpl registryEntry = new BasicRegistryEntryImpl();

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);

            if (ename.equals(E_ID)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    registryEntry.setId(value.trim());
                }
            } else if (ename.equals(E_DESCRIPTION)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    registryEntry.setDescription(value.trim());
                }
            } else if (ename.equals(E_TYPE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    registryEntry.setEntryType(value.trim());
                }
            } else if (ename.equals(E_KEYS)) {
                Set<String> keys = new HashSet<String>();
                
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_KEY)) {
                        final String value = reader.getValue();
                        if (!isEmptyOrNull(value)) {
                            keys.add(value.trim());
                        }
                    }
                    reader.moveUp();
                }
                
                registryEntry.setKeys(keys);
            }

            reader.moveUp();
        }

        return registryEntry;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == BasicRegistryEntryImpl.class;
    }
    
}