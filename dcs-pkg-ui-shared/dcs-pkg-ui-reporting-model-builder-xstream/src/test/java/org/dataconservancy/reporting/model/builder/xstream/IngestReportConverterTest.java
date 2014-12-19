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
package org.dataconservancy.reporting.model.builder.xstream;

import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_ALGORITHM;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_ALGORITHMS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_COLLECTION;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_COLLECTIONS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_COLLECTION_ID;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_COUNT;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_FILE_TYPE;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_DETAIL;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_FILE_TYPES;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_GENERATED_CHECKSUMS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_INGEST_REPORT;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_INVALID_METADATA_FILES;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_PACKAGE_CONFORMING;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_PACKAGE_HAS_UNRESOLVABLE_IDS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_PACKAGE_READABLE;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_PATH;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_REGISTERED_SCHEMA;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_REGISTERED_SCHEMAS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_SCHEMA_NAME;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_STATUS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_STATUS_MESSAGE;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_TOTAL_PACKAGE_SIZE;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_TYPE_NAME;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_UNMATCHED_FILE_TYPES;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_UNMATCHED_REGISTERED_SCHEMAS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_UNREGISTERED_SCHEMA;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_UNREGISTERED_SCHEMAS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_UNVERIFIED_CHECKSUMS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_VERIFIED_CHECKSUMS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_CONTENT_DETECTION_TOOLS;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_CONTENT_DETECTION_TOOL;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_NAME;
import static org.dataconservancy.reporting.model.builder.xstream.IngestReportConverter.E_VERSION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.reporting.model.IngestReport.Status;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

public class IngestReportConverterTest {
    
    private IngestReport ingestReport;
    private String xml;
    private XStream x;

    @Before
    public void setUp() {
        ingestReport = new IngestReport();
        
        ingestReport.setTotalPackageSize(234342342);
        
        Map<String, Integer> dataItemsPerCollection = new LinkedHashMap<String, Integer>();
        dataItemsPerCollection.put("Collection:1", 5);
        dataItemsPerCollection.put("Collection:2", 10);
        dataItemsPerCollection.put("Collection:3", 2);
        dataItemsPerCollection.put("Collection:4", 0);
        ingestReport.setDataItemsPerCollectionCount(dataItemsPerCollection);

        Map<String, Integer> fileTypeCount = new LinkedHashMap<String, Integer>();
        fileTypeCount.put("MS-Word 6.0", 20);
        fileTypeCount.put("MS-Word 2007", 26);
        fileTypeCount.put("PDF", 10);
        fileTypeCount.put("MS-Excel 2010", 45);
        ingestReport.setFileTypeCount(fileTypeCount);
        
        Map<String, Integer> generatedChecksumsCount = new HashMap<String, Integer>();
        generatedChecksumsCount.put("MD-5", 120);
        ingestReport.setGeneratedChecksumsCount(generatedChecksumsCount);

        Map<String, Integer> verifiedChecksumsCount = new HashMap<String, Integer>();
        verifiedChecksumsCount.put("MD-5", 120);
        ingestReport.setVerifiedChecksumsCount(verifiedChecksumsCount);

        Map<String, Integer> unverifiedChecksumsCount = new HashMap<String, Integer>();
        unverifiedChecksumsCount.put("SHA-1", 25);
        ingestReport.setUnverifiedChecksumsCount(unverifiedChecksumsCount);

        Map<String, String> unmatchedFileTypesCount = new HashMap<String, String>();
        unmatchedFileTypesCount.put("Movie file", "Did not match the detected 'AVI' format");
        ingestReport.setUnmatchedFileTypes(unmatchedFileTypesCount);

        Map<String, Integer> unmatchedRegisteredSchemasCount = new HashMap<String, Integer>();
        unmatchedRegisteredSchemasCount.put("FGDC", 4);
        ingestReport.setUnmatchedRegisteredSchemasCount(unmatchedRegisteredSchemasCount);
        
        List<String> invalidMetadataFileWRegisteredSchemas = new ArrayList<String>();
        invalidMetadataFileWRegisteredSchemas.add("/some/path/inpackage");
        ingestReport.setInvalidMetadataFileWRegisteredSchemas(invalidMetadataFileWRegisteredSchemas);

        Map<String, Integer> registeredSchemas = new HashMap<String, Integer>();
        registeredSchemas.put("FGDC-ID", 5);
        ingestReport.setRegisteredSchemasCount(registeredSchemas);
        
        Map<String, Integer> unregisteredSchemas = new HashMap<String, Integer>();
        unregisteredSchemas.put("SomeSchema-ID", 5);
        ingestReport.setUnRegisteredSchemasCount(unregisteredSchemas);

        Map<String, String> contentDetectionTools = new HashMap<String, String>();
        contentDetectionTools.put("DROID", "v6.1");
        ingestReport.setContentDetectionTools(contentDetectionTools);
        
        ingestReport.setPackageConforming(true);
        ingestReport.setPackageReadable(true);
        ingestReport.setPackageHasUnresolvableIds(false);
        ingestReport.setStatus(Status.SUCCESSFUL);
        ingestReport.setStatusMessage("Validation and Verification was successful.");
        
        x = XstreamIngestReportFactory.newInstance();
        setupXml();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }
    
