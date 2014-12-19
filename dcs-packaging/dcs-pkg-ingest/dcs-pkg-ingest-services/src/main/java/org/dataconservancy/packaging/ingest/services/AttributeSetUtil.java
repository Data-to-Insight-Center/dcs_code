package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.api.AttributeMatcher;
import org.dataconservancy.packaging.model.AttributeSetName;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
class AttributeSetUtil {

    /**
     * String used to concatenate the String components to produce a key.  It is illegal for any String component to
     * contain this String.
     */
    static final String CONCAT_CHAR = "_";

    private static final String RESERVED_CHAR_ERR = "Cannot compose a key using the string '%s' because it contains " +
            "the reserved character sequence '%s'";

    /**
     * An {@link AttributeMatcher} which matches all AttributeSets with the name {@link AttributeSetName#ORE_REM_PACKAGE}.
     */
    static final AttributeMatcher ORE_PACKAGE_MATCHER = new AttributeMatcher() {
        @Override
        public boolean matches(String attributeSetName, Attribute candidateAttribute) {
            return attributeSetName.equals(AttributeSetName.ORE_REM_PACKAGE);
        }
    };

    /**
     * An {@link AttributeMatcher} which matches all AttributeSets with the name {@link AttributeSetName#ORE_REM_PROJECT}.
     */
    static final AttributeMatcher ORE_PROJECT_MATCHER = new AttributeMatcher() {
        @Override
        public boolean matches(String attributeSetName, Attribute candidateAttribute) {
            return attributeSetName.equals(AttributeSetName.ORE_REM_PROJECT);
        }
    };

    /**
     * An {@link AttributeMatcher} which matches all AttributeSets with the name {@link AttributeSetName#ORE_REM_COLLECTION}.
     */
    static final AttributeMatcher ORE_COLLECTION_MATCHER = new AttributeMatcher() {
        @Override
        public boolean matches(String attributeSetName, Attribute candidateAttribute) {
            return attributeSetName.equals(AttributeSetName.ORE_REM_COLLECTION);
        }
    };

    /**
     * An {@link AttributeMatcher} which matches all AttributeSets with the name {@link AttributeSetName#ORE_REM_DATAITEM}.
     */
    static final AttributeMatcher ORE_DATAITEM_MATCHER = new AttributeMatcher() {
        @Override
        public boolean matches(String attributeSetName, Attribute candidateAttribute) {
            return attributeSetName.equals(AttributeSetName.ORE_REM_DATAITEM);
        }
    };

    /**
     * An {@link AttributeMatcher} which matches all AttributeSets with the name {@link AttributeSetName#ORE_REM_FILE}.
     */
    static final AttributeMatcher ORE_FILE_MATCHER = new AttributeMatcher() {
        @Override
        public boolean matches(String attributeSetName, Attribute candidateAttribute) {
            return attributeSetName.equals(AttributeSetName.ORE_REM_FILE);
        }
    };

    /**
     * Composes a key for an AttributeSet from two strings by concatenating them.
     * <br/>
     * By convention, the name of the AttributeSet is used as the first string, and the second string is a unique
     * identifier. For example: {@code composeKey(AttributeSetName.ORE_REM_PROJECT, "1234")} would return
     * {@code Ore-Rem-Project_1234}
     *
     * @param attributeSetName the first portion of the key; by convention, the name of the AttributeSet the key is
     *                         being composed for
     * @param s the second portion of the key; by convention, a unique identifier
     * @return a string, suitable for use as an AttributeSet key
     * @throws IllegalArgumentException if either string contains the character sequence used to concatenate the
     *                                  arguments
     */
    static String composeKey(String attributeSetName, String s) {
        if (attributeSetName.contains(CONCAT_CHAR)) {
            throw new IllegalArgumentException(String.format(RESERVED_CHAR_ERR, attributeSetName, CONCAT_CHAR));
        }

        if (s.contains(CONCAT_CHAR)) {
            throw new IllegalArgumentException(String.format(RESERVED_CHAR_ERR, s, CONCAT_CHAR));
        }

        return attributeSetName + CONCAT_CHAR + s;
    }

    /**
     * Decomposes a key produced by {@link #composeKey(String, String)} into its component parts.
     *
     * @param key the key
     * @return the component key parts
     */
    static String[] decomposeKey(String key) {
        return key.split(CONCAT_CHAR);

    }

    /**
     * Obtains a Set of all values of the named Attribute in the supplied Attribute Set.
     *
     * @param attrSet  the Attribute Set to obtain Attribute values from
     * @param attrName the name of the Attribute to obtain values for
     * @return all the values of the named Attribute in the supplied AttributeSet
     */
    static Set<String> values(AttributeSet attrSet, String attrName) {
        final Set<String> results = new HashSet<String>();
        for (Attribute idAttr : attrSet.getAttributesByName(attrName)) {
            results.add(idAttr.getValue());
        }

        return results;
    }
}
