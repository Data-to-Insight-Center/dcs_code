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

import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.packaging.ingest.api.*;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.AttributeValueType;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.reporting.model.builder.IngestReportBuilder;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;


/**
 * This report service gathers summary information regarding the validation and verification of a BagIt package being
 * ingested into the system. If the package's content is successfully validated and verified, the report will also
 * include information regarding the {@code BusinessObject} built from the package's content.
 *
 * <p/>
 *
 * A report with SUCCESSFUL status is built when the process completes without any {@code INGEST_FAILURE} event and all
 * content assertions can be validated and verified.
 *
 * <p/>
 *
 * A report with WARNING status is built when the process completes without any {@code INGEST_FAILURE} event, but the
 * following verification of content did not succeed:
 * <ul>
 *     <li>File format verification - the file format asserted in the package did not match up with the system-detected
 *     format.</li>
 * </ul>
 *
 * <p/>
 *
 * A report with ERROR status is built when the process does not complete and is aborted with a {@code INGEST_FAILURE}
 * event.
 */
public class PackagePreIngestReportService implements ReportService {

    private static final String MD5_CHECKSUM_ALG = "md5";
    private static final String SHA1_CHECKSUM_ALG = "sha1";
    private static final String EMPTY_STRING = "";
    private static final String UNSPECIFIED_COLLECTION_TITLE = "Collection with unspecified title (id: %s)";

    private IngestReportBuilder builder;

    @Override
    public DepositDocument produceReport(String depositId, IngestWorkflowState state) throws ReportServiceException {
        if (depositId == null || depositId.trim().length() == 0) {
            throw new IllegalArgumentException("Deposit identifier must not be empty or null!");
        }

        if (state == null) {
            throw new IllegalArgumentException("Ingest state must not be empty or null!");
        }

        if (state.getAttributeSetManager() == null) {
            throw new IllegalStateException("Ingest state must have an AttributeSetManager!");
        }

        if (state.getBusinessObjectManager() == null) {
            throw new IllegalStateException("Ingest state must have an BusinessObjectManager!");
        }

        if (state.getEventManager() == null) {
            throw new IllegalStateException("Ingest state must have an EventManager!");
        }

        if (builder == null) {
            throw new ReportServiceException("IngestReportBuilder cannot be null.");
        }

        IngestReport report = buildIngestReport(depositId, state);
        PreIngestProcessingReportDocument depositDocument = new PreIngestProcessingReportDocument();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        builder.buildIngestReport(report, baos);
        depositDocument.setInputStream(new ByteArrayInputStream(baos.toByteArray()));
        depositDocument.setLastModified(System.currentTimeMillis());
        depositDocument.getMetadata().put(Http.Header.CONTENT_LENGTH, String.valueOf(baos.toByteArray().length));
        depositDocument.getMetadata().put("document-type", "pre-ingest-report");
        return depositDocument;
    }

