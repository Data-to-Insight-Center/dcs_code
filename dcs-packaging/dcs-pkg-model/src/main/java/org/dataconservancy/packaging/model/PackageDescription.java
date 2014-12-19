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
package org.dataconservancy.packaging.model;

import java.util.List;

public interface PackageDescription {
    
    /**
     * Adds a new relationship to the package description
     * @param relationship The triple representing the relationship
     */
    public void addRelationship(Triple relationship);
    
    /**
     * Sets all the relationships in the package.
     * @param relationships The set of relationships in the package
     */
    public void setRelationships(List<Triple> relationships);
    
    /**
     * Gets all relationships in the package description
     * @return A set of all relationships in the package, maybe empty, but never null.
     */
    public List<Triple> getRelationships();
}