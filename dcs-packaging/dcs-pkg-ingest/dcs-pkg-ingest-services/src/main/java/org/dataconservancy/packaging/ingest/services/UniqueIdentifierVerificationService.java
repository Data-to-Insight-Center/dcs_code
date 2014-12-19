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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

/**
 * This ingest service performs checks to ensure that if dcterms:identifiers have been specified in the rem that all of the identifiers are unique.
 */
public class UniqueIdentifierVerificationService extends BaseIngestService {
   

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);
        
        Set<String> ids = new HashSet<String>();
        
        if (!visitProjects(depositId, state, ids)) {
            return;
        }
        
        if (!visitCollections(depositId, state, ids)) {
            return;
        }
        
        if (!visitDataItems(depositId, state, ids)) {
            return;
        }
        
        if (!visitFiles(depositId, state, ids)) {
            return;
        }
    }
    
    private boolean visitProjects(String depositId, IngestWorkflowState state, Set<String> ids) {
       return findAllAttributes(depositId, state, "Project-Identifier", ids);
    }
    
    private boolean visitCollections(String depositId, IngestWorkflowState state, Set<String> ids) {
        return findAllAttributes(depositId, state, "Collection-Identifier", ids);
    }
    
    private boolean visitDataItems(String depositId, IngestWorkflowState state, Set<String> ids) {
        return findAllAttributes(depositId, state, "DataItem-Identifier", ids);
    }
    
    private boolean visitFiles(String depositId, IngestWorkflowState state, Set<String> ids) {
        return findAllAttributes(depositId, state, "File-Identifier", ids);
    }
    
    private boolean findAllAttributes(String depositId, IngestWorkflowState state, String attributeName, Set<String> ids) {
        Attribute attributeToMatch = new AttributeImpl(attributeName, null, null);
        Set<AttributeSet> matchingAttributeSets = state.getAttributeSetManager().matches(attributeToMatch);
        for (AttributeSet attributeSet : matchingAttributeSets) {            
            for (Attribute matchingAttribute : attributeSet.getAttributes()) {
                if (matchingAttribute.getName().equalsIgnoreCase(attributeName)) {
                    if (!ids.add(matchingAttribute.getValue())) {
                        DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                        String stackTrace = "";
                        for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                            stackTrace += st + "\n";
                        }
                        event.setDetail(stackTrace);                
                        event.setOutcome("Duplicate Identifiers were found: "  + matchingAttribute.getValue());
                        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                        DcsEntityReference ref = new DcsEntityReference(matchingAttribute.getValue());
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
}