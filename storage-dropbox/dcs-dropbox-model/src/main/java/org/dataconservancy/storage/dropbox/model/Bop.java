/*
 * Copyright 2013 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.storage.dropbox.model;

import java.util.HashSet;
import java.util.Set;

public class Bop {
    
    private Set<DropboxModel> dropboxModels = new HashSet<DropboxModel>();
    private Set<Person> persons = new HashSet<Person>();
    
    /**
     * @return the dropboxModels
     */
    public Set<DropboxModel> getDropboxModels() {
        return dropboxModels;
    }
    
    /**
     * @param dropboxModels
     *            the dropboxModels to set
     */
    public void setDropboxModels(Set<DropboxModel> dropboxModels) {
        this.dropboxModels = dropboxModels;
    }
    
    public void addDropboxModel(DropboxModel... dropboxModel) {
        if (dropboxModel != null) {
            for (DropboxModel du : dropboxModel) {
                if (du != null) {
                    this.dropboxModels.add(new DropboxModel(du));
                }
            }
        }
    }
    
    /**
     * @return the persons
     */
    public Set<Person> getPersons() {
        return persons;
    }
    
    /**
     * @param persons
     *            the persons to set
     */
    public void setPersons(Set<Person> persons) {
        this.persons = persons;
    }
    
    public void addPerson(Person... person) {
        if (person != null) {
            for (Person du : person) {
                if (du != null) {
                    this.persons.add(new Person(du));
                }
            }
        }
    }

}
