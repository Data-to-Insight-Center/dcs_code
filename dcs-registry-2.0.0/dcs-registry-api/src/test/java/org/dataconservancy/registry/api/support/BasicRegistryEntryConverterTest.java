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

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.builder.xstream.DcsPullDriver;
import org.dataconservancy.registry.api.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.dataconservancy.registry.api.support.BasicRegistryEntryConverter.E_ENTRY;
import static org.dataconservancy.registry.api.support.BasicRegistryEntryConverter.E_DESCRIPTION;
import static org.dataconservancy.registry.api.support.BasicRegistryEntryConverter.E_TYPE;
import static org.dataconservancy.registry.api.support.BasicRegistryEntryConverter.E_ID;
import static org.dataconservancy.registry.api.support.BasicRegistryEntryConverter.E_KEYS;
import static org.dataconservancy.registry.api.support.BasicRegistryEntryConverter.E_KEY;


/**
 * A unit test for basic registry entry converter.
 */
public class BasicRegistryEntryConverterTest {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    private final static String ENTRY_KEY_ONE = "key:1";
    private final static String ENTRY_KEY_TWO = "key:2";
    private String XML;
    private XStream xstream;

    private RegistryEntry<String> entry;
    private void setupXML() {
        XML =
                "    <" + E_ENTRY + " xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\">\n" +
                "       <" + E_ID + ">" + entry.getId() + "</" + E_ID + ">\n" +
                "       <" + E_TYPE + ">" + entry.getType() + "</" + E_TYPE + ">\n" +
                "       <" + E_DESCRIPTION + ">" + entry.getDescription() + "</" + E_DESCRIPTION + ">\n" +
                "       <" + E_KEYS + ">\n" + 
                "           <" + E_KEY + ">" + ENTRY_KEY_ONE + "</" + E_KEY + ">\n" +
                "           <" + E_KEY + ">" + ENTRY_KEY_TWO + "</" + E_KEY + ">\n" +
                "       </" + E_KEYS + ">\n" +
                "    </" + E_ENTRY + ">\n";
    }
    @Before
    public void setUp() throws Exception {
        Set<String> keys = new HashSet<String>();
        keys.add(ENTRY_KEY_ONE);
        keys.add(ENTRY_KEY_TWO);
        entry = new BasicRegistryEntryImpl<String>("registryEntryId", "foo", "registryEntryType", keys, "sample registry entry");
        setupXML();
        setupXstream();
        
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }
    
    private void setupXstream() {
        final QNameMap qnames = new QNameMap();
        
        final String defaultnsUri = "http://dataconservancy.org/schemas/dcp/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        xstream = new XStream(driver);
        xstream.setMode(XStream.NO_REFERENCES);
        
        // XStream converter, alias, and QName registrations
        xstream.alias(BasicRegistryEntryConverter.E_ENTRY, BasicRegistryEntryImpl.class);
        xstream.registerConverter(new BasicRegistryEntryConverter());
        qnames.registerMapping(new QName(defaultnsUri, BasicRegistryEntryConverter.E_ENTRY), BasicRegistryEntryImpl.class);
    }
    
    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, xstream.toXML(entry));
        assertTrue(true);
    }

    @Test
    public void testUnmarshal() {
        RegistryEntry<String> actual = (RegistryEntry<String>) xstream.fromXML(XML);
        assertEquals(entry.getId(), actual.getId());
        assertEquals(entry.getType(), actual.getType());
        assertEquals(entry.getKeys(), actual.getKeys());
        assertEquals(entry.getDescription(), actual.getDescription());
        
        RegistryEntry<String> roundTripActual = (RegistryEntry<String>) xstream.fromXML(xstream.toXML(entry));
        assertEquals(entry.getId(), roundTripActual.getId());
        assertEquals(entry.getDescription(), roundTripActual.getDescription());
        assertEquals(entry.getKeys(), roundTripActual.getKeys());
        assertEquals(entry.getType(), roundTripActual.getType());
    }
}
