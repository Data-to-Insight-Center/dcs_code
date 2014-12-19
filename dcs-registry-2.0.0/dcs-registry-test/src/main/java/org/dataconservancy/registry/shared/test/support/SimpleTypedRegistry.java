package org.dataconservancy.registry.shared.test.support;

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A simple {@code TypedRegistry} implementation, merely used to prototype code in tests. It is <em>not</em> guaranteed
 * to adhere to the {@code TypedRegistry} contract, but it will implement the interface to satisfy the compiler.
 */
public class SimpleTypedRegistry<T> implements TypedRegistry<T> {

    private String type;
    private String description;
    private Map<String, RegistryEntry<T>> entries = new HashMap<String, RegistryEntry<T>>();

    @Override
    public String getType() {
        return type;
    }

    @Override
    public RegistryEntry<T> retrieve(String id) {
        return entries.get(id);
    }

    @Override
    public Set<RegistryEntry<T>> lookup(String... keys) {
        Set<RegistryEntry<T>> results = new HashSet<RegistryEntry<T>>();
        for (RegistryEntry<T> entry : entries.values()) {
            if (entry.getKeys().contains(keys[0])) {
                results.add(entry);
                //If we have more than one key make sure that all the other keys are also on the entry
                if (keys.length > 1) {
                    for (int i = 1; i < keys.length; i++) {
                        //If the key isn't on the entry remove it from the result and move to the next entry
                        if (!entry.getKeys().contains(keys[i])) {
                            results.remove(entry);
                            break;
                        }
                    }
                }
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

    public void setEntries(Map<String, RegistryEntry<T>> entries) {
        this.entries = entries;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public Iterator<RegistryEntry<T>> iterator() {
        return new RegistryEntryIterator(entries.entrySet().iterator());
    }

    private class RegistryEntryIterator implements Iterator<RegistryEntry<T>> {
        
        private Iterator<Entry<String, RegistryEntry<T>>> delegate;
        
        protected RegistryEntryIterator(Iterator<Entry<String, RegistryEntry<T>>> iter) {
            delegate = iter;
        }
       
        @Override
        public boolean hasNext() {
           return delegate.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RegistryEntry<T> next() {
            return delegate.next().getValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            delegate.remove();
        }
        
    }
    
}
