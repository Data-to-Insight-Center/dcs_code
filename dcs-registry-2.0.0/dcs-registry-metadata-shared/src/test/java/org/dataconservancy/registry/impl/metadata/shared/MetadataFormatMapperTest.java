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

import java.io.IOException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.support.DcpUtil;

import static org.junit.Assert.assertEquals;

public class MetadataFormatMapperTest {
    
    private DcsMetadataFormat format;
    private DcsMetadataScheme master;
    private DcsMetadataScheme child;
    
    private MetadataSchemeMapper schemeMapper;
    private MetadataFormatMapper underTest;
    
    @Before
    public void setup() {
        format = new DcsMetadataFormat();
        format.setName("test format");
        format.setVersion(MetadataRegistryConstant.METADATAFORMAT_VERSION_ONE);
        format.setId("dataconservancy.org:formats:file:metadata:fgdc:xml");
        
        master = new DcsMetadataScheme();
        master.setName("test master scheme");
        master.setSchemaUrl("www.dataconservancy.org");
        master.setSchemaVersion(MetadataRegistryConstant.METADATASCHEME_VERSION_ONE);
        master.setSource("/");
        
        child = new DcsMetadataScheme();
        child.setName("test child scheme");
        child.setSchemaUrl("www.google.com");
        child.setSchemaVersion(MetadataRegistryConstant.METADATASCHEME_VERSION_ONE);
        child.setSource("/");
        
        format.addScheme(master);
        format.addScheme(child);
        
        schemeMapper = new MetadataSchemeMapper();
        underTest = new MetadataFormatMapper(schemeMapper);
    }
    
    @Test
    public void testRoundTrip() throws IOException {
        Dcp dcp = new Dcp();
        underTest.serializeObjectState(format, dcp);
        DcsMetadataFormat actualFormat = underTest.deserializeObjectState(dcp);
        
        assertEquals(format, actualFormat);
        
        //Ensure the schemes are in the same order since order is now important.
        Iterator<DcsMetadataScheme> schemeIter = actualFormat.getSchemes().iterator();
        assertEquals(master, schemeIter.next());
        assertEquals(child, schemeIter.next());
    }
    
    @Test
    public void testDiscover() throws IOException {
        Dcp dcp = new Dcp();
        underTest.serializeObjectState(format, dcp);
        
        Set<String> ids = underTest.discover(dcp);
        assertEquals(1, ids.size());
        
        DcsDeliverableUnit du = (DcsDeliverableUnit) DcpUtil.asMap(dcp).get(ids.iterator().next());
        assertEquals(format.getName(),du.getTitle());
        assertEquals(MetadataRegistryConstant.METADATAFORMAT_REGISTRY_ENTRY_TYPE, du.getType());
    }
    
    @Test
    public void testRetrieveObjectDu() throws IOException {
        Dcp dcp = new Dcp();
        underTest.serializeObjectState(format, dcp);
        
        DcsDeliverableUnit du = underTest.retrieveObjectDu(dcp);
        assertEquals(format.getName(),du.getTitle());
        assertEquals(MetadataRegistryConstant.METADATAFORMAT_REGISTRY_ENTRY_TYPE, du.getType());
    }

    @Test
    public void testVersion() throws Exception {
        final DcsMetadataFormat format = new DcsMetadataFormat();
        final String version = "4.0.0";
        format.setId("fooId");
        format.setName("Foo");
        format.setVersion(version);

        final DcsMetadataScheme scheme = new DcsMetadataScheme();
        format.setSchemes(Arrays.asList(scheme));
        scheme.setName("Bar");
        scheme.setSchemaUrl("http://maven.apache.org/xsd/maven-4.0.0.xsd");
        scheme.setSchemaVersion(version);
        scheme.setSource("http://maven.apache.org/xsd/maven-4.0.0.xsd");

        final Dcp dcp = new Dcp();

        underTest.serializeObjectState(format, dcp);

        final DcsMetadataFormat formatP = underTest.deserializeObjectState(dcp);
        assertEquals(version, formatP.getVersion());
    }
}