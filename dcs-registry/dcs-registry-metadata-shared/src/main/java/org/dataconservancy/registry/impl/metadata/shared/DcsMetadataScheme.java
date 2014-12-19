package org.dataconservancy.registry.impl.metadata.shared;

/**
 * Represents a Metadata scheme that might be used to validate metadata instance documents.
 */
public class DcsMetadataScheme {

    private String name;
    private String schemaVersion;
    private String schemaUrl;
    private String source;

    /**
     * The URL of the schema document.
     *
     * @return
     */
    public String getSchemaUrl() {
        return schemaUrl;
    }

    /**
     * The URL of the schema document.
     *
     * @param schemaUrl
     */
    public void setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
    }

    /**
     * The human-readable name or description of the schema.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The human-readable name or description of the schema.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a reference to the schema source.
     * <p/>
     * This is what should be de-referenced when retrieving the contents of the schema document.
     *
     * @return
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns a reference to the schema source.
     * <p/>
     * This is what should be de-referenced when retrieving the contents of the schema document.
     *
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * A human-readable schema version.
     *
     * @return
     */
    public String getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * A human-readable schema version.
     *
     * @param schemaVersion
     */
    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsMetadataScheme that = (DcsMetadataScheme) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (schemaUrl != null ? !schemaUrl.equals(that.schemaUrl) : that.schemaUrl != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (schemaVersion != null ? !schemaVersion.equals(that.schemaVersion) : that.schemaVersion != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (schemaVersion != null ? schemaVersion.hashCode() : 0);
        result = 31 * result + (schemaUrl != null ? schemaUrl.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }
}
