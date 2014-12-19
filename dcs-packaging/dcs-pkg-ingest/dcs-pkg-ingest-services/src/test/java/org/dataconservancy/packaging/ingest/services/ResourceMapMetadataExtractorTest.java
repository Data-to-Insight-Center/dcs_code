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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * Tests the output of, and events created by, the ResourceMapMetadataExtractor.
 */
public class ResourceMapMetadataExtractorTest {
    private IngestWorkflowState state;
    private AttributeSetManager attributeSetManager;
    private BusinessObjectManager businessObjectManager;
    private EventManager eventManager;
    private ResourceMapMetadataExtractor resourceMapMetadataExtractor;
    private Map<String, AttributeSet> expectedAttributeSets = new HashMap<String, AttributeSet>();
    private List<DcsEvent> events;

    private static final String FILE_IDENTIFER_D219 = "urn:uuid:de726086-d12b-11e2-aee4-cef48d91d219";

    private static final String PACKAGE_RESOURCEID_471C = "urn:uuid:8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C";
    private static final String PROJECT_RESOURCEID_1DD8 = "urn:uuid:243A4C7A-CA89-4BA5-99D3-1E26D7C31DD8";
    private static final String COLLECTION_RESOURCEID_B17A = "urn:uuid:de7243bc-d12b-11e2-aee4-90a790b0b17a";
    private static final String COLLECTION_RESOURCEID_EDD8 = "urn:uuid:de72493d-d12b-11e2-aee4-e6917b6eedd8";
    private static final String COLLECTION_RESOURCEID_CD1C = "urn:uuid:de725faa-d12b-11e2-aee4-87b85d7ccd1c";
    private static final String COLLECTION_RESOURCEID_B447 = "urn:uuid:de726e77-d12b-11e2-aee4-ff82d6dab447";
    private static final String DATAITEM_RESOURCEID_629F = "urn:uuid:de726233-d12b-11e2-aee4-a0b4b916629f";
    private static final String DATAITEM_RESOURCEID_0A8F = "urn:uuid:de7256a4-d12b-11e2-aee4-d574cd660a8f";
    private static final String DATAITEM_RESOURCEID_B8FC = "urn:uuid:de724d8b-d12b-11e2-aee4-a439acceb8fc";
    private static final String DATAITEM_RESOURCEID_5ECF = "urn:uuid:3ae15e08-e282-4435-9c95-1f65a6345ecf";
    private static final String DATAITEM_RESOURCEID_D766 = "urn:uuid:61b29cfc-f687-4652-975a-bacd48f0d766";
    private static final String DATAITEM_RESOURCEID_FD64 = "urn:uuid:ad20517e-029a-4e05-aae7-f316f256fd64";
    private static final String DATAITEM_RESOURCEID_59F7 = "urn:uuid:ae15fc6a-3f92-4e58-9297-d254d81359f7";
    private static final String DATAITEM_RESOURCEID_C599 = "urn:uuid:d09dcb2d-9ec3-4b3d-a3cb-c133d936c599";
    private static final String DATAITEM_RESOURCEID_523D = "urn:uuid:de72713d-d12b-11e2-aee4-de599a44523d";
    private static final String DATAITEM_RESOURCEID_882A = "urn:uuid:e7b6cb09-2795-418d-967d-1802d7c0882a";
    private static final String FILE_RESOURCEID_SEA_ICE_CONDITIONS_TAKATAK =
            "file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Map_Belcher_Islands_Sea_Ice_Conditions-Takatak.pdf";
    private static final String FILE_RESOURCEID_ELOKA_PROJECT_BOP =
            "file:///ELOKA002.bag/data/ELOKA/ELOKA-project.bop.xml";


    private final static String[] ORE_REM_FILES = new String[] {
        "/SampleOreRemMetadata/ORE-REM/243A4C7A-CA89-4BA5-99D3-1E26D7C31DD8-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/3ae15e08-e282-4435-9c95-1f65a6345ecf-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/61b29cfc-f687-4652-975a-bacd48f0d766-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/ad20517e-029a-4e05-aae7-f316f256fd64-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/d09dcb2d-9ec3-4b3d-a3cb-c133d936c599-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/ae15fc6a-3f92-4e58-9297-d254d81359f7-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/de72493d-d12b-11e2-aee4-e6917b6eedd8-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/de724d8b-d12b-11e2-aee4-a439acceb8fc-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/de7256a4-d12b-11e2-aee4-d574cd660a8f-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/de725faa-d12b-11e2-aee4-87b85d7ccd1c-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/de726233-d12b-11e2-aee4-a0b4b916629f-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/de726e77-d12b-11e2-aee4-ff82d6dab447-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/de72713d-d12b-11e2-aee4-de599a44523d-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/e7b6cb09-2795-418d-967d-1802d7c0882a-ReM.xml",
        "/SampleOreRemMetadata/ORE-REM/de7243bc-d12b-11e2-aee4-90a790b0b17a-ReM.xml"
    };
    
