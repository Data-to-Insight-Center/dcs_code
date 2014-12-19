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
package org.dataconservancy.packaging.services.bag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.packaging.services.SerializationService;

public class BagPackageSerializationServiceImpl implements SerializationService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<File> getFilesFromPackage(PackageSerialization serialization) {
        return serialization.getFiles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getChecksumOfFile(PackageSerialization serialization, String fileName, String hashType) {
        return serialization.getChecksum(fileName, hashType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Package readPackage(PackageSerialization serialization, File... pkg) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File writePackage(PackageSerialization serialization, Package pkg) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PackageSerialization readSerializationFromFiles(File... files) {
        PackageSerialization serialization = new SerializationImpl();
        
        try {
            for (File file : files) {
                if (file.getName().equalsIgnoreCase("bag-info.txt")) {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        String name = line.substring(0, line.indexOf(':'));
                        String value = line.substring(line.indexOf(':')+1);
                        serialization.addPackageMetadata(name, value);
                    }
                    br.close();
                } else if (file.getName().startsWith("manifest-")) {
                    String hash = file.getName().substring(9, file.getName().length() - 4);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    
                    while ((line = br.readLine()) != null) {
                        String checksum = line.substring(0, line.indexOf(' '));
                        String fileName = line.substring(line.indexOf(' ')+1);
                        //only add the files paths once 
                        
                        serialization.addChecksum(hash, fileName, checksum);
                    }
                    br.close();
                    
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return serialization;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeSet getSerializationMetadata() {
        // TODO Auto-generated method stub
        return null;
    }
    
}