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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.mhf.extractor.api.MetadataExtractor;
import org.dataconservancy.mhf.extractors.BagItManifestMetadataExtractor;
import org.dataconservancy.mhf.extractors.BagItTagMetadataExtractor;
import org.dataconservancy.mhf.finders.BagItMetadataFinder;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.springframework.beans.factory.annotation.Required;

/**
 * Ingest service to extract BagIt metadata from the package.
 * 
 * Success produces AttributeSets named MetadataAttributeSetName.BAGIT_METADATA
 * and MetadataAttributeSetName.BAGIT_PROFILE_DATACONS_METADATA.
 */
public class PackageMetadataExtractor extends BaseIngestService {

    private MetadataObjectBuilder metadataObjectBuilder;

    /**
     * Sets the {@code MetadataObjectBuilder} to use for the metadataFinder.
     * 
     * @param metadataObjectBuilder
     */
    @Required
    public void setMetadataObjectBuilder(
            MetadataObjectBuilder metadataObjectBuilder) {
        this.metadataObjectBuilder = metadataObjectBuilder;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void execute(String depositId, IngestWorkflowState state)
            throws StatefulIngestServiceException {

        super.execute(depositId, state);

        PackageSerialization pkg = state.getPackage().getSerialization();
        BagItMetadataFinder finder = new BagItMetadataFinder(
                metadataObjectBuilder);

        // Combine all attribute sets with same name

        Map<String, AttributeSet> sets = new HashMap<String, AttributeSet>();

        for (MetadataInstance mi : finder.findMetadata(pkg)) {
            MetadataExtractor extractor;

            if (mi.getFormatId().equals(MetadataFormatId.BAGIT_TAG_FORMAT_ID)) {
                extractor = new BagItTagMetadataExtractor();
            } else if (mi.getFormatId().equals(
                    MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID)) {
                extractor = new BagItManifestMetadataExtractor(
                        finder.getChecksum());
            } else {
                continue;
            }

            for (MetadataRepresentation rep : extractor.extractMetadata(mi)) {
                AttributeSet rep_as = (AttributeSet) rep.getRepresentation();
                String asName = "";

                //Map mhf attribute set names to the pkg attribute set names
                if (rep_as.getName() == MetadataAttributeSetName.BAGIT_METADATA) {
                    asName = AttributeSetName.BAGIT;
                }
                else if (MetadataAttributeSetName.BAGIT_PROFILE_DATACONS_METADATA == rep_as.getName()) {
                    asName = AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA;
                }

                AttributeSet result_as = sets.get(asName);

                if (result_as == null) {
                    sets.put(asName, rep_as);
                } else {
                    result_as.getAttributes().addAll(rep_as.getAttributes());
                }
            }
        }

        if (sets.containsKey(AttributeSetName.BAGIT)
                && sets.containsKey(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)) {
            // Output combined attribute sets

            AttributeSetManager manager = state.getAttributeSetManager();

            for (String name : sets.keySet()) {
                manager.addAttributeSet(name, sets.get(name));
            }

            DcsEvent event = state.getEventManager().newEvent(Package.Events.TRANSFORM);
            event.setDetail("Extracted metadata for the package with deposit ID: " + depositId);
            event.setOutcome("Successfully extracted metadata.");
            DcsEntityReference ref = new DcsEntityReference(depositId);
            List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
            refs.add(ref);
            event.setTargets(refs);
            state.getEventManager().addEvent(depositId, event);
        } else {
            DcsEvent event = state.getEventManager().newEvent(Package.Events.TRANSFORM_FAIL);
            event.setDetail("Unable to extract BagIt metadata from the package for deposit: " + depositId);
            event.setOutcome("Failed to extract BagIt metadata.");
            DcsEntityReference ref = new DcsEntityReference(depositId);
            List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
            refs.add(ref);
            event.setTargets(refs);
            state.getEventManager().addEvent(depositId, event);
        }
    }
}
