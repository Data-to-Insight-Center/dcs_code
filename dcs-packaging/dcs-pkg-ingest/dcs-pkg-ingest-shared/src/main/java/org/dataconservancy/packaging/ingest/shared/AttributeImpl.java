package org.dataconservancy.packaging.ingest.shared;

import org.dataconservancy.mhf.representation.api.Attribute;

/**
 * Simple Attribute implementation used to support defensive copying.
 */
public class AttributeImpl implements Attribute {
    private String name;
    private String type;
    private String value;

    public AttributeImpl(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public AttributeImpl(Attribute toCopy) {
        this.name = toCopy.getName();
        this.type = toCopy.getType();
        this.value = toCopy.getValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttributeImpl attribute = (AttributeImpl) o;

        if (name != null ? !name.equals(attribute.name) : attribute.name != null) return false;
        if (type != null ? !type.equals(attribute.type) : attribute.type != null) return false;
        if (value != null ? !value.equals(attribute.value) : attribute.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AttributeImpl{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
