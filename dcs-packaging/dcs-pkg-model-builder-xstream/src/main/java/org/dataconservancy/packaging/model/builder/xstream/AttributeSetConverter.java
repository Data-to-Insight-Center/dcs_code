/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.model.builder.xstream;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * AttributeSetConverter is used by Xstream object builder to serialize and deserialize {@link AttributeSet} objects.
 */
public class AttributeSetConverter extends AbstractEntityConverter {
    
    protected static final String E_ATTRIBUTE_SET = "AttributeSet";
    protected static final String E_NAME = "Name";
    protected static final String E_ATTRIBUTES = "Attributes";
    protected static final String E_ATTRIBUTE = "Attribute";
    protected static final String E_TYPE = "Type";
    protected static final String E_VALUE = "Value";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        
        AttributeSetImpl attributeSet = (AttributeSetImpl) source;
        if (attributeSet.getName() != null) {
            writer.startNode(E_NAME);
            writer.setValue(attributeSet.getName());
            writer.endNode();
        }
        
        if (attributeSet.getAttributes() != null) {
            writer.startNode(E_ATTRIBUTES);
            for (Attribute att : attributeSet.getAttributes()) {
                if (att != null) {
                    writer.startNode(E_ATTRIBUTE);
                    if (att.getName() != null && !att.getName().isEmpty()) {
                        writer.startNode(E_NAME);
                        writer.setValue(att.getName());
                        writer.endNode();                        
                    }
                    
                    if (att.getType() != null && !att.getType().isEmpty()) {
                        writer.startNode(E_TYPE);
                        writer.setValue(att.getType());
                        writer.endNode();                        
                    }
                    
                    if (att.getValue() != null && !att.getValue().isEmpty()) {
                        writer.startNode(E_VALUE);
                        writer.setValue(att.getValue());
                        writer.endNode();
                    }
                    writer.endNode();
                }
            }
            writer.endNode();
        }

    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        AttributeSetImpl attributeSet = new AttributeSetImpl();
        
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String name = getElementName(reader);
            if (name.equals(E_NAME)) {
                String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    attributeSet.setName(value.trim());
                }
            }
            else if (name.equals(E_ATTRIBUTES)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_ATTRIBUTE)) {
                        String attName = null;
                        String attType = null;
                        String attValue = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_NAME)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    attName = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_TYPE)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    attType = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_VALUE)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    attValue = value.trim();
                                }
                            }
                            if (attName != null && attType != null && attValue != null) {
                                attributeSet.addAttribute(new AttributeImpl(attName, attType, attValue));
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
            }
            reader.moveUp();
        }
        
        return attributeSet;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return type == AttributeSetImpl.class;
    }

}
