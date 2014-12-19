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

import java.io.File;

import java.util.List;
import java.util.Map;

public interface PackageSerialization {
    
    /**
     * Adds a new checksum to the package
     * @param hash The hash type of the checksum
     * @param file The file path of the file whose checksum is being stored.
     * @param checksum The value of the checksum of the file
     */
    public void addChecksum(String hash, String file, String checksum);
    
    /**
     * Sets the map of all checksums in the package
     * @param checksums The map representing all the checksums in the file with file paths as the key.
     */
    public void setChecksums(Map<String, List<Checksum>> checksums);
    
    /**
     * Returns a list of all the checksums for the given file.
     * @param filePath The path of the file to retrieve the checksum for
     * @return The list of checksums for the file, or null if none exist
     */
    public List<Checksum> getChecksums(String filePath);
    
    /**
     * Retrieves the specific checksum for the given file path and hash type
     * @param filePath The file path of the file to retrieve the checksum for.
     * @param hash The hash type of the checksum to be returned.
     * @return The string representing the checksum or null if none are found.
     */
    public String getChecksum(String filePath, String hash);
    
    /**
     * Retrieves all the checksums for all the files in the package.
     * @return The map of all the checksums in the package, may be empty but never null.
     */
    public Map<String, List<Checksum>> getChecksums();
    
    /**
     * Sets the list of all the files in the package.
     * @param files The list with all the file
     */
    public void setFiles(List<File> files);
    
    /**
     * Adds a new file to the package
     * @param file The file to be added
     */
    public void addFile(File file);    
    
    /**
     * Returns a list of all the files, in the order they were added.  There are no guarantees as to whether or not
     * the returned list of files will be absolute or relative, and if they are relative, there are no guarantees as to
     * what they are relative to.
     *
     * @return A list of all the files in the package
     */
    public List<File> getFiles();

    /**
     * Returns a list of all files in the package, in the order they were added.  If {@code relative} is true, the
     * returned files will be relative to the {@link #getBaseDir base directory} of the package. For example,
     * if a file's absolute path is {@code /storage/bag/data/file.txt} and the (absolute) base directory is
     * {@code /storage/bag}, then this method will return {@code data/file.txt}.
     * <p/>
     * If {@code relative} is false, then this method is guaranteed to return files that have absolute paths.
     * <p/>
     * In either case, the returned {@code List&lt;File>} is immutable.
     *
     * @param relativize if true, the paths in the returned files will be relative to the absolute base directory
     * @return a list of all files in the package, in the order they were added
     */
    public List<File> getFiles(boolean relativize);
    
    public void addPackageMetadata(String name, String value);
    
    public void setPackageMetadata(Map<String, String> metadata);
    
    public Map<String, String> getPackageMetadata();
    
    public String getPackageMetadata(String name);

    /**
     * The base directory of the extracted files for the package; it is relative to {@link #getExtractDir() the extract
     * directory}.  <em>N.B.</em> the base directory can be an arbitrary number of levels deep! The proper way to create
     * a full, absolute path to the base directory of a package serialization is by combining the
     * {@link #getExtractDir()} with {@code getBaseDir()}:
     * <pre>
     * File absBaseDir = new File(getExtractDir(), getBaseDir().getPath())
     * </pre>
     * Be sure to use the Java {@code File} abstraction to maintain platform portability.  Be sure to use
     * {@code getBaseDir().getPath()} because the base directory may not be a single directory, it may be multiple
     * directories (e.g. {@code 1234/bagdir})
     *
     *
     * @return the base directory, that when combined with the extract directory, results in the full, absolute
     *         path to the base of the extracted package
     */
    public File getBaseDir();
    
    /**
     * The base directory of the extracted files for the package; it is relative to {@link #getExtractDir() the extract
     * directory}.  If the supplied directory is relative, it will be accepted as-is.  If it is absolute, and is not
     * rooted under {@link #getExtractDir()}, it should be re-based under the extract directory.  The supplied
     * {@code basedir} may be an arbitrary number of levels deep; e.g. a construct like {@code 1234/bagdir} is allowed.
     *
     * @param basedir the base directory, that when combined with the extract directory, results in the full, absolute
     *                path to the base of the extracted package
     */
    public void setBaseDir(File basedir);

    /**
     * The directory that the serialization resides under; will always be absolute.  Combining the absolute extract
     * directory with the relative {@link #getBaseDir() base directory} will result in the full, absolute, path to the
     * base of the extracted package.  <em>N.B.</em> the base directory can be an arbitrary number of levels deep! The
     * proper way to create a full, absolute path to the base directory of a package serialization is by combining the
     * {@code getExtractDir()} with {@link #getBaseDir()}:
     * <pre>
     * File absBaseDir = new File(getExtractDir(), getBaseDir().getPath())
     * </pre>
     * Be sure to use the Java {@code File} abstraction to maintain platform portability.  Be sure to use
     * {@code getBaseDir().getPath()} because the base directory may not be a single directory, it may be multiple
     * directories (e.g. {@code 1234/bagdir})
     *
     * @return the directory that a package will be extracted under, always an absolute path
     */
    public File getExtractDir();

    /**
     * The directory that the serialization resides under; will always be absolute.  Combining the absolute extract
     * directory with the relative {@link #getBaseDir() base directory} will result in the full, absolute, path to the
     * base of the extracted package.
     *
     * @param extractDir the directory that a package will be extracted under
     */
    public void setExtractDir(File extractDir);
    
}
