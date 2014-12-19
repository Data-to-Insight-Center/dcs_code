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
package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.dcs.contentdetection.api.ContentDetectionService;
import org.dataconservancy.dcs.util.ChecksumGeneratorVerifier;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeValueType;
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.model.impl.Pair;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Ingest service to analyze all of the files in a package. 
 * This service will produce the name, size and ingest date for every file in the package. 
 * It will also perform content detection and fixity calculation.
 * <p>
 * Note: This service only handles files it does <em>NOT</em> provide any information for folders.
 * </p> 
 */
public class PackageFileAnalyzer extends BaseIngestService {
    
    ContentDetectionService detectionService;
    ChecksumGeneratorVerifier checksumGenerator;
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void execute(String depositId, IngestWorkflowState state)
            throws StatefulIngestServiceException {
        super.execute(depositId, state);
        analyzePackage(depositId, state);
    }
    
    /**
     * Sets the {@code ContentDetectionService} to use for format detection.
     * @param detectionService
     */
    @Required
    public void setContentDetectionService(ContentDetectionService detectionService) {
        this.detectionService = detectionService;
    }
    
    /**
     * Sets the {@code ChecksumGeneratorVerifier} to use for checksum generation.
     * @param checksumGenerator
     */
    @Required
    public void setChecksumGenerator(ChecksumGeneratorVerifier checksumGenerator) {
        this.checksumGenerator = checksumGenerator;
    }
    
