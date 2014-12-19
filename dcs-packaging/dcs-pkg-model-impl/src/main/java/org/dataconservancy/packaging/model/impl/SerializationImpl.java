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

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationImpl implements PackageSerialization {

    private Logger log = LoggerFactory.getLogger(this.getClass());
   
    /**
     * A map of all the checksums in specified in the package manifests.
     */
    private Map<String, List<Checksum>> checksums;
    
    /**
     * A list of all the file paths in the package.
     */
    private List<File> files;
    
    /**
     * A map storing all the metadata that was stored in the package.
     */
    private Map<String, String> serializationMetadata;
    
    private File extractDirectory;
    private File packageBaseDirectory;
    
    public SerializationImpl() {
        checksums = new HashMap<String, List<Checksum>>();
        files = new ArrayList<File>();
        serializationMetadata = new HashMap<String, String>();
    }
    
    @Override
    public void addChecksum(String hash, String file, String checksum) {
        List<Checksum> existingChecksums = checksums.get(file);
        if (existingChecksums != null) {
            existingChecksums.add(new ChecksumImpl(hash, checksum));
        } else {
            List<Checksum> newChecksums = new ArrayList<Checksum>();
            newChecksums.add(new ChecksumImpl(hash, checksum));
            checksums.put(file, newChecksums);
        }
    }
    
    @Override
    public void setChecksums(Map<String, List<Checksum>> checksums) {
        this.checksums = checksums;
    }
    
    @Override
    public List<Checksum> getChecksums(String filePath) {
        return checksums.get(filePath);
    }
    
    @Override
    public String getChecksum(String filePath, String hash) {
        List<Checksum> allChecksums = checksums.get(filePath);
        
        String specifiedChecksum = null;
        if (allChecksums != null) {
            for( Checksum checksum : allChecksums) {
                if( checksum.getAlgorithm().equalsIgnoreCase(hash)) {
                    specifiedChecksum = checksum.getValue();
                    break;
                }
            }
        }
        
        return specifiedChecksum;
    }
    
    @Override
    public Map<String, List<Checksum>> getChecksums() {
        return checksums;
    }  
    
    @Override
    public void setFiles(List<File> files) {
        this.files = files;
    }
    
    @Override
    public void addFile(File file){
        files.add(file);
    }
    
    @Override
    public List<File> getFiles() {
        return files;
    }

    @Override
    public List<File> getFiles(boolean relativize) {
        final ArrayList<File> results = new ArrayList<File>(files.size());
        final File baseDir = absolutize(extractDirectory, packageBaseDirectory);

        if (relativize) {
            for (File f : files) {
                results.add(relativize(baseDir, f));
            }
        } else {
            for (File f : files) {
                results.add(absolutize(baseDir, f));
            }
        }

        return Collections.unmodifiableList(results);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPackageMetadata(String name, String value) {
        serializationMetadata.put(name, value);        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPackageMetadata(Map<String, String> metadata) {
        serializationMetadata = metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPackageMetadata() {
        return serializationMetadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackageMetadata(String name) {
        return serializationMetadata.get(name);
    }

    @Override
    public File getBaseDir() {
        return packageBaseDirectory;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:<br/>
     * This implementation requires that an extract directory be set first, before setting the base directory.
     *
     * @param basedir {@inheritDoc}
     * @throws IllegalStateException if the extract directory hasn't been set first
     */
    @Override
    public void setBaseDir(File basedir) {
        // We need to make sure that the base directory member variable is always a relative directory

        if (!basedir.isAbsolute()) {
            this.packageBaseDirectory = basedir;
            return;
        }

        if (extractDirectory == null) {
            throw new IllegalStateException("The extract directory must be set first, or a relative base " +
                    "directory path must be specified (you specified an absolute base directory " + basedir + ")");
        }

        // If it is an absolute directory, we see if it starts with the extract directory.
        // If it starts with the extract directory, strip off the extract directory and store the relative
        // base directory.
        if (basedir.getPath().startsWith(extractDirectory.getPath())) {
            this.packageBaseDirectory = relativize(extractDirectory, basedir);
            log.info("Relativizing base directory {} against extract directory {}: {}",
                    new Object[]{basedir, extractDirectory, this.packageBaseDirectory});

            // If it is an absolute directory but it doesn't start with the extract directory, we re-base the
            // the absolute base directory on top of the extract directory; essentially strip off the leading
            // slash that makes the basedir absolute.
        } else {
            String basePath = basedir.getPath();
            
            //This is a work around to drop the drive letter prefix from windows so we can move on directory under the other.
            char driveLetter = basePath.charAt(0);
            if (basePath.startsWith(driveLetter + ":")) {
                basePath = basePath.substring(2);
            }
            
            File temp = new File(extractDirectory, basePath);  // create an absolute file
            this.packageBaseDirectory = relativize(extractDirectory, temp);

            log.info("Re-basing the absolute base directory {} on top of the extract directory {}: {}",
                    new Object[]{basedir, extractDirectory, this.packageBaseDirectory});
        }
    }

    @Override
    public File getExtractDir() {
        return extractDirectory;
    }

    @Override
    public void setExtractDir(File extractDir) {
        // If the supplied directory isn't absolute, re-base it on top of the system's temporary directory
        if (!extractDir.isAbsolute()) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            log.info("Re-basing the relative extract directory {} on top of {}", extractDir, tmpDir);
            extractDir = new File(tmpDir, extractDir.getPath());
        }
        this.extractDirectory = extractDir;
    }

    File absolutize(File base, File toAbsolutize) {

        // If the file is already absolute, there's nothing for us to do here.
        if (toAbsolutize.isAbsolute()) {
            return toAbsolutize;
        }

        return new File(base, toAbsolutize.getPath());
    }

    File relativize(File base, File toRelativize) {

        // If the file is already relative, there's nothing for us to do here.
        if (!toRelativize.isAbsolute()) {
            return toRelativize;
        }

        final String basePath = base.getPath();
        String relativePath = toRelativize.getPath();

        // We can only make the File relative if it starts with the base.
        if (relativePath.startsWith(basePath)) {
            relativePath = relativePath.substring((relativePath.indexOf(basePath) + basePath.length() + 1));
            return new File(relativePath);
        }

        return toRelativize;
    }

    @Override
    public String toString() {
        return "SerializationImpl{" +
                "checksums=" + checksums +
                ", files=" + files +
                ", serializationMetadata=" + serializationMetadata +
                ", extractDirectory=" + extractDirectory +
                ", packageBaseDirectory=" + packageBaseDirectory +
                '}';
    }

    public String toString(HierarchicalPrettyPrinter hpp) {

        // Checksums
        hpp.appendWithIndentAndNewLine("Checksums: ");
        hpp.incrementDepth();
        for (Map.Entry<String, List<Checksum>> checksumEntry : checksums.entrySet()) {
            hpp.appendWithIndentAndNewLine(checksumEntry.getKey());
            hpp.incrementDepth();
            for (Checksum c : checksumEntry.getValue()) {
                hpp.appendWithIndentAndNewLine("Algorithm: " + c.getAlgorithm() + " Value: " + c.getValue());
            }
            hpp.decrementDepth();
        }
        hpp.decrementDepth();

        // Extract directory
        hpp.appendWithIndentAndNewLine("Extract directory: " + extractDirectory.getAbsolutePath());

        // Base Directory
        hpp.appendWithIndentAndNewLine("Base directory: " + packageBaseDirectory.getPath());

        // Files
        TreeSet<File> sortedFiles = new TreeSet<File>(new Comparator<File>() {
            @Override
            public int compare(File file, File file1) {
                return file.getAbsolutePath().compareTo(file1.getAbsolutePath());
            }
        });
        sortedFiles.addAll(files);

        hpp.appendWithIndentAndNewLine("Files: ");
        hpp.incrementDepth();
        for (File f : sortedFiles) {
            hpp.appendWithIndentAndNewLine(f.getAbsolutePath());
        }
        hpp.decrementDepth();

        // Serialization Metadata
        hpp.appendWithIndentAndNewLine("Serialization Metadata: " + serializationMetadata);

        return hpp.toString();

    }

}