    private static final String SAMPLE_BAG_NAME = "ELOKA002.bag";

    private final static String[] SUBJECTS = new String[] {
            AttributeSetName.ORE_REM_FILE + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/ELOKA002.fgdc.xml",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Ippak_Interview/Ippak1.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Ippak_Interview/Ippak2.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Ippak_Interview/Ippak3.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Ippak_Interview/Ippak4.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Ippak_Sea_Ice_Narration/Ippak_Photo_Narration1.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Ippak_Sea_Ice_Narration/Ippak_Photo_Narration2.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Ippak_Sea_Ice_Narration/Ippak_Photo_Narration3.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Map_Belcher_Islands_Sea_Ice_Conditions-Ippak.pdf",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Johnassie_Ippak/Map_Ippak_Sea_Ice_Photo_Locations.pdf",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Map_Belcher_Islands_Sea_Ice_Conditions-Takatak.pdf",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Takatak_Interview/Takatak1.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Takatak_Interview/Takatak2.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Takatak_Interview/Takatak3.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Takatak_Interview/Takatak4.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Takatak_Interview/Takatak5.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Map_Belcher_Islands_Placenames.pdf",
            AttributeSetName.ORE_REM_FILE + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Map_Regional_Reference.pdf",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk1.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk2.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk3.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk4.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Peter_Kattuk/Kattuk_Interview/Kattuk5.mp4",
            AttributeSetName.ORE_REM_FILE
                    + "_file:///ELOKA002.bag/data/ELOKA/ELOKA002/Peter_Kattuk/Map_Belcher_Islands_Sea_Ice_Features-Kattuk.pdf",
            AttributeSetName.ORE_REM_FILE + "_file:///ELOKA002.bag/data/ELOKA/ELOKA-project.bop.xml",
            AttributeSetName.ORE_REM_PACKAGE + "_" + PACKAGE_RESOURCEID_471C,
            AttributeSetName.ORE_REM_PROJECT + "_" + PROJECT_RESOURCEID_1DD8,
            AttributeSetName.ORE_REM_COLLECTION + "_" + COLLECTION_RESOURCEID_B17A,
            AttributeSetName.ORE_REM_COLLECTION + "_" + COLLECTION_RESOURCEID_EDD8,
            AttributeSetName.ORE_REM_COLLECTION + "_" + COLLECTION_RESOURCEID_CD1C,
            AttributeSetName.ORE_REM_COLLECTION + "_" + COLLECTION_RESOURCEID_B447,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_629F,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_0A8F,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_B8FC,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_5ECF,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_D766,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_FD64,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_59F7,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_C599,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_523D,
            AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_882A
    };



