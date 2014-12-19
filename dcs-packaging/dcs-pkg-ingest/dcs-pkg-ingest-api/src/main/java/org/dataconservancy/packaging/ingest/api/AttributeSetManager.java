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
package org.dataconservancy.packaging.ingest.api;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;

import java.util.Set;

/**
 * Manages AttributeSets that are generated or extracted during an ingest pipeline.  AttributeSets that are generated
 * or extracted during ingest are considered part of the state of the ingest.  Typically, ingest services will obtain
 * an instance of AttributeSetManager from an instance of {@link IngestWorkflowState}.
 * <p/>
 * The AttributeSetManager relies on the caller to supply a key when adding an AttributeSet, used to reference that
 * AttributeSet for subsequent operations like update or removal.  The key is an opaque string, and must be unique
 * within the scope of the instance of the manager.  No other semantics (such as identifier semantics) are placed on the
 * key by implementations of AttributeSetManager.
 * <p/>
 * Implementations are expected to document their behaviors for methods that have optional behaviors.
 */
public interface AttributeSetManager {

    /**
     * Adds the supplied {@code attributeSet} to this manager.  The key supplied with the AttributeSet must be unique to
     * this instance, otherwise an {@code ExistingAttributeSetException} will be thrown.
     *
     * @param key the key used by the caller to reference the AttributeSet in subsequent operations
     * @param attributeSet the AttributeSet to add
     * @throws ExistingAttributeSetException if an AttributeSet with the same key is already being managed by this
     *         manager.
     */
    public void addAttributeSet(String key, AttributeSet attributeSet);

    /**
     * Updates the {@code attributeSet} by replacing the existing instance in the manager with the supplied instance.
     * If the supplied key doesn't exist in the manager, the behavior of this method is implementation dependent.
     * Implementations may opt to silently add the AttributeSet, or they may opt to throw an exception.
     *
     * @param key the key used by the caller to reference the AttributeSet in subsequent operations
     * @param attributeSet the AttributeSet to add or update
     */
    public void updateAttributeSet(String key, AttributeSet attributeSet);

    /**
     * Removes the AttributeSet keyed by {@code key}.  If the supplied key doesn't exist in the manager, the behavior of
     * this method is implementation dependent. Implementations may opt to silently ignore a non-existent key, or they
     * may opt to throw an exception.
     *
     * @param key the key identifying the AttributeSet
     */
    public void removeAttributeSet(String key);

    /**
     * Obtain the AttributeSet identified by {@code key}.
     *
     * @param key the key identifying the AttributeSet
     * @return the AttributeSet, or {@code null} if the key references a non-existent AttributeSet
     */
    public AttributeSet getAttributeSet(String key);

    /**
     * Returns {@code true} if {@code key} identifies an AttributeSet that is currently managed by this interface.  If
     * {@code true}, calling {@link #addAttributeSet(String, org.dataconservancy.mhf.representation.api.AttributeSet)}
     * would fail with an {@code ExistingAttributeSetException}.  The caller should be able to reliably update or
     * remove the AttributeSet.
     *
     * @param key the key identifying the AttributeSet
     * @return true if the manager currently has an AttributeSet referenced by {@code key}; {@code false} otherwise.
     */
    public boolean contains(String key);

    /**
     * Returns all AttributeSets managed by this instance which match the supplied Attribute.  The logic used to match
     * AttributeSets is implementation dependant and should be documented in implementing classes.
     *
     * @param matchExpression the Attribute to match
     * @return the Set of AttributeSets that match the supplied Attribute.  May be empty but never {@code null}
     */
    public Set<AttributeSet> matches(Attribute matchExpression);

    /**
     * Returns all AttributeSets managed by this instance which match the supplied Attribute and have matching name to
     * the name provided.  The logic used to match AttributeSets is implementation dependant and should be documented
     * in implementing classes.
     *
     * @param attributeSetName name of the attribute to match
     * @param matchExpression the Attribute to match
     * @return the Set of AttributeSets that match the supplied Attribute.  May be empty but never {@code null}
     */
    public Set<AttributeSet> matches(String attributeSetName, Attribute matchExpression);

    /**
     * Returns all AttributeSets managed by this instance which match the supplied AttributeMatcher.  The logic used to
     * match AttributeSets is implementation dependant.
     *
     * @param matcher the implementation containing the matching logic
     * @return the Set of AttributeSets that match the supplied Attribute.  May be empty but never {@code null}
     */
    public Set<AttributeSet> matches(AttributeMatcher matcher);

    /**
     * Returns the set of keys for all attribute sets
     * @return
     */
    public Set<String> getKeys();
}
