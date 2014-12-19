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
package org.dataconservancy.packaging.ingest.impl;

import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATES_DATAITEM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.impl.IdentifierImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.ReportServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.AttributeValueType;
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.Pair;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.reporting.model.builder.IngestReportBuilder;
import org.dataconservancy.reporting.model.builder.xstream.XstreamIngestReportBuilder;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class PackagePreIngestReportServiceTest {
    
    private IngestWorkflowState state;
    private AttributeSetManager attributeSetManager;
    private BusinessObjectManager businessObjectManager;

    private EventManager eventManager;
    private List<DcsEvent> events;
    private String[] expectedKeys;
    private Map<String, AttributeSet> attributeMap;
    private Map<String, BusinessObject> businessObjectMap;
    private IdService idService;
    private org.dataconservancy.packaging.model.Package pkg;
    private int i;// counter for idService
    private IngestReportBuilder ingestReportBuilder;
    private PackagePreIngestReportService underTest;
    
    private DateTime publishDate = new DateTime(2013, 6, 10, 1, 55, 18);
    private String collectionName;
    private String subcollectionName;
    private String titleLessCollectionName;
    private String dataItemOneName;
    private String dataItemTwoName;
    private String dataItemThreeName;
    private String dataFileOneName;
    private String dataFileTwoName;
    private String dataFileThreeName;
    private String dataFileFourName;
    private String metadataFileOneName;
    private String metadataFileTwoName;
    private String metadataFileThreeName;

    private String collectionSummary;
    private String subcollectionSummary;
    private String titleLessCollectionSummary;
    private String dataItemOneDescription;
    private String dataItemTwoDescription;
    private String dataItemThreeDescription;
    
    private PersonName personName1;
    private PersonName personName2;
    private long dataFileOneSize = 132465798L;
    private long dataFileTwoSize = 564321321L;
    private long dataFileThreeSize = 556323213L;
    private long dataFileFourSize = 78765465461L;
    private long metadataFileOneSize = 674987654L;
    private long metadataFileTwoSize = 564798765L;
    private long metadataFileThreeSize = 658798465L;

    private String file1FileASKey;
    private String file2FileASKey;
    private String file3FileASKey;
    private String file4FileASKey;
    private String metadataFile1FileASKey;
    private String metadataFile2FileASKey;
    private String metadataFile3FileASKey;
    private String file1URI;
    private String file2URI;
    private String file3URI;
    private String file4URI;
    private String metadataFile1URI;
    private String metadataFile2URI;
    private String metadataFile3URI;

    private String dataFileFormatString;
    private String metadataFileFormatString;

    private DcsFormat dataFileFormat;
    private DcsFormat metadataFileFormat;

    private String creator2 = "TheJohns Hopkins University";
    
    private String dataFileOnePayloadPath;
    private String dataFileTwoPayloadPath;
    private String dataFileThreePayloadPath;
    private String dataFileFourPayloadPath;
    private String metadataFileOnePayloadPath;
    private String metadataFileTwoPayloadPath;
    private String metadataFileThreePayloadPath;

    private Collection collection;
    private Collection subcollection;
    private Collection titleLessCollection;
    private DataItem dataItemOne;
    private DataItem dataItemTwo;
    private DataItem dataItemThree;
    private DataFile dataFileOne;
    private DataFile dataFileTwo;
    private DataFile dataFileThree;
    private DataFile dataFileFour;
    private MetadataFile metadataFileOne;
    private MetadataFile metadataFileTwo;
    private MetadataFile metadataFileThree;

    private String depositId = "IngestReport-1";
    private String bagName = "ASMetadata-Bag";
    
    private AttributeSet collectionAttributeSet;
    private AttributeSet subcollectionAttributeSet;
    private AttributeSet titleLessCollectionAttributeSet;
    private AttributeSet dataItemOneAttributeSet;
    private AttributeSet dataItemTwoAttributeSet;
    private AttributeSet dataItemThreeAttributeSet;
    private AttributeSet dataFileOneAttributeSet;
    private AttributeSet dataFileTwoAttributeSet;
    private AttributeSet dataFileThreeAttributeSet;
    private AttributeSet dataFileFourAttributeSet;
    private AttributeSet metadataFileOneAttributeSet;
    private AttributeSet metadataFileTwoAttributeSet;
    private AttributeSet metadataFileThreeAttributeSet;
    private AttributeSet fileAttributeSet;
    
    private AttributeSet file1AS;
    private AttributeSet file2AS;
    private AttributeSet file3AS;
    private AttributeSet file4AS;
    
    private AttributeSet metadataFile1AS;
    private AttributeSet metadataFile2AS;
    private AttributeSet metadataFile3AS;
    
    private Pair<String, Checksum> dataFileOnePair;
    private Pair<String, Checksum> dataFileTwoPair;
    private Pair<String, Checksum> dataFileThreePair;
    private Pair<String, Checksum> dataFileFourPair;
    private Pair<String, Checksum> metadataFileOnePair;
    private Pair<String, Checksum> metadataFileTwoPair;
    private Pair<String, Checksum> metadataFileThreePair;
    
    private File extractDir;
    private File baseDir;
    private Attribute fileFormatDetectionToolName;
    private Attribute fileFormatDetectionToolVersion;

    @Before
    public void setUp() {
        underTest = new PackagePreIngestReportService();

        extractDir = new File("/tmp/package-extraction");
        extractDir.mkdir();
        extractDir.deleteOnExit();
        
        baseDir = new File(depositId, bagName);
        baseDir.mkdir();
        baseDir.deleteOnExit();

        PackageDescription description = new DescriptionImpl();
        PackageSerialization serialization = new SerializationImpl();
        serialization.setExtractDir(extractDir);
        serialization.setBaseDir(baseDir);
        pkg = new PackageImpl(description, serialization);
        
        // mocked services and managers
        attributeSetManager = mock(AttributeSetManager.class);
        businessObjectManager = mock(BusinessObjectManager.class);
        eventManager = mock(EventManager.class);
        idService = mock(IdService.class);
        ingestReportBuilder = new XstreamIngestReportBuilder();
        
        underTest.setIngestReportBuilder(ingestReportBuilder);
        
        // persistence for these
        attributeMap = new HashMap<String, AttributeSet>();
        businessObjectMap = new HashMap<String, BusinessObject>();
        events = new ArrayList<DcsEvent>();
        
        setupMockServices();
        initializeStrings();
        addAttributeSets();
        addFileAttributeSet();
        addBagitAttributeSet();
        addBusinessObjects();
    }

    /**
     * test that after the successul completion of phase 1 & 2, ReportService is able to gather and populate all fields
     * of the {@code IngestReport}
     * 
     * Expecting: 
     * - report.status = successful (from event manager) 
     * - DataItem count by Collection (from ORE-ReM-Collection, ORE-ReM-DataItem AS, and businessObjectManager) 
     * - file count by format (from FILE AS) 
     * - number of checksum created (from FILE AS) 
     * - total package size (from FILE AS) 
     * - content detection tools (from FILE AS)
     * 
     * @throws InvalidXmlException
     */
    @Test
    public void testProduceSuccessfulReport() throws InvalidXmlException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
        
        DepositDocument produceReport = underTest.produceReport(depositId, state);
        IngestReport ingestReport = ingestReportBuilder.buildIngestReport(produceReport.getInputStream());

        assertNotNull(ingestReport);
        assertTrue(ingestReport.getStatus().name().equalsIgnoreCase("successful"));
        assertTrue(ingestReport.getDataItemsPerCollectionCount().size() > 0);
        assertEquals(getExpectedDICountToCollectionMap(), ingestReport.getDataItemsPerCollectionCount());
        assertEquals(getExpectedFileTypeCountMap(), ingestReport.getFileTypeCount());
        assertNotNull(ingestReport.getTotalPackageSize());
        assertEquals(getExpectedContentDectionToolInfo(), ingestReport.getContentDetectionTools());
    }


    /**
     * test that after the successul completion of phase 1 & 2, ReportService is able to gather and populate all fields
     * of the {@code IngestReport}
     *
     * Expecting:
     * - report.status = warnings (from event manager)
     * - DataItem count by Collection (from ORE-ReM-Collection, ORE-ReM-DataItem AS, and businessObjectManager)
     * - file count by format (from FILE AS)
     * - number of checksum created (from FILE AS)
     * - total package size (from FILE AS)
     * - content detection tools (from FILE AS)
     * - file with unverified formats (from event manager)
     * @throws InvalidXmlException 
     */
    @Test
    public void testProduceReportWithWarnings() throws InvalidXmlException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);

        //mock format verification failure event
        DcsEvent failedFormatVerificationEvent = eventManager.newEvent(Package.Events.FORMAT_VERIFICATION_FAILED);
        failedFormatVerificationEvent.setOutcome("One or more files failed format verification.");
        String formatVerificationFailureEventDetails = "Asserted format: [Word] was not amongst detected formats: Jpeg, Jpeg2000";
        failedFormatVerificationEvent.setDetail(formatVerificationFailureEventDetails);
        failedFormatVerificationEvent.addTargets(new DcsEntityReference("Asserted file is probably not very finely asserted!"));
        java.util.Collection<DcsEvent> events = new HashSet<DcsEvent>();
        events.add(failedFormatVerificationEvent);
        when(state.getEventManager().getEvents(depositId, Package.Events.FORMAT_VERIFICATION_FAILED))
                .thenReturn(events);

        //mock illegal file aggregation event
        failedFormatVerificationEvent = eventManager.newEvent(Package.Events.UNSUPPORTED_FILE_AGGREGATION);
        DcsEntityReference illegalFileAggregationEventTarget = new DcsEntityReference("file:///samplebag/data/FileForProject.txt");
        String illegalFileAggregationOutcome =  illegalFileAggregationEventTarget.getRef()
                + " is removed due to Project-Aggregates-File assertion not supported.";
        String illegalFileAggregationDetails = "Reason file was removed";

        failedFormatVerificationEvent.setOutcome(illegalFileAggregationOutcome);
        failedFormatVerificationEvent.setDetail(illegalFileAggregationDetails);
        failedFormatVerificationEvent.addTargets(illegalFileAggregationEventTarget);
        events = new HashSet<DcsEvent>();
        events.add(failedFormatVerificationEvent);
        when(state.getEventManager().getEvents(depositId, Package.Events.UNSUPPORTED_FILE_AGGREGATION))
                .thenReturn(events);

        //produce report
        DepositDocument produceReport = underTest.produceReport(depositId, state);
        IngestReport ingestReport = ingestReportBuilder.buildIngestReport(produceReport.getInputStream());

        assertNotNull(ingestReport);
        //assert report has warning status
        assertTrue(ingestReport.getStatus().name().equalsIgnoreCase("warnings"));
        //assert report message mentioned file that were illegally aggregated
        assertTrue(ingestReport.getStatusMessage().contains(illegalFileAggregationEventTarget.getRef()));
        assertTrue(ingestReport.getStatusMessage().contains(illegalFileAggregationDetails));
        assertTrue(ingestReport.getStatusMessage().contains(illegalFileAggregationOutcome));
        //assert report message mentioned format verification failure
        assertTrue(ingestReport.getStatusMessage().contains(formatVerificationFailureEventDetails));
        //asserted the number of files whose format was verified successful is reported.
        assertEquals(1, ingestReport.getUnmatchedFileTypes().size());


        //asssert that the count of data items per collection is as expected.
        assertEquals(getExpectedDICountToCollectionMap(), ingestReport.getDataItemsPerCollectionCount());
        //assert that the count of file per file types as is as expected.
        assertEquals(getExpectedFileTypeCountMap(), ingestReport.getFileTypeCount());
        //assert that total package size was calculated.
        assertNotNull(ingestReport.getTotalPackageSize());
        //assert that content detection info is as expected.
        assertEquals(getExpectedContentDectionToolInfo(), ingestReport.getContentDetectionTools());
    }

    /**
     * Similar to {@code testProduceSuccessfulReport}, but pay close attention to the case where collection's title is
     * missing.
     * 
     * Expecting (besides other things) string "Collection with unspecified title (id: [collection-id])" to be used as
     * collection's title.
     * 
     * @throws InvalidXmlException
     */
    @Test
    public void testProduceSuccessfulReportWithMissingCollectionTitle() throws InvalidXmlException {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
        
        DepositDocument produceReport = underTest.produceReport(depositId, state);
        IngestReport ingestReport = ingestReportBuilder.buildIngestReport(produceReport.getInputStream());

        assertNotNull(ingestReport);
        assertTrue(ingestReport.getStatus().name().equalsIgnoreCase("successful"));
        assertTrue(ingestReport.getDataItemsPerCollectionCount().size() > 0);
        assertEquals(getExpectedDICountToCollectionMap(), ingestReport.getDataItemsPerCollectionCount());

        assertTrue(ingestReport.getDataItemsPerCollectionCount().containsKey(
                "Collection with unspecified title (id: collection-id)"));
        assertEquals(getExpectedFileTypeCountMap(), ingestReport.getFileTypeCount());
        assertNotNull(ingestReport.getTotalPackageSize());
        assertEquals(getExpectedContentDectionToolInfo(), ingestReport.getContentDetectionTools());
    }

    @Test(expected = ReportServiceException.class)
    public void  testProduceReportWithNullIngestReportBuilder() {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
        underTest.setIngestReportBuilder(null);
        underTest.produceReport(depositId, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProduceReportWithInvalidDepositId() {
        underTest.produceReport(null, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProduceReportWithNullWorkflowState() {
        underTest.produceReport(null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testProduceReportWithNullEventManager() {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getPackage()).thenReturn(pkg);
        underTest.produceReport(depositId, state);
    }

    @Test(expected = IllegalStateException.class)
    public void testProduceReportWithNullAttributSetManager() {
        state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
        underTest.produceReport(depositId, state);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testProduceReportWithNullBusinessObjectManager() {
        state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
        underTest.produceReport(depositId, state);
    }

    private void initializeStrings() {
        collectionName = "Collection-1";
        subcollectionName = "Collection-2";
        titleLessCollectionName = "";
        dataItemOneName = "DataItem-1";
        dataItemTwoName = "DataItem-2";
        dataItemThreeName = "DataItem-3";
        dataFileOneName = "DataFile-1.jpg";
        dataFileTwoName = "DataFile-2.jpg";
        dataFileThreeName = "DataFile-3.jpg";
        dataFileFourName = "DataFile-4.jpg";
        metadataFileOneName = "MetaDataFile-1.xml";
        metadataFileTwoName = "MetaDataFile-2.xml";
        metadataFileThreeName = "MetaDataFile-3.xml";
        
        dataFileOnePayloadPath = new File("data", dataFileOneName).getPath();
        dataFileTwoPayloadPath = new File("data", dataFileTwoName).getPath();
        dataFileThreePayloadPath = new File("data", dataFileThreeName).getPath();
        dataFileFourPayloadPath = new File("data", dataFileFourName).getPath();
        metadataFileOnePayloadPath = new File("data", metadataFileOneName).getPath();
        metadataFileTwoPayloadPath = new File("data", metadataFileTwoName).getPath();
        metadataFileThreePayloadPath = new File("data", metadataFileThreeName).getPath();
        
        File payloadFileBaseDir = new File(pkg.getSerialization().getExtractDir(), pkg.getSerialization().getBaseDir()
                .getPath());
        
        File dataDir = new File(payloadFileBaseDir, "data");
        // AttributeSetKey for File AttributeSet is the path to the file on local system.
        // Test string used here is an example of a typical file path
        file1FileASKey = new File(dataDir, dataFileOneName).getPath();
        file2FileASKey = new File(dataDir, dataFileTwoName).getPath();
        file3FileASKey = new File(dataDir, dataFileThreeName).getPath();
        file4FileASKey = new File(dataDir, dataFileFourName).getPath();
        metadataFile1FileASKey = new File(dataDir, metadataFileOneName).getPath();
        metadataFile2FileASKey = new File(dataDir, metadataFileTwoName).getPath();
        metadataFile3FileASKey = new File(dataDir, metadataFileThreeName).getPath();
        
        file1URI = "file:///" + bagName + "/" + dataFileOnePayloadPath.replace("\\", "/");
        file2URI = "file:///" + bagName + "/" + dataFileTwoPayloadPath.replace("\\", "/");
        file3URI = "file:///" + bagName + "/" + dataFileThreePayloadPath.replace("\\", "/");
        file4URI = "file:///" + bagName + "/" + dataFileFourPayloadPath.replace("\\", "/");
        metadataFile1URI = "file:///" + bagName + "/" + metadataFileOnePayloadPath.replace("\\", "/");
        metadataFile2URI = "file:///" + bagName + "/" + metadataFileTwoPayloadPath.replace("\\", "/");
        metadataFile3URI = "file:///" + bagName + "/" + metadataFileThreePayloadPath.replace("\\", "/");
        
        collectionSummary = "Test-Big-Collection";
        subcollectionSummary = "Test-Subcollection";
        titleLessCollectionSummary = "TitleLess-Collection";
        dataItemOneDescription = "Test-Collection-DataItemOne";
        dataItemTwoDescription = "Test-Collection-DataItemTwo";
        dataItemThreeDescription = "Test-Collection-DataItemThree";

        dataFileFormat = new DcsFormat();
        dataFileFormat.setFormat("Jpeg");
        dataFileFormat.setName("Jpeg File Format");
        dataFileFormat.setSchemeUri("PRONOM");
        dataFileFormat.setVersion("4.3");
        dataFileFormatString = dataFileFormat.toString();

        metadataFileFormat = new DcsFormat();
        metadataFileFormat.setFormat("application/xml");
        metadataFileFormat.setName("XML File Format");
        metadataFileFormat.setSchemeUri("PRONOM");
        metadataFileFormat.setVersion("4.3");
        metadataFileFormatString = metadataFileFormat.toString();

        personName1 = new PersonName();
        personName1.setPrefixes("Dr.");
        personName1.setGivenNames("Robert");
        personName1.setMiddleNames("Moses");
        personName1.setFamilyNames("Kildare");

        personName2 = new PersonName();
        personName2.setFamilyNames(creator2);

    }
    
    @SuppressWarnings("unchecked")
    private void setupMockServices() {
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {
                DcsEvent event = (DcsEvent) invocation.getArguments()[1];
                events.add(event);
                return event;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));
        
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {
                String type = (String) invocation.getArguments()[0];
                DcsEvent event = new DcsEvent();
                event.setDate(new DateTime().toString());
                event.setEventType(type);
                return event;
            }
            
        }).when(eventManager).newEvent(anyString());
        
        doAnswer(new Answer<Identifier>() {
            @Override
            public Identifier answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the event type to be added", args);
                Assert.assertEquals("Expected one argument: the event type to be added", 1, args.length);
                assertTrue("Expected argument to be of type String", args[0] instanceof String);
                String eventType = (String) args[0];
                return new IdentifierImpl(eventType, String.valueOf(i++));
            }
        }).when(idService).create(anyString());
        
        doAnswer(new Answer<AttributeSet>() {
            @Override
            public AttributeSet answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the key and the attribute set to be added", args);
                assertEquals("Expected two arguments: the key and the attribute set to be added", 2, args.length);
                assertTrue("Expected argument one to be of type string", args[0] instanceof String);
                assertTrue("Expected argument to be of type AttributeSet", args[1] instanceof AttributeSet);
                String key = (String) args[0];
                AttributeSet attrSet = (AttributeSet) args[1];
                attributeMap.put(key, attrSet);
                return null;
            }
        }).when(attributeSetManager).addAttributeSet(anyString(), any(AttributeSet.class));
        
        doAnswer(new Answer<AttributeSet>() {
            @Override
            public AttributeSet answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the key and the attribute set to be added", args);
                assertEquals("Expected two arguments: the key and the attribute set to be added", 2, args.length);
                assertTrue("Expected argument one to be of type string", args[0] instanceof String);
                assertTrue("Expected argument to be of type AttributeSet", args[1] instanceof AttributeSet);
                String key = (String) args[0];
                AttributeSet attrSet = (AttributeSet) args[1];
                attributeMap.put(key, attrSet);
                return null;
            }
        }).when(attributeSetManager).updateAttributeSet(anyString(), any(AttributeSet.class));
        
        doAnswer(new Answer<AttributeSet>() {
            @Override
            public AttributeSet answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the key of the attribute set to be retrieved", args);
                assertEquals("Expected one argument: the key of the attribute set to be retrieved", 1, args.length);
                assertTrue("Expected argument to be of type string", args[0] instanceof String);
                String key = (String) args[0];
                
                return attributeMap.get(key);
            }
        }).when(attributeSetManager).getAttributeSet(anyString());


        
        doAnswer(new Answer<Set<String>>() {
            @Override
            public Set<String> answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                return attributeMap.keySet();
            }
        }).when(attributeSetManager).getKeys();
        
        doAnswer(new Answer<BusinessObject>() {
            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull(
                        "Expected two arguments: the local id of the business object to be retrieved, and the class of the business object",
                        args);
                assertEquals(
                        "Expected one argument: the class of the business object to be retrieved, and the class of the business object",
                        2, args.length);
                assertTrue("Expected argument to be of type string", args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject class", args[1] instanceof Class);
                String key = (String) args[0];
                return businessObjectMap.get(key);
            }
        }).when(businessObjectManager).get(anyString(), any(Class.class));
        
        doAnswer(new Answer<BusinessObject>() {
            
            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected three arguments: the local id of the business object to be retrieved,"
                        + " the instance of the business object, and the class of the business object", args);
                assertEquals("Expected three arguments: the class of the business object to be retrieved,"
                        + " the instance of the business object, and the class of the business object", 3, args.length);
                assertTrue("Expected argument to be of type string", args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject", args[1] instanceof BusinessObject);
                assertTrue("Expected argument to be of type BusinessObject class", args[2] instanceof Class);
                String key = (String) args[0];
                BusinessObject bo = (BusinessObject) args[1];
                businessObjectMap.put(key, bo);
                return null;
            }
        }).when(businessObjectManager).add(anyString(), any(BusinessObject.class), any(Class.class));
        
        doAnswer(new Answer<BusinessObject>() {
            @Override
            public BusinessObject answer(InvocationOnMock invocation) throws Throwable {
                
                Object[] args = invocation.getArguments();
                assertNotNull("Expected three arguments: the local id of the business object to be retrieved,"
                        + " the instance of the business object, and the class of the business object", args);
                assertEquals("Expected three arguments: the class of the business object to be retrieved,"
                        + " the instance of the business object, and the class of the business object", 3, args.length);
                assertTrue("Expected argument to be of type string", args[0] instanceof String);
                assertTrue("Expected argument to be of type BusinessObject", args[1] instanceof BusinessObject);
                assertTrue("Expected argument to be of type BusinessObject class", args[2] instanceof Class);
                String key = (String) args[0];
                BusinessObject bo = (BusinessObject) args[1];
                businessObjectMap.put(key, bo);
                return null;
            }
        }).when(businessObjectManager).update(anyString(), any(BusinessObject.class), any(Class.class));
        
        doAnswer(new Answer<Set<BusinessObject>>() {
            @Override
            public Set<BusinessObject> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one arguments: the the class of the business object", args);
                assertEquals("Expected one argument: the class of the business objects to be retrieved", 1, args.length);
                assertTrue("Expected argument to be of type BusinessObject class", args[0] instanceof Class);
                
                Set<MetadataFile> metadataFileSet = new HashSet<MetadataFile>();
                Set<DataFile> dataFileSet = new HashSet<DataFile>();
                Set<Collection> collectionSet = new HashSet<Collection>();
                Set<DataItem> dataItemSet = new HashSet<DataItem>();
                
                for (String key : businessObjectMap.keySet()) {
                    if (businessObjectMap.get(key) instanceof MetadataFile) {
                        metadataFileSet.add((MetadataFile) businessObjectMap.get(key));
                    }
                    else if (businessObjectMap.get(key) instanceof DataFile) {
                        dataFileSet.add((DataFile) businessObjectMap.get(key));
                    }
                    else if (businessObjectMap.get(key) instanceof Collection) {
                        collectionSet.add((Collection) businessObjectMap.get(key));
                    }
                    else if (businessObjectMap.get(key) instanceof DataItem) {
                        dataItemSet.add((DataItem) businessObjectMap.get(key));
                    }
                }
                
                Set<BusinessObject> result = new HashSet<BusinessObject>();
                
                if (args[0].equals(MetadataFile.class)) {
                    result.addAll(metadataFileSet);
                }
                else if (args[0].equals(DataFile.class)) {
                    result.addAll(dataFileSet);
                }
                else if (args[0].equals(Collection.class)) {
                    result.addAll(collectionSet);
                }
                else if (args[0].equals(DataItem.class)) {
                    result.addAll(dataItemSet);
                }
                return result;
            }
        }).when(businessObjectManager).getInstancesOf(any(Class.class));

    }

    /**
     * Build expected map between collection's title and the count of data items it contains.
     * For test in this suite, AttributeSet had been setup so that the following is true:
     * - {@code} collection contains 1 DataItem (dataItemTwo)
     * - {@code} subcollection contains 1 DataItem (dataItemThree)
     * - {@code} titlelessCollection contains 1 DataItem (dataItemOne)
     *
     * If the setup changes, the expected map should be changed accordingly.
     * @return
     */
    private Map<String, Integer> getExpectedDICountToCollectionMap() {
        Map<String, Integer> expectedMap = new HashMap<String, Integer>();
        expectedMap.put(collectionName, 1);
        expectedMap.put(subcollectionName, 1);
        expectedMap.put("Collection with unspecified title (id: " + titleLessCollection.getId() + ")", 1);
        return  expectedMap;
    }

    /**
     * Build expected map between file type and the count of files in that type.
     * For test in this suite, AttributeSet had been setup so that the following is true:
     * - There are 4 files in {@code dataFileFormat}
     * - There are 3 files in {@code metadataFileFormat);
     *
     * If the setup changes, the expected map should be changed accordingly.
     * @return
     */
    private Map<String, Integer> getExpectedFileTypeCountMap() {
        Map<String, Integer> expectedMap = new HashMap<String, Integer>();
        expectedMap.put(dataFileFormat.getName(), 4);
        expectedMap.put(metadataFileFormat.getName(), 3);
        return  expectedMap;
    }

    /**
     * Build expected map between content detection tool name and its version.
     * For test in this suite, AttributeSet had been setup so that the following is true:
     * - There only ONE content detection tool used, name: DROID, version:v6.1
     *
     * If the setup changes, the expected map should be changed accordingly.
     * @return
     */
    private Map<String, String> getExpectedContentDectionToolInfo() {
        Map<String, String> expectedMap = new HashMap<String, String>();
        expectedMap.put(fileFormatDetectionToolName.getValue(), fileFormatDetectionToolVersion.getValue());
        return  expectedMap;
    }
    
    private void addBusinessObjects() {
        collection = new Collection();
        collection.setId("id:collection");
        collection.setTitle(collectionName);
        collection.setSummary(collectionSummary);
        collection.getCreators().add(personName1);
        collection.setPublicationDate(publishDate);
        collection.getChildrenIds().add("id:subcollection");
        collection.getChildrenIds().add("collection-id");
        //collection.addMetadataFile("metadataFile:1");
        //collection.addMetadataFile("metadataFile:2");
        businessObjectManager.add(collectionName, collection, Collection.class);

        subcollection = new Collection();
        subcollection.setId("id:subcollection");
        subcollection.setTitle(subcollectionName);
        subcollection.setSummary(subcollectionSummary);
        subcollection.getCreators().add(personName2);
        subcollection.setPublicationDate(publishDate);
        subcollection.setParentId(collection.getId());
        businessObjectManager.add(subcollectionName, subcollection, Collection.class);
        
        titleLessCollection = new Collection();
        titleLessCollection.setId("collection-id");
        titleLessCollection.setSummary(titleLessCollectionSummary);
        titleLessCollection.getCreators().add(personName2);
        titleLessCollection.setPublicationDate(publishDate);
        titleLessCollection.setParentId(collection.getId());
        businessObjectManager.add(titleLessCollectionName, titleLessCollection, Collection.class);

        dataFileOne = new DataFile();
        dataFileOne.setId("dataFile:1");
        dataFileOne.setName(dataFileOneName);
        dataFileOne.setPath(dataFileOnePayloadPath);
        dataFileOne.setFormat(dataFileFormat.getFormat());
        businessObjectManager.add(dataFileOneName, dataFileOne, DataFile.class);

        dataFileTwo = new DataFile();
        dataFileTwo.setId("dataFile:2");
        dataFileTwo.setName(dataFileTwoName);
        dataFileTwo.setPath(dataFileTwoPayloadPath);
        dataFileTwo.setFormat(dataFileFormat.getFormat());
        businessObjectManager.add(dataFileTwoName, dataFileTwo, DataFile.class);

        dataFileThree = new DataFile();
        dataFileThree.setId("dataFile:3");
        dataFileThree.setName(dataFileThreeName);
        dataFileThree.setPath(dataFileThreePayloadPath);
        dataFileThree.setFormat(dataFileFormat.getFormat());
        businessObjectManager.add(dataFileThreeName, dataFileThree, DataFile.class);

        dataFileFour = new DataFile();
        dataFileFour.setId("dataFile:4");
        dataFileFour.setName(dataFileFourName);
        dataFileFour.setPath(dataFileFourPayloadPath);
        dataFileFour.setFormat(dataFileFormat.getFormat());
        businessObjectManager.add(dataFileFourName, dataFileFour, DataFile.class);

        metadataFileOne = new MetadataFile();
        metadataFileOne.setId("metadataFile:1");
        metadataFileOne.setName(metadataFileOneName);
        metadataFileOne.setPath(metadataFileOnePayloadPath);
        metadataFileOne.setSize(metadataFileOneSize);
        metadataFileOne.setFormat(metadataFileFormat.getFormat());
        businessObjectManager.add(metadataFileOneName, metadataFileOne, MetadataFile.class);

        metadataFileTwo = new MetadataFile();
        metadataFileTwo.setId("metadataFile:2");
        metadataFileTwo.setName(metadataFileTwoName);
        metadataFileTwo.setPath(metadataFileTwoPayloadPath);
        metadataFileTwo.setSize(metadataFileTwoSize);
        metadataFileOne.setFormat(metadataFileFormat.getFormat());
        businessObjectManager.add(metadataFileTwoName, metadataFileTwo, MetadataFile.class);

        metadataFileThree = new MetadataFile();
        metadataFileThree.setId("metadataFile:3");
        metadataFileThree.setName(metadataFileThreeName);
        metadataFileThree.setPath(metadataFileThreePayloadPath);
        metadataFileThree.setSize(metadataFileThreeSize);
        metadataFileOne.setFormat(metadataFileFormat.getFormat());
        businessObjectManager.add(metadataFileThreeName, metadataFileThree, MetadataFile.class);

        dataItemOne = new DataItem();
        dataItemOne.setId("dataItem:1");
        dataItemOne.setName(dataItemOneName);
        dataItemOne.setDescription(dataItemOneDescription);
        dataItemOne.getFiles().add(dataFileOne);
        dataItemOne.getFiles().add(dataFileTwo);
        businessObjectManager.add(dataItemOneName, dataItemOne, DataItem.class);

        dataItemTwo = new DataItem();
        dataItemTwo.setId("dataItem:2");
        dataItemTwo.setName(dataItemTwoName);
        dataItemTwo.setDescription(dataItemTwoDescription);
        dataItemTwo.getFiles().add(dataFileThree);
        businessObjectManager.add(dataItemTwoName, dataItemTwo, DataItem.class);

        dataItemThree = new DataItem();
        dataItemThree.setId("dataItem:3");
        dataItemThree.setName(dataItemThreeName);
        dataItemThree.setDescription(dataItemThreeDescription);
        dataItemThree.getFiles().add(dataFileFour);
        businessObjectManager.add(dataItemThreeName, dataItemThree, DataItem.class);
    }

    private void addAttributeSets() {
        collectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        subcollectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        titleLessCollectionAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
        dataItemOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        dataItemTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        dataItemThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
        dataFileOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        dataFileTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        dataFileThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        dataFileFourAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        metadataFileOneAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        metadataFileTwoAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        metadataFileThreeAttributeSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
        fileAttributeSet = new AttributeSetImpl(AttributeSetName.FILE);
        
        String creationDate = "2013-06-10T01:55:18Z";
        
        String creator1 = "Dr. Robert Moses Kildare";
        
        expectedKeys = new String[] { collectionAttributeSet.getName() + "_" + collectionName,
                subcollectionAttributeSet.getName() + "_" + subcollectionName,
                dataItemOneAttributeSet.getName() + "_" + dataItemOneName,
                dataItemTwoAttributeSet.getName() + "_" + dataItemTwoName,
                dataItemThreeAttributeSet.getName() + "_" + dataItemThreeName,
                dataFileOneAttributeSet.getName() + "_" + dataFileOneName,
                dataFileTwoAttributeSet.getName() + "_" + dataFileTwoName,
                dataFileThreeAttributeSet.getName() + "_" + dataFileThreeName,
                dataFileFourAttributeSet.getName() + "_" + dataFileFourName,
                metadataFileOneAttributeSet.getName() + "_" + metadataFileOneName,
                metadataFileTwoAttributeSet.getName() + "_" + metadataFileTwoName,
                metadataFileThreeAttributeSet.getName() + "_" + metadataFileThreeName,
                fileAttributeSet.getName() + "_Unique",
                titleLessCollectionAttributeSet.getName() + titleLessCollectionName };


        // Create attribute sets and put them in the manager
        collectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, collectionName));
        collectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, collectionSummary));
        collectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, creator1));
        collectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_AGGREGATES_COLLECTION, AttributeValueType.STRING,
                        subcollectionName));
        collectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_AGGREGATES_DATAITEM, AttributeValueType.STRING, dataItemTwoName));
        collectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_CREATED, "DateTime", creationDate));
        collectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, collectionName));
        attributeSetManager.addAttributeSet(expectedKeys[0], collectionAttributeSet);
        
        subcollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, subcollectionName));
        subcollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING, subcollectionSummary));
        subcollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, creator2));
        subcollectionAttributeSet.getAttributes()
                .add(new AttributeImpl(Metadata.COLLECTION_IS_PART_OF_COLLECTION, AttributeValueType.STRING,
                        collectionName));
        subcollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_CREATED, "DateTime", creationDate));
        subcollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, subcollectionName));
        subcollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_AGGREGATES_DATAITEM, AttributeValueType.STRING, dataItemThreeName));
        attributeSetManager.addAttributeSet(expectedKeys[1], subcollectionAttributeSet);
        
        dataItemOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemOneName));
        dataItemOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemOneDescription));
        dataItemOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileOneName));
        dataItemOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileTwoName));
        dataItemOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[2], dataItemOneAttributeSet);
        
        dataItemTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemTwoName));
        dataItemTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemTwoDescription));
        dataItemTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileThreeName));
        dataItemTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemTwoName));
        attributeSetManager.addAttributeSet(expectedKeys[3], dataItemTwoAttributeSet);
        
        dataItemThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_TITLE, AttributeValueType.STRING, dataItemThreeName));
        dataItemThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_DESCRIPTION, AttributeValueType.STRING, dataItemThreeDescription));
        dataItemThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATA_ITEM_AGGREGATES_FILE, AttributeValueType.STRING, dataFileFourName));
        dataItemThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemThreeName));
        attributeSetManager.addAttributeSet(expectedKeys[4], dataItemThreeAttributeSet);
        
        dataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileOneName));
        dataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, file1URI));
        dataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, dataFileFormatString));
        dataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileOneName));
        attributeSetManager.addAttributeSet(expectedKeys[5], dataFileOneAttributeSet);
        
        dataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileTwoName));
        dataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, file2URI));
        dataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, dataFileFormatString));
        dataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileTwoName));
        attributeSetManager.addAttributeSet(expectedKeys[6], dataFileTwoAttributeSet);
        
        dataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileThreeName));
        dataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, file3URI));
        dataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, dataFileFormatString));
        dataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileThreeName));
        attributeSetManager.addAttributeSet(expectedKeys[7], dataFileThreeAttributeSet);
        
        dataFileFourAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, dataFileFourName));
        dataFileFourAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, file4URI));
        dataFileFourAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, dataFileFormatString));
        dataFileFourAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, dataFileFourName));
        attributeSetManager.addAttributeSet(expectedKeys[8], dataFileFourAttributeSet);
        
        metadataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, metadataFileOneName));
        metadataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFile1URI));
        metadataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, metadataFileFormatString));
        metadataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileOneName));
        metadataFileOneAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, collectionName));
        attributeSetManager.addAttributeSet(expectedKeys[9], metadataFileOneAttributeSet);
        
        metadataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, metadataFileTwoName));
        metadataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFile2URI));
        metadataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, metadataFileFormatString));
        metadataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileTwoName));
        metadataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, collectionName));
        metadataFileTwoAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[10], metadataFileTwoAttributeSet);
        
        metadataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, metadataFileThreeName));
        metadataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, metadataFile3URI));
        metadataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, metadataFileFormatString));
        metadataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, metadataFileThreeName));
        metadataFileThreeAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.FILE_IS_METADATA_FOR, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[11], metadataFileThreeAttributeSet);



        titleLessCollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_TITLE, AttributeValueType.STRING, titleLessCollectionName));
        titleLessCollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_DESCRIPTION, AttributeValueType.STRING,
                        titleLessCollectionSummary));
        titleLessCollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_CREATOR_NAME, AttributeValueType.STRING, creator1));
        titleLessCollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_AGGREGATES_COLLECTION, AttributeValueType.STRING,
                        subcollectionName));
        titleLessCollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_CREATED, AttributeValueType.DATETIME, creationDate));
        titleLessCollectionAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, titleLessCollectionName));
        titleLessCollectionAttributeSet.getAttributes().add(
                new AttributeImpl(COLLECTION_AGGREGATES_DATAITEM, AttributeValueType.STRING, dataItemOneName));
        attributeSetManager.addAttributeSet(expectedKeys[13], titleLessCollectionAttributeSet);
    }
    
    private void addFileAttributeSet() {
        fileFormatDetectionToolName = new AttributeImpl(Metadata.FILE_FORMAT_DETECTION_TOOL_NAME,
                AttributeValueType.STRING, "DROID");
        fileFormatDetectionToolVersion = new AttributeImpl(Metadata.FILE_FORMAT_DETECTION_TOOL_VERSION,
                AttributeValueType.STRING, "v6.1");


        file1AS = new AttributeSetImpl(AttributeSetName.FILE);
        file1AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileOneSize)));
        file1AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, "This is file one"));
        file1AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, dataFileFormatString));
        file1AS.getAttributes().add(fileFormatDetectionToolName);
        file1AS.getAttributes().add(fileFormatDetectionToolVersion);
        attributeSetManager.addAttributeSet(file1FileASKey, file1AS);
        
        file2AS = new AttributeSetImpl(AttributeSetName.FILE);
        file2AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileTwoSize)));
        file2AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, "This is file two"));
        file2AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, dataFileFormatString));
        file2AS.getAttributes().add(fileFormatDetectionToolName);
        file2AS.getAttributes().add(fileFormatDetectionToolVersion);
        attributeSetManager.addAttributeSet(file2FileASKey, file2AS);
        
        file3AS = new AttributeSetImpl(AttributeSetName.FILE);
        file3AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileThreeSize)));
        file3AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, "This is file three"));
        file3AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, dataFileFormatString));
        file3AS.getAttributes().add(fileFormatDetectionToolName);
        file3AS.getAttributes().add(fileFormatDetectionToolVersion);
        attributeSetManager.addAttributeSet(file3FileASKey, file3AS);
        
        file4AS = new AttributeSetImpl(AttributeSetName.FILE);
        file4AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(dataFileFourSize)));
        file4AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, "This is file four"));
        file4AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, dataFileFormatString));
        file4AS.getAttributes().add(fileFormatDetectionToolName);
        file4AS.getAttributes().add(fileFormatDetectionToolVersion);
        attributeSetManager.addAttributeSet(file4FileASKey, file4AS);
        
        metadataFile1AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile1AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(metadataFileOneSize)));
        metadataFile1AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, "This is metadata file one"));
        metadataFile1AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, metadataFileFormatString));
        metadataFile1AS.getAttributes().add(fileFormatDetectionToolName);
        metadataFile1AS.getAttributes().add(fileFormatDetectionToolVersion);
        attributeSetManager.addAttributeSet(metadataFile1FileASKey, metadataFile1AS);
        
        metadataFile2AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile2AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(metadataFileTwoSize)));
        metadataFile2AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, "This is metadata file two"));
        metadataFile2AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, metadataFileFormatString));
        metadataFile2AS.getAttributes().add(fileFormatDetectionToolName);
        metadataFile2AS.getAttributes().add(fileFormatDetectionToolVersion);
        attributeSetManager.addAttributeSet(metadataFile2FileASKey, metadataFile2AS);
        
        metadataFile3AS = new AttributeSetImpl(AttributeSetName.FILE);
        metadataFile3AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, Long.toString(metadataFileThreeSize)));
        metadataFile3AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, "This is metadata file three"));
        metadataFile3AS.getAttributes().add(
                new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, metadataFileFormatString));
        metadataFile2AS.getAttributes().add(fileFormatDetectionToolName);
        metadataFile3AS.getAttributes().add(fileFormatDetectionToolVersion);
        attributeSetManager.addAttributeSet(metadataFile3FileASKey, metadataFile3AS);

        Set<AttributeSet> fileAttributeSets = new HashSet<AttributeSet>();
        fileAttributeSets.add(file1AS);
        fileAttributeSets.add(file2AS);
        fileAttributeSets.add(file3AS);
        fileAttributeSets.add(file4AS);
        fileAttributeSets.add(metadataFile1AS);
        fileAttributeSets.add(metadataFile2AS);
        fileAttributeSets.add(metadataFile3AS);
        when(attributeSetManager.matches(PackagePreIngestReportService.FILE_MATCHER)).thenReturn(fileAttributeSets);
    }
    
    private void addBagitAttributeSet() {
        
        Checksum checksum1 = new ChecksumImpl("md5", Long.toString(dataFileOneSize));
        Checksum checksum2 = new ChecksumImpl("md5", Long.toString(dataFileTwoSize));
        Checksum checksum3 = new ChecksumImpl("md5", Long.toString(dataFileThreeSize));
        Checksum checksum4 = new ChecksumImpl("md5", Long.toString(dataFileFourSize));
        Checksum checksum5 = new ChecksumImpl("md5", Long.toString(metadataFileOneSize));
        Checksum checksum6 = new ChecksumImpl("md5", Long.toString(metadataFileTwoSize));
        Checksum checksum7 = new ChecksumImpl("md5", Long.toString(metadataFileThreeSize));
        
        dataFileOnePair = new Pair<String, Checksum>(dataFileOnePayloadPath, checksum1);
        dataFileTwoPair = new Pair<String, Checksum>(dataFileTwoPayloadPath, checksum2);
        dataFileThreePair = new Pair<String, Checksum>(dataFileThreePayloadPath, checksum3);
        dataFileFourPair = new Pair<String, Checksum>(dataFileFourPayloadPath, checksum4);
        
        metadataFileOnePair = new Pair<String, Checksum>(metadataFileOnePayloadPath, checksum5);
        metadataFileTwoPair = new Pair<String, Checksum>(metadataFileTwoPayloadPath, checksum6);
        metadataFileThreePair = new Pair<String, Checksum>(metadataFileThreePayloadPath, checksum7);
        
        AttributeSet bagitAttributeSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        bagitAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileOnePair.toString()));
        bagitAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileTwoPair.toString()));
        bagitAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileThreePair.toString()));
        bagitAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, dataFileFourPair.toString()));
        bagitAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileOnePair.toString()));
        bagitAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileTwoPair.toString()));
        bagitAttributeSet.getAttributes().add(
                new AttributeImpl(Metadata.BAGIT_CHECKSUM, AttributeValueType.PAIR, metadataFileThreePair.toString()));
        
        attributeSetManager.addAttributeSet(AttributeSetName.BAGIT, bagitAttributeSet);
    }

}