    private IngestReport buildIngestReport(String depositId, IngestWorkflowState state) throws ReportServiceException {
        IngestReport report = new IngestReport();
        //inspect the successful or failure of the ingest workflow.
        DcsEvent failureEvent =  state.getEventManager().getEventByType(depositId,
                Package.Events.INGEST_FAIL);
        StringBuilder reportSummary = new StringBuilder();
        Set<AttributeSet> fileAttributeSets = state.getAttributeSetManager().matches(FILE_MATCHER);

        final HierarchicalPrettyPrinter eventPrinter = new HierarchicalPrettyPrinter();
        if (failureEvent != null) {
            //if ERRORS -> build error ingest report
            report.setStatus(IngestReport.Status.ERRORS);

            reportSummary.append("Pre-ingest process encountered these errors and could not continue: <br/>" );
            failureEvent.toString(eventPrinter);
            reportSummary.append(eventPrinter.toString());

            report.setStatusMessage(reportSummary.toString());
        } else {
            //Using a list structure to ensure Warning events are returned in order.
            List<DcsEvent> warningEvents = getWarningEvents(depositId, state);

            if (warningEvents != null && !warningEvents.isEmpty()) {
                report.setStatus(IngestReport.Status.WARNINGS);
                Map<String, String> filesWithUnverifiedFormats = new HashMap<String, String>();
                reportSummary.append("Pre-ingest process produced the following warning(s): <br/>" );
                reportSummary.append("<ul>");
                for (DcsEvent warningEvent : warningEvents) {
                    //******* Compiling events' information base on even types.
                    //Handling format verification failure events
                    if (warningEvent.getEventType().equals(Package.Events.FORMAT_VERIFICATION_FAILED)) {
                        for (DcsEntityReference target : warningEvent.getTargets()) {
                            filesWithUnverifiedFormats.put(target.getRef(), warningEvent.getOutcome());
                        }
                        reportSummary.append("<li>" + warningEvent.getDetail() + "</li>");

                    //Handling unsupported file aggregation events.
                    } else if (warningEvent.getEventType().equals(Package.Events.UNSUPPORTED_FILE_AGGREGATION)) {
                        reportSummary.append("<li>");
                        reportSummary.append(warningEvent.getOutcome() + ". Reason: ");
                        reportSummary.append(warningEvent.getDetail());
                        reportSummary.append("</li>");
                    }

                }
                reportSummary.append("</ul>");

                report.setUnmatchedFileTypes(filesWithUnverifiedFormats);
                report.setStatusMessage(reportSummary.toString());
            } else { //no events which constitute warning conditions -> SUCCESS
                report.setStatus(IngestReport.Status.SUCCESSFUL);
                report.setStatusMessage("Pre-ingest process completed successfully with no warnings or errors.");
            }
        }

        //regardless of the state of the ingest, try to collect whatever info possible for the report.

        report.setDataItemsPerCollectionCount(buildDataItemCountToCollectionIdMap(state));

        //get total byte to be archived
        report.setTotalPackageSize(getTotalBytesForIngest(state));

        //get checksum check results: count for each result outcome
        report.setGeneratedChecksumsCount(getGeneratedChecksumsCount(depositId, state));

        report.setContentDetectionTools(getContentDetectionToolInfo(fileAttributeSets));

        report.setFileTypeCount(getFileCountByFormat(fileAttributeSets));
        return report;
    }

    /**
     *
     * @param depositId used to retrieve events relating to a specific deposit.
     * @param state IngestWorkflowState
     * @return an ordered list of events which constitute warning condition for the report.
     */
    private List<DcsEvent> getWarningEvents(String depositId, IngestWorkflowState state) {
        List<DcsEvent> warningEvents = new ArrayList<DcsEvent>();
        java.util.Collection<DcsEvent> failedFormatVerificationEvents =
                state.getEventManager().getEvents(depositId, Package.Events.FORMAT_VERIFICATION_FAILED);

        java.util.Collection<DcsEvent> unsupportedFiles =
                state.getEventManager().getEvents(depositId, Package.Events.UNSUPPORTED_FILE_AGGREGATION);
        warningEvents.addAll(failedFormatVerificationEvents);
        warningEvents.addAll(unsupportedFiles);
        return warningEvents;
    }

