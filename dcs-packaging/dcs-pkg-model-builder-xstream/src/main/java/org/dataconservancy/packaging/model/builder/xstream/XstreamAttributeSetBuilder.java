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

import com.thoughtworks.xstream.XStream;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.model.builder.AttributeSetBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * XStream impl of AttributeSetBuilder
 */
public class XstreamAttributeSetBuilder implements AttributeSetBuilder {

    private XStream x;

    public XstreamAttributeSetBuilder(XStream x) {
        this.x = x;
    }

    @Override
    public AttributeSet buildAttributeSet(InputStream inputStream) {
        return (AttributeSet)x.fromXML(inputStream);
    }

    @Override
    public void buildAttributeSet(AttributeSet attributeSet, OutputStream outputStream) {
       x.toXML(attributeSet, outputStream);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<AttributeSet> buildAttributeSets(InputStream inputStream) {
        return (Set<AttributeSet>)x.fromXML(inputStream);
    }

    @Override
    public void buildAttributeSets(Set<AttributeSet> attributeSets, OutputStream outputStream) {
       x.toXML(attributeSets, outputStream);
    }


}
