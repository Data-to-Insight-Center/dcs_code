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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.AttributeValueType;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageSerialization;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Ingest service to walk the resource map graph of the package and extract necessary
 * attributes. This service produces AttributeSets for Business Objects.
 */
public class ResourceMapMetadataExtractor extends BaseIngestService implements ResourceMapConstants {
    @Override
    public void execute(String depositId, IngestWorkflowState state)
            throws StatefulIngestServiceException {
        super.execute(depositId, state);

        PackageSerialization pkg = state.getPackage().getSerialization();

        // Get absolute base directory containing the package as a directory.
        File base_dir = new File(pkg.getExtractDir(), pkg.getBaseDir().getPath()).getParentFile();

        Model model = ModelFactory.createDefaultModel();

        AttributeSetManager manager = state.getAttributeSetManager();

        URI uri = ResourceMapUtil.getPackageResourceMapURI(manager);
        Resource resmap = model.createResource(uri.toString());

        // Walk the graph constructing attribute sets for each object and
        // generate events.

        visitResourceMap(resmap, base_dir, model, manager);

        DcsEvent event = state.getEventManager().newEvent(
                org.dataconservancy.packaging.ingest.api.Package.Events.TRANSFORM);
        event.setDetail("AttributeSets generated for the ORE-ReM with deposit ID: " + depositId);
        event.setOutcome("Succeeded in extracting Attributes from ORE-ReM");
        DcsEntityReference ref = new DcsEntityReference(depositId);
        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
        refs.add(ref);
        event.setTargets(refs);
        state.getEventManager().addEvent(depositId, event);
    }

    private boolean hasType(Resource subject, Resource type) {
        return subject.hasProperty(RDF.type, type);
    }

    private void visitResourceMap(final Resource resmap, File base_dir, Model model,
            AttributeSetManager manager) throws StatefulIngestServiceException {

        if (!model.contains(resmap, DESCRIBES_PROPERTY)) {
            // Load the resource map and log any problems

            RDFReader reader = model.getReader();
            reader.setProperty(JENA_ERROR_MODE_URI, JENA_ERROR_MODE_STRICT);

            RDFErrorHandler handler = new RDFErrorHandler() {
                public void warning(Exception e) {
                    log.warn("Warning loading resource map: " + resmap, e);
                }

                @Override
                public void fatalError(Exception e) {
                    log.error("Fatal error loading resource map: " + resmap, e);

                }

                @Override
                public void error(Exception e) {
                    log.error("Error loading resource map: " + resmap, e);
                }
            };

            reader.setErrorHandler(handler);

            ResourceMapUtil.loadRDF(reader, model, base_dir, resmap.getURI());
        }

        Resource agg = resmap.getPropertyResourceValue(DESCRIBES_PROPERTY);

        if (agg == null) {
            throw new StatefulIngestServiceException("The resource map <" + resmap.getURI()
                    + "> does not describe an aggregation.");
        }

        visitAggregation(agg, base_dir, model, manager);
    }

    private void visitAggregation(Resource agg, File base_dir, Model model,
            AttributeSetManager manager) throws StatefulIngestServiceException {
        if (!model.contains(agg, AGGREGATES_PROPERTY)) {
            throw new StatefulIngestServiceException("The aggregation " + agg.getURI()
                    + " does not have an aggregates property.");
        }

        if (hasType(agg, DC_PACKAGE_TYPE)) {
            visitPackage(agg, base_dir, model, manager);
        } else if (hasType(agg, DC_PROJECT_TYPE)) {
            visitProject(agg, base_dir, model, manager);
        } else if (hasType(agg, DCMI_COLLECTION_TYPE)) {
            visitCollection(agg, base_dir, model, manager);
        } else if (hasType(agg, DC_DATA_ITEM_TYPE)) {
            visitDataItem(agg, base_dir, model, manager);
        } else {
            throw new StatefulIngestServiceException(
                    "Unable to find any datacons type in the aggregation " + agg.getURI());
        }
    }

