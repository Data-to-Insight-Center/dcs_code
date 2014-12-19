package org.dataconservancy.registry.shared.test.support;

import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.RegistryEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A simple {@code Registry} implementation, merely used to prototype code in tests. It is <em>not</em> guaranteed
 * to adhere to the {@code Registry} contract, but it will implement the interface to satisfy the compiler.
 */
public class SimpleRegistry implements Registry {

    private String description;
    private Map<String, RegistryEntry> entries = new HashMap<String, RegistryEntry>();

    @Override
    public RegistryEntry retrieve(String id) {
        return entries.get(id);
    }

    @Override
    public Set<RegistryEntry> lookup(String... keys) {
        Set<RegistryEntry> results = new HashSet<RegistryEntry>();
        for (RegistryEntry entry : entries.values()) {
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

    public void setEntries(Map<String, RegistryEntry> entries) {
        this.entries = entries;
    }

    @Override
    public Iterator<RegistryEntry> iterator() {
        return new RegistryEntryIterator(entries.entrySet().iterator());
    }

    private class RegistryEntryIterator implements Iterator<RegistryEntry> {
        
        private Iterator<Entry<String, RegistryEntry>> delegate;
        
        protected RegistryEntryIterator(Iterator<Entry<String, RegistryEntry>> iter) {
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
        public RegistryEntry next() {
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
