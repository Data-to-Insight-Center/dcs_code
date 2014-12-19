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
package org.dataconservancy.packaging.ingest.services;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Project;

/**
 * BusinessObjectValidator takes all the built businessObjects from the {@code BusinessObjectManager} and ensures they
 * have the minimum requirements for that {@code BusinessObject}.
 */
public class BusinessObjectValidator extends BaseIngestService {
    
    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);
        
        BusinessObjectManager businessObjectManager = state.getBusinessObjectManager();
        List<DcsEntityReference> references = checkBusinessObjectsValidity(businessObjectManager);
        
        if (references.size() > 0) {
            // Emit event that indicates built business objects are valid.
            DcsEvent event = state.getEventManager().newEvent(Package.Events.BUSINESS_OBJECTS_VALIDATED);
            event.setDetail("Successfully validated the built business objects.");
            event.setOutcome("Successfully validated a total number of: " + references.size() + " BusinessObjects.");
            event.setTargets(references);
            state.getEventManager().addEvent(depositId, event);
        }
        
    }
    
    private List<DcsEntityReference> checkBusinessObjectsValidity(BusinessObjectManager businessObjectManager)
            throws StatefulIngestServiceException {
        List<DcsEntityReference> references = new ArrayList<DcsEntityReference>();
        // Check Projects
        for (Project project : businessObjectManager.getInstancesOf(Project.class)) {
            if (project.getName() == null || project.getName().length() == 0) {
                throw new StatefulIngestServiceException("Project doesn't have a valid name.");
            }
            else if (project.getDescription() == null || project.getDescription().length() == 0) {
                throw new StatefulIngestServiceException("Project " + project.getName() + " doesn't have a valid description.");
            }
            else if (project.getFundingEntity() == null || project.getFundingEntity().length() == 0) {
                throw new StatefulIngestServiceException("Project " + project.getName() + " doesn't have a valid funding entity.");
            }
            // TODO: Should we also check that the provided date is well formatted?
            else if (project.getStartDate() == null) {
                throw new StatefulIngestServiceException("Project " + project.getName() + " doesn't have a valid start date.");
            }
            // TODO: Should we also check that the provided date is well formatted?
            else if (project.getEndDate() == null) {
                throw new StatefulIngestServiceException("Project " + project.getName() + " doesn't have a valid end date.");
            }
            else if (project.getNumbers() == null || project.getNumbers().size() == 0) {
                throw new StatefulIngestServiceException("Project " + project.getName() + " doesn't have a valid award number.");
            }
            references.add(new DcsEntityReference(project.getId()));
        }
        
        // Check Collections
        for (Collection collection : businessObjectManager.getInstancesOf(Collection.class)) {
            if (collection.getTitle() == null || collection.getTitle().length() == 0) {
                throw new StatefulIngestServiceException("Collection doesn't have a valid title.");
            }
            if (collection.getSummary() == null || collection.getSummary().length() == 0) {
                throw new StatefulIngestServiceException("Collection " + collection.getTitle() + " doesn't have a valid summary.");
            }
            if (collection.getCreators() == null || collection.getCreators().size() == 0) {
                throw new StatefulIngestServiceException("Collection " + collection.getTitle() + " doesn't have a valid creator or entity.");
            }
            if ( (collection.getParentProjectId() == null || collection.getParentProjectId().isEmpty()) 
                    && (collection.getParentId() == null || collection.getParentId().isEmpty())) {
                throw new StatefulIngestServiceException("Collection " + collection.getTitle() + " is missing a parent ID");
            }
            references.add(new DcsEntityReference(collection.getId()));
        }
        
        // Check DataItems
        for (DataItem dataItem : businessObjectManager.getInstancesOf(DataItem.class)) {
            if (dataItem.getName() == null || dataItem.getName().length() == 0) {
                throw new StatefulIngestServiceException("DataItem doesn't have a valid name.");
            }
            
            if (dataItem.getDepositDate() == null) {
                throw new StatefulIngestServiceException("DataItem " + dataItem.getName() + " is missing a deposit date");
            }
            
            if (dataItem.getDepositorId() == null || dataItem.getDepositorId().isEmpty()) {
                throw new StatefulIngestServiceException("DataItem " + dataItem.getName() + " is missing a depositor");
            }
            
            references.add(new DcsEntityReference(dataItem.getId()));
        }
        
        // Check DataFile
        for (DataFile dataFile : businessObjectManager.getInstancesOf(DataFile.class)) {
            if (dataFile.getName() == null || dataFile.getName().length() == 0) {
                throw new StatefulIngestServiceException("DataFile doesn't have a valid name.");
            }
            else if (dataFile.getSize() == 0) {
                throw new StatefulIngestServiceException("DataFile " + dataFile.getName() + " can't have a size of zero");
            }
            else if (dataFile.getSource() == null || dataFile.getSource().length() == 0) {
                throw new StatefulIngestServiceException("DataFile " + dataFile.getName() + " doesn't have a source.");
            }
            references.add(new DcsEntityReference(dataFile.getId()));
        }
        
        // Check MetadataFile
        for (MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)) {
            if (metadataFile.getName() == null || metadataFile.getName().length() == 0) {
                throw new StatefulIngestServiceException("MetadataFile doesn't have a valid name.");
            }
            else if (metadataFile.getSize() == 0) {
                throw new StatefulIngestServiceException("MetadataFile " + metadataFile.getName() + " can't have a size of zero");
            }
            else if (metadataFile.getSource() == null || metadataFile.getSource().length() == 0) {
                throw new StatefulIngestServiceException("MetadataFile " + metadataFile.getName() + " doesn't have a source.");
            }
            references.add(new DcsEntityReference(metadataFile.getId()));
        }
        
        return references;
    }
}
