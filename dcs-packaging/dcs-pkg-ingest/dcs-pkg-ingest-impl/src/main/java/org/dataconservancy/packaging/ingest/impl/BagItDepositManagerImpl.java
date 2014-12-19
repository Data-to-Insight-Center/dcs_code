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
package org.dataconservancy.packaging.ingest.impl;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.dcs.contentdetection.api.ContentDetectionService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.packaging.ingest.api.*;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.shared.BagUtil;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.ui.exceptions.UnpackException;
import org.dataconservancy.ui.util.PackageExtractor;
import org.dataconservancy.ui.util.PackageSelector;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 */
public class BagItDepositManagerImpl implements ResumableDepositManager, Cancelable {

    private static final String DEPOSIT_SUMMARY_NOTSTARTED = "Ingest %s has not started as of %s";

    private static final String DEPOSIT_SUMMARY_COMPLETE = "Ingest %s completed %s at %s";

    private static final String DEPOSIT_SUMMARY_INPROGRESS = "Ingest %s in progress since %s";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private StatefulBootstrap bootstrap;

    private PackageSelector packageSelector;
    
    private ContentDetectionService contentDetectionService;

    private ReportService preIngestReportService;

    private IdService idService;

    private IngestWorkflowStateFactory stateFactory;

    private DepositStateManager stateManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public DepositInfo resume(String depositId) {
        final IngestWorkflowState state = stateManager.get(depositId);
        if (state == null) {
            // nothing to do
            return null;
        }

        if (state.isCancelled()) {
            // We don't resume cancelled ingests.
            return getDepositInfo(depositId);
        }

        bootstrap.startIngest(depositId, state);
        return getDepositInfo(depositId);
    }
    
