package org.dataconservancy.packaging.ingest.shared;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * Simple AttributeSet implementation used to support defensive copying.
 */
public class AttributeSetImpl implements AttributeSet {
    private String name;
    private List<Attribute> attributes;

    /**
     * No-Arg constructor
     */
    public AttributeSetImpl() {
    }

    public AttributeSetImpl(String name) {
        this.name = name;
        this.attributes = new ArrayList<Attribute>();
    }

    public AttributeSetImpl(String name, Collection<Attribute> attributes) {
        this.name = name;
        setAttributes(attributes);
    }

    public AttributeSetImpl(AttributeSet toCopy) {
        this.name = toCopy.getName();
        final Collection<Attribute> attributesToCopy = toCopy.getAttributes();
        if (attributesToCopy != null) {
            this.attributes = new ArrayList<Attribute>(attributesToCopy.size());
            for (Attribute attr : attributesToCopy) {
                attributes.add(new AttributeImpl(attr));
            }
        }
    }

    @Override
    public Collection<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Attribute> getAttributesByName(String attributeName) {
        Collection<Attribute> matchingAttributes = new HashSet<Attribute>();
        for (Attribute attribute : attributes) {
            if (attribute.getName().trim().equals(attributeName.trim())) {
                matchingAttributes.add(attribute);
            }
        }
        return matchingAttributes;
    }

    /**
     * Sets the Attributes that are contained in this AttributeSet.  Overwrites any Attributes that may have been
     * managed previously.
     *
     * @param attributes
     */
    public void setAttributes(Collection<Attribute> attributes) {
        ArrayList<Attribute> attrs = new ArrayList<Attribute>(attributes.size());
        attrs.addAll(attributes);
        this.attributes = attrs;
    }

    /**
     * Adds the supplied Attributes to this AttributeSet
     * @param attribute
     */
    public void addAttribute(Attribute... attribute) {
        if (this.attributes == null) {
            this.attributes = new ArrayList<Attribute>();
        }

        this.attributes.addAll(Arrays.asList(attribute));
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttributeSetImpl that = (AttributeSetImpl) o;

        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AttributeSetImpl{" +
                "attributes=" + attributes +
                ", name='" + name + '\'' +
                '}';
    }

    public String toString(HierarchicalPrettyPrinter hpp) {
        // Sort the attributes so it makes them easier to find
        List<Attribute> sorted = new ArrayList<Attribute>();
        sorted.addAll(this.attributes);
        Collections.sort(sorted, new Comparator<Attribute>() {
            @Override
            public int compare(Attribute a1, Attribute a2) {
                if (!a1.getName().equals(a2.getName())) {
                    return a1.getName().compareTo(a2.getName());
                }

                if (!a1.getType().equals(a2.getType())) {
                    return a1.getType().compareTo(a2.getType());
                }

                return a1.getValue().compareTo(a2.getValue());
            }
        });

        hpp.appendWithIndent("AttributeSet: ").appendWithNewLine(name);
        hpp.incrementDepth();
        hpp.appendWithIndentAndNewLine("Attributes:");
        hpp.incrementDepth();
        for (Attribute attr : sorted) {
            hpp.appendWithIndentAndNewLine("Name: '" + attr.getName() + "' Type: '" + attr.getType() +
                    "' Value: '" + attr.getValue() + "'");
        }
        hpp.decrementDepth();
        hpp.decrementDepth();
        return hpp.toString();
    }
}
