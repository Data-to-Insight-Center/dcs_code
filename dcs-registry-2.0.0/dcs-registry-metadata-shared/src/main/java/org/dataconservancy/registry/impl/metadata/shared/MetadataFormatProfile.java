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
import org.dataconservancy.profile.api.DcpProfile;

/**
 *
 */
public class MetadataFormatProfile implements DcpProfile {

    @Override
    public boolean conforms(Dcp candidatePackage) {
        // Default method body
        return true;
    }

    @Override
    public String getType() {
        return MetadataRegistryConstant.METADATAFORMAT_REGISTRY_ENTRY_TYPE;
    }

    @Override
    public String getVersion() {
        return MetadataRegistryConstant.METADATAFORMAT_VERSION_ONE;
    }

}
