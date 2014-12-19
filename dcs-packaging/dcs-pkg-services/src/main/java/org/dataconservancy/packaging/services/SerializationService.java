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
import org.dataconservancy.packaging.model.PackageSerialization;

public interface SerializationService {
    
    /**
     * Returns a list of all the files in the package.
     * @return The list of the all the files included in the package.
     */
    public List<File> getFilesFromPackage(PackageSerialization serialization);
    
    /**
     * Returns the checksum of the specified file in the specified type
     * @param fileName The name of the file in the package.
     * @param hashType The type of the hash used to generate the checksum. 
     * @return The string representation of the checksum, or null if not found.
     */
    public String getChecksumOfFile(PackageSerialization serialization, String fileName, String hashType);
    
    /**
     * Returns the attribute set representing all of the serialization metadata. 
     * @return The attribute set representing the serialization metadata. 
     */
    public AttributeSet getSerializationMetadata();
    
    /**
     * Reads the specified package from the disk.
     * @param pkg The file representing the package serialization.
     * @return The package representation of the file, or null if it can't be created.
     */
    public Package readPackage(PackageSerialization serialization, File... pkg);
    
    /**
     * Writes a package to a file.
     * @param pkg The package object to write to a file.
     * @return The file representing the package serialization.
     */
    public File writePackage(PackageSerialization serialization, Package pkg);
    
    /**
     * Parses out the {@code PackageSerialization} reads from  
     * @param files The files making up the package to be parsed.
     * @return The {@code PackageSerialization} object representing the serialization of the provided package.
     */
    public PackageSerialization readSerializationFromFiles(File... files);
    
}