package org.dataconservancy.registry.api.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;

/**
 * Utility methods for resolving registry-related resources.
 */
public class ResourceResolverUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceResolverUtil.class);

    static final String CLASSPATH_PREFIX = "classpath:";

    static final String WILDCARD_CLASSPATH_PREFIX = "classpath*:";

    static final String FILE_URL_PREFIX = "file:/";

    /**
     * Attempt to resolve a DcsFile source to a Spring Resource.
     * <p/>
     * If the source starts with "classpath:" or "classpath*:", the source is interpreted as a classpath resource.  If
     * the source starts with a forward slash, it is interpreted as a file system resource.  Otherwise, the source is
     * interpreted as a URL resource.
     *
     * @param source the DcsFile source
     * @return a Spring Resource abstracting the DcsFile source
     * @throws java.net.MalformedURLException
     * @throws IllegalArgumentException if {@code source} is null
     */
    public static Resource resolveFileSource(String source) throws MalformedURLException {
        Resource r = null;

        if (source == null) {
            IllegalArgumentException iae = new IllegalArgumentException("source string must not be null.");
            LOG.debug("Refusing to resolve null source.", iae);
            throw iae;
        } else {
            LOG.debug("Attempting to resolve source {}", source);
        }

        if (source.startsWith(WILDCARD_CLASSPATH_PREFIX)) {
            r = new ClassPathResource(source.substring(WILDCARD_CLASSPATH_PREFIX.length()));
        } else if (source.startsWith(CLASSPATH_PREFIX)) {
            r = new ClassPathResource(source.substring(CLASSPATH_PREFIX.length()));
        } else if (source.startsWith("/")) {
            r = new FileSystemResource(source);
        } else if ( source.charAt(1) == ':' && source.charAt(2) == '\\') { //Check for windows file path
            r = new FileSystemResource(source);
        } else {
            r = new UrlResource(source);
        }

        LOG.debug("Resolved '{}' to '{}'", source, r);

        return r;
    }

}
