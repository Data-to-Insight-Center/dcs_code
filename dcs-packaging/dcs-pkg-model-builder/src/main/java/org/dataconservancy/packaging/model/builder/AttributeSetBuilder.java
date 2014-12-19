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
package org.dataconservancy.packaging.model.builder;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.dataconservancy.mhf.representation.api.AttributeSet;

/**
 * 
 * AttributeSetBuilder is an abstraction that provides methods to serialize and deserialize an {@link AttributeSetImpl}
 * objects
 * 
 */
public interface AttributeSetBuilder {
    
    /**
     * Builds a {@link AttributeSet } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * AttributeSetImpl serialization specification. The <code>InputStream</code> will be deserialized into the
     * corresponding AttributeSetImpl java object and returned.
     * 
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @throws {@link InvalidXmlException}
     * @return the {@link AttributeSet } object
     */
    public AttributeSet buildAttributeSet(InputStream inputStream);

    /**
     * Builds a {@link Set<AttributeSet> } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * AttributeSetImpl serialization specification. The <code>InputStream</code> will be deserialized into the
     * corresponding AttributeSetImpl java object and returned.
     * 
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @throws {@link InvalidXmlException}
     * @return the {@link AttributeSet } object
     */
    public Set<AttributeSet> buildAttributeSets(InputStream inputStream);
    
    /**
     * Serializes the supplied {@link AttributeSet} to XML, formatted according to the AttributeSetImpl serialization
     * specification.
     * 
     * @param attributeSet the {@link AttributeSet} to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     */
    public void buildAttributeSet(AttributeSet attributeSet, OutputStream outputStream);
    
    /**
     * Serializes the supplied set of {@link Set<AttributeSet>} to XML, formatted according to the AttributeSetImpl serialization
     * specification.
     * 
     * @param attributeSets the {@link Set<AttributeSet>} to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     */
    public void buildAttributeSets(Set<AttributeSet> attributeSets, OutputStream outputStream);
}
