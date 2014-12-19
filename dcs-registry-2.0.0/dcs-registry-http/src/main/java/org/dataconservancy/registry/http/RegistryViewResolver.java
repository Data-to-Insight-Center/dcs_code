package org.dataconservancy.registry.http;

import org.dataconservancy.dcs.spring.mvc.NotFoundView;
import org.dataconservancy.dcs.spring.mvc.ServletRequestAttributesSource;
import org.dataconservancy.dcs.spring.mvc.ViewKey;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Custom view resolver which consults the Spring ModelAndView to determine the appropriate response.
 */
public class RegistryViewResolver implements ViewResolver {

    /**
     * Returns a 404.  Looks up the reason phrase from the ModelAndView using the {@code ViewKey#REASON_PHRASE}.
     */
    private static final NotFoundView notFoundView = new NotFoundView();

    /**
     * Returns a 200 and a serialized RegistryEntry as XML.
     */
    private static final RegistryEntryView registryEntryView = new RegistryEntryView();

    /**
     * Returns a 300 and a set of URLs to RegistryEntries.
     */
    private static final EntryReferencesView entryReferencesView = new EntryReferencesView();

    /**
     * The reason phrase to use when a RegistryEntry or entries are not found.
     */
    private static final String NOT_FOUND_REASON_PHRASE = "Registry entry ID: [%s] Type: [%s] not found.";

    /**
     * Provides access to ServletAttributes which contain the Spring ModelAndView.  The ModelAndView is placed there by
     * the {@code ModelExposingHandlerInterceptor}
     */
    private final ServletRequestAttributesSource requestAttributesSource;

    public RegistryViewResolver(ServletRequestAttributesSource requestAttributesSource) {
        this.requestAttributesSource = requestAttributesSource;
    }

    /**
     * Resolves the proper View based on the attributes in the Spring ModelAndView.
     *
     * @param viewName
     * @param locale
     * @return
     * @throws Exception
     */
    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        final ModelAndView mv = (ModelAndView) requestAttributesSource.getRequestAttributes()
                        .getAttribute(ViewKey.MODEL_AND_VIEW.name(), RequestAttributes.SCOPE_REQUEST);
        final Map<String,Object> model = mv.getModel();

        // Populate the reason phrase for the 404 response if the registry entry (or entries) wasn't found
        model.put(ViewKey.REASON_PHRASE.name(), String.format(NOT_FOUND_REASON_PHRASE,
                model.get(RegistryModelAttribute.ENTRY_ID.name()),
                model.get(RegistryModelAttribute.ENTRY_TYPE.name())));


        // If there is a RegistryEntryHolder, and it contains an entry, return a RegistryEntryView.  Otherwise return
        // a NotFoundView.
        final RegistryEntryHolder holder = (RegistryEntryHolder) model.get(RegistryModelAttribute.ENTRY.name());
        if (holder != null && holder.entry != null) {
            return registryEntryView;
        } else if (holder != null) {
            return notFoundView;
        }

        @SuppressWarnings("unchecked")
        ReferencesHolder refHolder = (ReferencesHolder) model.get(RegistryModelAttribute.REFS.name());
        if (refHolder != null) {
            if (refHolder.references != null & !refHolder.references.isEmpty()) {
                return entryReferencesView;
            }
        }

        return notFoundView;
    }
}
