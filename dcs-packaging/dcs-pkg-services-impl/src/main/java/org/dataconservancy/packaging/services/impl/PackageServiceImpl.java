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
package org.dataconservancy.packaging.services.impl;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.Triple;
import org.dataconservancy.packaging.services.DescriptionService;
import org.dataconservancy.packaging.services.PackageService;
import org.dataconservancy.packaging.services.SerializationService;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.packaging.model.impl.PackageImpl;

import org.dataconservancy.ui.model.BusinessObject;

public class PackageServiceImpl implements PackageService {

    private DescriptionService descriptionService;
    private SerializationService serializationService;
    
    public PackageServiceImpl(DescriptionService descriptionService, SerializationService serializationService) {
        this.descriptionService = descriptionService;
        this.serializationService = serializationService;
        
        
    }
    
    @Override
    public Map<String, AttributeSet> getMetadata(Package pkg) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BusinessObject> getItems(Package pkg) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BusinessObject getItem(Package pkg, String id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putObjectsIntoPackage(Package pkg, List<BusinessObject> objects) {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Package buildPackage(File... filePackage) {
        // TODO Unpack file if it is tar or zip
        PackageSerialization serialization = serializationService.readSerializationFromFiles(filePackage);
        PackageDescription description = descriptionService.parseDescription(serialization, filePackage);
        Package newPackage = new PackageImpl(description, serialization);
        
        return newPackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFiles() {
        // TODO Auto-generated method stub
        return null;
    }
    
}