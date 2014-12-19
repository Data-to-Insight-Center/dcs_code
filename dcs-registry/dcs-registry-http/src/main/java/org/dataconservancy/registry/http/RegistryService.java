package org.dataconservancy.registry.http;

import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.api.support.RegistryEntry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates Registry business logic invoked by the RegistryController.  Provides a facade over configured
 * {@code Registry&lt;T>} instances.
 */
public class RegistryService {

    /**
     * A Map of registry types to Registry instances.  There can only be a single Registry instance per type.
     */
    private Map<String, Registry<?>> registries;

    /**
     * Constructs a facade over the supplied Registry instances.
     *
     * @param registries a Map keyed by strings representing Registry types to Registry instances
     */
    public RegistryService(Map<String, Registry<?>> registries) {
        this.registries = registries;
    }

    /**
     * Retrieve a single entry from a Registry.  Since an id may be used across multiple registries, the registry type
     * must be supplied to insure a single entry being returned.
     *
     * @param entryId the identifier of the registry entry in the Registry instance identified by {@code entryType}
     * @param entryType the type of Registry instance
     * @return the RegistryEntry, or null if it isn't found.
     */
    public RegistryEntry<?> getEntry(String entryId, String entryType) {
        Object domainObject = registries.get(entryType).get(entryId);
        if (domainObject == null) {
            return null;
        }
        return adapt(entryId, entryType, domainObject);
    }

    /**
     * Queries multiple registries for entries with the supplied identifier.  Since an entry's identifier may be used
     * across multiple registries, this method can return multiple RegistryEntry objects.
     *
     * @param entryId the identifier of the registry entry to return
     * @return a Set of RegistryEntry objects of multiple Registry types.
     */
    public Set<RegistryEntry<?>> getEntriesById(String entryId) {
        Set<RegistryEntry<?>> results = new HashSet<RegistryEntry<?>>();
        for (Registry<?> registry : registries.values()) {
            if (registry.containsKey(entryId)) {
                results.add(adapt(entryId, registry.getEntryType(), registry.get(entryId)));
            }
        }

        return results;
    }

    /**
     * Queries a single registry for all of its entries.  The entries are not sorted.
     *
     * @param entryType the type of Registry instance to query
     * @return the Registry entries in the identified Registry type.  May be null if the Registry type {@code entryType}
     *         doesn't exist.  The returned Set may also be empty.
     */
    public Set<RegistryEntry<?>> getEntriesByType(String entryType) {
        Set<RegistryEntry<?>> results = new HashSet<RegistryEntry<?>>(1024);
        Registry<?> registry = registries.get(entryType);

        if (registry == null) {
            // No Registry for the supplied type exists
            return null;
        }

        for (Map.Entry<String, ?> entry : registry.entrySet() ) {
            results.add(adapt(entry.getKey(), entryType, entry.getValue()));
        }
        return results;
    }

    /**
     * Obtains all Registry types that are served by this facade.  Any of the returned String types can be used as
     * valid {@code entryType} parameters for other methods on this interface.
     *
     * @return all Registry types that are served by this facade.
     */
    public Set<String> getRegistryTypes() {
        return registries.keySet();
    }

    /**
     * Convenience method converting an entry id, entry type, and the entry object into a RegistryEntry object.
     *
     * @param entryId the entry id
     * @param entryType the entry type
     * @param entry the entry domain object
     * @param <T> the type of the domain object
     * @return the RegistryEntry
     */
    private <T> RegistryEntry<?> adapt(String entryId, String entryType, T entry) {
        return new BasicRegistryEntryImpl<T>(entryId, entry, entryType);
    }
}