    /**
     * Inspect AttributeSets and BusinessObject available in the states return a count of data item per collection
     * in the package.
     * @param state
     * @return
     */
    private Map<String, Integer> buildDataItemCountToCollectionIdMap(IngestWorkflowState state)
            throws ReportServiceException {
        AttributeSetManager asm = state.getAttributeSetManager();
        BusinessObjectManager bom = state.getBusinessObjectManager();

        Set<String> keys = asm.getKeys();
        Set<ParentChildBOPair> parentChildBOPairs = new HashSet<ParentChildBOPair>();
        String currentResourceId;
        AttributeSet attributeSet;

        //Loop through the available attribute set to examine collection to data item relationship.
        for (String asKey : keys) {
            if (asKey.contains(AttributeSetName.ORE_REM_COLLECTION)) {
                attributeSet = asm.getAttributeSet(asKey);
                currentResourceId = getResourceId(attributeSet);
                if (currentResourceId == null) {
                    throw new ReportServiceException("Found an Ore-Rem-Collection AttributeSet with a null resource-id " +
                            "attribute. Identifiers of resources in the package cannot be null.");
                }
                Collection currentCollection = (Collection)bom.get(currentResourceId, Collection.class);
                //*** Looking for children objects: data-items
                List<String> childrenDataItemResourceIds = getRepeatedAttributeValue(attributeSet, Metadata.COLLECTION_AGGREGATES_DATAITEM);
                if (childrenDataItemResourceIds.size() > 0) {
                    // /if there are children data-item resource ids, indiccating that this collection aggregates data-item
                    // in the package
                    for (String resourceId : childrenDataItemResourceIds) {
                        //look up the child data-item in the BusinessObjectManager, so that its businessId can be obtained
                        DataItem childDataItem = (DataItem)bom.get(resourceId, DataItem.class);
                        //Add parent-child pair of business ids to the set.
                        parentChildBOPairs
                                .add(new ParentChildBOPair(currentCollection.getTitle() != null ? currentCollection
                                        .getTitle() : String.format(UNSPECIFIED_COLLECTION_TITLE,
                                        currentCollection.getId()), childDataItem.getId()));
                    }
                } else {
                    //add an empty collection to the parentChildBOPairs
                    parentChildBOPairs.add(new ParentChildBOPair(currentCollection.getId(), EMPTY_STRING));
                }
            } else if (asKey.contains(AttributeSetName.ORE_REM_DATAITEM)) {
                attributeSet = asm.getAttributeSet(asKey);
                currentResourceId = getResourceId(attributeSet);
                if (currentResourceId == null) {
                    throw new ReportServiceException("Found an Ore-Rem-DataItem AttributeSet with a null resource-id " +
                            "attribute. Identifiers of resources in the package cannot be null.");
                }
                DataItem currentDataItem = (DataItem)bom.get(currentResourceId, DataItem.class);

                String containingCollectionRef = getSingleAttributeValue(attributeSet, Metadata.DATA_ITEM_IS_PART_OF_COLLECTION);
                if (containingCollectionRef != null) {
                    Collection containingCollection = (Collection)bom.get(containingCollectionRef, Collection.class);
                    if (containingCollection != null) {
                        parentChildBOPairs.add(
                            new ParentChildBOPair(containingCollection.getTitle() != null ? containingCollection.getTitle() :
                                    String.format(UNSPECIFIED_COLLECTION_TITLE, containingCollection.getId()),
                            currentDataItem.getId()));
                    } else { //containingCollectionResourceId is a reference to existing collection in the system
                        parentChildBOPairs.add(new ParentChildBOPair(containingCollectionRef, currentDataItem.getId()));
                    }
                }
            }
        }

        Map<String, Integer> dataItemCountToCollectionIdMap = new HashMap<String, Integer>();
        for (ParentChildBOPair pair : parentChildBOPairs) {
            if (!dataItemCountToCollectionIdMap.containsKey(pair.getParent())) {
                dataItemCountToCollectionIdMap.put(pair.getParent(), 0);
            }
            if (!pair.getChild().equals(EMPTY_STRING)) {
                dataItemCountToCollectionIdMap.put(pair.getParent(), dataItemCountToCollectionIdMap.get(pair.getParent()) + 1);
            }
        }
        return dataItemCountToCollectionIdMap;
    }

    private Map<String, String> getContentDetectionToolInfo(Set<AttributeSet> fileAttributeSets) {
        Map<String, String> toolNameAndVersionMap = new HashMap<String, String>();
        for (AttributeSet fileAttributeSet : fileAttributeSets) {
            toolNameAndVersionMap.put(getSingleAttributeValue(fileAttributeSet, Metadata.FILE_FORMAT_DETECTION_TOOL_NAME),
                                      getSingleAttributeValue(fileAttributeSet, Metadata.FILE_FORMAT_DETECTION_TOOL_VERSION));
        }
        return toolNameAndVersionMap;
    }