    @Before
    public void setup() throws Exception {
        resourceMapMetadataExtractor = new ResourceMapMetadataExtractor();
        attributeSetManager = new AttributeSetManagerImpl();
        eventManager = mock(EventManager.class);
        businessObjectManager = mock(BusinessObjectManager.class);
        String key;

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

        AttributeSetImpl bagit = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> atts = bagit.getAttributes();
        atts.add(new AttributeImpl("PKG-ORE-REM", "String", "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        attributeSetManager.addAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA, bagit);

        //develop expected attribute set for a sample Package
        key = AttributeSetName.ORE_REM_PACKAGE + "_" + PACKAGE_RESOURCEID_471C;
        AttributeSet attSet = new AttributeSetImpl("Ore-Rem-Package");
        atts = attSet.getAttributes();

        atts.add(new AttributeImpl("Package-Title", "String", "Example ELOKA Package"));
        atts.add(new AttributeImpl("Package-Identifier", "String", "Package:1"));
        atts.add(new AttributeImpl("Package-Creator-Name", "String", "The ELOKA Project"));
        atts.add(new AttributeImpl("Package-Creator-Email", "String", "eloka@eloka.org"));
        atts.add(new AttributeImpl("Package-Creator-Phone", "String", "555-555-5555"));
        atts.add(new AttributeImpl("Package-Description", "String", "This is the sample package with ELOKA data"));
        atts.add(new AttributeImpl("Package-Created", "DateTime", "2013-06-10T01:55:18Z"));
        atts.add(new AttributeImpl("Package-Modified", "DateTime", "2013-06-10T01:55:18Z"));
        atts.add(new AttributeImpl("Package-Aggregates-Project", "String", PROJECT_RESOURCEID_1DD8));
        atts.add(new AttributeImpl("Package-Aggregates-Collection", "String", COLLECTION_RESOURCEID_B17A));
        atts.add(new AttributeImpl("Package-Aggregates-DataItem", "String", DATAITEM_RESOURCEID_5ECF));
        atts.add(new AttributeImpl("Package-Property", "String", PACKAGE_RESOURCEID_471C));
        atts.add(new AttributeImpl(Metadata.PACKAGE_RESOURCEID, "String", PACKAGE_RESOURCEID_471C));
        expectedAttributeSets.put(key, attSet);

        //develop expected attribute set for a sample Project
        key = AttributeSetName.ORE_REM_PROJECT + "_" + PROJECT_RESOURCEID_1DD8;
        attSet = new AttributeSetImpl("Ore-Rem-Project");
        atts = attSet.getAttributes();

        atts.add(new AttributeImpl("Project-Title", "String", "The ELOKA Project"));
        atts.add(new AttributeImpl("Project-Identifier", "String", "Project:1"));
        atts.add(new AttributeImpl("Project-Creator-Name", "String", "The ELOKA Project"));
        atts.add(new AttributeImpl("Project-Creator-Email", "String", "eloka@eloka.org"));
        atts.add(new AttributeImpl("Project-Creator-Phone", "String", "555-555-5555"));
        atts.add(new AttributeImpl("Project-Description", "String", "This is a sample Project description"));
        atts.add(new AttributeImpl("Project-Created", "DateTime", "2013-06-10T01:55:18Z"));
        atts.add(new AttributeImpl("Project-Modified", "DateTime", "2013-06-10T01:55:18Z"));
        atts.add(new AttributeImpl("Project-Aggregates-Collection", "String", COLLECTION_RESOURCEID_B17A));
        atts.add(new AttributeImpl("Project-Aggregates-File", "String", "file:///ELOKA002.bag/data/ELOKA/ELOKA-project.bop.xml"));
        atts.add(new AttributeImpl("Project-Property", "String", PROJECT_RESOURCEID_1DD8));
        atts.add(new AttributeImpl(Metadata.PROJECT_RESOURCEID, "String", PROJECT_RESOURCEID_1DD8));
        expectedAttributeSets.put(key, attSet);

        //develop expected attribute set for a sample Collection
        key = AttributeSetName.ORE_REM_COLLECTION + "_" + COLLECTION_RESOURCEID_B17A;
        attSet = new AttributeSetImpl("Ore-Rem-Collection");
        atts = attSet.getAttributes();

        atts.add(new AttributeImpl("Collection-Title", "String", "data/ELOKA/ELOKA002"));
        atts.add(new AttributeImpl("Collection-Identifier", "String", "Collection:1"));
        atts.add(new AttributeImpl("Collection-Creator-Name", "String", "The ELOKA Project"));
        atts.add(new AttributeImpl("Collection-Creator-Email", "String", "eloka@eloka.org"));
        atts.add(new AttributeImpl("Collection-Creator-Phone", "String", "555-555-5555"));
        atts.add(new AttributeImpl("Collection-Description", "String", "This is a sample Collection description"));
        atts.add(new AttributeImpl("Collection-Created", "DateTime", "2013-06-10T01:55:18Z"));
        atts.add(new AttributeImpl("Collection-Modified", "DateTime", "2013-06-10T01:55:18Z"));
        atts.add(new AttributeImpl("Collection-IsPartOf-Collection", "String", "MOO-Collection"));
        atts.add(new AttributeImpl("Collection-Aggregates-DataItem", "String", DATAITEM_RESOURCEID_D766));
        atts.add(new AttributeImpl("Collection-Aggregates-File", "String", "file:///ELOKA002.bag/data/ELOKA/ELOKA002/ELOKA002.fgdc.xml"));
        atts.add(new AttributeImpl("Collection-Aggregates-Collection", "String", COLLECTION_RESOURCEID_EDD8));
        atts.add(new AttributeImpl("Collection-Aggregates-DataItem", "String", DATAITEM_RESOURCEID_5ECF));
        atts.add(new AttributeImpl("Collection-Aggregates-Collection", "String", COLLECTION_RESOURCEID_CD1C));
        atts.add(new AttributeImpl("Collection-Aggregates-Collection", "String", COLLECTION_RESOURCEID_B447));
        atts.add(new AttributeImpl("Collection-Aggregated-By-Project", "String", PROJECT_RESOURCEID_1DD8));
        atts.add(new AttributeImpl("Collection-Property", "String", COLLECTION_RESOURCEID_B17A));
        atts.add(new AttributeImpl(Metadata.COLLECTION_RESOURCEID, "String", COLLECTION_RESOURCEID_B17A));
        expectedAttributeSets.put(key, attSet);

        //develop expected attribute set for a sample DataItem
        key = AttributeSetName.ORE_REM_DATAITEM + "_" + DATAITEM_RESOURCEID_5ECF;
        attSet = new AttributeSetImpl("Ore-Rem-DataItem");
        atts = attSet.getAttributes();

        atts.add(new AttributeImpl("DataItem-Title", "String", "data/ELOKA/ELOKA002/Map_Regional_Reference.pdf"));
        atts.add(new AttributeImpl("DataItem-Identifier", "String", "DataItem:1"));
        atts.add(new AttributeImpl("DataItem-Creator-Name", "String", "The ELOKA Project"));
        atts.add(new AttributeImpl("DataItem-Creator-Email", "String", "eloka@eloka.org"));
        atts.add(new AttributeImpl("DataItem-Creator-Phone", "String", "555-555-5555"));
        atts.add(new AttributeImpl("DataItem-Description", "String", "This is a sample DataItem description"));
        atts.add(new AttributeImpl("DataItem-Created", "DateTime", "2009-11-20T17:55:00Z"));
        atts.add(new AttributeImpl("DataItem-Modified", "DateTime", "2009-11-20T17:55:00Z"));
        atts.add(new AttributeImpl("DataItem-IsPartOf-Collection", "String", "MOOOOOO"));
        atts.add(new AttributeImpl("DataItem-IsVersionOf-DataItem", "String", "MOOOOOO-old"));
        atts.add(new AttributeImpl("DataItem-Aggregates-File", "String", "file:///ELOKA002.bag/data/ELOKA/ELOKA002/Map_Regional_Reference.pdf"));
        atts.add(new AttributeImpl("DataItem-Property", "String", DATAITEM_RESOURCEID_5ECF));
        atts.add(new AttributeImpl(Metadata.DATAITEM_RESOURCEID, "String", DATAITEM_RESOURCEID_5ECF));
        expectedAttributeSets.put(key, attSet);

        //develop expected attribute set for a sample File
        key = AttributeSetName.ORE_REM_FILE
                + "_" + FILE_RESOURCEID_SEA_ICE_CONDITIONS_TAKATAK;
        attSet = new AttributeSetImpl("Ore-Rem-File");
        atts = attSet.getAttributes();

        atts.add(new AttributeImpl("File-Title", "String", "data/ELOKA/ELOKA002/Lucassie_Takatak/Map_Belcher_Islands_Sea_Ice_Conditions-Takatak.pdf"));
        atts.add(new AttributeImpl("File-Identifier", "String", FILE_IDENTIFER_D219));
        atts.add(new AttributeImpl("File-Path", "String", "file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Map_Belcher_Islands_Sea_Ice_Conditions-Takatak.pdf"));
        atts.add(new AttributeImpl("File-Creator-Name", "String", "The ELOKA Project"));
        atts.add(new AttributeImpl("File-Creator-Email", "String", "eloka@eloka.org"));
        atts.add(new AttributeImpl("File-Creator-Phone", "String", "555-555-5555"));
        atts.add(new AttributeImpl("File-Description", "String", "data/ELOKA/ELOKA002/Lucassie_Takatak/Map_Belcher_Islands_Sea_Ice_Conditions-Takatak.pdf"));
        atts.add(new AttributeImpl("File-Created", "DateTime", "2009-11-20T17:55:00Z"));
        atts.add(new AttributeImpl("File-Modified", "DateTime", "2009-11-20T17:55:00Z"));
        atts.add(new AttributeImpl("File-Format", "String", "application/pdf"));
        atts.add(new AttributeImpl("File-Conforms-To", "String", "info:pronom/fmt/18"));
        //atts.add(new AttributeImpl("File-IsMetadataFor", "String", "")); //tested below
        atts.add(new AttributeImpl("File-Property", "String", "file:///ELOKA002.bag/data/ELOKA/ELOKA002/Lucassie_Takatak/Map_Belcher_Islands_Sea_Ice_Conditions-Takatak.pdf" ));
        atts.add(new AttributeImpl(Metadata.FILE_RESOURCEID, "String", FILE_RESOURCEID_SEA_ICE_CONDITIONS_TAKATAK));
        expectedAttributeSets.put(key, attSet);

        //develop expected attribute set for a sample Metadata File
        key = AttributeSetName.ORE_REM_FILE + "_" + FILE_RESOURCEID_ELOKA_PROJECT_BOP;
        attSet = new AttributeSetImpl("Ore-Rem-File");
        atts = attSet.getAttributes();

        atts.add(new AttributeImpl("File-Title", "String", "ELOKA002.bag/data/ELOKA/ELOKA-project.bop.xml"));
        atts.add(new AttributeImpl("File-Identifier", "String", "file:///ELOKA002.bag/data/ELOKA/ELOKA-project.bop.xml"));
        atts.add(new AttributeImpl("File-Path", "String", "file:///ELOKA002.bag/data/ELOKA/ELOKA-project.bop.xml"));
        atts.add(new AttributeImpl("File-Creator-Name", "String", "The ELOKA Project"));
        //atts.add(new AttributeImpl("File-Creator-Email", "String", "eloka@eloka.org")); //tested above
        //atts.add(new AttributeImpl("File-Creator-Phone", "String", "555-555-5555")); /tested above
        atts.add(new AttributeImpl("File-Description", "String", "ELOKA002.bag/data/ELOKA/ELOKA-project.bop.xml"));
        atts.add(new AttributeImpl("File-Created", "DateTime", "2013-06-09T15:47:32Z"));
        atts.add(new AttributeImpl("File-Modified", "DateTime", "2013-06-09T15:47:32Z"));
        //atts.add(new AttributeImpl("File-Format", "String", "")); //tested above
        atts.add(new AttributeImpl("File-Conforms-To", "String", "http://dataconservancy.org/schemas/bop/1.0"));
        atts.add(new AttributeImpl("File-IsMetadataFor", "String", PROJECT_RESOURCEID_1DD8));
        atts.add(new AttributeImpl("File-Property", "String", "file:///ELOKA002.bag/data/ELOKA/ELOKA-project.bop.xml" ));
        atts.add(new AttributeImpl(Metadata.FILE_RESOURCEID, "String", FILE_RESOURCEID_ELOKA_PROJECT_BOP));
        expectedAttributeSets.put(key, attSet);
    }

    /**
     * Makes sure that correct AttributeSets and events are generated.
     *
     * @throws Exception
     *
     */
    @Test
    public void testSuccess() throws Exception {
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);

        org.dataconservancy.packaging.model.Package pkg = createPackage(true);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);


        resourceMapMetadataExtractor.execute("ore-rem-extract:1", state);

        assertTrue(attributeSetManager.contains(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA));

        //make sure there is an attribute set for each subject
        for(String subject : SUBJECTS){
            assertTrue(attributeSetManager.contains(subject));
        }
       //verify that several extracted sample attribute sets are as expected
       for(String expectedKey : expectedAttributeSets.keySet()){
           assertEquals( new HashSet<Attribute>(expectedAttributeSets.get(expectedKey).getAttributes()), new HashSet<Attribute>(attributeSetManager.getAttributeSet(expectedKey).getAttributes()));

       }

        assertTrue(events.size() > 0);

        for (DcsEvent event : events) {
            assertEquals(Package.Events.TRANSFORM, event.getEventType());
        }
    }

