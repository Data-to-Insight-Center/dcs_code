package org.dataconservancy.packaging.ingest.api;

import org.dataconservancy.mhf.representation.api.Attribute;

/**
 * Abstracts logic for matching a candidate Attribute
 */
public interface AttributeMatcher {

    /**
     * Return {@code true} if the candidate Attribute matches some implementation-specific criteria.
     *
     * @param attributeSetName the name of the AttributeSet the {@code candidateAttribute} belongs to
     * @param candidateAttribute the candidate Attribute to match
     * @return {@code true} if the {@code Attribute} matches, {@code false} otherwise
     */
    public boolean matches(String attributeSetName, Attribute candidateAttribute);

}
