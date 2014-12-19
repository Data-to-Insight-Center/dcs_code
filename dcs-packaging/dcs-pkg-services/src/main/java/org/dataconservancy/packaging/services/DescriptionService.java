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

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.Triple;
import org.dataconservancy.ui.model.BusinessObject;

public interface DescriptionService {
    
    /**
     * Lists all of the business objects in the package.
     * @return A list of all the business objects in the package
     */
    public List<BusinessObject> listContents(PackageDescription description);
    
    /**
     * Lists all of the metadata attribute sets from the description
     * @return A list of all the attribute sets of metadata for the package description.
     */
    public List<AttributeSet> getMetadata(PackageDescription description);
    
    /**
     * Lists all the package relationships contained in the description.
     * @return A list of all relationship triples contained in the package.
     */
    public List<Triple> listRelationships(PackageDescription description);
    
    /**
     * Parse the {@code PackageDescription} from the provided package files.
     * @param files The files making package.
     * @return The {@code PackageDescription} object representing the description of the package.
     */
    public PackageDescription parseDescription(PackageSerialization serialization, File... files);
}