    private void visitAggregatedResources(Resource agg, File base_dir, Model model,
            AttributeSetManager manager) throws StatefulIngestServiceException {
        NodeIterator iter = model.listObjectsOfProperty(agg, AGGREGATES_PROPERTY);

        while (iter.hasNext()) {
            Resource res = iter.next().asResource();

            if (res.hasProperty(IS_DESCRIBED_BY_PROPERTY)) {
                // Careful about this, this is where we could have a loop...
                visitResourceMap(res.getPropertyResourceValue(IS_DESCRIBED_BY_PROPERTY), base_dir, model,
                        manager);
            } else {
                visitByteStream(res, model, manager);
            }
        }
    }

    private void visitByteStream(Resource res, Model model, AttributeSetManager manager)
            throws StatefulIngestServiceException {
        createAttributeSet(res, model, manager, Types.File);
    }

    private void visitPackage(Resource agg, File base_dir, Model model, AttributeSetManager manager)
            throws StatefulIngestServiceException {
        visitAggregatedResources(agg, base_dir, model, manager);
        createAttributeSet(agg, model, manager, Types.Package);
    }

    private void visitProject(Resource agg, File base_dir, Model model, AttributeSetManager manager)
            throws StatefulIngestServiceException {
        visitAggregatedResources(agg, base_dir, model, manager);
        createAttributeSet(agg, model, manager, Types.Project);
    }

    private void visitCollection(Resource agg, File base_dir, Model model,
            AttributeSetManager manager) throws StatefulIngestServiceException {
        visitAggregatedResources(agg, base_dir, model, manager);
        createAttributeSet(agg, model, manager, Types.Collection);
    }

    private void visitDataItem(Resource agg, File base_dir, Model model, AttributeSetManager manager)
            throws StatefulIngestServiceException {
        visitAggregatedResources(agg, base_dir, model, manager);
        createAttributeSet(agg, model, manager, Types.DataItem);
    }

    /**
     * BusinessObject enums.
     */
    private enum Types {
        ResMap, Package, Project, Collection, DataItem, File;

        public String getString() {
            return this.toString();
        }
    }

    private void add(Collection<Attribute> attributes, String prefix, String name, String value) {
        add(attributes, AttributeValueType.STRING, prefix, name, value);
    }

    private void add(Collection<Attribute> attributes, String attr_type, String prefix,
            String name, String value) {
        attributes.add(new AttributeImpl(prefix + "-" + name, attr_type, value));
    }

    private void add(Collection<Attribute> attributes, String prefix, String name, Resource res,
            Property prop) throws StatefulIngestServiceException {
        add(attributes, AttributeValueType.STRING, prefix, name, res, prop);
    }

    /**
     * Add all objects of the given property with the given subject as
     * attributes.
     * 
     * @param attributes
     * @param prefix
     * @param name
     * @param res
     *            subject
     * @param prop
     *            subject property
     * @throws StatefulIngestServiceException
     */
    private void add(Collection<Attribute> attributes, String attr_type, String prefix,
            String name, Resource res, Property prop) throws StatefulIngestServiceException {
        StmtIterator iter = res.listProperties(prop);

        while (iter.hasNext()) {
            add(attributes, attr_type, prefix, name, iter.next().getObject());
        }
    }

    private void add(Collection<Attribute> attributes, String prefix, String name, Resource res)
            throws StatefulIngestServiceException {
        add(attributes, AttributeValueType.STRING, prefix, name, res);
    }

    private void add(Collection<Attribute> attributes, String attr_type, String prefix,
            String name, RDFNode node) throws StatefulIngestServiceException {
        String value = null;

        if (attr_type.equals(AttributeValueType.DATETIME)) {
            // Eventually could deal with the typed DateTime literal
        }

        if (node.isLiteral()) {
            value = node.asLiteral().getString();
        } else if (node.isURIResource()) {
            value = node.asResource().getURI();
        } else {
            throw new StatefulIngestServiceException("Expected resource to be literal or uri: "
                    + node);
        }

        add(attributes, attr_type, prefix, name, value);
    }

