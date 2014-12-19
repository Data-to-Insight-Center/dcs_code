package org.dataconservancy.registry.impl.metadata.shared;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.profile.api.DcpMapper;

import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MetadataFormatMapper implements DcpMapper<DcsMetadataFormat> {
    @Override
    public Set<String> discover(Dcp conformingPackage) {
        // Default method body
        return null;
    }

    @Override
    public Dcp to(DcsMetadataFormat domainObject, Map<String, Object> context) {
        // Default method body
        return null;
    }

    @Override
    public DcsMetadataFormat from(String identifier, Dcp dcp, Map<String, Object> context) {
        // Default method body
        return null;
    }
}
