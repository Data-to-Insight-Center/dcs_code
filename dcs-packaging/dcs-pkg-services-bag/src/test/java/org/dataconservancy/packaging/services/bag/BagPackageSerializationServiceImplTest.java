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

import java.io.File;

import org.junit.Test;

import org.dataconservancy.packaging.model.PackageSerialization;

import static org.junit.Assert.assertEquals;


public class BagPackageSerializationServiceImplTest {
    private static final String manifestSampleFilePath = "/manifest-sample.txt";
    
    @Test
    public void testReadSampleManifestFile() {
        File sampleManifestFile = new File(BagPackageSerializationServiceImplTest.class.getResource(manifestSampleFilePath).getPath());
        BagPackageSerializationServiceImpl underTest = new BagPackageSerializationServiceImpl();
        
        PackageSerialization serialization = underTest.readSerializationFromFiles(sampleManifestFile);
        
        assertEquals(8, serialization.getChecksums().size());
        
        assertEquals(1, serialization.getChecksums("text.txt").size());
        assertEquals(1, serialization.getChecksums("duck/duck/goose.data").size());
        
        assertEquals("00AF520", serialization.getChecksum("text.txt", "sample"));
        assertEquals("2380280", serialization.getChecksum("duck/duck/goose.data", "sample"));
    }
}