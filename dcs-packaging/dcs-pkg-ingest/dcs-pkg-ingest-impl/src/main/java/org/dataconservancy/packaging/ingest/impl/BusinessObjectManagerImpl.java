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

import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.NonexistentBusinessObjectException;
import org.dataconservancy.ui.model.BusinessObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BusinessObjectManagerImpl implements BusinessObjectManager {

    private Map<Key, BusinessObject> businessObjectVault = new HashMap<Key, BusinessObject>();
    private Map<String, Key> businessIdToKeyMap = new HashMap<String, Key>();

    @Override
    public <T extends BusinessObject> void add(String localId, T boInstance, Class<T> boClass) {
        businessObjectVault.put(new Key(localId, boClass), boInstance);
        businessIdToKeyMap.put(boInstance.getId(), new Key(localId, boClass));
    }

    @Override
    public <T extends BusinessObject> void update(String localId, T boInstance, Class<T> boClass) {
        Key key = new Key(localId, boClass);

        if (businessObjectVault.get(key) == null) {
            throw new NonexistentBusinessObjectException("Business object with type " + boClass + " and " +
                    "localID \"" + localId + "\" could not be found. Cannot update nonexistent business object.");
        }
        businessObjectVault.put(new Key(localId, boClass), boInstance);
    }

    @Override
    public void remove(String localId, Class<? extends BusinessObject> boClass) {
        Key key = new Key(localId, boClass);

        if (businessObjectVault.get(key) == null) {
            throw new NonexistentBusinessObjectException("Business object with type " + boClass + " and " +
                    "localID \"" + localId + "\" could not be found. Cannot remove nonexistent business object.");
        }
        businessObjectVault.remove(new Key(localId, boClass));
    }

    @Override
    public <T extends BusinessObject> BusinessObject get(String localId, Class<T> boClass) {
        return businessObjectVault.get(new Key (localId, boClass));
    }
    
    @Override
    public BusinessObject get(String businessObjectId) {
        BusinessObject object = null;
        if (businessIdToKeyMap.containsKey(businessObjectId)) {
            object = businessObjectVault.get(businessIdToKeyMap.get(businessObjectId));
        }
        return object;
    }
    
    @Override
    public <T extends BusinessObject> Class<T> getType(String localId) {
        for ( Key key : businessObjectVault.keySet()) {
            if (key.getLocalId().equalsIgnoreCase(localId)) {
                return key.getBoClass();
            }
        }
        
        return null;
    }

    @Override
    public <T extends BusinessObject> Set<T> getInstancesOf(Class<T> boClass) {
        Set<T> businessObjects = new HashSet<T>();
        Set<Key> keySet = businessObjectVault.keySet();
        for (Key key : keySet) {
            if (key.getBoClass().equals(boClass)) {
                businessObjects.add(boClass.cast(businessObjectVault.get(key)));
            }
        }
        return businessObjects;
    }

    @Override
    public Map<BusinessObject, String> createMap() {
        Map<BusinessObject, String> result = new HashMap<BusinessObject, String>();
        
        for (Key key : businessObjectVault.keySet()) {
            result.put(businessObjectVault.get(key), key.getLocalId());
        }
        
        return result;
    }
    
    public class Key {
        private String localId;
        private Class boClass;

        public <T extends BusinessObject> Key (String localId, Class<T> boClass) {
            this.localId = localId;
            this.boClass = boClass;
        }

        private String getLocalId() {
            return this.localId;
        }

        private <T extends BusinessObject> Class<T> getBoClass() {
            return this.boClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;

            Key key = (Key) o;

            if (boClass != null ? !boClass.equals(key.boClass) : key.boClass != null) return false;
            if (localId != null ? !localId.equals(key.localId) : key.localId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = localId != null ? localId.hashCode() : 0;
            result = 31 * result + (boClass != null ? boClass.hashCode() : 0);
            return result;
        }
    }
}
