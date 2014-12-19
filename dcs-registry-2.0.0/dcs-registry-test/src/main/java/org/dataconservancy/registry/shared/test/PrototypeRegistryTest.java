package org.dataconservancy.registry.shared.test;

import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.shared.test.support.AnotherSampleDomainObject;
import org.dataconservancy.registry.shared.test.support.SampleDomainObject;
import org.dataconservancy.registry.shared.test.support.SimpleRegistryFacade;
import org.dataconservancy.registry.shared.test.support.SimpleTypedRegistry;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Simple tests focusing on prototyping the Registry APIs, more so than testing an implementation's compliance with
 * the API contracts.
 */
public class PrototypeRegistryTest {

    /**
     * A test which really just prototypes the instantiation and population of a simple registry.
     *
     * @throws Exception
     */
    @Test
    public void testInstantiateAndPopulateTypedRegistry() throws Exception {
        // Instantiate an empty TypedRegistry; provides access to RegistryEntries wrapping instances of
        // SampleDomainObject
        final TypedRegistry<SampleDomainObject> registry = newSimpleTypedRegistry();

        // Create a Map of RegistryEntries that will back the registry.  The entries are keyed by their identifier
        final Map<String, RegistryEntry<SampleDomainObject>> entries = newBackingMap();


        // Create a single entry to go into the registry.
        final SampleDomainObject object = new SampleDomainObject();
        final String entryId = "id";
        BasicRegistryEntryImpl<SampleDomainObject> entry = new BasicRegistryEntryImpl<SampleDomainObject>(entryId, object,
                "type", Collections.<String>emptyList(), "desc");

        // Populate the map.
        entries.put(entry.getId(), entry);

        // Set the map on the Registry.
        ((SimpleTypedRegistry<SampleDomainObject>) registry).setEntries(entries);

        // Simply retrieve the entry we just added
        assertEquals(entry, registry.retrieve(entryId));
    }

    /**
     * Constructs two TypedRegistries, each registry supporting a different type of domain object.  These two registries
     * are placed behind a Registry façade.  The members of each TypedRegistry are retrieved through the Registry
     * façade.
     *
     * @throws Exception
     */
    @Test
    public void testRegistryFacadePattern() throws Exception {
        final TypedRegistry<SampleDomainObject> registryOne = newSimpleTypedRegistry();
        final TypedRegistry<AnotherSampleDomainObject> registryTwo = newSimpleTypedRegistry();

        final SampleDomainObject obj1 = new SampleDomainObject();
        final AnotherSampleDomainObject obj2 = new AnotherSampleDomainObject();

        final String id1 = "id1";
        final String id2 = "id2";

        final BasicRegistryEntryImpl<SampleDomainObject> entry1 = new BasicRegistryEntryImpl<SampleDomainObject>(id1,
                obj1, "type1", Collections.<String>emptyList(), "desc1");
        final BasicRegistryEntryImpl<AnotherSampleDomainObject> entry2 =
                new BasicRegistryEntryImpl<AnotherSampleDomainObject>(id2, obj2, "type2",
                        Collections.<String>emptyList(), "desc2");

        ((SimpleTypedRegistry<SampleDomainObject>) registryOne).setEntries(createAndPopulateBackingMap(entry1));
        ((SimpleTypedRegistry<AnotherSampleDomainObject>) registryTwo).setEntries(createAndPopulateBackingMap(entry2));

        assertEquals(entry1, registryOne.retrieve(id1));
        assertEquals(entry2, registryTwo.retrieve(id2));

        final Registry facade = new SimpleRegistryFacade();
        ((SimpleRegistryFacade)facade).setBackingRegistries(registryOne, registryTwo);

        assertEquals(entry1, facade.retrieve(id1));
        assertEquals(entry2, facade.retrieve(id2));
    }


    /**
     * Creates a Map suitable for backing a SimpleTypedRegistry instance.  Keys in the map are the identifiers of the
     * RegistryEntry objects, and the values in the map are RegistryEntry instances that wrap the domain object.
     *
     * @param <T> the type of domain object served by the registry
     * @return a new Map, empty of entries
     */
    private <T> Map<String, RegistryEntry<T>> newBackingMap() {
        // Create a Map of RegistryEntries that will back the registry.  The entries are keyed by their identifier
        return new HashMap<String, RegistryEntry<T>>();
    }

    private <T> Map<String, RegistryEntry<T>> createAndPopulateBackingMap(RegistryEntry<T>... members) {
        Map<String, RegistryEntry<T>> map = newBackingMap();
        for (RegistryEntry<T> member : members) {
            map.put(member.getId(), member);
        }

        return map;
    }

    /**
     * Creates a new, empty, instance of TypedRegistry, implemented by SimpleTypedRegistry.
     *
     * @param <T> the type of domain object served by the registry
     * @return a new instance, empty of entries
     */
    private <T> TypedRegistry<T> newSimpleTypedRegistry() {
        // Instantiate an empty TypedRegistry; provides access to RegistryEntries wrapping instances of
        // SampleDomainObject
        return new SimpleTypedRegistry<T>();
    }

}
