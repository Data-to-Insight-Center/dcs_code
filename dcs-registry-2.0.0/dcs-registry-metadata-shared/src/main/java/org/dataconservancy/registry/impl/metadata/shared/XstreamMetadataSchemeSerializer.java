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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class XstreamMetadataSchemeSerializer implements MetadataSerializer<DcsMetadataScheme> {

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
