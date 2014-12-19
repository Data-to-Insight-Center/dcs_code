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
package org.dataconservancy.packaging.ingest.services;

import java.io.File;
import java.net.URI;

import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.PackageSerialization;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This ingest service is responsible for workflow step 'Cardinal'. Starting
 * from the root resource map of the package it walks the graph of resource maps
 * checking to make sure each is syntactically correct RDF/XML and stops on the
 * first failure.
 */
public class RDFXMLSyntaxChecker extends BaseIngestService implements ResourceMapConstants {

    @Override
    public void execute(String depositId, IngestWorkflowState state)
            throws StatefulIngestServiceException {
        super.execute(depositId, state);

        Model model = ModelFactory.createDefaultModel();
        AttributeSetManager manager = state.getAttributeSetManager();

        PackageSerialization pkg = state.getPackage().getSerialization();
        
        // Get absolute base directory containing the package as a directory.
        File base_dir = new File(pkg.getExtractDir(), pkg.getBaseDir().getPath()).getParentFile();

        URI uri = ResourceMapUtil.getPackageResourceMapURI(manager);
        Resource resmap = model.createResource(uri.toString());

        visitResourceMap(resmap, base_dir, model);
    }

    private boolean hasType(Resource subject, Resource type) {
        return subject.hasProperty(RDF.type, type);
    }

    private void visitResourceMap(Resource resmap, File base_dir, Model model)
            throws StatefulIngestServiceException {
        if (!model.contains(resmap, DESCRIBES_PROPERTY)) {
            checkAndLoad(resmap, base_dir, model);
        }

        Resource agg = resmap.getPropertyResourceValue(DESCRIBES_PROPERTY);

        if (agg == null) {
            throw new StatefulIngestServiceException("The resource map <" + resmap.getURI()
                    + "> does not descripe an aggregation.");
        }

        visitAggregation(agg, base_dir, model);
    }

    private void checkAndLoad(Resource resmap, File base_dir, Model model)
            throws StatefulIngestServiceException {
        RDFReader reader = model.getReader();
        reader.setProperty(JENA_ERROR_MODE_URI, JENA_ERROR_MODE_STRICT);
        SimpleRDFErrorHandler handler = new SimpleRDFErrorHandler();
        reader.setErrorHandler(handler);

        ResourceMapUtil.loadRDF(reader, model, base_dir, resmap.getURI());

        if (!handler.isErrorFree()) {
            throw new StatefulIngestServiceException("Error loading resource map " + resmap + ": "
                    + handler.getErrorDetails());
        }
    }

    private void visitAggregation(Resource agg, File base_dir, Model model)
            throws StatefulIngestServiceException {
        if (!model.contains(agg, AGGREGATES_PROPERTY)) {
            throw new StatefulIngestServiceException("The aggregation <" + agg.getURI()
                    + "> is not actually an aggregation.");
        }

        visitAggregatedResources(agg, base_dir, model);
    }

    private void visitAggregatedResources(Resource agg, File base_dir, Model model)
            throws StatefulIngestServiceException {
        NodeIterator iter = model.listObjectsOfProperty(agg, AGGREGATES_PROPERTY);

        while (iter.hasNext()) {
            Resource res = iter.next().asResource();

            if (res.hasProperty(IS_DESCRIBED_BY_PROPERTY)) {
                visitResourceMap(res.getPropertyResourceValue(IS_DESCRIBED_BY_PROPERTY), base_dir,
                        model);
            }
        }
    }

    private class SimpleRDFErrorHandler implements RDFErrorHandler {
        private boolean isErrorFree;
        private StringBuilder errorDetails;

        public SimpleRDFErrorHandler() {
            isErrorFree = true;
            errorDetails = new StringBuilder();
        }

        @Override
        public void warning(Exception e) {
            return;
        }

        @Override
        public void fatalError(Exception e) {
            errorDetails.append("RDF Syntax check - FATAL error:" + e.getMessage());
            isErrorFree = false;
        }

        @Override
        public void error(Exception e) {
            errorDetails.append("RDF syntax check - error: " + e.getMessage());
            isErrorFree = false;
        }

        public String getErrorDetails() {
            return errorDetails.toString();
        }

        public boolean isErrorFree() {
            return isErrorFree;
        }
    }
}