    @Override
    public DepositInfo deposit(InputStream content, String contentType, String packaging, Map<String, String> metadata)
            throws PackageException {

        if (!Package.Types.BAGIT_DCS_10.equals(packaging)) {
            throw new PackageException("Unknown package type '" + packaging + "'.  This implementation can only handle " +
                    "packages of type " + Package.Types.BAGIT_DCS_10);
        }

        final String depositId = idService.create(IdTypes.DEPOSIT).getUid();
        final IngestWorkflowState state = stateFactory.newInstance();
        addDepositEvent(metadata, depositId, state);

        //Get the user from the metadata
        if (metadata.containsKey(Http.Header.X_DCS_AUTHENTICATED_USER)) {
            final String userId = metadata.get(Http.Header.X_DCS_AUTHENTICATED_USER);
            state.setIngestUserId(userId);
        }
        
        final PackageExtractor packageExtractor = packageSelector.selectPackageExtractor(content, metadata);
        
        if (packageExtractor == null) {
            final String msg = "Error unpacking package (depositId: " + depositId + "): No extractor available.";
            log.error(msg);
            throw new PackageException(msg);
        }

        final File fileName = new File(filenameFromHeader(metadata));

        try {
            List<File> files = packageExtractor.getFilesFromPackageStream(
                    BagUtil.sanitizeStringForFile(depositId).getPath(), filenameFromHeader(metadata), content);

            final File basePackageDir = BagUtil.deriveBaseDirectory(depositId, fileName);

            //If only one file is extracted check if the inner file is also an extractable package
            if (files.size() == 1) {
                Map<String, String> secondaryMetadata = new HashMap<String, String>();
                File innerFile = files.get(0);

                List<DcsFormat> formats = contentDetectionService.detectFormats(innerFile);
                for (DcsFormat format : formats) {
                    //Only set mime type if we have a known type this will 
                    //prevent application/unknown from going in if we have multiple types found.
                    if (format.getFormat().equalsIgnoreCase(Http.MimeType.APPLICATION_XGZIP) ||
                            format.getFormat().equalsIgnoreCase(Http.MimeType.APPLICATION_ZIP) ||
                            format.getFormat().equalsIgnoreCase(Http.MimeType.APPLICATION_XTAR)) {
                        secondaryMetadata.put(Http.Header.CONTENT_TYPE, format.getFormat());
                        break;
                    }
                }
                FileInputStream innerFileStream = null;
                try {
                    innerFileStream = new FileInputStream(innerFile);
                } catch (FileNotFoundException e) {
                    final String msg = "Error unpacking package (depositId: " + depositId + "): " + e.getMessage();
                    log.error(msg, e);
                    throw new PackageException(msg, e);
                }

                secondaryMetadata.put(Http.Header.CONTENT_DISPOSITION, "attachment;  " +
                        "filename=\"" + innerFile.getName() + "\"");

                final PackageExtractor secondaryExtractor =
                        packageSelector.selectPackageExtractor(innerFileStream, secondaryMetadata);
                // If the inner file has an extractor then replace the file list with the files extracted
                // from the inner extractor.

                if (secondaryExtractor != null) {
                    files = secondaryExtractor.getFilesFromPackageStream(
                            BagUtil.sanitizeStringForFile(depositId).getPath(), innerFile.getName(), innerFileStream);
                }
            }

            state.getPackage().getSerialization().setExtractDir(new File(packageExtractor.getExtractDirectory()));
            state.getPackage().getSerialization().setBaseDir(basePackageDir);

            for (File packageFile : files) {
                state.getPackage().getSerialization().addFile(packageFile);
            }

            addExtractionEvent(depositId, filenameFromHeader(metadata), files, state);
        } catch (UnpackException e) {
            final String msg = "Error unpacking package (depositId: " + depositId + "): " + e.getMessage();
            log.error(msg, e);
            BagUtil.deleteDepositDirectory(depositId, state.getPackage());
            throw new PackageException(msg, e);
        } catch (Throwable t) {
            BagUtil.deleteDepositDirectory(depositId, state.getPackage());
            throw new PackageException(t);
        }

        startIngest(depositId, state);

        return getDepositInfo(depositId);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:<br/>
     * <ul>
     *     <li>Determines if the deposit is complete by looking for a ingest success or failure event</li>
     *     <li>Determines if the deposit is successful by looking for a ingest sucess event</li>
     *     <li>Creates short summary of the current status</li>
     *     <li>Composes a status document which is a textual representation of the events that have been emitted so
     *         far</li>
     *     <li>Currently the deposit content document will be null</li>
     * </ul>
     * <strong>TODO: DC-1447</strong><br/>
     * <ul>
     *     <li>Building the deposit status needs to be more intelligent than just a simple dump of the events</li>
     *     <li>We need to decide what the deposit content document will be.  Perhaps a representation (DCP?) of
     *         the business objects in the package</li>
     * </ul>
     *
     *
     * @param depositId the deposit identifier
     * @return
     */
    @Override
    public DepositInfo getDepositInfo(String depositId) {
        final IngestWorkflowState state = stateManager.get(depositId);

        if (state == null) {
            return null;
        }

        final BagDepositInfo depositInfo = new BagDepositInfo();
        final EventManager em = state.getEventManager();

        depositInfo.depositId = depositId;
        depositInfo.managerId = getManagerID();
        depositInfo.completed = (em.getEventByType(depositId, Package.Events.INGEST_SUCCESS) != null ||
                em.getEventByType(depositId, Package.Events.INGEST_FAIL) != null);
        depositInfo.successful = em.getEventByType(depositId, Package.Events.INGEST_SUCCESS) != null;

        if (depositInfo.completed) {
            depositInfo.summary = String.format(DEPOSIT_SUMMARY_COMPLETE, depositId,
                    ((depositInfo.successful) ? "successfully" : "with errors"),
                    ((depositInfo.successful) ? em.getEventByType(depositId, Package.Events.INGEST_SUCCESS).getDate() :
                            em.getEventByType(depositId, Package.Events.INGEST_FAIL).getDate()));
        } else if (em.getEventByType(depositId, Package.Events.DEPOSIT) != null) {
            depositInfo.summary = String.format(DEPOSIT_SUMMARY_INPROGRESS, depositId,
                    em.getEventByType(depositId, Package.Events.DEPOSIT).getDate());
        } else {
            depositInfo.summary = String.format(DEPOSIT_SUMMARY_NOTSTARTED, depositId,
                    DateUtility.toIso8601(System.currentTimeMillis()));
        }

        depositInfo.depositDocument = null;

        boolean isInPausedState = false;
        IngestPhase currentIngestPhase = state.getIngestPhase();
        if (currentIngestPhase != null && currentIngestPhase.getPauseIngest()) {
            Collection<DcsEvent> phaseCompleteEvents = em.getEvents(depositId, Package.Events.INGEST_PHASE_COMPLETE);
            for (DcsEvent event : phaseCompleteEvents) {
                if (event.getOutcome().trim().equals(currentIngestPhase.getPhaseNumber().toString())) {
                    isInPausedState = true;
                    break;
                }
            }
        }

        if (isInPausedState) {
            depositInfo.depositStatus = preIngestReportService.produceReport(depositId, state);
        } else {
            final HierarchicalPrettyPrinter depositStatus = new HierarchicalPrettyPrinter();
            buildDepositStatus(depositId, em, depositStatus);

            // TODO DC-1447: this DepositDocument will need to be refactored later when we are doing more complex rendering of
            // deposit status (such as highlighting warnings, for example)
            depositInfo.depositStatus = new DepositDocument() {

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(depositStatus.toString().getBytes());
                }

                @Override
                public String getMimeType() {
                    return Http.MimeType.TEXT_PLAIN;
                }

                @Override
                public long getLastModified() {
                    TreeSet<DcsEvent> sortedEvents = new TreeSet<DcsEvent>(new DcsEventComparator());
                    Collection<DcsEvent> events = em.getEvents(depositInfo.depositId);
                    if (events != null && events.size() > 0) {
                        sortedEvents.addAll(events);
                        return DateUtility.parseDate(sortedEvents.last().getDate());
                    } else {
                        return System.currentTimeMillis();
                    }
                }

                @Override
                public Map<String, String> getMetadata() {
                    Map<String, String> md = new HashMap<String, String>();
                    md.put(Http.Header.LAST_MODIFIED, DateUtility.toRfc822(getLastModified()));
                    md.put(Http.Header.CONTENT_TYPE, getMimeType());
                    md.put(Http.Header.CONTENT_LENGTH, String.valueOf(depositStatus.toString().getBytes().length));
                    return md;
                }
            };
        }
        return depositInfo;
    }

