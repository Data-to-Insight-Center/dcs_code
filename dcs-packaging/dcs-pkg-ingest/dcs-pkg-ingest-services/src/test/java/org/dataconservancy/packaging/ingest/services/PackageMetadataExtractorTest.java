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
package org.dataconservancy.packaging.ingest.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.extractor.api.MetadataExtractor;
import org.dataconservancy.mhf.extractors.BagItManifestMetadataExtractor;
import org.dataconservancy.mhf.extractors.BagItTagMetadataExtractor;
import org.dataconservancy.mhf.finders.BagItMetadataFinder;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.Package.Events;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.schlichtherle.io.FileOutputStream;

public class PackageMetadataExtractorTest {
    private IngestWorkflowState state;
    private PackageMetadataExtractor pkgExtractor;
    private AttributeSetManager attributeSetManager;
    private BusinessObjectManager businessObjectManager;
    private List<DcsEvent> events;

    private final static String[] BAGIT_METADATA_FILES = new String[] {
            "/SampleBagItMetadata/bag-info.txt",
            "/SampleBagItMetadata/bagit.txt",
            "/SampleBagItMetadata/manifest-md5.txt",
            "/SampleBagItMetadata/tagmanifest-md5.txt" };

    @Before
    public void setup() throws Exception {
        pkgExtractor = new PackageMetadataExtractor();
        pkgExtractor
                .setMetadataObjectBuilder(mock(MetadataObjectBuilder.class));

        attributeSetManager = new AttributeSetManagerImpl();
        EventManager eventManager = mock(EventManager.class);

        events = new ArrayList<DcsEvent>();

        eventManager = mock(EventManager.class);
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation)
                    throws Throwable {
                DcsEvent event = (DcsEvent) invocation.getArguments()[1];
                events.add(event);
                return event;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));

        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation)
                    throws Throwable {
                String type = (String) invocation.getArguments()[0];

                DcsEvent event = new DcsEvent();
                event.setEventType(type);
                return event;
            }

        }).when(eventManager).newEvent(anyString());

        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        businessObjectManager = mock(BusinessObjectManager.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
    }

    private org.dataconservancy.packaging.model.Package createPackage(
            boolean valid) throws IOException {

        List<File> pkg_files = new ArrayList<File>();

        File top = File.createTempFile("testbag", null);
        top.delete();
        top.mkdir();
        top.deleteOnExit();

        File datadir = new File(top, "data");
        datadir.mkdir();
        datadir.deleteOnExit();

        File data = new File(datadir, "chicken.jpg");
        data.createNewFile();
        data.deleteOnExit();

        pkg_files.add(data);

        if (valid) {
            for (String path : BAGIT_METADATA_FILES) {
                URL url = this.getClass().getResource(path);

                File file = new File(top, new File(path).getName());

                InputStream is = url.openStream();
                OutputStream os = new FileOutputStream(file);
                copy(is, os);
                is.close();
                os.close();

                pkg_files.add(file);
                file.deleteOnExit();
            }
        }
        
        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(pkg_files);
        when(serialization.getExtractDir()).thenReturn(new File(System.getProperty("java.io.tmpdir")));
        when(serialization.getBaseDir()).thenReturn(new File(top.getName()));
        
        org.dataconservancy.packaging.model.Package pkg = new PackageImpl(null,
                serialization);

        return pkg;
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] buf = new byte[16 * 1024];
        int n = 0;

        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
    }

    @SuppressWarnings("rawtypes")
    private Map<String, AttributeSet> expectedAttributeSets(
            org.dataconservancy.packaging.model.Package pkg) {
        Map<String, AttributeSet> result = new HashMap<String, AttributeSet>();

        BagItMetadataFinder finder = new BagItMetadataFinder(
                mock(MetadataObjectBuilder.class));

        for (MetadataInstance mi : finder.findMetadata(pkg.getSerialization())) {
            MetadataExtractor extractor;

            if (mi.getFormatId().equals(MetadataFormatId.BAGIT_TAG_FORMAT_ID)) {
                extractor = new BagItTagMetadataExtractor();
            } else if (mi.getFormatId().equals(
                    MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID)) {
                extractor = new BagItManifestMetadataExtractor(
                        finder.getChecksum());
            } else {
                continue;
            }

            for (MetadataRepresentation rep : extractor.extractMetadata(mi)) {
                AttributeSet as = (AttributeSet) rep.getRepresentation();
                
                String asName = "";
                if (as.getName() == MetadataAttributeSetName.BAGIT_METADATA) {
                    asName = AttributeSetName.BAGIT;
                } else if (MetadataAttributeSetName.BAGIT_PROFILE_DATACONS_METADATA == as.getName()) {
                    asName = AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA;
                }
                
                AttributeSet all = result.get(asName);
                if (all == null) {
                    result.put(asName, as);
                } else {
                    all.getAttributes().addAll(as.getAttributes());
                }
            }
        }

        return result;
    }

    /**
     * Makes sure that correct AttributeSets and events are generated.
     * 
     * @throws Exception
     * 
     */
    @Test
    public void testSuccess() throws Exception {
        org.dataconservancy.packaging.model.Package pkg = createPackage(true);
        Map<String, AttributeSet> expected = expectedAttributeSets(pkg);

        assertTrue(expected.size() > 0);

        when(state.getPackage()).thenReturn(pkg);
        pkgExtractor.execute("pkg-extract:1", state);

        for (String id : expected.keySet()) {
            AttributeSet as = attributeSetManager.getAttributeSet(id);
            AttributeSet expected_as = expected.get(id);

            assertNotNull(as);
            assertEquals(expected_as.getName(), as.getName());
            assertTrue(equals(expected_as.getAttributes(), as.getAttributes()));
        }

        assertTrue(attributeSetManager
                .contains(AttributeSetName.BAGIT));
        assertTrue(attributeSetManager
                .contains(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA));

        assertTrue(events.size() > 0);

        for (DcsEvent event : events) {
            assertEquals(Events.TRANSFORM, event.getEventType());
        }
    }

    // Different Attribute implementations do not implement equals in a
    // compatible way.

    private boolean equals(Collection<Attribute> c1, Collection<Attribute> c2) {
        if (c1.size() != c2.size()) {
            return false;
        }

        for (Attribute a : c1) {
            if (!contains(c2, a)) {
                return false;
            }
        }

        for (Attribute a : c2) {
            if (!contains(c1, a)) {
                return false;
            }
        }

        return true;
    }

    private boolean contains(Collection<Attribute> c, Attribute test) {
        for (Attribute a : c) {
            if (equals(a, test)) {
                return true;
            }
        }

        return false;
    }

    private boolean equals(Attribute a1, Attribute a2) {
        return a1.getName().equals(a2.getName())
                && a1.getValue().equals(a2.getValue())
                && a1.getType().equals(a2.getType());
    }

    /**
     * Makes sure failure events are generated on failure.
     * 
     * @throws Exception
     */
    @Test
    public void testFailure() throws Exception {
        org.dataconservancy.packaging.model.Package pkg = createPackage(false);

        when(state.getPackage()).thenReturn(pkg);
        pkgExtractor.execute("pkg-extract:1", state);

        assertFalse(attributeSetManager
                .contains(AttributeSetName.BAGIT));
        assertFalse(attributeSetManager
                .contains(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA));

        assertTrue(events.size() > 0);

        for (DcsEvent event : events) {
            assertEquals(Events.TRANSFORM_FAIL, event.getEventType());
        }
    }
}