    private Map<String, Integer> getFileCountByFormat(Set<AttributeSet> fileAttributeSets) {
        //TODO: map keyed by format's name. keyed by DcsFormat object would be more reliable. Report consumer can
        //decide what field of DcsFormat to use. But IngestReport doesn't current support mapping keyed on DcsFormat type.
        Map<String, Integer> fileCountByFormat = new HashMap<String, Integer>();

        for (AttributeSet fileAttributeSet : fileAttributeSets) {
            //extract file-formats
            java.util.Collection<Attribute> matchingAttributes =
                    fileAttributeSet.getAttributesByName(Metadata.FILE_FORMAT);
            for (Attribute attribute : matchingAttributes) {
                if (attribute.getType().equals(AttributeValueType.DCS_FORMAT)) {
                    DcsFormat format = DcsFormat.parseDcsFormat(attribute.getValue());
                    // Only add the Pronom format to the report to be displayed or the unknown mime-type
                    // (application/octet-stream)
                    if (format.getSchemeUri().contains("PRONOM")
                            || format.getName().equalsIgnoreCase("application/octet-stream")) {
                        // if an entry for this format does not exist yet
                        if (!fileCountByFormat.containsKey(format.getName())) {
                            // create the new entry
                            fileCountByFormat.put(format.getName(), 0);
                        }
                        // increase count for the format entry.
                        fileCountByFormat.put(format.getName(), fileCountByFormat.get(format.getName()) + 1);
                    }
                }
            }
        }

        return fileCountByFormat;
    }
    /**
     * Used to get singular attribute from the attribute set. Expecting multiple attributes matching the given name.
     * @param attributeSet
     * @param attributeName
     * @return
     */
    private List<String> getRepeatedAttributeValue(AttributeSet attributeSet, String attributeName) {
        List<String> resultAttributeValues = new ArrayList<String>();
        java.util.Collection<Attribute> matchingAttributes  = attributeSet.getAttributesByName(attributeName);
        for (Attribute attribute : matchingAttributes) {
            String blah = attribute.getValue();
            resultAttributeValues.add(blah);
        }
        return resultAttributeValues;
    }

    /**
     * Used to get singular attribute from the attribute set. Expecting only ONE attribute matching the given name.
     * @param attributeSet
     * @param attributeName
     * @return
     */
    private String getSingleAttributeValue(AttributeSet attributeSet, String attributeName) {
        java.util.Collection<Attribute> matchingAttributes  = attributeSet.getAttributesByName(attributeName);
        if ( matchingAttributes != null && matchingAttributes.iterator().hasNext()) {
            return matchingAttributes.iterator().next().getValue();
        }
        return null;
    }

    private String getResourceId(AttributeSet attributeSet) {
        java.util.Collection<Attribute> matchingAttributes ;
        if ( attributeSet.getName().equals(AttributeSetName.ORE_REM_COLLECTION) ) {
            matchingAttributes = attributeSet.getAttributesByName(Metadata.COLLECTION_RESOURCEID);
        } else if ( attributeSet.getName().equals(AttributeSetName.ORE_REM_DATAITEM) ) {
            matchingAttributes = attributeSet.getAttributesByName(Metadata.DATAITEM_RESOURCEID);
        } else {
            throw new ReportServiceException("Not expecting to handle retrieval of resource id for resources other " +
                    "than COLLECTION and DATA_ITEM. AttributeSet given was a " + attributeSet.getName() + " AttributeSet.");
        }

        if (matchingAttributes != null && matchingAttributes.iterator().hasNext()) {
            //expects only ONE resource-id attribute
            return matchingAttributes.iterator().next().getValue();
        }
        return null;
    }

