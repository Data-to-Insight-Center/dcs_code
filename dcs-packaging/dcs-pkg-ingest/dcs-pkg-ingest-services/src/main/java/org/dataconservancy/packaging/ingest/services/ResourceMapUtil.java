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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.AttributeSetName;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.shared.JenaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceMapUtil {

    private static Logger LOG = LoggerFactory.getLogger(ResourceMapUtil.class);

    /**
     * Using the given reader, load RDF into a model.
     * 
     * If the given rdf uri has a file scheme, it is treated as relative to the
     * base directory.
     * 
     * @param reader
     * @param model
     * @param base_dir
     * @param rdf_uri
     * @throws StatefulIngestServiceException
     */
    public static void loadRDF(RDFReader reader, Model model, File base_dir, String rdf_uri)
            throws StatefulIngestServiceException {
        URI uri;

        try {
            uri = resolveURI(base_dir, new URI(rdf_uri));
        } catch (URISyntaxException e) {
            throw new StatefulIngestServiceException("Resource map uri invalid: " + rdf_uri, e);
        }

        URL url;

        try {
            url = uri.toURL();
        } catch (IllegalArgumentException e) {
            throw new StatefulIngestServiceException("Error creating resource map url: " + uri, e);
        } catch (MalformedURLException e) {
            throw new StatefulIngestServiceException("Error creating resource map url: " + uri, e);
        }

        try {
            reader.read(model, url.toString());
        } catch (JenaException e) {
            throw new StatefulIngestServiceException("Error reading resource map url: " + url, e);
        }
    }

    /**
     * If the uri has a file scheme, return a new absolute file url joining it
     * to the base directory. Otherwise return the uri.
     * 
     * @param base_dir
     * @param uri
     * @return
     */
    public static URI resolveURI(File base_dir, URI uri) {
        if (uri.getScheme().equals("file")) {
            return new File(base_dir, uri.getPath()).toURI();
        }

        return uri;
    }

    /**
     * Return the resource map uri of the package by looking up the
     * corresponding attribute.
     * 
     * @param manager
     * @return
     * @throws StatefulIngestServiceException
     */
    public static URI getPackageResourceMapURI(AttributeSetManager manager)
            throws StatefulIngestServiceException {
        AttributeSet as = manager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);

        if (as == null) {
            throw new StatefulIngestServiceException("Could not find attribute set");
        }

        String uri = null;

        for (Attribute attr : as.getAttributesByName("PKG-ORE-REM")) {
            uri = attr.getValue();
        }

        if (uri == null) {
            throw new StatefulIngestServiceException("Could not find resource map uri");
        }

        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new StatefulIngestServiceException("Resource map uri not well formed.", e);
        }
    }

    /**
     * Loads all of the resource maps and returns a unified Model of their contents.  This method starts by loading the
     * resource map located at {@code resourceMapUri} (resolved against {@code baseDir}).  Then, every resource
     * referenced by the {@code &lt;ore:isDescribedBy rdf:resource="file:///...."/>} predicate is resolved and loaded
     * recursively.
     * <p/>
     * If a package named {@code my-bag.tar.gz} is unpacked to {@code /storage/bags/my-bag}, then the base directory
     * parameter should be {@code /storage/bags}.  If the resource map is located at
     * {@code /storage/bags/my-bag/ORE-REM/rem.xml}, then the {@code resourceMapUri} should be
     * {@code file:///my-bag/ORE-REM/rem.xml}.
     *
     * @param resourceMapUri the resource map URI of the package (from {@link #getPackageResourceMapURI(org.dataconservancy.packaging.ingest.api.AttributeSetManager)})
     * @param baseDir the base directory of the package in the file system
     * @return a unified Model, composed of all of the ORE resource maps in the package
     * @throws StatefulIngestServiceException
     */
    public static Model loadRems(URI resourceMapUri, File baseDir) throws StatefulIngestServiceException {
        List<String> loadedRems = new ArrayList<String>();
        loadedRems.add(resourceMapUri.toString());

        Model m = ModelFactory.createDefaultModel();
        ResourceMapUtil.loadRDF(m.getReader(), m, baseDir, resourceMapUri.toString());
        LOG.debug("Loaded ReM file {}", resourceMapUri);
        m = load(m, baseDir, loadedRems);
        return m;
    }

    /**
     * Recursively loads all resources into a unified Model.  Resources that are objects of
     * {@code &lt;ore:isDescribedBy.../>} predicates are parsed and loaded.
     *
     * @param m the model
     * @param baseDir the base directory against which resources are resolved
     * @param loadedRems A list of files containing the rems loaded so far
     * @throws StatefulIngestServiceException
     */
    private static Model load(Model m, File baseDir, List<String> loadedRems) throws StatefulIngestServiceException {
        Selector isDescribedBySelector =
                new SimpleSelector(null, ResourceMapConstants.IS_DESCRIBED_BY_PROPERTY, (Object) null);

        StmtIterator sItr = m.listStatements(isDescribedBySelector);
        while (sItr.hasNext()) {
            final Statement stmt = sItr.next();
            final String rdfUri = stmt.getObject().toString();
            if (loadedRems.contains(rdfUri)) {
                LOG.trace("Already loaded ReM from {}", rdfUri);
                continue;
            } else {
                LOG.debug("Loading ReM file {}", rdfUri);
            }

            Model loaded = ModelFactory.createDefaultModel();
            RDFReader reader = loaded.getReader();
            reader.setProperty(ResourceMapConstants.JENA_ERROR_MODE_URI, ResourceMapConstants.JENA_ERROR_MODE_STRICT);
            ResourceMapUtil.loadRDF(reader, loaded, baseDir, rdfUri);
            loadedRems.add(rdfUri);
            m = load(m.union(loaded), baseDir, loadedRems);
        }

        return m;
    }
}
