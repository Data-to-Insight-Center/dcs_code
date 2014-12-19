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

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Constants needed for interpreting resource maps in a package.
 */
public interface ResourceMapConstants {
    public static final String ORE_NS_URI = "http://www.openarchives.org/ore/terms/";

    public static final String FEDORA_RELS_EXT_NS_URI = "info:fedora/fedora-system:def/relations-external#";

    public static final String DCMI_TYPES_NS_URI = "http://purl.org/dc/dcmitype/";

    public static final String DATACONS_NS_URI = "http://dataconservancy.org/ns/types/";

    public static final Resource RESOURCE_MAP_TYPE = ResourceFactory
            .createResource(ORE_NS_URI + "ResourceMap");

    public static final Resource AGGREGATION_TYPE = ResourceFactory.createResource(ORE_NS_URI + "Aggregation");
    
    public static final Resource DC_PROJECT_TYPE = ResourceFactory.createResource(DATACONS_NS_URI + "Project");
    
    public static final Resource DC_PACKAGE_TYPE = ResourceFactory.createResource(DATACONS_NS_URI + "Package");

    public static final Resource DC_DATA_ITEM_TYPE = ResourceFactory.createResource(DATACONS_NS_URI + "DataItem");

    public static final Resource DCMI_COLLECTION_TYPE = ResourceFactory.createResource(DCMI_TYPES_NS_URI + "Collection");

    public static final Property AGGREGATES_PROPERTY = ResourceFactory.createProperty(ORE_NS_URI, "aggregates");
    
    public static final Property IS_AGGREGATED_BY_PROPERTY = ResourceFactory.createProperty(ORE_NS_URI, "isAggregatedBy");
    
    public static final Property DESCRIBES_PROPERTY = ResourceFactory.createProperty(ORE_NS_URI, "describes");
    
    public static final Property IS_DESCRIBED_BY_PROPERTY = ResourceFactory.createProperty(ORE_NS_URI, "isDescribedBy");
    
    public static final Property IS_METADATA_FOR_PROPERTY = ResourceFactory.createProperty(FEDORA_RELS_EXT_NS_URI, "isMetadataFor");

    /**
     * URI for Jena's error-mode property
     */
    String JENA_ERROR_MODE_URI = "http://jena.hpl.hp.com/arp/properties/error-mode";

    /**
     * Jena Strict error handling
     */
    String JENA_ERROR_MODE_STRICT = "strict";
}
