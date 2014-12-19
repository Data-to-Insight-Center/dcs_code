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
        return false;
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
