/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.ingest.api;

import org.dataconservancy.ui.model.BusinessObject;

import java.util.Map;
import java.util.Set;

/**
 * Manages BusinessObjects that are generated during an ingest pipeline. BusinessObjects that are generated
 * during ingest are considered part of the state of the ingest.  Typically, ingest services will obtain
 * an instance of BusinessObjectManager from an instance of {@link IngestWorkflowState}.
 */
public interface BusinessObjectManager {

    /** Add a {@link BusinessObject} instance to the manager
     *
     * @param localId Package-local identifier (e.g. BagIt URI local file URI)
     * @param boInstance BusinessObject instance
     * @param boClass Class of the BusinessObject, for the sake of retrieval.
     */
    public <T extends BusinessObject> void add(String localId, T boInstance, Class<T> boClass);


    /** Replaces a BusinessObject instance referred to by the given localId.
     * <p>
     * If there is no instance matching the localId, or if the instance is of a different class
     * than the one it is replacing, an exception will be thrown.
     * </p>
     * @param localId Package-local identifier (e.g. BagIt URI local file URI)
     * @param boInstance BusinessObject instance
     * @param boClass Class of the matching BusinessObject
     * @throws {@code NonexistentBusinessObjectException} if there is no matching instance, or a type mismatch.
     */
    public <T extends BusinessObject> void update(String localId, T boInstance, Class<T> boClass);


    /** Removes a BusinessObject from the manager.
     *
     * @param localId Package-local identifier (e.g BagIt URI, local file URI)
     * @throws {@code NonexistentBusinessObjectException} if there is no matching instance, or a type mismatch.
     */
    public void remove(String localId, Class<? extends BusinessObject> boClass);


    /** Retrieve a BusinessObject instance by local identifier, assuring it is an instance of the given class.
     *
     * @param localId Package-local identifier (e.g BagIt URI, local file URI)
     * @param boClass Desired BusinessObject class.
     * @return An instance of the specified class, or NULL if not present
     */
    public <T extends BusinessObject> BusinessObject get(String localId, Class<T> boClass);


    /** Get all instances of a particular BusinessObject class.
     * <p>
     * Instances will be matched exactly based on the declared class during {@link #add(String, BusinessObject, Class)}.
     * That is to say, if Y is a subclass of X, an instance added with a declared class of Y will only
     * be returned for only for Y, not X.
     * </p>
     * @param boClass Class of BusinessObjects to be returned.
     * @return A Set containing all matching BusinessObject instances.  Will not be null, but can be empty.
     */
    public <T extends BusinessObject> Set<T> getInstancesOf(Class<T> boClass);
    
    /**
     * Get the business object for a matching business object identifier.
     * @param businessId The business object id of the object to return.
     * @return The business object matching the provided Id, or null if none can be found.
     */
    public BusinessObject get(String businessId);
    
    /**
     * Gets the type associated with a given local id.
     * @param the local id to the type for.
     */
    public <T extends BusinessObject> Class<T> getType(String localID);
    
    /**
     * Map each business object to its local id.
     * 
     * @return a newly allocated map
     */
    public Map<BusinessObject, String> createMap();
}
