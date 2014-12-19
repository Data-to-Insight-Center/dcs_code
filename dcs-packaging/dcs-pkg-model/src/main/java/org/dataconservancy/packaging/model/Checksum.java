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

public interface Checksum {
    
    public final static String MD5 = "md5";
    public final static String SHA1 = "sha1";
    
    /**
     * Retrieves the hash algorithm used to calculate the checksum value
     * @return the string representing the hash algorithm used to calculate the checksum value
     */
    public String getAlgorithm();
    
    /**
     * Retrieves the checksum value
     * @return the string representing the checksum value
     */
    public String getValue();
}