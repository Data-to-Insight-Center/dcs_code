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
package org.dataconservancy.packaging.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.Triple;

public class DescriptionImpl implements PackageDescription {
    
    private List<Triple> relationships;
    
    public DescriptionImpl() {
        relationships = new ArrayList<Triple>();
    }
    
    public void addRelationship(Triple relationship) {
        relationships.add(relationship);
    }
    
    public void setRelationships(List<Triple> relationships) {
        this.relationships = relationships;
    }
    
    public List<Triple> getRelationships() {
        return relationships;
    }

    @Override
    public String toString() {
        return "DescriptionImpl{" +
                "relationships=" + relationships +
                '}';
    }
}