    private void analyzePackage(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {

        org.dataconservancy.packaging.model.Package pkg =  state.getPackage();

        List<File> files = pkg.getSerialization().getFiles();

     
        //Get the attributes of the contentDetectionService 
        Attribute fileFormatDetectionToolName = new AttributeImpl(Metadata.FILE_FORMAT_DETECTION_TOOL_NAME, AttributeValueType.STRING, detectionService.getDetectorName());
        Attribute fileFormatDetectionToolVersion = new AttributeImpl(Metadata.FILE_FORMAT_DETECTION_TOOL_VERSION, AttributeValueType.STRING, detectionService.getDetectorVersion());
        
        //Loop through all the files in the package and calculate the metadata for each one        
        for (File file : files) {

            if (file == null) {
                DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                String stackTrace = "";
                for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                    stackTrace += st + "\n";
                }
                event.setDetail(stackTrace);                           
                event.setOutcome("Failed to calculate file attributes, null file included in package");
                List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                for (File refFile : files) {
                    if (refFile != null) {
                        DcsEntityReference ref = new DcsEntityReference(refFile.getPath());
                        refs.add(ref);
                    }
                }
                event.setTargets(refs);

                state.getEventManager().addEvent(depositId, event);        
                return;
            }
            
            //Folders are handled seperately from files this code only deals with getting the file attributes.
            if (!file.isDirectory()) {
                if (!file.exists()) {
                    DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                    String stackTrace = "";
                    for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                        stackTrace += st + "\n";
                    }
                    event.setDetail(stackTrace);              
                    event.setOutcome("Failed to calculate file attributes, unable to read file: " + file.getPath());
                    List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                    for (File refFile : files) {
                        if (refFile != null) {
                            DcsEntityReference ref = new DcsEntityReference(refFile.getPath());
                            refs.add(ref);
                        }
                    }
                    event.setTargets(refs);
    
                    state.getEventManager().addEvent(depositId, event);        
                    return;
                }
            
                //Get the Attribute set for the given file if one already exists
                boolean existingAttributeSet = true;
                AttributeSet fileAttributeSet = state.getAttributeSetManager().getAttributeSet(file.getPath());
                if (fileAttributeSet == null) {
                    existingAttributeSet = false;
                    fileAttributeSet = new AttributeSetImpl("File");
                }
                Collection<Attribute> attributes = fileAttributeSet.getAttributes();
                
                attributes.add(new AttributeImpl(Metadata.FILE_NAME, AttributeValueType.STRING, file.getName()));
                
                attributes.add(new AttributeImpl(Metadata.FILE_SIZE, AttributeValueType.LONG, String.valueOf(file.length())));
    
                attributes.add(new AttributeImpl(Metadata.FILE_IMPORTED_DATE, AttributeValueType.DATETIME, new DateTime().toString()));
                
                //Run content detection on the file
                attributes.add(fileFormatDetectionToolName);
                attributes.add(fileFormatDetectionToolVersion);
                
                
                List<DcsFormat> formats = detectionService.detectFormats(file);
                for (DcsFormat format : formats) {
                    Attribute formatAttribute = new AttributeImpl(Metadata.FILE_FORMAT, AttributeValueType.DCS_FORMAT, format.toString());
                    attributes.add(formatAttribute);
                    
                    //Generate event for each format detection
                    DcsEvent event = state.getEventManager().newEvent(Package.Events.CHARACTERIZATION_FORMAT);
                    event.setDetail("Calculated format for: " + file.getPath());
                    event.setOutcome(format.getSchemeUri() + " " + format.getFormat());
                    DcsEntityReference ref = new DcsEntityReference(file.getPath());
                    List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                    refs.add(ref);
                    event.setTargets(refs);
                    state.getEventManager().addEvent(depositId, event);
                }
                
                FileInputStream fis;
                FileInputStream fisTwo;
                try {
                    fis = new FileInputStream(file);
                    String md5Checksum = checksumGenerator.generateMD5checksum(fis);
                    
                    //Create an event for the checksum generation
                    DcsEvent event = state.getEventManager().newEvent(Package.Events.FIXITY_CALCULATED);
                    event.setDetail("Checksum calculated for: " + file.getPath());
                    event.setOutcome("md5 " + md5Checksum);
                    DcsEntityReference ref = new DcsEntityReference(file.getPath());
                    List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                    refs.add(ref);
                    event.setTargets(refs);
                    state.getEventManager().addEvent(depositId, event);
                    fis.close();
                    
                    fisTwo = new FileInputStream(file);                    
                    String sha1Checksum = checksumGenerator.generateSHA1checksum(fisTwo);
                    
                    //Create an event for the checksum generation
                    DcsEvent shaEvent = state.getEventManager().newEvent(Package.Events.FIXITY_CALCULATED);
                    shaEvent.setDetail("Checksum calculated for: " + file.getPath());
                    shaEvent.setOutcome("sha1 " + sha1Checksum);
                    DcsEntityReference shaRef = new DcsEntityReference(file.getPath());
                    List<DcsEntityReference> shaRefs = new ArrayList<DcsEntityReference>();
                    shaRefs.add(shaRef);
                    shaEvent.setTargets(shaRefs);
                    state.getEventManager().addEvent(depositId, shaEvent);
                    fisTwo.close();
                    
                    Checksum fileChecksum = new ChecksumImpl(Checksum.MD5, md5Checksum);
                    Pair<String, Checksum> pair = new Pair<String, Checksum>(file.getPath(), fileChecksum);
                    attributes.add(new AttributeImpl("Calculated-Checksum", AttributeValueType.PAIR, pair.toString()));
                    
                    Checksum shaChecksum = new ChecksumImpl(Checksum.SHA1, sha1Checksum);
                    Pair<String, Checksum> shaEntry = new Pair<String, Checksum>(file.getPath(), shaChecksum);
                    attributes.add(new AttributeImpl("Calculated-Checksum", AttributeValueType.PAIR, shaEntry.toString()));
                } catch (Exception e) {
                    DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                    event.setDetail("Unable to read package file: " + file.getPath() + " " + e.getStackTrace());
                    event.setOutcome("Failed to calculate file attributes");
                    List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                    for (File refFile : files) {
                        DcsEntityReference ref = new DcsEntityReference(refFile.getPath());
                        refs.add(ref);
                    }
                    event.setTargets(refs);
    
                    state.getEventManager().addEvent(depositId, event);        
                } 
                
                //Create an event to show that metadata has been generated for the file
                DcsEvent event = state.getEventManager().newEvent(org.dataconservancy.packaging.ingest.api.Package.Events.METADATA_GENERATED);
                event.setDetail("Metadata Generated for: " + file.getPath());
                event.setOutcome(fileAttributeSet.getName() + " " + file.getPath());
                DcsEntityReference ref = new DcsEntityReference(depositId);
                List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                refs.add(ref);
                event.setTargets(refs);
                state.getEventManager().addEvent(depositId, event);
                
                if (!existingAttributeSet)
                {
                    state.getAttributeSetManager().addAttributeSet(file.getPath(), fileAttributeSet);
                } else {
                    state.getAttributeSetManager().updateAttributeSet(file.getPath(), fileAttributeSet);
                }
            }
        }
    }
}