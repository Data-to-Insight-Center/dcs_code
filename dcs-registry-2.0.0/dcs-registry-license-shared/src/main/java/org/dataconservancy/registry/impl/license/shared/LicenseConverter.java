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
package org.dataconservancy.registry.impl.license.shared;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.net.URI;

/**
 *
 */
public class LicenseConverter implements Converter {

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        DcsLicense license = (DcsLicense) source;

        writer.startNode("name");
        writer.setValue(license.getName());
        writer.endNode();

        writer.startNode("tag");
        writer.setValue(license.getTag());
        writer.endNode();

        writer.startNode("version");
        writer.setValue(license.getVersion());
        writer.endNode();

        writer.startNode("summary");
        writer.setValue(license.getSummary());
        writer.endNode();

        writer.startNode("fullText");
        writer.setValue(license.getFullText());
        writer.endNode();

        writer.startNode("uris");
        for (URI uri : license.getUris()) {
            writer.startNode("uri");
            writer.setValue(uri.toASCIIString());
            writer.endNode();
        }
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // Default method body
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == DcsLicense.class;
    }
}