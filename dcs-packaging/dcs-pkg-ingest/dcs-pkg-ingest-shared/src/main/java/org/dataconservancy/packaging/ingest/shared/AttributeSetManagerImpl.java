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
package org.dataconservancy.packaging.ingest.shared;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.packaging.ingest.api.AttributeMatcher;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.ExistingAttributeSetException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * An implementation of AttributeSetManager that uses an in-memory data structure to persist AttributeSets.  It has no
 * capabilities to persist AttributeSets otherwise.  Note: this implementation is <em>not</em> thread-safe.
 */
public class AttributeSetManagerImpl implements AttributeSetManager {

    private Map<String, AttributeSet> attributeSets = new HashMap<String, AttributeSet>();

    @Override
    public void addAttributeSet(String key, AttributeSet attributeSet) {
        checkKey(key);
        checkAttributeSet(attributeSet);

        if (contains(key)) {
            throw new ExistingAttributeSetException("AttributeSet key '" + key + "' already in use.");
        }

        updateAttributeSet(key, copy(attributeSet));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:<br/>
     * If {@code key} does not reference a currently managed AttributeSet, this method will silently add the supplied
     * AttributeSet.
     */
    @Override
    public void updateAttributeSet(String key, AttributeSet attributeSet) {
        checkKey(key);
        checkAttributeSet(attributeSet);

        attributeSets.put(key, copy(attributeSet));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:<br/>
     * If {@code key} does not reference a currently managed AttributeSet, this method will silently fail.
     */
    @Override
    public void removeAttributeSet(String key) {
        attributeSets.remove(key);
    }

    @Override
    public AttributeSet getAttributeSet(String key) {
        return copy(attributeSets.get(key));
    }

    @Override
    public boolean contains(String key) {
        return attributeSets.containsKey(key);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:<br/>
     * An Attribute expression differs from an Attribute only in its semantics.  If any of the fields of an
     * Attribute expression are {@code null}, then that means that the field can be matched against any value.  For
     * example, if an Attribute expression has a name "foo", type "bar", and a value of {@code null}, then
     * it will match any Attribute of the same name and type, and ignore the value.
     */
    @Override
    public Set<AttributeSet> matches(Attribute matchExpression) {
        final Set<AttributeSet> results = new HashSet<AttributeSet>();
        for (AttributeSet candidate : attributeSets.values()) {
            for (Attribute candidateAttr : candidate.getAttributes()) {
                if (AttributeMatcher.matches(matchExpression, candidateAttr)) {
                    results.add(copy(candidate));
                }
            }
        }

        return results;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:<br/>
     * An Attribute expression differs from an Attribute only in its semantics.  If any of the fields of an
     * Attribute expression are {@code null}, then that means that the field can be matched against any value.  For
     * example, if an Attribute expression has a name "foo", type "bar", and a value of {@code null}, then
     * it will match any Attribute of the same name and type, and ignore the value.
     */
    @Override
    public Set<AttributeSet> matches(String attributeSetName, Attribute matchExpression) {
        final Set<AttributeSet> results = new HashSet<AttributeSet>();
        for (AttributeSet candidate : attributeSets.values()) {
            if (candidate.getName().equals(attributeSetName)) {
                for (Attribute candidateAttr : candidate.getAttributes()) {
                    if (AttributeMatcher.matches(matchExpression, candidateAttr)) {
                        results.add(copy(candidate));
                    }
                }
            }
        }

        return results;
    }

    /**
     * Returns AttributeSets that match the supplied {@code matcher}.  Note that empty AttributeSets will not be
     * considered for matching.
     *
     * @param matcher the implementation containing the matching logic
     * @return
     */
    @Override
    public Set<AttributeSet> matches(org.dataconservancy.packaging.ingest.api.AttributeMatcher matcher) {
        final Set<AttributeSet> results = new HashSet<AttributeSet>();
        for (AttributeSet candidate : attributeSets.values()) {
            for (Attribute candidateAttr : candidate.getAttributes()) {
                if (matcher.matches(candidate.getName(), candidateAttr)) {
                    results.add(candidate);
                }
            }
        }

        return results;
    }

    @Override
    public Set<String> getKeys(){
        return attributeSets.keySet();
    }

    /**
     * Creates a defensive copy of the supplied AttributeSet.  If the supplied AttributeSet is null, then null is
     * returned.
     *
     * @param toCopy the AttributeSet to copy, may be null
     * @return a defensive copy of the AttributeSet, may be null
     */
    private AttributeSet copy(AttributeSet toCopy) {
        if (toCopy != null) {
            return new AttributeSetImpl(toCopy);
        }

        return null;
    }

    /**
     * Asserts that the key used to reference the AttributeSet is not empty or null.
     *
     * @param key a key
     * @throws IllegalArgumentException if the key is empty or null
     */
    private void checkKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty or null.");
        }
    }

    /**
     * Asserts that the AttributeSet is not empty or null.
     *
     * @param attributeSet the AttributeSet
     * @throws IllegalArgumentException if the AttributeSet is empty or null
     */
    private void checkAttributeSet(AttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new IllegalArgumentException("AttributeSet must not be null.");
        }
    }

    @Override
    public String toString() {
        return toString(new HierarchicalPrettyPrinter());
    }

    public String toString(HierarchicalPrettyPrinter hpp) {

        // Sort the AttributeSets by name, then by key
        final TreeMap<AttributeSet, String> invertedMap = new TreeMap<AttributeSet, String>(new Comparator<AttributeSet>() {
            @Override
            public int compare(AttributeSet one, AttributeSet two) {
                if (one.getName().compareTo(two.getName()) != 0) {
                    return one.getName().compareTo(two.getName());
                }

                String key1 = null;
                String key2 = null;

                for (Map.Entry<String, AttributeSet> entry : attributeSets.entrySet()) {
                    if (key1 != null && key2 != null) {
                        break;
                    }
                    if (entry.getValue().equals(one)) {
                        key1 = entry.getKey();
                    }
                    if (entry.getValue().equals(two)) {
                        key2 = entry.getKey();
                    }
                }

                return key1.compareTo(key2);
            }
        });

        for (Map.Entry<String, AttributeSet> entry : this.attributeSets.entrySet()) {
            invertedMap.put(entry.getValue(), entry.getKey());
        }

        hpp.appendWithIndentAndNewLine("AttributeSets:");
        hpp.incrementDepth();

        for (Map.Entry<AttributeSet, String> entry : invertedMap.entrySet()) {
            final AttributeSet as = entry.getKey();
            final String asKey = entry.getValue();


            // Sort the Attributes by name, then type, then value
            final TreeSet<Attribute> sortedAttributes = new TreeSet<Attribute>(new Comparator<Attribute>() {
                @Override
                public int compare(Attribute one, Attribute two) {
                    if (one.getName().compareTo(two.getName()) != 0) {
                        return one.getName().compareTo(two.getName());
                    }

                    if (one.getType().compareTo(two.getType()) != 0) {
                        return one.getType().compareTo(two.getType());
                    }

                    return one.getValue().compareTo(two.getValue());
                }
            });

            for (Attribute a : as.getAttributes()) {
                sortedAttributes.add(a);
            }

            hpp.appendWithIndent("Attribute Set: ").append(as.getName()).append(" (").append(asKey)
                    .appendWithNewLine(")");

            hpp.incrementDepth();

            for (Attribute a : sortedAttributes) {
                hpp.appendWithIndent("Attribute: ").append(a.getName()).append(" ")
                        .append(a.getType()).append(" ").appendWithNewLine(a.getValue());
            }

            hpp.decrementDepth();
        }

        hpp.decrementDepth();

        return attributeSets.toString();
    }

    /**
     * Encapsulates logic used to match an Attribute expression against a candidate Attribute.
     * <p/>
     * An Attribute expression differs from an Attribute only in its semantics.  If any of the fields of an
     * Attribute expression are {@code null}, then that means that the field can be matched against any value.  For
     * example, if an Attribute expression has a name "foo", type "bar", and a value of {@code null}, then
     * it will match any Attribute of the same name and type, and ignore the value.
     */
    private static class AttributeMatcher {

        /**
         * Returns true if the Attribute expression matches the candidate Attribute.
         *
         * @param matchExpression the match expression
         * @param candidate the candidate attribute to match
         * @return true if the Attribute expression matches the candidate Attribute
         */
        private static boolean matches(Attribute matchExpression, Attribute candidate) {

            boolean matches = true;

            if (hasName(matchExpression)) {
                matches = namesMatch(matchExpression, candidate);
            }

            if (matches && hasType(matchExpression)) {
                matches = typesMatch(matchExpression, candidate);
            }

            if (matches && hasValue(matchExpression)) {
                matches = valuesMatch(matchExpression, candidate);
            }


            return matches;
        }

        /**
         * Returns true if the attribute has a non-null name field.
         *
         * @param attribute the attribute
         * @return true if the attribute has a non-null name field.
         */
        private static boolean hasName(Attribute attribute) {
            return attribute.getName() != null;
        }

        /**
         * Returns true if the attribute has a non-null type field.
         *
         * @param attribute the attribute
         * @return true if the attribute has a non-null type field.
         */
        private static boolean hasType(Attribute attribute) {
            return attribute.getType() != null;
        }

        /**
         * Returns true if the attribute has a non-null value field.
         *
         * @param attribute the attribute
         * @return true if the attribute has a non-null value field.
         */
        private static boolean hasValue(Attribute attribute) {
            return attribute.getValue() != null;
        }

        /**
         * Returns true if the {@code matchExpression} and {@code candidate} have equal names.
         *
         * @param matchExpression the match expression
         * @param candidate the candidate attribute to match
         * @return true if the {@code matchExpression} and {@code candidate} have equal names
         */
        private static boolean namesMatch(Attribute matchExpression, Attribute candidate) {
            return matchExpression.getName().equals(candidate.getName());
        }

        /**
         * Returns true if the {@code matchExpression} and {@code candidate} have equal types.
         *
         * @param matchExpression the match expression
         * @param candidate       the candidate attribute to match
         * @return true if the {@code matchExpression} and {@code candidate} have equal types
         */
        private static boolean typesMatch(Attribute matchExpression, Attribute candidate) {
            return matchExpression.getType().equals(candidate.getType());
        }

        /**
         * Returns true if the {@code matchExpression} and {@code candidate} have equal values.
         *
         * @param matchExpression the match expression
         * @param candidate       the candidate attribute to match
         * @return true if the {@code matchExpression} and {@code candidate} have equal values
         */
        private static boolean valuesMatch(Attribute matchExpression, Attribute candidate) {
            return matchExpression.getValue().equals(candidate.getValue());
        }
    }

}
