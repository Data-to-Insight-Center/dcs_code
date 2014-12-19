package org.dataconservancy.registry.api.support;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Insures that the ResourceResolverUtil properly resolves resources
 */
public class ResourceResolverUtilTest {

    private static final String ROOT_RESOURCE = "/rootResource.txt";

    private static final String ROOT_CONTENT = "root";

    private static final String SUPPORT_RESOURCE = "/org/dataconservancy/registry/api/support/supportResource.txt";

    private static final String SUPPORT_CONTENT = "support";

    @Before
    public void setUp() throws Exception {
        // Verify the assumption that our two resources actually exist
        assertNotNull(this.getClass().getResource(ROOT_RESOURCE));
        assertNotNull(this.getClass().getResource(SUPPORT_RESOURCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveNullResource() throws Exception {
        ResourceResolverUtil.resolveFileSource(null);
    }

    @Test
    public void testResolveClasspathResource() throws Exception {
        Resource r = ResourceResolverUtil.resolveFileSource(generateClasspathResource(ROOT_RESOURCE));
        assertNotNull(r);
        assertEquals(ROOT_CONTENT, IOUtils.toString(r.getInputStream()).trim());

        r = ResourceResolverUtil.resolveFileSource(generateClasspathResource(SUPPORT_RESOURCE));
        assertNotNull(r);
        assertEquals(SUPPORT_CONTENT, IOUtils.toString(r.getInputStream()).trim());
    }

    @Test
    public void testWildcardClasspathResource() throws Exception {
        Resource r = ResourceResolverUtil.resolveFileSource(generateWildcardClasspathResource(ROOT_RESOURCE));
        assertNotNull(r);
        assertEquals(ROOT_CONTENT, IOUtils.toString(r.getInputStream()).trim());

        r = ResourceResolverUtil.resolveFileSource(generateWildcardClasspathResource(SUPPORT_RESOURCE));
        assertNotNull(r);
        assertEquals(SUPPORT_CONTENT, IOUtils.toString(r.getInputStream()).trim());
    }

    @Test
    public void testFilesystemClasspathResource() throws Exception {
        Resource r = ResourceResolverUtil.resolveFileSource(generateFileResource(ROOT_RESOURCE));
        assertNotNull(r);
        assertEquals(ROOT_CONTENT, IOUtils.toString(r.getInputStream()).trim());

        r = ResourceResolverUtil.resolveFileSource(generateFileResource(SUPPORT_RESOURCE));
        assertNotNull(r);
        assertEquals(SUPPORT_CONTENT, IOUtils.toString(r.getInputStream()).trim());
    }

    @Test(timeout = 5000)
    @Ignore("TODO: fails locally on esm's Mac OS, waiting to see if it fails on other platforms.")
    public void testUrlResource() throws Exception {
        Resource r = ResourceResolverUtil.resolveFileSource(generateFileUrlResource(ROOT_RESOURCE));
        assertNotNull(r);
        assertEquals(ROOT_CONTENT, IOUtils.toString(r.getInputStream()).trim());

        r = ResourceResolverUtil.resolveFileSource(generateFileUrlResource(SUPPORT_RESOURCE));
        assertNotNull(r);
        assertEquals(SUPPORT_CONTENT, IOUtils.toString(r.getInputStream()).trim());
    }

    private String generateClasspathResource(String resource) {
        return ResourceResolverUtil.CLASSPATH_PREFIX + resource;
    }

    private String generateFileUrlResource(String resource) {
        return ResourceResolverUtil.FILE_URL_PREFIX + generateFileResource(resource);
    }

    private String generateWildcardClasspathResource(String resource) {
        return ResourceResolverUtil.WILDCARD_CLASSPATH_PREFIX + resource;
    }

    private String generateFileResource(String resource) {
        URL u = this.getClass().getResource(resource);
        assertNotNull(u);
        return new File(u.getPath()).getAbsolutePath();
    }
}
