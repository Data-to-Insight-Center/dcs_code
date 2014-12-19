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
package org.dataconservancy.reporting.model;

import java.util.List;
import java.util.Map;

/**
 * 
 * Data container class to be used by an external service to generate the IngestReport
 * 
 */
public class IngestReport {
    
    private long totalPackageSize;
    private Map<String, Integer> dataItemsPerCollectionCount;
    private Map<String, Integer> fileTypeCount;
    private Map<String, Integer> generatedChecksumsCount;
    private Map<String, Integer> verifiedChecksumsCount;
    private Map<String, Integer> unverifiedChecksumsCount;
    private Map<String, String> unmatchedFileTypes;
    private Map<String, Integer> unmatchedRegisteredSchemasCount;
    private Map<String, Integer> registeredSchemasCount;
    private Map<String, Integer> unRegisteredSchemasCount;
    private Map<String, String> contentDetectionTools;
    private List<String> invalidMetadataFileWRegisteredSchemas;
    private String statusMessage;
    private boolean packageReadable;
    private boolean packageConforming;
    private boolean packageHasUnresolvableIds;
    private Status status;
    
    /**
     * Default no-arg Constructor
     */
    public IngestReport() {
    }

    /**
     * Constructor including all the fields.
     * 
     * @param totalPackageSize
     * @param dataItemsPerCollectionCount
     * @param fileTypeCount
     * @param generatedChecksumsCount
     * @param verifiedChecksumsCount
     * @param unverifiedChecksumsCount
     * @param unmatchedFileTypesCount
     * @param unmatchedRegisteredSchemasCount
     * @param registeredSchemasCount
     * @param unRegisteredSchemasCount
     * @param contentDetectionTools
     * @param invalidMetadataFileWRegisteredSchemas
     * @param statusMessage
     * @param packageReadable
     * @param packageConforming
     * @param packageHasUnresolvableIds
     * @param status
     */
    public IngestReport(long totalPackageSize, Map<String, Integer> dataItemsPerCollectionCount,
            Map<String, Integer> fileTypeCount, Map<String, Integer> generatedChecksumsCount,
            Map<String, Integer> verifiedChecksumsCount, Map<String, Integer> unverifiedChecksumsCount,
            Map<String, String> unmatchedFileTypesCount, Map<String, Integer> unmatchedRegisteredSchemasCount,
            Map<String, Integer> registeredSchemasCount, Map<String, Integer> unRegisteredSchemasCount,
            Map<String, String> contentDetectionTools, List<String> invalidMetadataFileWRegisteredSchemas,
            String statusMessage, boolean packageReadable, boolean packageConforming,
            boolean packageHasUnresolvableIds, Status status) {
        super();
        this.totalPackageSize = totalPackageSize;
        this.dataItemsPerCollectionCount = dataItemsPerCollectionCount;
        this.fileTypeCount = fileTypeCount;
        this.generatedChecksumsCount = generatedChecksumsCount;
        this.verifiedChecksumsCount = verifiedChecksumsCount;
        this.unverifiedChecksumsCount = unverifiedChecksumsCount;
        this.unmatchedFileTypes = unmatchedFileTypesCount;
        this.unmatchedRegisteredSchemasCount = unmatchedRegisteredSchemasCount;
        this.registeredSchemasCount = registeredSchemasCount;
        this.unRegisteredSchemasCount = unRegisteredSchemasCount;
        this.contentDetectionTools = contentDetectionTools;
        this.invalidMetadataFileWRegisteredSchemas = invalidMetadataFileWRegisteredSchemas;
        this.statusMessage = statusMessage;
        this.packageReadable = packageReadable;
        this.packageConforming = packageConforming;
        this.packageHasUnresolvableIds = packageHasUnresolvableIds;
        this.status = status;
    }

    /**
     * Constructor using a copy object.
     * 
     * @param toCopy
     */
    public IngestReport(IngestReport toCopy) {
        this.totalPackageSize = toCopy.getTotalPackageSize();
        this.dataItemsPerCollectionCount = toCopy.getDataItemsPerCollectionCount();
        this.fileTypeCount = toCopy.getFileTypeCount();
        this.generatedChecksumsCount = toCopy.getGeneratedChecksumsCount();
        this.verifiedChecksumsCount = toCopy.getVerifiedChecksumsCount();
        this.unverifiedChecksumsCount = toCopy.getUnverifiedChecksumsCount();
        this.unmatchedFileTypes = toCopy.getUnmatchedFileTypes();
        this.unmatchedRegisteredSchemasCount = toCopy.getUnmatchedRegisteredSchemasCount();
        this.registeredSchemasCount = toCopy.getRegisteredSchemasCount();
        this.unRegisteredSchemasCount = toCopy.getUnRegisteredSchemasCount();
        this.invalidMetadataFileWRegisteredSchemas = toCopy.getInvalidMetadataFileWRegisteredSchemas();
        this.packageReadable = toCopy.isPackageReadable();
        this.packageConforming = toCopy.isPackageConforming();
        this.packageHasUnresolvableIds = toCopy.packageHasUnresolvableIds;
        this.status = toCopy.getStatus();
    }

