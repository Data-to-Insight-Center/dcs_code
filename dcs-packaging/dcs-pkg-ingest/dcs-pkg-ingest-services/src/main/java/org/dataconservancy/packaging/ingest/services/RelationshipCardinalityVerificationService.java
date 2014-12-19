/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.ingest.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;

import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.STRING;
import static org.dataconservancy.packaging.ingest.services.RelationshipCardinalityVerificationService.Cardinality.EXACTLY_ONE;
import static org.dataconservancy.packaging.ingest.services.RelationshipCardinalityVerificationService.Cardinality.EXACTLY_ZERO;
import static org.dataconservancy.packaging.ingest.services.RelationshipCardinalityVerificationService.Cardinality.ONE_OR_MORE;
import static org.dataconservancy.packaging.ingest.services.RelationshipCardinalityVerificationService.Cardinality.ZERO_OR_ONE;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATED_BY_PROJECT;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_IDENTIFIER;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_IS_PART_OF_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.DATA_ITEM_AGGREGATES_FILE;
import static org.dataconservancy.packaging.model.Metadata.DATA_ITEM_IDENTIFIER;
import static org.dataconservancy.packaging.model.Metadata.DATA_ITEM_IS_PART_OF_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATES_DATAITEM;
import static org.dataconservancy.packaging.model.Metadata.FILE_RESOURCEID;
import static org.dataconservancy.packaging.model.Metadata.IS_METADATA_FOR;

/**
 * Package ingest service for verifying correct cardinality of ReM
 * relationships.
 * <p>
 * The relevant ReM relationships are parsed out into {@link AttributeSet}s.
 * This service inspects ReM relationship attributes and compares them against
 * the expected cardinality with respect to the DCS object model. The specific
 * checks are as follows:
 * </p>
 * <ul>
 * <li>A root Collection (isPartOf = NULL) must belong to (aggregatedBy) exactly
 * one Project</li>
 * <li>A subcollection (aggregatedBy = NULL) must belong to (isPartOf) exactly
 * one Collection</li>
 * <li>A DataItem must belong (isPartOf) exactly one Collection</li>
 * <li>A DataItem must aggegate (aggregates) at least one File</li>
 * <li>A MetadataFile may only describe zero or one objects</li>
 * </ul>
 * 
 * @author apb18
 */