    @Override
    public void cancel(String depositId) {
        final IngestWorkflowState state = stateManager.get(depositId);
        if (state == null) {
            // nothing we can do
            return;
        }

        // Remove _all_ files associated with the deposit
        BagUtil.deleteDepositDirectory(depositId, state.getPackage());

        // Set the cancellation flag on the ingest state.  Cancelled ingests cannot be resumed.
        state.setCancelled();
    }

    @Override
    public String getManagerID() {
        return this.getClass().getName();
    }

    public StatefulBootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(StatefulBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public IdService getIdService() {
        return idService;
    }

    public void setIdService(IdService idService) {
        this.idService = idService;
    }

    public PackageSelector getPackageSelector() {
        return packageSelector;
    }

    public void setPackageSelector(PackageSelector packageSelector) {
        this.packageSelector = packageSelector;
    }

    public ContentDetectionService getContentDetectionService() {
        return contentDetectionService;
    }
    
    public void setContentDetectionService(ContentDetectionService detectionService) {
        this.contentDetectionService = detectionService;
    }

    public void setPreIngestReportService(ReportService reportService) {
        this.preIngestReportService = reportService;
    }

    public IngestWorkflowStateFactory getStateFactory() {
        return stateFactory;
    }

    public void setStateFactory(IngestWorkflowStateFactory stateFactory) {
        this.stateFactory = stateFactory;
    }

    public DepositStateManager getStateManager() {
        return stateManager;
    }

    public void setStateManager(DepositStateManager stateManager) {
        this.stateManager = stateManager;
    }

    /**
     * Populates the {@link #stateManager} and starts the ingest.
     *
     * @param depositId the deposit identifier representing the deposit workflow being started
     * @param state the ingest state
     */
    void startIngest(String depositId, IngestWorkflowState state) {
        stateManager.put(depositId, state);
        bootstrap.startIngest(depositId, state);
    }

    /**
     * Looks for the {@link Http.Header#CONTENT_DISPOSITION} HTTP header in {@code metadata}, and parses it for a filename.  If
     * it isn't found, or cannot be parsed, {@code null} is returned.
     *
     * @param metadata map of metadata using HTTP header conventions
     * @return the filename, or {@code null}
     */
    String filenameFromHeader(Map<String, String> metadata) {
        if (metadata.containsKey(Http.Header.CONTENT_DISPOSITION) &&
                metadata.get(Http.Header.CONTENT_DISPOSITION).contains("filename=\"")) {
            String filenameParam = metadata.get(Http.Header.CONTENT_DISPOSITION).split("filename=\"")[1];
            String tmp = filenameParam.substring(0, filenameParam.indexOf('"'));
            tmp = tmp.substring(0, filenameParam.indexOf('"'));
            return tmp;
        }

        return null;
    }

    /**
     * Creates a {@link org.dataconservancy.packaging.ingest.api.Package.Events#FILE_EXTRACTION} event, representing all of the files that were extracted from
     * the Package, and adds it to the ingest state.
     *
     * @param depositId the deposit (transaction) identifier
     * @param archiveFileName the original name of the package file that was uploaded
     * @param extractedFiles a list of files extracted from the package
     * @param state the ingest state
     */
    void addExtractionEvent(String depositId, String archiveFileName, List<File> extractedFiles,
                                    IngestWorkflowState state) {
        final PackageSerialization ser = state.getPackage().getSerialization();
        DcsEvent extractionEvent = state.getEventManager().newEvent(Package.Events.FILE_EXTRACTION);
        extractionEvent.setOutcome(String.valueOf(extractedFiles.size()));
        extractionEvent.setDetail("Extracted " + extractedFiles.size() + " files from " + archiveFileName +
                " to " + new File(ser.getExtractDir(), ser.getBaseDir().getPath()).getAbsolutePath() +
                "(" + depositId + ")");
        for (File f : extractedFiles) {
            extractionEvent.addTargets(new DcsEntityReference(f.getName()));
        }
        state.getEventManager().addEvent(depositId, extractionEvent);
    }

    /**
     * Creates a {@link org.dataconservancy.packaging.ingest.api.Package.Events#DEPOSIT} event, representing the receipt of the Package, and adds it to the
     * ingest state.
     *
     * @param metadata map of metadata using HTTP header conventions
     * @param depositId the deposit (transaction) identifier
     * @param state the ingest state
     */
    void addDepositEvent(Map<String, String> metadata, String depositId, IngestWorkflowState state) {
        DcsEvent depositEvent = state.getEventManager().newEvent(org.dataconservancy.packaging.ingest.api.Package.Events.DEPOSIT);
        depositEvent.setOutcome(depositId);
        depositEvent.addTargets(new DcsEntityReference(filenameFromHeader(metadata)));
        depositEvent.setDetail("Accepted package named " + filenameFromHeader(metadata) + " on " +
                depositEvent.getDate() + " deposited by " + metadata.get(Http.Header.X_DCS_AUTHENTICATED_USER));
        state.getEventManager().addEvent(depositId, depositEvent);
    }

    /**
     * Obtains the DcsEvents from the EventManager, sorts them in chronological order, and
     * places a textual representation of those events into the supplied HierarchicalPrettyPrinter.
     *
     * @param id the depositId of the ingest being reported on
     * @param em the EventManager containing the DcsEvents used to build the status document
     * @param hpp the HierarchicalPrettyPrinter to be populated with the text representation of the events
     */
    void buildDepositStatus(String id, EventManager em, HierarchicalPrettyPrinter hpp) {
        final TreeSet<DcsEvent> sortedEvents = new TreeSet<DcsEvent>(new DcsEventComparator());

        final Collection<DcsEvent> events = em.getEvents(id, null);

        if (events == null || events.isEmpty()) {
            return;
        }

        sortedEvents.addAll(events);

        for (DcsEvent e : sortedEvents) {
            e.toString(hpp);
        }
    }

    static class DcsEventComparator implements Comparator<DcsEvent> {
        @Override
        public int compare(DcsEvent one, DcsEvent two) {

            if (one == two) {
                return 0;
            }

            if (one == null) {
                return -1;
            }

            if (two == null) {
                return 1;
            }

            if (one.equals(two)) {
                return 0;
            }

            // Sort by date, first.
            DateTime oneDt = null;
            try {
                oneDt = DateUtility.parseDateString(one.getDate());
            } catch (Exception e) {
                // ignore
            }

            DateTime twoDt = null;
            try {
                twoDt = DateUtility.parseDateString(two.getDate());
            } catch (Exception e) {
                // ignore
            }

            if (oneDt == null) {
                return -1;
            }

            if (twoDt == null) {
                return 1;
            }

            if (!oneDt.equals(twoDt)) {
                return oneDt.compareTo(twoDt);
            }

            // Then id
            return one.getId().compareTo(two.getId());
        }
    }

    class BagDepositInfo implements DepositInfo {

        private DepositDocument depositDocument;
        private DepositDocument depositStatus;
        private String depositId;
        private String managerId;
        private String summary;
        private boolean completed;
        private boolean successful;
        private Map<String, String> metadata;

        @Override
        public DepositDocument getDepositContent() {
            return depositDocument;
        }

        @Override
        public String getDepositID() {
            return depositId;
        }

        @Override
        public String getManagerID() {
            return managerId;
        }

        @Override
        public DepositDocument getDepositStatus() {
            return depositStatus;
        }

        @Override
        public String getSummary() {
            return summary;
        }

        @Override
        public boolean hasCompleted() {
            return completed;
        }

        @Override
        public boolean isSuccessful() {
            return successful;
        }

        @Override
        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
}