    /**
     * Returns the total size of the package being ingested into the archive in bytes.
     * 
     * @return long value
     */
    public long getTotalPackageSize() {
        return totalPackageSize;
    }
    
    /**
     * @param totalPackageSize
     */
    public void setTotalPackageSize(long totalPackageSize) {
        this.totalPackageSize = totalPackageSize;
    }
    
    /**
     * Returns a map that includes the file type and a count of total number of times that file type exists in the
     * package.
     * 
     * @return map
     */
    public Map<String, Integer> getFileTypeCount() {
        return fileTypeCount;
    }
    
    /**
     * @param fileTypeCount
     */
    public void setFileTypeCount(Map<String, Integer> fileTypeCount) {
        this.fileTypeCount = fileTypeCount;
    }
    
    /**
     * Returns a map of total number of checksums generated and the algorithm used.
     * 
     * @return map
     */
    public Map<String, Integer> getGeneratedChecksumsCount() {
        return generatedChecksumsCount;
    }
    
    /**
     * @param generatedChecksumsCount
     */
    public void setGeneratedChecksumsCount(Map<String, Integer> generatedChecksumsCount) {
        this.generatedChecksumsCount = generatedChecksumsCount;
    }
    
    /**
     * Returns a map of total number of checksums verified and the algorithm used.
     * 
     * @return map
     */
    public Map<String, Integer> getVerifiedChecksumsCount() {
        return verifiedChecksumsCount;
    }
    
    /**
     * @param verifiedChecksumsCount
     */
    public void setVerifiedChecksumsCount(Map<String, Integer> verifiedChecksumsCount) {
        this.verifiedChecksumsCount = verifiedChecksumsCount;
    }
    
    /**
     * Returns a map of checksums that were provided in the bag but did not verify against the system generated
     * checksum.
     * 
     * @return map
     */
    public Map<String, Integer> getUnverifiedChecksumsCount() {
        return unverifiedChecksumsCount;
    }
    
    /**
     * @param unverifiedChecksumsCount
     */
    public void setUnverifiedChecksumsCount(Map<String, Integer> unverifiedChecksumsCount) {
        this.unverifiedChecksumsCount = unverifiedChecksumsCount;
    }
    
    /**
     * Returns a map of File Types that were claimed in the package but didn't match the type determined by the system
     * with an explanation as the value.
     * 
     * @return map
     */
    public Map<String, String> getUnmatchedFileTypes() {
        return unmatchedFileTypes;
    }
    
    /**
     * @param unmatchedFileTypes
     */
    public void setUnmatchedFileTypes(Map<String, String> unmatchedFileTypes) {
        this.unmatchedFileTypes = unmatchedFileTypes;
    }
    
    /**
     * Returns a map of schemas that claimed to be a DCS registered schema, but didn't match any.
     * 
     * @return map
     */
    public Map<String, Integer> getUnmatchedRegisteredSchemasCount() {
        return unmatchedRegisteredSchemasCount;
    }
    
    /**
     * @param unmatchedRegisteredSchemasCount
     */
    public void setUnmatchedRegisteredSchemasCount(Map<String, Integer> unmatchedRegisteredSchemasCount) {
        this.unmatchedRegisteredSchemasCount = unmatchedRegisteredSchemasCount;
    }
    
    /**
     * Returns list of local paths (file locations in the package) for files which did not validate against the XML
     * schema it claims to conform to.
     * 
     * @return map
     */
    public List<String> getInvalidMetadataFileWRegisteredSchemas() {
        return invalidMetadataFileWRegisteredSchemas;
    }
    
    /**
     * @param invalidMetadataFileWRegisteredSchemas
     */
    public void setInvalidMetadataFileWRegisteredSchemas(List<String> invalidMetadataFileWRegisteredSchemas) {
        this.invalidMetadataFileWRegisteredSchemas = invalidMetadataFileWRegisteredSchemas;
    }
    
