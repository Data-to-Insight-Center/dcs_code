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

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Metadata;

/**
 * This service checks that both subject and predicate exist in the bag for all relationships specified in the ore-rem. Note that this only handles
 * objects contained in the ORE-REM, objects that already exist in the archive are resolved in a seperate service. 
 */
public class InternalReferenceChecker extends BaseReferenceChecker {  

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);
        
        
        if (!checkPackageReferences(depositId, state)) {
            return;
        }
        
        if (!checkProjectReferences(depositId, state)) {
            return;
        }
        
        if (!checkCollectionReferences(depositId, state)) {
            return;
        }
        
        if (!checkDataItemReferences(depositId, state)) {
            return;
        }
        
        if (!checkFileReferences(depositId, state)) {            
            return;
        }
        
        Set<AttributeSet> fileAttributeSets = state.getAttributeSetManager().matches(AttributeSetName.ORE_REM_FILE,
                                                                                     new AttributeImpl(null, null, null));
        for (AttributeSet file : fileAttributeSets) { 
            Collection<Attribute> filePathAttributes = file.getAttributesByName(Metadata.FILE_PATH);
            File payloadFileBaseDir =  new File( state.getPackage().getSerialization().getExtractDir(),
                                                 state.getPackage().getSerialization().getBaseDir().getPath()).getParentFile();
            //There should only ever be one of these but if there are more than one we'll check them all since that would be a problem.
            for (Attribute pathAttribute : filePathAttributes) {
                try {
                    String localFilePath = new URI(pathAttribute.getValue()).getPath();
                    
                    File fileAbsolutePath = new File(payloadFileBaseDir, localFilePath);
                    if (!fileAbsolutePath.exists()) { 
                        DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                        String stackTrace = "";
                        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                        for (int i = 1; i < stackTraceElements.length; i++ ) {
                            stackTrace += stackTraceElements[i] + "\n";
                        }
                        event.setDetail(stackTrace);
                        event.setOutcome("ORE REM File is missing: " + fileAbsolutePath.getPath());
                        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                        DcsEntityReference ref = new DcsEntityReference(fileAbsolutePath.getPath());
                        refs.add(ref);
                        
                        event.setTargets(refs);
                        
                        state.getEventManager().addEvent(depositId, event);
                        return;
                    }
                } catch (URISyntaxException e) {
                    throw new StatefulIngestServiceException(e);
                }
            }
        }
    }

    @Override
    protected boolean checkReferenceExists(String depositId, IngestWorkflowState state, String reference, String expectedReferenceType) {
        if (!reference.startsWith("http:")) {
            if (expectedReferenceType.equalsIgnoreCase("collection|project")) {
                String key = getAttributeNamePrefixByType("collection") + "_" + reference;
                if (!state.getAttributeSetManager().contains(key)) {
                    key = getAttributeNamePrefixByType("project") + "_" + reference;
                    if (!state.getAttributeSetManager().contains(key)) {
                        DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                        String stackTrace = "";
                        for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                            stackTrace += st + "\n";
                        }
                        event.setDetail(stackTrace);
                        event.setOutcome("DataItem Missing internal reference: " + reference);
                        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                        DcsEntityReference ref = new DcsEntityReference(reference);
                        refs.add(ref);
                        
                        event.setTargets(refs);
                        
                        state.getEventManager().addEvent(depositId, event);
                        return false;
                    }                        
                }
            }
            else
            {
                String key = getAttributeNamePrefixByType(expectedReferenceType) + "_" + reference;
                if (!state.getAttributeSetManager().contains(key)) {
                    DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                    String stackTrace = "";
                    for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                        stackTrace += st + "\n";
                    }
                    event.setDetail(stackTrace);
                    event.setOutcome("DataItem Missing internal reference: " + reference);
                    List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                    DcsEntityReference ref = new DcsEntityReference(reference);
                    refs.add(ref);
                    
                    event.setTargets(refs);
                    
                    state.getEventManager().addEvent(depositId, event);
                    return false;
                }
            }
        }
        
        return true;
    }
    

    @Override
    protected boolean checkMetadataReferenceExists(String depositId,
                                                   IngestWorkflowState state,
                                                   String reference) {
        if (!reference.startsWith("http:")) {
            String key = getAttributeNamePrefixByType("project") + "_" + reference;
            if (!state.getAttributeSetManager().contains(key)) {
                String collectionKey = getAttributeNamePrefixByType("collection") + "_" + reference;
                if (!state.getAttributeSetManager().contains(collectionKey)) {
                    String dataItemKey = getAttributeNamePrefixByType("dataItem") + "_" + reference;
                    if (!state.getAttributeSetManager().contains(dataItemKey)) {
                        DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                        String stackTrace = "";
                        for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                            stackTrace += st + "\n";
                        }
                        event.setDetail(stackTrace);
                        event.setOutcome("DataItem Missing internal reference: " + reference);
                        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                        DcsEntityReference ref = new DcsEntityReference(reference);
                        refs.add(ref);
                        
                        event.setTargets(refs);
                        
                        state.getEventManager().addEvent(depositId, event);
                        return false;                        
                    }
                }
            }
        }
        
        return true;
    }
    
    private String getAttributeNamePrefixByType(String type) {
        String prefix = "";
        
        if (type.equalsIgnoreCase("project")) {
            prefix = AttributeSetName.ORE_REM_PROJECT;
        } else if (type.equalsIgnoreCase("package")) {
            prefix = AttributeSetName.ORE_REM_PACKAGE;
        } else if (type.equalsIgnoreCase("collection")) {
            prefix = AttributeSetName.ORE_REM_COLLECTION;
        } else if (type.equalsIgnoreCase("dataItem")) {
            prefix = AttributeSetName.ORE_REM_DATAITEM;
        } else if (type.equalsIgnoreCase("file")) {
            prefix = AttributeSetName.ORE_REM_FILE;
        }
        
        return prefix;
        	
    }

   
    
}