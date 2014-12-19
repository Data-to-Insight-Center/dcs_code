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
package org.dataconservancy.registry.impl.metadata.shared;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.profile.api.DcpProfile;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE;
import static org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant.METADATASCHEME_VERSION_ONE;

/**
 * The DCP profile for a MetadataScheme
 */
public class MetadataSchemeProfile implements DcpProfile {

    @Override
    public boolean conforms(Dcp candidatePackage) {

        Set<DcsDeliverableUnit> candidateDus = new HashSet<DcsDeliverableUnit>();

        for (DcsDeliverableUnit du : candidatePackage.getDeliverableUnits()) {
            if (du.getType().equals(getType())) {
                candidateDus.add(du);
            }
        }

        Iterator<DcsManifestation> manItr = candidatePackage.getManifestations().iterator();
        Iterator<DcsFile> fileItr = candidatePackage.getFiles().iterator();

        for (DcsDeliverableUnit du : candidateDus) {
            DcsManifestation man = null;
            while (manItr.hasNext() && man == null) {
                DcsManifestation candidateMan = manItr.next();
                if (du.getId().equals(candidateMan.getDeliverableUnit()) &&
                        candidateMan.getTechnicalEnvironment().size() > 0  &&
                        candidateMan.getTechnicalEnvironment().iterator().next().startsWith(METADATASCHEME_REGISTRY_ENTRY_TYPE)) {
                    man = candidateMan;
                }
            }

            if (man == null) {
                continue;
            }

            DcsFile file = null;
            while (fileItr.hasNext() && file == null) {
                DcsFile candidateFile = fileItr.next();
                for (DcsManifestationFile mf : man.getManifestationFiles()) {
                    if (mf.getRef().getRef().equals(candidateFile.getId())
                            && candidateFile.getName().equals("schema-source")) {
                        file = candidateFile;
                    }
                }
            }

            if (file == null) {
                continue;
            }

            // We found a DU, Manifestation, and File
            return true;
        }

        return false;
    }

    @Override
    public String getType() {
        return METADATASCHEME_REGISTRY_ENTRY_TYPE;
    }

    @Override
    public String getVersion() {
        return METADATASCHEME_VERSION_ONE;
    }

}
