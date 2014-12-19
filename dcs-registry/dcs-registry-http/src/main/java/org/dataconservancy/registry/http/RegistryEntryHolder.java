package org.dataconservancy.registry.http;

import org.dataconservancy.registry.api.support.RegistryEntry;

/**
 * Holds a single registry entry.  The purpose of this class is to provide for null semantics in the model.  If
 * an instance of the RegistryEntryHolder is placed in the model and the Holder instance doesn't contain a
 * RegistryEntry, then we know that an attempt was made to look up an entry and it wasn't found.
 */
class RegistryEntryHolder {

    /** The RegistryEntry Id */
    String id;

    /** The RegistryEntry Type */
    String type;

    /** The RegistryEntry itself */
    RegistryEntry<?> entry;

}
