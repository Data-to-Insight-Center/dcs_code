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
package org.dataconservancy.registry.shared.test;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.junit.Before;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Base test harness for Registry implementations that are backed by the Query Framework.
 */
public abstract class AbstractQueryFrameworkRegistryTest<T> extends AbstractRegistryTest<T> {

    private boolean isSeeded = false;

    private SolrServer solrServer;

    @Before
    public void setUp() throws IOException, SAXException, ParserConfigurationException, InvalidXmlException, IndexServiceException {

        // Verify that a solr.solr.home system property exists, that the path it points to exists, and that the
        // path is readable.  This assumes that the solr home directory has been created using the maven resources plugin
        assertNotNull("System property 'solr.solr.home' must be defined!", System.getProperty("solr.solr.home"));
        final FileSystemResource solrHome = new FileSystemResource(System.getProperty("solr.solr.home"));
        assertTrue("Solr home directory (a Spring Resource) does not exist: " + solrHome, solrHome.exists());
        assertTrue("Solr home directory (a Spring Resource) is not readable or isn't a directory: "
                + solrHome.getFile().getAbsolutePath(), solrHome.getFile().canRead() && solrHome.getFile().isDirectory());

        log.info("Solr home directory is " + solrHome.getFile().getAbsolutePath());
        if (!isSeeded) {
            log.info("Seeding index in {}", solrHome.getFile().getAbsolutePath());
            seedIndex(getIndexService());
            isSeeded = true;
        }

        underTest = getUnderTest();
    }

    /**
     * Return a <code>Collection</code> of DCP objects to index.  This method will be called once per test class
     * instantiation (e.g. in a JUnit {@link org.junit.BeforeClass} method).
     * <p/>
     * The DCP objects returned by this method are used to seed the search index,
     *
     * @return a collection of Dcp objects to be indexed
     */
    protected abstract Collection<Dcp> getDcpsToIndex();

    /**
     * Seed the provided index.  This method will be called once per test class instantiation (e.g. in a JUnit
     * {@link org.junit.BeforeClass} method).
     * <p/>
     * By default this method calls {@link #getDcpsToIndex()}, and indexes each DCP using a {@link org.dataconservancy.dcs.index.api.BatchIndexer} obtained
     * from the <code>indexService</code>
     *
     * @param indexService the index service
     */
    protected void seedIndex(IndexService<Dcp> indexService) {

        final Collection<Dcp> toIndex = getDcpsToIndex();
        assertTrue("Error: there were no DCP packages found to index!", toIndex.size() > 0);

        BatchIndexer<Dcp> indexer = null;
        try {
            indexer = indexService.index();
            for (Dcp dcp : toIndex) {
                log.info("Indexing DCP: {}", dcp);
                indexer.add(dcp);
            }
        } catch (IndexServiceException e) {
            final String msg = "Error seeding the test index: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            try {
                if (indexer != null) {
                    indexer.close();
                }
            } catch (IndexServiceException e) {
                final String msg = "Error close()ing the index: " + e.getMessage();
                log.error(msg, e);
            }
        }
    }

    /**
     * Instantiate an IndexService which will be used to seed the index for this test suite.  This method will be called
     * once per test class instantiation (e.g. in a JUnit {@link org.junit.BeforeClass} method).
     * <p/>
     * By default this method instantiates an embedded Solr server, and returns a {@link org.dataconservancy.dcs.index.dcpsolr.DcpIndexService}.  This implementation
     * expects to find a Solr configuration on the classpath, and to have the <code>solr.solr.home</code> system property
     * set.
     * <p/>
     * Subclasses are free override this method.
     *
     * @return a configured IndexService
     */
    protected IndexService<Dcp> getIndexService() {
        // Instantiate a DcpIndexService so we can seed an embedded Solr instance with indexed DCPs
        final SolrService solrService = new SolrService(getSolrServer());
        return new DcpIndexService(solrService);
    }

    protected SolrServer getSolrServer() {
        if (solrServer != null) {
            return solrServer;
        }
        
        final CoreContainer container;
        try {
            final String solrHome = System.getProperty("solr.solr.home");
            container = new CoreContainer(solrHome, new File(solrHome, "solr.xml"));
        } catch (Exception e) {
            final String msg = "Unable to instantiate Solr Core Container: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        final EmbeddedSolrServer solrServer = new EmbeddedSolrServer(container, "default");
        return solrServer;
    }

//    @Test
//    public void testRetrieveDcp() throws QueryServiceException {
//
//        for (Dcp expectedDcp : getDcpsToIndex()) {
//            for (DcsEntity entity : expectedDcp) {
//                T domainObject = underTest.get(entity.getId());
//            }
//        }
//
//
//        // Retrieve a DCP by DeliverableUnit id
//        final String duId = "664d0819-f1fe-4e8c-ae04-5cc9fb4bc45c";
//        QueryResult<Dcp> results = underTest.query("id:" + duId, 0, Integer.MAX_VALUE);
//        assertNotNull(results);
//        assertNotNull(results.getMatches());
//        assertEquals(1, results.getMatches().size());
//        assertEquals(1, results.getTotal());
//        assertEquals(0, results.getOffset());
//
//        final Dcp dcp = results.getMatches().get(0).getObject();
//        assertNotNull(dcp);
//        assertFalse(dcp.getDeliverableUnits().isEmpty());
//        assertFalse(dcp.getManifestations().isEmpty());
//        assertFalse(dcp.getFiles().isEmpty());
//        assertEquals(duId, dcp.getDeliverableUnits().iterator().next().getId());
//
//        // Retrieve the same DCP by Manifestation id
//        final String manId = "459e1d79-a137-4825-8df1-2696b2177aac";
//        results = underTest.query("id:" + manId, 0, Integer.MAX_VALUE);
//        assertNotNull(results);
//        assertNotNull(results.getMatches());
//        assertEquals(1, results.getMatches().size());
//        assertEquals(1, results.getTotal());
//        assertEquals(0, results.getOffset());
//
//        assertEquals(dcp, results.getMatches().get(0).getObject());
//        assertEquals(manId, results.getMatches().get(0).getObject().getManifestations().iterator().next().getId());
//
//        // Retrieve the same DCP by File id
//        final String fileId = "08981f34-7e84-4d17-9fec-b85d0c3e34f1";
//        results = underTest.query("id:" + fileId, 0, Integer.MAX_VALUE);
//        assertNotNull(results);
//        assertNotNull(results.getMatches());
//        assertEquals(1, results.getMatches().size());
//        assertEquals(1, results.getTotal());
//        assertEquals(0, results.getOffset());
//
//        assertEquals(dcp, results.getMatches().get(0).getObject());
//        assertEquals(fileId, results.getMatches().get(0).getObject().getFiles().iterator().next().getId());
//    }

}
