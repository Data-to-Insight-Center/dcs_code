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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.reporting.model.IngestReport.Status;
import org.dataconservancy.reporting.model.builder.IngestReportBuilder;
import org.junit.Before;
import org.junit.Test;

public class XstreamIngestReportBuilderTest {
    
    private IngestReportBuilder ingestReportBuilder;
    private IngestReport ingestReport;
    
    @Before
    public void setUp() {
        ingestReportBuilder = new XstreamIngestReportBuilder();
        ingestReport = new IngestReport();
        
        ingestReport.setTotalPackageSize(393982983);
        
        Map<String, Integer> dataItemsPerCollection = new LinkedHashMap<String, Integer>();
        dataItemsPerCollection.put("Collection:1", 5);
        dataItemsPerCollection.put("Collection:2", 10);
        dataItemsPerCollection.put("Collection:3", 2);
        dataItemsPerCollection.put("Collection:4", 0);
        ingestReport.setDataItemsPerCollectionCount(dataItemsPerCollection);
        
        Map<String, Integer> fileTypeCount = new HashMap<String, Integer>();
        fileTypeCount.put("MS-Word 6.0", 20);
        fileTypeCount.put("MS-Word 2007", 26);
        fileTypeCount.put("PDF", 10);
        fileTypeCount.put("MS-Excel 2010", 45);
        ingestReport.setFileTypeCount(fileTypeCount);
        
        Map<String, Integer> generatedChecksumsCount = new HashMap<String, Integer>();
        generatedChecksumsCount.put("MD5", 120);
        ingestReport.setGeneratedChecksumsCount(generatedChecksumsCount);
        
        Map<String, Integer> verifiedChecksumsCount = new HashMap<String, Integer>();
        verifiedChecksumsCount.put("MD5", 120);
        ingestReport.setVerifiedChecksumsCount(verifiedChecksumsCount);
        
        Map<String, Integer> unverifiedChecksumsCount = new HashMap<String, Integer>();
        unverifiedChecksumsCount.put("SHA1", 25);
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
        registeredSchemas.put("SomeSchema-ID", 5);
        ingestReport.setUnRegisteredSchemasCount(unregisteredSchemas);
        
        ingestReport.setPackageConforming(true);
        ingestReport.setPackageReadable(true);
        ingestReport.setPackageHasUnresolvableIds(false);
        ingestReport.setStatus(Status.SUCCESSFUL);
    }
    
    @Test
    public void testBuildIngestReportRoundTrip() throws InvalidXmlException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        ingestReportBuilder.buildIngestReport(ingestReport, sink);
        
        ByteArrayInputStream stream = new ByteArrayInputStream(sink.toByteArray());
        IngestReport returnedIngestReport = ingestReportBuilder.buildIngestReport(stream);
        
        Assert.assertEquals(ingestReport, returnedIngestReport);
    }

}
