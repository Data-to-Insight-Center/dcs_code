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

public interface Triple {
    
    /**
     * Sets the subject of the triple
     * @param subject The string representing the subject of the triple
     */
    public void setSubject(String subject);
    
    /**
     * Retrieves the subject of the triple.
     * @return The string representing the subject.
     */
    public String getSubject();
    
    /**
     * Sets the predicate of the triple.
     * @param predicate The string representing the predicate of the triple.
     */
    public void setPredicate(String predicate);
    
    /**
     * Returns the predicate of the triple.
     * @return The string representing the predicate.
     */
    public String getPredicate();
    
    /**
     * Sets the object of the triple
     * @param object The string representing the object of the of the triple.
     */
    public void setObject(String object);
    
    /**
     * Retrieves the object of the triple.
     * @return The string representing the object.
     */
    public String getObject();
}