public class RelationshipCardinalityVerificationService
        extends BaseIngestService {

    private static final Attribute COLLECTIONS =
            new AttributeImpl(COLLECTION_IDENTIFIER, STRING, null);

    private static final Attribute DATA_ITEMS =
            new AttributeImpl(DATA_ITEM_IDENTIFIER, STRING, null);

    private static final Attribute METADATA_FILES =
            new AttributeImpl(FILE_RESOURCEID, STRING, null);

    @Override
    public void execute(String depositId, IngestWorkflowState state)
            throws StatefulIngestServiceException {

        AttributeSetManager attrSets = state.getAttributeSetManager();

        Set<String> aggregated = new HashSet<String>();
        

        /* First, validate cardinality of all Collection rels */
        for (AttributeSet attrs : attrSets.matches(COLLECTIONS)) {

            String collectionId =
                    getAttributeValue(COLLECTION_IDENTIFIER, attrs);

            /*
             * Collections may have at most one isPartOf or aggregatedBy
             * relation
             */
            verifyCardinalityOf(ZERO_OR_ONE,
                                COLLECTION_IS_PART_OF_COLLECTION,
                                attrs,
                                collectionId);
            verifyCardinalityOf(ZERO_OR_ONE,
                                COLLECTION_AGGREGATED_BY_PROJECT,
                                attrs,
                                collectionId);

            /*
             * Collections must have either an isPartOf relation or
             * aggregatedBy, but not both
             */
            if (hasAttribute(COLLECTION_AGGREGATED_BY_PROJECT, attrs)) {
                verifyCardinalityOf(EXACTLY_ZERO,
                                    COLLECTION_IS_PART_OF_COLLECTION,
                                    attrs,
                                    collectionId);
            } else if (hasAttribute(COLLECTION_IS_PART_OF_COLLECTION, attrs)) {
                verifyCardinalityOf(EXACTLY_ZERO,
                                    COLLECTION_AGGREGATED_BY_PROJECT,
                                    attrs,
                                    collectionId);
            } else {
                String msg =
                        String.format("Collection %s must have either an %s relationship or %s",
                                      collectionId,
                                      COLLECTION_AGGREGATED_BY_PROJECT,
                                      COLLECTION_IS_PART_OF_COLLECTION);
                throw new StatefulIngestServiceException(msg);
            }
            
            //Verify that each data item aggregation is only listed in one collection attribute set.        
            for (Attribute attr: attrs.getAttributesByName(COLLECTION_AGGREGATES_DATAITEM)) {                    
                if (aggregated.contains(attr.getValue())) {
                    throw new StatefulIngestServiceException("Failed cardinality check: A DataItem may not be aggregated by more than one collection.");
                }
                
                aggregated.add(attr.getValue());
            }

        }

        /* Verify DataItem cardinality */
        for (AttributeSet attrs : attrSets.matches(DATA_ITEMS)) {

            String dataItemId = getAttributeValue(DATA_ITEM_IDENTIFIER, attrs);

            //Data Item needs to be either Aggregated OR Part of an External Collection
            if (!aggregated.contains(dataItemId)) {                
                verifyCardinalityOf(EXACTLY_ONE,
                                    DATA_ITEM_IS_PART_OF_COLLECTION,
                                    attrs,
                                    dataItemId);
            } else {
                verifyCardinalityOf(EXACTLY_ZERO,
                                    DATA_ITEM_IS_PART_OF_COLLECTION,
                                    attrs,
                                    dataItemId);
            }
            
            verifyCardinalityOf(ONE_OR_MORE,
                                DATA_ITEM_AGGREGATES_FILE,
                                attrs,
                                dataItemId);
        }

        /* Verify Metadata File cardinality: a MdF can (optionally) describe a single object:
         * the cardinality of fedora-rels-ext:isMetadataFor is 0,1 */


        for (AttributeSet attrs : attrSets.matches(METADATA_FILES)) {

            String fileId = getAttributeValue(FILE_RESOURCEID, attrs);

            verifyCardinalityOf(ZERO_OR_ONE,
                                IS_METADATA_FOR,
                                attrs,
                                fileId);
        }
    }

    private void verifyCardinalityOf(Cardinality card,
                                     String rel,
                                     AttributeSet attrs,
                                     String objectId)
            throws StatefulIngestServiceException {

        if (!card.test(attrs.getAttributesByName(rel))) {
            String msg =
                    String.format("Failed cardinality check:  must have %s %s relationships for object %s",
                                  card.name(),
                                  rel,
                                  objectId);
            throw new StatefulIngestServiceException(msg);
        }
    }

    private boolean hasAttribute(String attr, AttributeSet attrs) {
        return !attrs.getAttributesByName(attr).isEmpty();
    }

    private String getAttributeValue(String attr, AttributeSet attrs) {
        /* We only attempt to retrieve attributes that exist, so don't worry */
        return attrs.getAttributesByName(attr).iterator().next().getValue();
    }

    static enum Cardinality {
        EXACTLY_ZERO(new Comparable<Integer>() {

            public int compareTo(Integer sz) {
                return sz.compareTo(0);
            }
        }), EXACTLY_ONE(new Comparable<Integer>() {

            public int compareTo(Integer sz) {
                return sz.compareTo(1);
            }
        }), ONE_OR_MORE(new Comparable<Integer>() {

            public int compareTo(Integer sz) {
                return sz.compareTo(1) >= 0 ? 0 : -1;
            }
        }), ZERO_OR_ONE(new Comparable<Integer>() {

            public int compareTo(Integer sz) {
                return (sz == 0 || sz == 1) ? 0 : sz.compareTo(1);
            }
        });

        private Comparable<Integer> cardinality;

        private Cardinality(Comparable<Integer> comp) {
            cardinality = comp;
        }

        public boolean test(Collection<?> stuff) {
            return cardinality.compareTo(stuff.size()) == 0;
        }
    }
}
