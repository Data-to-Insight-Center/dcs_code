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
package org.dataconservancy.packaging.services.ore;

import java.io.File;

import java.util.List;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.Triple;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.services.DescriptionService;
import org.dataconservancy.ui.model.BusinessObject;

public class OREPackageDescriptionServiceImpl implements DescriptionService {

    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public List<BusinessObject> listContents(PackageDescription description) {
        return null;        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AttributeSet> getMetadata(PackageDescription description) {
        return null;
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Triple> listRelationships(PackageDescription description) {
        return description.getRelationships();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PackageDescription parseDescription(PackageSerialization serialization, File... files) {
        PackageDescription description = new DescriptionImpl();
        String oreFilePath = serialization.getPackageMetadata("DCS-ORE-REM");
        File oreFile = new File(oreFilePath);
        
        return null;
    }
    
}