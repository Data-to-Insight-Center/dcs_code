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
package org.dataconservancy.packaging.services;

import java.io.File;

import java.util.List;
import java.util.Map;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.ui.model.BusinessObject;

public interface PackageService {
    
    /**
     * Returns all of the package file metadata attribute sets
     * @return A list of all the attribute sets of file metadata from the package.
     */
    public Map<String, AttributeSet> getMetadata(Package pkg);
    
    /**
     * Returns a list of all file objects from the package.
     * @return This list of file paths in the pacakge.
     */
    public List<String> getFiles();
    
    /**
     * Lists all the business objects contained in the package.
     * @return A list of all the business objects that are contained in the package
     */
    public List<BusinessObject> getItems(Package pkg);
    
    /**
     * Returns the specified item from the package
     * @param id The string identifier of the item in the package
     * @return The business object representation of the item in the package, or null if not found
     */
    public BusinessObject getItem(Package pkg, String id);
    
    /**
     * Puts the business objects into a package.
     * @param objects The objects to be serialized into a package
     */
    public void putObjectsIntoPackage(Package pkg, List<BusinessObject> objects);
    
    /**
     * Creates a package from the provided file.
     * @param filePackage The files making up the package
     * @return The {@code Package} object representing the uploaded file package.
     */
    public Package buildPackage(File... filePackage);
    
}