package org.dataconservancy.registry.http;

/**
 * Keys used to access objects in the Spring ModelAndView.
 */
enum RegistryModelAttribute {

    /**
     * The RegistryEntryHolder containing the entry to serialize in the response.  If the holder is empty,
     * then a 404 will be returned.  If the holder is not empty, a 200 will be returned along with the serialized
     * RegistryEntry.
     */
    ENTRY,

    /**
     * A ReferencesHolder containing URLs to be serialized in the response.  If the Set is null or empty, a 404 will be
     * returned.
     */
    REFS,

    /**
     * The requested registry entry identifier.  May be null.
     */
    ENTRY_ID,

    /**
     * The requested registry entry type.  May be null.
     */
    ENTRY_TYPE,

}
