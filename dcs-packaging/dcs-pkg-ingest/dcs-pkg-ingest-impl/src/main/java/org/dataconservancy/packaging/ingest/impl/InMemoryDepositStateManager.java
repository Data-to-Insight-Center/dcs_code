/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.ingest.impl;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mediates access to IngestWorkflowState for a certain number of deposit identifiers.
 * <p/>
 * This implementation will manage access to a maximum number of IngestWorkflowState objects.  Its capacity may be
 * specified on construction; by default its capacity is set to 100.  Every time a state object is added to this
 * manager, the oldest entry in the map will be removed if the number of objects exceeds the capacity.
 * <p/>
 * Note that this implementation is an in-memory implementation, meaning that all state will be lost when the JVM
 * exits.
 */
class InMemoryDepositStateManager implements DepositStateManager {

    private static final int DEFAULT_SIZE = 100;

    private int size = DEFAULT_SIZE;

    private LinkedHashMap<String, IngestWorkflowState> stateMap;

    InMemoryDepositStateManager() {
        stateMap = new CachingLinkedHashMap<String, IngestWorkflowState>(DEFAULT_SIZE);
    }

    InMemoryDepositStateManager(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be an integer greater than 0");
        }

        this.size = size;
        stateMap = new CachingLinkedHashMap<String, IngestWorkflowState>(size);

    }

    @Override
    public IngestWorkflowState get(String depositId) {
        return stateMap.get(depositId);
    }

    @Override
    public void put(String depositId, IngestWorkflowState state) {
        stateMap.put(depositId, state);
    }

    /**
     * Returns the maximum number of state objects that will be managed by this instance.
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    private class CachingLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

        private CachingLinkedHashMap(int initialSize) {
            super(initialSize);
        }

        /**
         * Removes the eldest entry in the Map if the size of the Map is greater than the configured capacity.
         *
         * @param entry the eldest entry that may be removed
         * @return true if {@code entry} should be removed
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry entry) {
            return size() > size;
        }
    }

}