    @Test
    public void testMarshall() throws SAXException, IOException {
        XMLAssert.assertXMLEqual(xml, x.toXML(ingestReport));
    }
    
    @Test
    public void testUnMarshall() {
        IngestReport actual = (IngestReport) x.fromXML(xml);
        Assert.assertEquals(ingestReport, actual);
        Assert.assertEquals(ingestReport, x.fromXML(x.toXML(ingestReport)));
    }

    private void setupXml() {
        xml =           "<" + E_INGEST_REPORT + ">\n" +
                            "<" + E_TOTAL_PACKAGE_SIZE + ">234342342</" + E_TOTAL_PACKAGE_SIZE + ">\n" +
                            "<" + E_COLLECTIONS + ">\n" +
                                "<" + E_COLLECTION + ">\n" +
                                    "<" + E_COLLECTION_ID + ">Collection:1</" + E_COLLECTION_ID + ">\n" +
                                    "<" + E_COUNT + ">5</" + E_COUNT + ">\n" +
                                "</" + E_COLLECTION + ">\n" +
                                "<" + E_COLLECTION + ">\n" +
                                    "<" + E_COLLECTION_ID + ">Collection:2</" + E_COLLECTION_ID + ">\n" +
                                    "<" + E_COUNT + ">10</" + E_COUNT + ">\n" +
                                "</" + E_COLLECTION + ">\n" +
                                "<" + E_COLLECTION + ">\n" +
                                    "<" + E_COLLECTION_ID + ">Collection:3</" + E_COLLECTION_ID + ">\n" +
                                    "<" + E_COUNT + ">2</" + E_COUNT + ">\n" +
                                "</" + E_COLLECTION + ">\n" +
                                "<" + E_COLLECTION + ">\n" +
                                    "<" + E_COLLECTION_ID + ">Collection:4</" + E_COLLECTION_ID + ">\n" +
                                    "<" + E_COUNT + ">0</" + E_COUNT + ">\n" +
                                "</" + E_COLLECTION + ">\n" +
                            "</" + E_COLLECTIONS + ">\n" +
                            "<" + E_FILE_TYPES + ">\n" +
                                "<" + E_FILE_TYPE + ">\n" +
                                    "<" + E_TYPE_NAME + ">MS-Word 6.0</" + E_TYPE_NAME + ">\n" +
                                    "<" + E_COUNT + ">20</" + E_COUNT + ">\n" +
                                "</" + E_FILE_TYPE + ">\n" +
                                "<" + E_FILE_TYPE + ">\n" +
                                     "<" + E_TYPE_NAME + ">MS-Word 2007</" + E_TYPE_NAME + ">\n" +
                                     "<" + E_COUNT + ">26</" + E_COUNT + ">\n" +
                                "</" + E_FILE_TYPE + ">\n" +
                                "<" + E_FILE_TYPE + ">\n" +
                                     "<" + E_TYPE_NAME + ">PDF</" + E_TYPE_NAME + ">\n" +
                                     "<" + E_COUNT + ">10</" + E_COUNT + ">\n" +
                                "</" + E_FILE_TYPE + ">\n" +
                                "<" + E_FILE_TYPE + ">\n" +
                                     "<" + E_TYPE_NAME + ">MS-Excel 2010</" + E_TYPE_NAME + ">\n" +
                                     "<" + E_COUNT + ">45</" + E_COUNT + ">\n" +
                                "</" + E_FILE_TYPE + ">\n" +      
                            "</" + E_FILE_TYPES + ">\n" + 
                            "<" + E_GENERATED_CHECKSUMS + ">\n" +
                                "<" + E_ALGORITHMS + ">\n" +
                                    "<" + E_ALGORITHM + ">MD-5</" + E_ALGORITHM + ">\n" + 
                                    "<" + E_COUNT + ">120</" + E_COUNT + ">\n" +
                                "</" + E_ALGORITHMS + ">\n" +
                            "</" + E_GENERATED_CHECKSUMS + ">\n" +
                            "<" + E_VERIFIED_CHECKSUMS + ">\n" +
                                "<" + E_ALGORITHMS + ">\n" +
                                    "<" + E_ALGORITHM + ">MD-5</" + E_ALGORITHM + ">\n" + 
                                    "<" + E_COUNT + ">120</" + E_COUNT + ">\n" +
                                "</" + E_ALGORITHMS + ">\n" +
                            "</" + E_VERIFIED_CHECKSUMS + ">\n" +
                            "<" + E_UNVERIFIED_CHECKSUMS + ">\n" +
                                "<" + E_ALGORITHMS + ">\n" +
                                    "<" + E_ALGORITHM + ">SHA-1</" + E_ALGORITHM + ">\n" + 
                                    "<" + E_COUNT + ">25</" + E_COUNT + ">\n" +
                                "</" + E_ALGORITHMS + ">\n" +
                            "</" + E_UNVERIFIED_CHECKSUMS + ">\n" +
                            "<" + E_UNMATCHED_FILE_TYPES + ">\n" +
                                "<" + E_FILE_TYPE + ">\n" +
                                    "<" + E_TYPE_NAME + ">Movie file</" + E_TYPE_NAME + ">\n" +
                                    "<" + E_DETAIL + ">Did not match the detected 'AVI' format</" + E_DETAIL + ">\n" +
                                "</" + E_FILE_TYPE + ">\n" +
                            "</" + E_UNMATCHED_FILE_TYPES + ">\n" +
                            "<" + E_UNMATCHED_REGISTERED_SCHEMAS + ">\n" +
                                "<" + E_REGISTERED_SCHEMA + ">\n" +
                                    "<" + E_SCHEMA_NAME + ">FGDC</" + E_SCHEMA_NAME + ">\n" +
                                    "<" + E_COUNT + ">4</" + E_COUNT + ">\n" +
                                "</" + E_REGISTERED_SCHEMA + ">\n" +
                            "</" + E_UNMATCHED_REGISTERED_SCHEMAS + ">\n" +
                            "<" + E_INVALID_METADATA_FILES + ">" +
                                "<" + E_PATH + ">/some/path/inpackage</" + E_PATH + ">\n" +
                            "</" + E_INVALID_METADATA_FILES + ">\n" +
                            "<" + E_REGISTERED_SCHEMAS + ">\n" +
                                "<" + E_REGISTERED_SCHEMA + ">\n" +
                                    "<" + E_SCHEMA_NAME + ">FGDC-ID</" + E_SCHEMA_NAME + ">\n" +
                                    "<" + E_COUNT + ">5</" + E_COUNT + ">\n" +
                                "</" + E_REGISTERED_SCHEMA + ">\n" +
                            "</" + E_REGISTERED_SCHEMAS + ">\n" +
                            "<" + E_UNREGISTERED_SCHEMAS + ">\n" +
                                "<" + E_UNREGISTERED_SCHEMA + ">\n" +
                                    "<" + E_SCHEMA_NAME + ">SomeSchema-ID</" + E_SCHEMA_NAME + ">\n" +
                                    "<" + E_COUNT + ">5</" + E_COUNT + ">\n" +
                                "</" + E_UNREGISTERED_SCHEMA + ">\n" +
                            "</" + E_UNREGISTERED_SCHEMAS + ">\n" +
                            "<" + E_CONTENT_DETECTION_TOOLS + ">\n" +
                                "<" + E_CONTENT_DETECTION_TOOL + ">\n" +
                                    "<" + E_NAME + ">DROID</" + E_NAME + ">\n" +
                                    "<" + E_VERSION + ">v6.1</" + E_VERSION + ">\n" +
                                "</" + E_CONTENT_DETECTION_TOOL + ">\n" +
                            "</" + E_CONTENT_DETECTION_TOOLS + ">\n" +
                            "<" + E_PACKAGE_CONFORMING + ">True</" + E_PACKAGE_CONFORMING + ">\n" +
                            "<" + E_PACKAGE_READABLE + ">True</" + E_PACKAGE_READABLE + ">\n" +
                            "<" + E_PACKAGE_HAS_UNRESOLVABLE_IDS + ">False</" + E_PACKAGE_HAS_UNRESOLVABLE_IDS + ">\n" +
                            "<" + E_STATUS + ">SUCCESSFUL</" + E_STATUS + ">\n" +
                            "<" + E_STATUS_MESSAGE + ">Validation and Verification was successful.</" + E_STATUS_MESSAGE + ">\n" +
                        "</" + E_INGEST_REPORT + ">";
    }
    
}