    /**
     * Creates attribute set for a given business object type and adds it to the
     * manager.
     * 
     * @param agg
     * @param manager
     * @param type
     * @throws StatefulIngestServiceException
     */
    private void createAttributeSet(Resource agg, Model model, AttributeSetManager manager,
            Types type) throws StatefulIngestServiceException {

        String attNamePrefix = type.getString();
        boolean currentSet = true;
        int attsSize = 0;

        AttributeSet attSet = null;

        if (type == Types.Package) {
            attSet = manager.getAttributeSet(AttributeSetName.ORE_REM_PACKAGE + "_" + agg.getURI());
            if (attSet == null) {
                currentSet = false;
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_PACKAGE);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.PACKAGE_RESOURCEID, AttributeValueType.STRING, agg.getURI()));
            }
        } else if (type == Types.Project) {
            attSet = manager.getAttributeSet(AttributeSetName.ORE_REM_PROJECT + "_" + agg.getURI());
            if (attSet == null) {
                currentSet = false;
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_PROJECT);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.PROJECT_RESOURCEID, AttributeValueType.STRING, agg.getURI()));
            }
        } else if (type == Types.Collection) {
            attSet = manager.getAttributeSet(AttributeSetName.ORE_REM_COLLECTION + "_"
                    + agg.getURI());
            if (attSet == null) {
                currentSet = false;
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_COLLECTION);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, agg.getURI()));
            }
        } else if (type == Types.DataItem) {
            attSet = manager
                    .getAttributeSet(AttributeSetName.ORE_REM_DATAITEM + "_" + agg.getURI());
            if (attSet == null) {
                currentSet = false;
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_DATAITEM);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, agg.getURI()));
            }
        } else if (type == Types.File) {
            attSet = manager.getAttributeSet(AttributeSetName.ORE_REM_FILE + "_" + agg.getURI());
            if (attSet == null) {
                currentSet = false;
                attSet = new AttributeSetImpl(AttributeSetName.ORE_REM_FILE);
                ((AttributeSetImpl)attSet).addAttribute(
                        new AttributeImpl(Metadata.FILE_RESOURCEID, AttributeValueType.STRING, agg.getURI()));
            }
        }

        Collection<Attribute> atts = attSet.getAttributes();
        attsSize = atts.size();

        if (agg.hasProperty(DCTerms.title) || agg.hasProperty(DC.title)) {
            add(atts, attNamePrefix, Metadata.TITLE, agg, DC.title);
            add(atts, attNamePrefix, Metadata.TITLE, agg, DCTerms.title);
        } else {
            log.debug("<title> property doesn't exist.");
        }

        if (agg.hasProperty(DCTerms.identifier) || agg.hasProperty(DC.identifier)) {
            add(atts, attNamePrefix, Metadata.IDENTIFIER, agg, DC.identifier);
            add(atts, attNamePrefix, Metadata.IDENTIFIER, agg, DCTerms.identifier);
        } else {
            log.debug("<identifier> property doesn't exist.");
        }

        // File uri should be path

        if (type == Types.File) {
            add(atts, attNamePrefix, Metadata.PATH, agg.getURI());
        }

        if (agg.hasProperty(DCTerms.creator) || agg.hasProperty(DC.creator)) {
            List<RDFNode> nodes = model.listObjectsOfProperty(agg, DC.creator).toList();
            nodes.addAll(model.listObjectsOfProperty(agg, DCTerms.creator).toList());

            for (RDFNode node : nodes) {
                if (!node.isResource()) {
                    continue;
                }

                Resource creator = node.asResource();

                add(atts, attNamePrefix, Metadata.CREATOR_NAME, creator, FOAF.name);
                add(atts, attNamePrefix, Metadata.CREATOR_EMAIL, creator, FOAF.mbox);
                add(atts, attNamePrefix, Metadata.CREATOR_PHONE, creator, FOAF.phone);
            }
        } else {
            log.debug("<creator> property doesn't exist.");
        }

        if (agg.hasProperty(DCTerms.description) || agg.hasProperty(DC.description)) {
            add(atts, attNamePrefix, Metadata.DESCRIPTION, agg, DC.description);
            add(atts, attNamePrefix, Metadata.DESCRIPTION, agg, DCTerms.description);
        } else {
            log.debug("<description> property doesn't exist.");
        }

        if (agg.hasProperty(DCTerms.created)) {
            add(atts, "DateTime", attNamePrefix, Metadata.CREATED, agg, DCTerms.created);
        } else {
            log.debug("<created> property doesn't exist");
        }

        if (agg.hasProperty(DCTerms.modified)) {
            add(atts, "DateTime", attNamePrefix, Metadata.MODIFIED, agg, DCTerms.modified);
        } else {
            log.debug("<modified> property doesn't exist");
        }

        if (agg.hasProperty(DCTerms.isPartOf)) {
            add(atts, attNamePrefix, Metadata.IS_PART_OF_COLLECTION, agg, DCTerms.isPartOf);
        } else {
            log.debug("<isPartOf> property doesn't exist");
        }

        if (agg.hasProperty(DCTerms.isVersionOf)) {
            add(atts, attNamePrefix, Metadata.IS_VERSION_OF_DATAITEM, agg, DCTerms.isVersionOf);
        } else {
            log.debug("<isVersionOf> property doesn't exist");
        }

        if (agg.hasProperty(DCTerms.format) || agg.hasProperty(DC.format)) {
            add(atts, attNamePrefix, Metadata.FORMAT, agg, DCTerms.format);
            add(atts, attNamePrefix, Metadata.FORMAT, agg, DC.format);
        } else {
            log.debug("<format> property doesn't exist");
        }

        if (agg.hasProperty(DCTerms.conformsTo)) {
            add(atts, attNamePrefix, Metadata.CONFORMS_TO, agg, DCTerms.conformsTo);
        } else {
            log.debug("<conformsTo> property doesn't exist");
        }

        if (agg.hasProperty(IS_METADATA_FOR_PROPERTY)) {
            add(atts, attNamePrefix, Metadata.IS_METADATA_FOR, agg, IS_METADATA_FOR_PROPERTY);
        } else {
            log.debug("<isMetadataFor> property doesn't exist");
        }

        if (agg.hasProperty(AGGREGATES_PROPERTY)) {
            for (RDFNode node : model.listObjectsOfProperty(agg, AGGREGATES_PROPERTY).toList()) {
                if (!node.isResource()) {
                    continue;
                }

                Resource object = node.asResource();

                if (hasType(object, DC_PROJECT_TYPE)) {
                    add(atts, attNamePrefix, Metadata.AGGREGATES_PROJECT, object);
                } else if (hasType(object, DCMI_COLLECTION_TYPE)) {
                    add(atts, attNamePrefix, Metadata.AGGREGATES_COLLECTION, object);
                } else if (hasType(object, DC_DATA_ITEM_TYPE)) {
                    add(atts, attNamePrefix, Metadata.AGGREGATES_DATAITEM, object);
                } else if (!object.hasProperty(RDF.type)) {
                    // Must be a file
                    add(atts, attNamePrefix, Metadata.AGGREGATES_FILE, object);
                }
            }
        } else {
            log.debug("<aggregates> property doesn't exist");
        }

        if (agg.hasProperty(IS_AGGREGATED_BY_PROPERTY)) {
            add(atts, attNamePrefix, Metadata.AGGREGATED_BY_PROJECT, agg, IS_AGGREGATED_BY_PROPERTY);
        } else {
            log.debug("<isAggregateBy> property doesn't exist");
        }

        add(atts, attNamePrefix, Metadata.PROPERTY, agg);

        if (atts.size() > attsSize) {
            if (!currentSet) {
                manager.addAttributeSet(attSet.getName() + "_" + agg.getURI(), attSet);
            } else {
                manager.updateAttributeSet(attSet.getName() + "_" + agg.getURI(), attSet);
            }
        }

        else {
            throw new StatefulIngestServiceException("Unable to extract any attributes.");
        }
    }
}