    /**
     * Returns a map of registered schema IDs and their count.
     * 
     * @return map
     */
    public Map<String, Integer> getRegisteredSchemasCount() {
        return registeredSchemasCount;
    }
    
    /**
     * @param registeredSchemasCount
     */
    public void setRegisteredSchemasCount(Map<String, Integer> registeredSchemasCount) {
        this.registeredSchemasCount = registeredSchemasCount;
    }
    
    /**
     * Returns a map of unregistered schema IDs and their count.
     * 
     * @return map
     */
    public Map<String, Integer> getUnRegisteredSchemasCount() {
        return unRegisteredSchemasCount;
    }
    
    /**
     * @param unRegisteredSchemasCount
     */
    public void setUnRegisteredSchemasCount(Map<String, Integer> unRegisteredSchemasCount) {
        this.unRegisteredSchemasCount = unRegisteredSchemasCount;
    }
    
    /**
     * Returns false if the package to be ingested is not readable.
     * 
     * @return boolean
     */
    public boolean isPackageReadable() {
        return packageReadable;
    }
    
    /**
     * @param packageReadable
     */
    public void setPackageReadable(boolean packageReadable) {
        this.packageReadable = packageReadable;
    }
    
    /**
     * Returns false if the package fails to conform to the package specifications.
     * 
     * @return boolean
     */
    public boolean isPackageConforming() {
        return packageConforming;
    }
    
    /**
     * @param packageConforming
     */
    public void setPackageConforming(boolean packageConforming) {
        this.packageConforming = packageConforming;
    }
    
    /**
     * Returns true if the package contains current DCS IDs that cannot be resolved.
     * 
     * @return boolean
     */
    public boolean packageHasUnresolvableIds() {
        return packageHasUnresolvableIds;
    }
    
    /**
     * @param packageHasUnresolvableIds
     */
    public void setPackageHasUnresolvableIds(boolean packageHasUnresolvableIds) {
        this.packageHasUnresolvableIds = packageHasUnresolvableIds;
    }
    
    /**
     * @return the statusMessage
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * @param statusMessage
     *            the statusMessage to set
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns a map of Content Detection Tool as the key and the version of the tool as its value.
     * 
     * @return map
     */
    public Map<String, String> getContentDetectionTools() {
        return contentDetectionTools;
    }
    
    /**
     * @param contentDetectionTools
     *            the contentDetectionTools to set
     */
    public void setContentDetectionTools(Map<String, String> contentDetectionTools) {
        this.contentDetectionTools = contentDetectionTools;
    }

    /**
     * @return the dataItemsPerCollectionCount
     */
    public Map<String, Integer> getDataItemsPerCollectionCount() {
        return dataItemsPerCollectionCount;
    }
    
    /**
     * @param dataItemsPerCollectionCount
     *            the dataItemsPerCollectionCount to set
     */
    public void setDataItemsPerCollectionCount(Map<String, Integer> dataItemsPerCollectionCount) {
        this.dataItemsPerCollectionCount = dataItemsPerCollectionCount;
    }

