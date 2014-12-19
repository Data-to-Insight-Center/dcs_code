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
package org.dataconservancy.registry.impl.metadata.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DcsMetadataFormat {

    private String name;
    private String version;
    private List<DcsMetadataScheme> schemes;
    private String id;

    public DcsMetadataFormat() {
        schemes = new ArrayList<DcsMetadataScheme>();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public List<DcsMetadataScheme> getSchemes() {
        return schemes;
    }

    public void setSchemes(List<DcsMetadataScheme> scheme) {
        schemes = scheme;
    }
    
    public void addScheme(DcsMetadataScheme scheme) {
        
        schemes.add(scheme);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public boolean equals(Object o) {
        Set<DcsMetadataScheme> schemeSet = new HashSet<DcsMetadataScheme>(schemes);
        
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsMetadataFormat that = (DcsMetadataFormat) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (schemes == null) {
            if (that.schemes != null)
                return false;
        }
        else if (!schemeSet.equals(new HashSet<DcsMetadataScheme>(that.schemes)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (schemes != null ? schemes.hashCode() : 0);
        return result;
    }
}
