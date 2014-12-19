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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.builder.AttributeSetBuilder;
import org.dataconservancy.packaging.model.builder.xstream.XstreamAttributeSetBuilder;
import org.dataconservancy.packaging.model.builder.xstream.XstreamAttributeSetFactory;
import org.junit.Before;
import org.junit.Test;

public class XstreamAttributeSetBuilderTest {
    
    private AttributeSetBuilder attributeSetBuilder;

    @Before
    public void setUp() {
        attributeSetBuilder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
    }

    @Test
    public void testBuildAttributeSetRoundTrip() {
        
        AttributeSet attributeSet = new AttributeSetImpl("TestAttSet");
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        attributeSetBuilder.buildAttributeSet(attributeSet, sink);
        
        ByteArrayInputStream stream = new ByteArrayInputStream(sink.toByteArray());
        AttributeSet returnedAttributeSet = attributeSetBuilder.buildAttributeSet(stream);
        
        Assert.assertEquals(attributeSet, returnedAttributeSet);
    }
    
    @Test
    public void testBuildAttributeSetsRoundTrip() {
        
        Set<AttributeSet> attributeSets = new HashSet<AttributeSet>();
        
        AttributeSet attributeSet = new AttributeSetImpl("TestAttSet");
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));
        
        AttributeSet attributeSet2 = new AttributeSetImpl("TestAttSet2");
        attributeSet2.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet2.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet2.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));
        
        AttributeSet attributeSet3 = new AttributeSetImpl("TestAttSet3");
        attributeSet3.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet3.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet3.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));
        
        attributeSets.add(attributeSet);
        attributeSets.add(attributeSet2);
        attributeSets.add(attributeSet3);

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        attributeSetBuilder.buildAttributeSets(attributeSets, sink);
        
        ByteArrayInputStream stream = new ByteArrayInputStream(sink.toByteArray());
        Set<AttributeSet> returnedAttributeSets = attributeSetBuilder.buildAttributeSets(stream);
        
        Assert.assertEquals(attributeSets, returnedAttributeSets);
    }
}