    public enum Status {
        SUCCESSFUL,
        WARNINGS,
        ERRORS,
        IN_PROGRESS
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contentDetectionTools == null) ? 0 : contentDetectionTools.hashCode());
        result = prime * result + ((dataItemsPerCollectionCount == null) ? 0 : dataItemsPerCollectionCount.hashCode());
        result = prime * result + ((fileTypeCount == null) ? 0 : fileTypeCount.hashCode());
        result = prime * result + ((generatedChecksumsCount == null) ? 0 : generatedChecksumsCount.hashCode());
        result = prime
                * result
                + ((invalidMetadataFileWRegisteredSchemas == null) ? 0 : invalidMetadataFileWRegisteredSchemas
                        .hashCode());
        result = prime * result + (packageConforming ? 1231 : 1237);
        result = prime * result + (packageHasUnresolvableIds ? 1231 : 1237);
        result = prime * result + (packageReadable ? 1231 : 1237);
        result = prime * result + ((registeredSchemasCount == null) ? 0 : registeredSchemasCount.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((statusMessage == null) ? 0 : statusMessage.hashCode());
        result = prime * result + (int) (totalPackageSize ^ (totalPackageSize >>> 32));
        result = prime * result + ((unRegisteredSchemasCount == null) ? 0 : unRegisteredSchemasCount.hashCode());
        result = prime * result + ((unmatchedFileTypes == null) ? 0 : unmatchedFileTypes.hashCode());
        result = prime * result
                + ((unmatchedRegisteredSchemasCount == null) ? 0 : unmatchedRegisteredSchemasCount.hashCode());
        result = prime * result + ((unverifiedChecksumsCount == null) ? 0 : unverifiedChecksumsCount.hashCode());
        result = prime * result + ((verifiedChecksumsCount == null) ? 0 : verifiedChecksumsCount.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IngestReport other = (IngestReport) obj;
        if (contentDetectionTools == null) {
            if (other.contentDetectionTools != null)
                return false;
        }
        else if (!contentDetectionTools.equals(other.contentDetectionTools))
            return false;
        if (dataItemsPerCollectionCount == null) {
            if (other.dataItemsPerCollectionCount != null)
                return false;
        }
        else if (!dataItemsPerCollectionCount.equals(other.dataItemsPerCollectionCount))
            return false;
        if (fileTypeCount == null) {
            if (other.fileTypeCount != null)
                return false;
        }
        else if (!fileTypeCount.equals(other.fileTypeCount))
            return false;
        if (generatedChecksumsCount == null) {
            if (other.generatedChecksumsCount != null)
                return false;
        }
        else if (!generatedChecksumsCount.equals(other.generatedChecksumsCount))
            return false;
        if (invalidMetadataFileWRegisteredSchemas == null) {
            if (other.invalidMetadataFileWRegisteredSchemas != null)
                return false;
        }
        else if (!invalidMetadataFileWRegisteredSchemas.equals(other.invalidMetadataFileWRegisteredSchemas))
            return false;
        if (packageConforming != other.packageConforming)
            return false;
        if (packageHasUnresolvableIds != other.packageHasUnresolvableIds)
            return false;
        if (packageReadable != other.packageReadable)
            return false;
        if (registeredSchemasCount == null) {
            if (other.registeredSchemasCount != null)
                return false;
        }
        else if (!registeredSchemasCount.equals(other.registeredSchemasCount))
            return false;
        if (status != other.status)
            return false;
        if (statusMessage == null) {
            if (other.statusMessage != null)
                return false;
        }
        else if (!statusMessage.equals(other.statusMessage))
            return false;
        if (totalPackageSize != other.totalPackageSize)
            return false;
        if (unRegisteredSchemasCount == null) {
            if (other.unRegisteredSchemasCount != null)
                return false;
        }
        else if (!unRegisteredSchemasCount.equals(other.unRegisteredSchemasCount))
            return false;
        if (unmatchedFileTypes == null) {
            if (other.unmatchedFileTypes != null)
                return false;
        }
        else if (!unmatchedFileTypes.equals(other.unmatchedFileTypes))
            return false;
        if (unmatchedRegisteredSchemasCount == null) {
            if (other.unmatchedRegisteredSchemasCount != null)
                return false;
        }
        else if (!unmatchedRegisteredSchemasCount.equals(other.unmatchedRegisteredSchemasCount))
            return false;
        if (unverifiedChecksumsCount == null) {
            if (other.unverifiedChecksumsCount != null)
                return false;
        }
        else if (!unverifiedChecksumsCount.equals(other.unverifiedChecksumsCount))
            return false;
        if (verifiedChecksumsCount == null) {
            if (other.verifiedChecksumsCount != null)
                return false;
        }
        else if (!verifiedChecksumsCount.equals(other.verifiedChecksumsCount))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "IngestReport [totalPackageSize=" + totalPackageSize + ", dataItemsPerCollectionCount="
                + dataItemsPerCollectionCount + ", fileTypeCount=" + fileTypeCount + ", generatedChecksumsCount="
                + generatedChecksumsCount + ", verifiedChecksumsCount=" + verifiedChecksumsCount
                + ", unverifiedChecksumsCount=" + unverifiedChecksumsCount + ", unmatchedFileTypesCount="
                + unmatchedFileTypes + ", unmatchedRegisteredSchemasCount=" + unmatchedRegisteredSchemasCount
                + ", registeredSchemasCount=" + registeredSchemasCount + ", unRegisteredSchemasCount="
                + unRegisteredSchemasCount + ", contentDetectionTools=" + contentDetectionTools
                + ", invalidMetadataFileWRegisteredSchemas=" + invalidMetadataFileWRegisteredSchemas
                + ", statusMessage=" + statusMessage + ", packageReadable=" + packageReadable + ", packageConforming="
                + packageConforming + ", packageHasUnresolvableIds=" + packageHasUnresolvableIds + ", status=" + status
                + "]";
    }

}