    private long getTotalBytesForIngest(IngestWorkflowState state) {
        long  totalFileSize = 0L;
        Set<String> keySet = state.getAttributeSetManager().getKeys();
        File packageBaseDir =  new File( state.getPackage().getSerialization().getExtractDir(),
                state.getPackage().getSerialization().getBaseDir().getPath());
        File payloadDir = new File(packageBaseDir, "data");

        //FILE attribute set containg the file size information are keyed by file's absolute path
        for (String key : keySet) {
            //checking to may sure key contains path to the payload directory ensure that only sizes of payload files
            // are being considered
            if (key.contains(payloadDir.getPath())) {
                AttributeSet attributeSet = state.getAttributeSetManager().getAttributeSet(key);
                String fileSizeString = getSingleAttributeValue(attributeSet, Metadata.FILE_SIZE);
                try {
                    long thisFilesSize = Long.parseLong(fileSizeString);
                    totalFileSize = totalFileSize + thisFilesSize;
                } catch (NumberFormatException e) {
                    throw new ReportServiceException("Unexpected value for " + Metadata.FILE_SIZE + " attribute. Expected a" +
                            " long value, but was actually \'" + fileSizeString + "\'" );
                }

            }
        }
        return totalFileSize;
    }

    private Map<String, Integer> getGeneratedChecksumsCount(String depositId, IngestWorkflowState state) {
        File packageBaseDir =  new File( state.getPackage().getSerialization().getExtractDir(),
                state.getPackage().getSerialization().getBaseDir().getPath());
        File payloadDir = new File(packageBaseDir, "data");

        java.util.Collection<DcsEvent> fixityCalculatedEvents =
                state.getEventManager().getEvents(depositId, Package.Events.FIXITY_CALCULATED);

        int generatedMd5ChecksumCount = 0;
        int generatedSha1ChecksumCount = 0;
        for (DcsEvent event : fixityCalculatedEvents) {
            if (event.getOutcome().contains(MD5_CHECKSUM_ALG)) {
                //add to md5 checksum count
                generatedMd5ChecksumCount ++;
            } else if (event.getOutcome().contains(SHA1_CHECKSUM_ALG)) {
                generatedSha1ChecksumCount ++;
            }
        }

        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put(MD5_CHECKSUM_ALG, generatedMd5ChecksumCount);
        result.put(SHA1_CHECKSUM_ALG, generatedSha1ChecksumCount);

        return result;
    }

    /**
     * Capture a pair of business ids of {@code BusinessObject}.
     * <p/>
     * {@code parent} string represents the business id of the containing {@code BusinessObject}.
     *
     * <p/>
     * {@code child} string represents the busines id of the contained {@code BusinessObject}.
     */
    private class ParentChildBOPair {


        private String parent;
        private String child;

        public ParentChildBOPair(String parent, String child) {
            this.parent = parent;
            this.child = child;
        }

        public List<String> getParentChildPairAsList() {
            List<String> parentChildPairAsList = new ArrayList<String>();
            parentChildPairAsList.add(this.parent);
            parentChildPairAsList.add(this.child);
            return parentChildPairAsList;
        }

        private String getParent() {
            return parent;
        }

        private String getChild() {
            return child;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParentChildBOPair)) return false;

            ParentChildBOPair that = (ParentChildBOPair) o;

            if (child != null ? !child.equals(that.child) : that.child != null) return false;
            if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + (child != null ? child.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ParentChildBOPair{" +
                    "parent='" + parent + '\'' +
                    ", child='" + child + '\'' +
                    '}';
        }
    }

    public void setIngestReportBuilder (IngestReportBuilder builder) {
        this.builder = builder;
    }

    public IngestReportBuilder getIngestReportBuilder () {
        return builder;
    }

    /**
     * An {@link AttributeMatcher} which matches all AttributeSets with the name {@link AttributeSetName#FILE}.
     */
    static final AttributeMatcher FILE_MATCHER = new AttributeMatcher() {
        @Override
        public boolean matches(String attributeSetName, Attribute candidateAttribute) {
            return attributeSetName.equals(AttributeSetName.FILE);
        }
    };
}
