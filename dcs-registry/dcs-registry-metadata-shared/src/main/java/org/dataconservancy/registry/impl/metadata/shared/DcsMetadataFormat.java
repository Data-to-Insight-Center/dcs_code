package org.dataconservancy.registry.impl.metadata.shared;

/**
 *
 */
public class DcsMetadataFormat {

    private String name;
    private String version;
    private DcsMetadataScheme scheme;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DcsMetadataScheme getScheme() {
        return scheme;
    }

    public void setScheme(DcsMetadataScheme scheme) {
        this.scheme = scheme;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
