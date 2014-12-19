package org.dataconservancy.registry.shared.test.support;

import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A simple {@code Registry} implementation, merely used to prototype code in tests. It is <em>not</em> guaranteed
 * to adhere to the {@code Registry} contract, but it will implement the interface to satisfy the compiler.
 */
public class SimpleRegistryFacade implements Registry {

    private String description;
    private List<TypedRegistry<?>> backingRegistries = new ArrayList<TypedRegistry<?>>();

    @Override
    public RegistryEntry retrieve(String id) {
        for (TypedRegistry<?> registry : backingRegistries) {
            RegistryEntry<?> entry = registry.retrieve(id);
            if (entry != null) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public Set<RegistryEntry<?>> lookup(String... keys) {
        Set<RegistryEntry<?>> results = new HashSet<RegistryEntry<?>>();
        for (TypedRegistry<?> registry : backingRegistries) {
            for(String key : keys) {
                results.addAll(registry.lookup(key));
            }
        }
        return results;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBackingRegistries(List<TypedRegistry<?>> backingRegistries) {
        this.backingRegistries = backingRegistries;
    }

    public void setBackingRegistries(TypedRegistry<?>... backingRegistries) {
        this.backingRegistries.addAll(Arrays.asList(backingRegistries));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<TypedRegistry<?>> iterator() {
        return backingRegistries.iterator();
    }


}
