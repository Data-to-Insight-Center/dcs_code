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

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestPhase;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.model.Package;

/**
  * Exposes components used to manage the state of an ingest pipeline.  Typically, each ingest service will receive an
  * instance it can use to access and mutate the current state of the ingest pipeline.
  * <p/>
  * This class <em>requires</em> the following components before it can be used:
  * <dl>
  *     <dt>Event Manager</dt>
  *     <dd>Ingest services use the Event Manager to add events to the ingest workflow state, or to reason over
  *         events contributed by previous ingest services in the same workflow.</dd>
  *     <dt>Attribute Set Manager</dt>
  *     <dd>Ingest services use the AttributeSet Manager to add AttributeSets to the ingest workflow state, or to reason
  *         over AttributeSets contributed by previous ingest services in the same workflow.</dd>
  *     <dt>Package</dt>
  *     <dd>Ingest services are responsible for building a package model.  Multiple ingest services may contribute to
  *         the model as a ingest progresses.</dd>
  * </dl>
 */
public class IngestWorkflowStateImpl implements IngestWorkflowState {

    private AttributeSetManager attributeSetManager;

    private BusinessObjectManager businessObjectManager;

    private EventManager eventManager;

    private Package thePackage;
    
    private IngestPhase ingestPhase = null;

    private boolean isCancelled = false;
    
    private String userId;

    @Override
    public AttributeSetManager getAttributeSetManager() {
        return attributeSetManager;
    }

    @Override
    public BusinessObjectManager getBusinessObjectManager() {
        return businessObjectManager;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public Package getPackage() {
        return thePackage;
    }

    public void setAttributeSetManager(AttributeSetManager attributeSetManager) {
        if (attributeSetManager == null) {
            throw new IllegalArgumentException("AttributeSetManager must not be null!");
        }

        this.attributeSetManager = attributeSetManager;
    }

    public void setBusinessObjectManager(BusinessObjectManager businessObjectManager) {
        if (businessObjectManager == null) {
            throw new IllegalArgumentException("BusinessObjectManager must not be null!");
        }

        this.businessObjectManager = businessObjectManager;
    }

    public void setEventManager(EventManager eventManager) {
        if (eventManager == null) {
            throw new IllegalArgumentException("EventManager must not be null!");
        }

        this.eventManager = eventManager;
    }

    public void setPackage(Package thePackage) {
        if (thePackage == null) {
            throw new IllegalArgumentException("The Package instance must not be null!");
        }

        this.thePackage = thePackage;
    }


    @Override
    public IngestPhase getIngestPhase() {
        return ingestPhase;
    }

    @Override
    public void setIngestPhase(IngestPhase ingestPhase) {
        this.ingestPhase = ingestPhase;        
    }

    @Override
    public boolean isCancelled() {
        // Default method body
        return false;
    }

    public void setCancelled() {

    }

    @Override
    public void setIngestUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getIngestUserId() {
        return userId;
    }

}
