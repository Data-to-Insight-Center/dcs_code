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
import java.util.List;

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

public class RemoveUnsupportedAggregationsService extends BaseIngestService {
    
    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        Attribute matchingAttribute = new AttributeImpl(Metadata.PROJECT_AGGREGATES_FILE, null, null);
        
        //Loop through all of the attribute sets that contain a project aggregating a file since we don't currently support this behavior.
        for (AttributeSet projectAggregatingFileSets : state.getAttributeSetManager().matches(AttributeSetName.ORE_REM_PROJECT, matchingAttribute)) {
            //Now loop through all the files aggregated by the project, and remove the aggregation attribute and update the project attribute set in the manager.
            for (Attribute aggregatedFileAttribute : projectAggregatingFileSets.getAttributesByName(Metadata.PROJECT_AGGREGATES_FILE)) {
                //Remove the file attribute set from the project.
                projectAggregatingFileSets.getAttributes().remove(aggregatedFileAttribute);
                
                //Form the key and update the project.
                if (projectAggregatingFileSets.getAttributesByName(Metadata.PROJECT_RESOURCEID).iterator().hasNext()) {
                    String projectKey = AttributeSetName.ORE_REM_PROJECT + "_" + projectAggregatingFileSets.getAttributesByName(Metadata.PROJECT_RESOURCEID).iterator().next();
                    state.getAttributeSetManager().updateAttributeSet(projectKey, projectAggregatingFileSets);
                } else {
                    throw new StatefulIngestServiceException("Failed to find project resource id, to remove file aggregation");
                }
                
                //Now look for the attribute sets associated with the file.
                Attribute fileResourceAttribute = new AttributeImpl(Metadata.FILE_RESOURCEID, null, aggregatedFileAttribute.getValue());
                
                //There should only be one match but in case we have more attribute sets in the future loop through them all.
                for (AttributeSet fileAttributeSet : state.getAttributeSetManager().matches(AttributeSetName.ORE_REM_FILE, fileResourceAttribute)) {
                    //Calculate the absolute path of the file and remove the bag file attribute set.
                    String absoluteFilePath = getFilePath(state, fileAttributeSet);
                    state.getAttributeSetManager().removeAttributeSet(absoluteFilePath);
                    
                    //Remove the ore rem attribute set for the file
                    String fileKey = AttributeSetName.ORE_REM_FILE + "_" + aggregatedFileAttribute.getValue();
                    state.getAttributeSetManager().removeAttributeSet(fileKey);
                }
                
                DcsEvent event = state.getEventManager().newEvent(Package.Events.UNSUPPORTED_FILE_AGGREGATION);
                event.setDetail("Project aggregating a file isn't currently supported by package ingest");
                event.setOutcome(aggregatedFileAttribute.getValue() + " has been removed from the package.");
                DcsEntityReference ref = new DcsEntityReference(aggregatedFileAttribute.getValue());
                List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                refs.add(ref);
                event.setTargets(refs);
                state.getEventManager().addEvent(depositId, event);
            }
        }
    }
    
    private String getFilePath(IngestWorkflowState state, AttributeSet fileAttributeSet) throws StatefulIngestServiceException {
        String fileAbsolutePath = "";

        if (fileAttributeSet.getAttributesByName(Metadata.FILE_RESOURCEID).iterator().hasNext()) {
            //There should only be ONE file-resource id attribute in the AttributeSet.
            Attribute filePathAttribute = fileAttributeSet.getAttributesByName(Metadata.FILE_RESOURCEID).iterator().next();
            //retrieve the the decoded file-path value from the local file-uri
            try {
                String relativeFilePath = new URI(filePathAttribute.getValue()).getPath();
                File payloadFileBaseDir =  new File( state.getPackage().getSerialization().getExtractDir(),
                                                     state.getPackage().getSerialization().getBaseDir().getPath())
                                                     .getParentFile();
                File payloadFile = new File(payloadFileBaseDir, relativeFilePath);
                fileAbsolutePath = payloadFile.getPath();
            } catch (URISyntaxException e) {
                throw new StatefulIngestServiceException("Error generating file uri: " + filePathAttribute.getValue(), e);
            }
        }
        
        return fileAbsolutePath;
    }
    
}