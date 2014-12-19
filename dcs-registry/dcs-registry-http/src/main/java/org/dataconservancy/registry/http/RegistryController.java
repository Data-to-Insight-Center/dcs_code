package org.dataconservancy.registry.http;

import org.dataconservancy.dcs.util.http.RequestUtil;
import org.dataconservancy.registry.api.support.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * The entry point into the Data Conservancy Registry HTTP API.
 */
@Controller
@RequestMapping("/registry")
public class RegistryController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The HTTP request parameter used to identify a RegistryEntry
     */
    private final String ID_REQUEST_PARM = "id";

    /**
     * The HTTP request parameter used to identify the type of Registry to be queried
     */
    private final String REGISTRY_REQUEST_PARAM = "registry";

    /**
     * Part of the HTTP request URI that indicates a single RegistryEntry is being requested.
     *
     * @see #getRegistryEntry(javax.servlet.http.HttpServletRequest, String, String)
     */
    private final String ENTRY_PATH_PART = "entry";

    /**
     * Part of the HTTP request URI that indicates a Set of URL references to RegistryEntries are being requested.
     *
     * @see #getRegistryEntries(javax.servlet.http.HttpServletRequest, String)
     */
    private final String ENTRIES_PATH_PART = "entries";

    /**
     * Part of the HTTP request URI that indicates a Set of URL references to Registries exposed by this API are
     * being requested.
     *
     * @see #getRegistries(javax.servlet.http.HttpServletRequest)
     */
    private final String TYPES_PATH_PART = "types";

    /**
     * Used to reconstruct the request URI
     */
    @Autowired
    private RequestUtil requestUtil;

    /**
     * Facade over configured {@code Registry&lt;T>} instances
     */
    @Autowired
    private RegistryService registryService;

    public RegistryController() {

    }

    public RegistryController(RegistryService registryService, RequestUtil requestUtil) {
        this.registryService = registryService;
        this.requestUtil = requestUtil;
    }

    /**
     * Handles requests for individual registry entries using their identifier. Requests are expected to be in the form
     * {@code http://dataconservancy.org/dcs/registry/entry?id=&lt;registry entry id>&amp;registry=&lt;registry type string>}
     * <p/>
     * <table>
     * <tr>
     * <th>HTTP Status Code</th><th>Description</th>
     * </tr>
     * <tr>
     * <td>200</td><td>Success.  Entity body contains the requested registry entry or references.</td>
     * </tr>
     * <tr>
     * <td>300</td><td>Success.  Entity body contains references to the requested entries.</td>
     * </tr>
     * <tr>
     * <td>404</td><td>Failure.  The requested registry entry could not be found; the entity body will be
     * empty.</td>
     * </tr>
     * <tr>
     * <td>503</td><td>Failure.  The requested entry may exist, but a server error occurred and it could not be
     * returned; the entity body will be empty</td>
     * </tr>
     * </table>
     *
     * @return
     */
    @RequestMapping(value = "/entry", method = {GET})
    public Model getRegistryEntry(HttpServletRequest request,
                                  @RequestParam(ID_REQUEST_PARM) String entryId,
                                  @RequestParam(value = REGISTRY_REQUEST_PARAM, required = false) String entryType) {

        Model model = new ExtendedModelMap();

        model.addAttribute(RegistryModelAttribute.ENTRY_ID.name(), entryId);

        // If the optional 'entryType' parameter is supplied, or if there is only one entry for the supplied
        // 'entryId', populate the Set of RegistryEntry objects in the model.
        if (entryType != null && entryType.trim().length() > 0) {
            RegistryEntryHolder holder = new RegistryEntryHolder();
            holder.id = entryId;
            holder.type = entryType;
            holder.entry = registryService.getEntry(entryId, entryType);
            model.addAttribute(RegistryModelAttribute.ENTRY.name(), holder);
            model.addAttribute(RegistryModelAttribute.ENTRY_TYPE.name(), entryType);
        } else if (registryService.getEntriesById(entryId).size() == 1) {
            RegistryEntryHolder holder = new RegistryEntryHolder();
            holder.id = entryId;
            holder.type = entryType;
            holder.entry = registryService.getEntriesById(entryId).iterator().next();
            model.addAttribute(RegistryModelAttribute.ENTRY.name(), holder);
        } else {
            // Otherwise, we populate a Set of URL references to RegistryEntry objects in the model.
            ReferencesHolder holder = new ReferencesHolder();
            Set<URL> entryRefs = new HashSet<URL>();
            for (RegistryEntry<?> entry : registryService.getEntriesById(entryId)) {
                URL u = toUrlReference(entry, request);
                if (u != null) {
                    entryRefs.add(u);
                }
            }

            holder.references = entryRefs;
            holder.statusCode = 300; // because the client asked for a single entry but wasn't specific enough (e.g. the semantics of the response to do not align with the method invoked)

            model.addAttribute(RegistryModelAttribute.REFS.name(), holder);
        }

        return model;
    }

    @RequestMapping(value = "/entries", method = {GET})
    public Model getRegistryEntries(HttpServletRequest request,
                                    @RequestParam(REGISTRY_REQUEST_PARAM) String entryType) {

        Model model = new ExtendedModelMap();

        Set<RegistryEntry<?>> entries = registryService.getEntriesByType(entryType);
        model.addAttribute(RegistryModelAttribute.ENTRY_TYPE.name(), entryType);

        if (entries == null) {
            return model;
        }

        // Populate a Set of URL references to RegistryEntry objects in the model.
        Set<URL> entryRefs = new HashSet<URL>();

        for (RegistryEntry<?> entry : entries) {
            URL u = toUrlReference(entry, request);
            if (u != null) {
                entryRefs.add(u);
            }
        }

        ReferencesHolder holder = new ReferencesHolder();
        holder.statusCode = 200; // because a list of entries is what the client requested (e.g. the semantics of the response align with the method invoked)
        holder.references = entryRefs;
        model.addAttribute(RegistryModelAttribute.REFS.name(), holder);
        return model;
    }

    @RequestMapping(value = "/types", method = {GET})
    public Model getRegistries(HttpServletRequest request) {

        Set<URL> registryRefs = new HashSet<URL>();
        for (String registryType : registryService.getRegistryTypes()) {
            registryRefs.add(toUrlReference(registryType, request));
        }

        ReferencesHolder holder = new ReferencesHolder();
        holder.statusCode = 200; // because the list of registries is what the client requested (e.g. the semantics of the response align with the method invoked)
        holder.references = registryRefs;

        Model model = new ExtendedModelMap();
        model.addAttribute(RegistryModelAttribute.REFS.name(), holder);

        return model;

    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public RequestUtil getRequestUtil() {
        return requestUtil;
    }

    public void setRequestUtil(RequestUtil requestUtil) {
        this.requestUtil = requestUtil;
    }

    /**
     * Converts a RegistryEntry to a URL.  The URL can be dereferenced without any processing on the client side to
     * retrieve a representation of the RegistryEntry.
     *
     * @param entry
     * @param request
     * @return
     */
    private URL toUrlReference(RegistryEntry<?> entry, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            String requestUrl = requestUtil.buildRequestUrl(request);
            if (requestUrl.endsWith(ENTRIES_PATH_PART)) {
                requestUrl = requestUrl.replace(ENTRIES_PATH_PART, ENTRY_PATH_PART);
            }
            sb.append(requestUrl);

            sb.append("?").append(ID_REQUEST_PARM).append("=").append(entry.getId());
            sb.append("&").append(REGISTRY_REQUEST_PARAM).append("=").append(entry.getEntryType());
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            log.debug("Unable to create URL reference ({}) to registry entry with id: {} and type: {}: {}",
                    new Object[]{sb.toString(), entry.getId(), entry.getEntryType(), e.getMessage()}, e);
        }

        return null;
    }

    /**
     * Converts a String representing a registry type to a URL.  The URL can be dereferenced without any processing on
     * the client side to retrieve references to all entries in the registry type.
     *
     * @param registryType
     * @param request
     * @return
     */
    private URL toUrlReference(String registryType, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            String requestUrl = requestUtil.buildRequestUrl(request);
            if (requestUrl.endsWith(TYPES_PATH_PART)) {
                requestUrl = requestUrl.replace(TYPES_PATH_PART, ENTRIES_PATH_PART);
            }
            sb.append(requestUrl);

            sb.append("?").append(REGISTRY_REQUEST_PARAM).append("=").append(registryType);
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            log.debug("Unable to create URL reference ({}) to registry with type: {}: {}",
                    new Object[]{sb.toString(), registryType, e.getMessage()}, e);
        }

        return null;
    }

}
