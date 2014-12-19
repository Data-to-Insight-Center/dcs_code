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

import static org.dataconservancy.packaging.model.builder.xstream.AttributeSetConverter.E_ATTRIBUTE;
import static org.dataconservancy.packaging.model.builder.xstream.AttributeSetConverter.E_ATTRIBUTES;
import static org.dataconservancy.packaging.model.builder.xstream.AttributeSetConverter.E_ATTRIBUTE_SET;
import static org.dataconservancy.packaging.model.builder.xstream.AttributeSetConverter.E_NAME;
import static org.dataconservancy.packaging.model.builder.xstream.AttributeSetConverter.E_TYPE;
import static org.dataconservancy.packaging.model.builder.xstream.AttributeSetConverter.E_VALUE;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

public class AttributeSetConverterTest {
    
    private String attributeSetXml;
    private String attributeSetsXml;
    private XStream x;
    private AttributeSet attributeSet;
    private Set<AttributeSet> attributeSets;
    
    @Before
    public void setUp() {
        x = XstreamAttributeSetFactory.newInstance();
        attributeSets = new LinkedHashSet<AttributeSet>();
        attributeSet = new AttributeSetImpl("TestAttSet");
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));
        attributeSet.getAttributes().add(new AttributeImpl("SomeAttribute4", "String", "SomeValue4"));
        AttributeSet attributeSet2 = new AttributeSetImpl("TestAttSet2");
        attributeSet2.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet2.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet2.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));
        attributeSet2.getAttributes().add(new AttributeImpl("SomeAttribute4", "String", "SomeValue4"));
        AttributeSet attributeSet3 = new AttributeSetImpl("TestAttSet3");
        attributeSet3.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet3.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet3.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));
        attributeSet3.getAttributes().add(new AttributeImpl("SomeAttribute4", "String", "SomeValue4"));
        AttributeSet attributeSet4 = new AttributeSetImpl("TestAttSet4");
        attributeSet4.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet4.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet4.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));
        attributeSet4.getAttributes().add(new AttributeImpl("SomeAttribute4", "String", "SomeValue4"));
        AttributeSet attributeSet5 = new AttributeSetImpl("TestAttSet5");
        attributeSet5.getAttributes().add(new AttributeImpl("SomeAttribute", "String", "SomeValue"));
        attributeSet5.getAttributes().add(new AttributeImpl("SomeAttribute2", "String", "SomeValue2"));
        attributeSet5.getAttributes().add(new AttributeImpl("SomeAttribute3", "String", "SomeValue3"));
        attributeSet5.getAttributes().add(new AttributeImpl("SomeAttribute4", "String", "SomeValue4"));
        attributeSets.add(attributeSet);
        attributeSets.add(attributeSet2);
        attributeSets.add(attributeSet3);
        attributeSets.add(attributeSet4);
        attributeSets.add(attributeSet5);
        setupXml();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }
    
    @Test
    public void testMarshall() throws SAXException, IOException {
        XMLAssert.assertXMLEqual(attributeSetXml, x.toXML(attributeSet));
        XMLAssert.assertXMLEqual(attributeSetsXml, x.toXML(attributeSets));
    }
    
    @Test
    public void testUnMarshall() {
        AttributeSet actual = (AttributeSet) x.fromXML(attributeSetXml);
        Assert.assertEquals(attributeSet, actual);
        Assert.assertEquals(attributeSet, x.fromXML(x.toXML(attributeSet)));
        @SuppressWarnings("unchecked")
        Set<AttributeSet> actuals = (Set<AttributeSet>) x.fromXML(attributeSetsXml);
        Assert.assertEquals(attributeSets, actuals);
        Assert.assertEquals(attributeSets, x.fromXML(x.toXML(attributeSets)));
    }
    
    private void setupXml() {
        attributeSetXml = "<" + E_ATTRIBUTE_SET + ">\n" +
                              "<" + E_NAME + ">TestAttSet</" + E_NAME + ">\n" +
                              "<" + E_ATTRIBUTES + ">\n" +
                                  "<" + E_ATTRIBUTE + ">\n" +
                                      "<" + E_NAME + ">SomeAttribute</" + E_NAME + ">\n" +
                                      "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                      "<" + E_VALUE + ">SomeValue</" + E_VALUE + ">\n" +
                                  "</" + E_ATTRIBUTE + ">\n" +
                                  "<" + E_ATTRIBUTE + ">\n" +
                                      "<" + E_NAME + ">SomeAttribute2</" + E_NAME + ">\n" +
                                      "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                      "<" + E_VALUE + ">SomeValue2</" + E_VALUE + ">\n" +
                                  "</" + E_ATTRIBUTE + ">\n" +
                                  "<" + E_ATTRIBUTE + ">\n" +
                                      "<" + E_NAME + ">SomeAttribute3</" + E_NAME + ">\n" +
                                      "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                      "<" + E_VALUE + ">SomeValue3</" + E_VALUE + ">\n" +
                                  "</" + E_ATTRIBUTE + ">\n" +
                                  "<" + E_ATTRIBUTE + ">\n" +
                                      "<" + E_NAME + ">SomeAttribute4</" + E_NAME + ">\n" +
                                      "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                      "<" + E_VALUE + ">SomeValue4</" + E_VALUE + ">\n" +
                                  "</" + E_ATTRIBUTE + ">\n" +
                              "</" + E_ATTRIBUTES + ">\n" +
                          "</" + E_ATTRIBUTE_SET + ">";
        
        attributeSetsXml = 
                   "<linked-hash-set>" +
                       "<" + E_ATTRIBUTE_SET + ">\n" +
                           "<" + E_NAME + ">TestAttSet</" + E_NAME + ">\n" +
                           "<" + E_ATTRIBUTES + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                    "<" + E_NAME + ">SomeAttribute2</" + E_NAME + ">\n" +
                                    "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                    "<" + E_VALUE + ">SomeValue2</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute3</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue3</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute4</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue4</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                           "</" + E_ATTRIBUTES + ">\n" +
                       "</" + E_ATTRIBUTE_SET + ">" +
                       "<" + E_ATTRIBUTE_SET + ">\n" +
                           "<" + E_NAME + ">TestAttSet2</" + E_NAME + ">\n" +
                           "<" + E_ATTRIBUTES + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                  "<" + E_NAME + ">SomeAttribute2</" + E_NAME + ">\n" +
                                  "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                  "<" + E_VALUE + ">SomeValue2</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute3</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue3</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute4</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue4</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                           "</" + E_ATTRIBUTES + ">\n" +
                       "</" + E_ATTRIBUTE_SET + ">" +
                       "<" + E_ATTRIBUTE_SET + ">\n" +
                               "<" + E_NAME + ">TestAttSet3</" + E_NAME + ">\n" +
                               "<" + E_ATTRIBUTES + ">\n" +
                                   "<" + E_ATTRIBUTE + ">\n" +
                                       "<" + E_NAME + ">SomeAttribute</" + E_NAME + ">\n" +
                                       "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                       "<" + E_VALUE + ">SomeValue</" + E_VALUE + ">\n" +
                                   "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute2</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue2</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute3</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue3</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute4</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue4</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                           "</" + E_ATTRIBUTES + ">\n" +
                       "</" + E_ATTRIBUTE_SET + ">" +
                       "<" + E_ATTRIBUTE_SET + ">\n" +
                           "<" + E_NAME + ">TestAttSet4</" + E_NAME + ">\n" +
                           "<" + E_ATTRIBUTES + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute2</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue2</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute3</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue3</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute4</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue4</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                           "</" + E_ATTRIBUTES + ">\n" +
                       "</" + E_ATTRIBUTE_SET + ">" +
                       "<" + E_ATTRIBUTE_SET + ">\n" +
                           "<" + E_NAME + ">TestAttSet5</" + E_NAME + ">\n" +
                           "<" + E_ATTRIBUTES + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute2</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue2</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute3</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue3</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                               "<" + E_ATTRIBUTE + ">\n" +
                                   "<" + E_NAME + ">SomeAttribute4</" + E_NAME + ">\n" +
                                   "<" + E_TYPE + ">String</" + E_TYPE + ">\n" +
                                   "<" + E_VALUE + ">SomeValue4</" + E_VALUE + ">\n" +
                               "</" + E_ATTRIBUTE + ">\n" +
                           "</" + E_ATTRIBUTES + ">\n" +
                       "</" + E_ATTRIBUTE_SET + ">" +
                   "</linked-hash-set>";
    }
}
