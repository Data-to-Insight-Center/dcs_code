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

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;

/**
 * An IngestWorkflowStateFactory that has its components wired at compile-time.
 * Configuration:
 * <dl>
 * <dd>idCreationService</dd>
 * <dt>an instance of BulkIdCreationService</dt>
 * </dl>
 */
public class HardwiredIngestWorkflowStateFactory implements IngestWorkflowStateFactory {

    private BulkIdCreationService idCreationService;

    @Override
    public IngestWorkflowState newInstance() {
        final InMemoryEventManager em = new InMemoryEventManager();
        final AttributeSetManagerImpl asm = new AttributeSetManagerImpl();
        final BusinessObjectManagerImpl bom = new BusinessObjectManagerImpl();
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();

        em.setIdService(idCreationService);

        state.setAttributeSetManager(asm);
        state.setBusinessObjectManager(bom);
        state.setEventManager(em);
        state.setPackage(new PackageImpl(new DescriptionImpl(), new SerializationImpl()));

        return state;
    }

    BulkIdCreationService getIdCreationService() {
        return idCreationService;
    }

    public void setIdCreationService(BulkIdCreationService idCreationService) {
        this.idCreationService = idCreationService;
    }

}