    /**
     * Makes sure failure events are generated on failure.
     *
     * @throws Exception
     */
    @Test(expected=StatefulIngestServiceException.class)
    public void testFailure() throws Exception {
        org.dataconservancy.packaging.model.Package pkg = createPackage(false);

        when(state.getPackage()).thenReturn(pkg);

        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        resourceMapMetadataExtractor.execute("ore-rem-extract:1", state);
    }

    private org.dataconservancy.packaging.model.Package createPackage(
            boolean valid) throws IOException {

        List<File> pkgFiles = new ArrayList<File>();

        File top = File.createTempFile("testbag", null);
        top.delete();
        top.mkdir();
        top.deleteOnExit();
        
        File bagdir = new File(top, SAMPLE_BAG_NAME);

        bagdir.mkdir();
        bagdir.deleteOnExit();

        File oreRemDir = new File(bagdir, "ORE-REM");        
        oreRemDir.mkdir();
        
        // pkgFiles.add(top);
        pkgFiles.add(oreRemDir);
        if (valid) {
            for (String path : ORE_REM_FILES) {
                URL url = this.getClass().getResource(path);

                File file = new File(oreRemDir, new File(path).getName());

                InputStream is = url.openStream();
                OutputStream os = new FileOutputStream(file);
                copy(is, os);
                is.close();
                os.close();
                pkgFiles.add(file);
                file.deleteOnExit();
            }
        }

        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getExtractDir()).thenReturn(top);
        when(serialization.getBaseDir()).thenReturn(new File(SAMPLE_BAG_NAME));
        
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

}
