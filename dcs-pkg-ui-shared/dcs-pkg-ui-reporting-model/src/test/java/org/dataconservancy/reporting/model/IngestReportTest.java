package org.dataconservancy.reporting.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.dataconservancy.reporting.model.IngestReport.Status;
import org.junit.Before;
import org.junit.Test;

public class IngestReportTest {
    
    private IngestReport ingestReportOne;
    private IngestReport ingestReportTwo;
    private IngestReport ingestReportThree;
    private IngestReport emptyIngestReport;
    
    @Before
    public void setUp() {
        ingestReportOne = new IngestReport();
        ingestReportTwo = new IngestReport();
        ingestReportThree = new IngestReport();
        emptyIngestReport = new IngestReport();

        ingestReportOne.setTotalPackageSize(234342342);
        Map<String, Integer> dataItemsPerCollection = new HashMap<String, Integer>();
        dataItemsPerCollection.put("Collection:1", 5);
        dataItemsPerCollection.put("Collection:2", 10);
        dataItemsPerCollection.put("Collection:3", 2);
        dataItemsPerCollection.put("Collection:4", 0);
        ingestReportOne.setDataItemsPerCollectionCount(dataItemsPerCollection);
        
        Map<String, Integer> fileTypeCount = new HashMap<String, Integer>();
        fileTypeCount.put("MS-Word 6.0", 20);
        fileTypeCount.put("MS-Word 2007", 26);
        fileTypeCount.put("PDF", 10);
        fileTypeCount.put("MS-Excel 2010", 45);
        ingestReportOne.setFileTypeCount(fileTypeCount);
        
        Map<String, Integer> generatedChecksumsCount = new HashMap<String, Integer>();
        generatedChecksumsCount.put("MD-5", 120);
        ingestReportOne.setGeneratedChecksumsCount(generatedChecksumsCount);

        Map<String, Integer> verifiedChecksumsCount = new HashMap<String, Integer>();
        verifiedChecksumsCount.put("MD-5", 120);
        ingestReportOne.setVerifiedChecksumsCount(verifiedChecksumsCount);

        Map<String, Integer> unverifiedChecksumsCount = new HashMap<String, Integer>();
        unverifiedChecksumsCount.put("SHA-1", 25);
        ingestReportOne.setUnverifiedChecksumsCount(unverifiedChecksumsCount);

        Map<String, String> unmatchedFileTypesCount = new HashMap<String, String>();
        unmatchedFileTypesCount.put("Movie file", "Did not match the detected 'AVI' format");
        ingestReportOne.setUnmatchedFileTypes(unmatchedFileTypesCount);

        Map<String, Integer> unmatchedRegisteredSchemasCount = new HashMap<String, Integer>();
        unmatchedRegisteredSchemasCount.put("FGDC", 4);
        ingestReportOne.setUnmatchedRegisteredSchemasCount(unmatchedRegisteredSchemasCount);
        
        List<String> invalidMetadataFileWRegisteredSchemas = new ArrayList<String>();
        invalidMetadataFileWRegisteredSchemas.add("/some/path/inpackage");
        ingestReportOne.setInvalidMetadataFileWRegisteredSchemas(invalidMetadataFileWRegisteredSchemas);

        Map<String, Integer> registeredSchemas = new HashMap<String, Integer>();
        registeredSchemas.put("FGDC-ID", 5);
        ingestReportOne.setRegisteredSchemasCount(registeredSchemas);
        
        Map<String, Integer> unregisteredSchemas = new HashMap<String, Integer>();
        unregisteredSchemas.put("SomeSchema-ID", 5);
        ingestReportOne.setUnRegisteredSchemasCount(unregisteredSchemas);

        Map<String, String> contentDetectionTools = new HashMap<String, String>();
        contentDetectionTools.put("DROID", "v6.1");
        ingestReportOne.setContentDetectionTools(contentDetectionTools);

        ingestReportOne.setPackageConforming(true);
        ingestReportOne.setPackageReadable(true);
        ingestReportOne.setPackageHasUnresolvableIds(false);
        ingestReportOne.setStatus(Status.SUCCESSFUL);
        ingestReportOne.setStatusMessage("Validation and Verification was successful.");

        ingestReportTwo.setTotalPackageSize(ingestReportOne.getTotalPackageSize());
        ingestReportTwo.setDataItemsPerCollectionCount(ingestReportOne.getDataItemsPerCollectionCount());
        ingestReportTwo.setFileTypeCount(ingestReportOne.getFileTypeCount());
        ingestReportTwo.setGeneratedChecksumsCount(ingestReportOne.getGeneratedChecksumsCount());
        ingestReportTwo.setVerifiedChecksumsCount(ingestReportOne.getVerifiedChecksumsCount());
        ingestReportTwo.setUnverifiedChecksumsCount(ingestReportOne.getUnverifiedChecksumsCount());
        ingestReportTwo.setUnmatchedFileTypes(ingestReportOne.getUnmatchedFileTypes());
        ingestReportTwo.setUnmatchedRegisteredSchemasCount(ingestReportOne.getUnmatchedRegisteredSchemasCount());
        ingestReportTwo.setInvalidMetadataFileWRegisteredSchemas(ingestReportOne
                .getInvalidMetadataFileWRegisteredSchemas());
        ingestReportTwo.setRegisteredSchemasCount(ingestReportOne.getRegisteredSchemasCount());
        ingestReportTwo.setUnRegisteredSchemasCount(ingestReportOne.getUnRegisteredSchemasCount());
        ingestReportTwo.setContentDetectionTools(ingestReportOne.getContentDetectionTools());
        ingestReportTwo.setPackageConforming(ingestReportOne.isPackageConforming());
        ingestReportTwo.setPackageReadable(ingestReportOne.isPackageReadable());
        ingestReportTwo.setPackageHasUnresolvableIds(ingestReportOne.packageHasUnresolvableIds());
        ingestReportTwo.setStatus(ingestReportOne.getStatus());
        ingestReportTwo.setStatusMessage(ingestReportOne.getStatusMessage());
        
        ingestReportThree.setTotalPackageSize(ingestReportTwo.getTotalPackageSize());
        ingestReportThree.setDataItemsPerCollectionCount(ingestReportTwo.getDataItemsPerCollectionCount());
        ingestReportThree.setFileTypeCount(ingestReportTwo.getFileTypeCount());
        ingestReportThree.setGeneratedChecksumsCount(ingestReportTwo.getGeneratedChecksumsCount());
        ingestReportThree.setVerifiedChecksumsCount(ingestReportTwo.getVerifiedChecksumsCount());
        ingestReportThree.setUnverifiedChecksumsCount(ingestReportTwo.getUnverifiedChecksumsCount());
        ingestReportThree.setUnmatchedFileTypes(ingestReportTwo.getUnmatchedFileTypes());
        ingestReportThree.setUnmatchedRegisteredSchemasCount(ingestReportTwo.getUnmatchedRegisteredSchemasCount());
        ingestReportThree.setInvalidMetadataFileWRegisteredSchemas(ingestReportTwo
                .getInvalidMetadataFileWRegisteredSchemas());
        ingestReportThree.setRegisteredSchemasCount(ingestReportTwo.getRegisteredSchemasCount());
        ingestReportThree.setUnRegisteredSchemasCount(ingestReportTwo.getUnRegisteredSchemasCount());
        ingestReportThree.setContentDetectionTools(ingestReportTwo.getContentDetectionTools());
        ingestReportThree.setPackageConforming(ingestReportTwo.isPackageConforming());
        ingestReportThree.setPackageReadable(ingestReportTwo.isPackageReadable());
        ingestReportThree.setPackageHasUnresolvableIds(ingestReportTwo.packageHasUnresolvableIds());
        ingestReportThree.setStatus(ingestReportTwo.getStatus());
        ingestReportThree.setStatusMessage(ingestReportTwo.getStatusMessage());
        
    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        Assert.assertFalse(ingestReportOne.equals(emptyIngestReport));
    }
    
    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        Assert.assertTrue(ingestReportOne.equals(ingestReportTwo));
        Assert.assertTrue(ingestReportTwo.equals(ingestReportOne));
    }
    
    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        Assert.assertTrue(ingestReportOne.equals(ingestReportTwo));
        Assert.assertTrue(ingestReportTwo.equals(ingestReportThree));
        Assert.assertTrue(ingestReportOne.equals(ingestReportThree));
    }
    
    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        Assert.assertTrue(ingestReportOne.equals(ingestReportTwo));
        Assert.assertTrue(ingestReportOne.equals(ingestReportTwo));
    }
    
    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        Assert.assertFalse(ingestReportOne.equals(null));
    }
}
