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
package org.dataconservancy.registry.impl.license.shared;

import java.net.URI;

/**
 * Encapsulates a rights statement.  A rights statement contains a human-readable description of the statement, a URI
 * that identifies the statement, and the license related to the rights statement.
 * <p/>
 * This has been influenced by the <a href="http://creativecommons.org/ns#">Creative Commons Rights Expression Language</a>.
 * Whereby a CC REL "Work" could be a Deliverable Unit, Manifestation, or File.  The {@link DcsRights} links a Work to
 * its rights information.  A CC REL "License" is represented by {@link DcsLicense}.  Currently other CC REL objects
 * are not represented.
 * <p/>
 * Clearly the concepts of rights overlaps with many different aspects of the DC, and this is just a starting attempt.
 * I believe that because we expect the DC to interpret and reason over rights information, that we will need a
 * fine-grained model and services for rights.  I also believe that these objects should be serialized, persisted, and
 * preserved in the archive alongside the data they assert relationships to.
 *
 * @see DcsLicense
 */
public class DcsRights {
    private String description;
    private URI rightsUri;
    private DcsLicense license;

    /**
     * A human-readable description of the rights statement.
     *
     * @return the rights description
     */
    public String getDescription() {
        return description;
    }

    /**
     * A human-readable description of the rights statement.
     *
     * @param description the rights description
     */
    public DcsRights setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * A uri which unambiguously identifies the rights statement
     *
     * @return the rights statement URI
     */
    public URI getRightsUri() {
        return rightsUri;
    }

    /**
     * A uri which unambiguously identifies the rights statement
     *
     * @param rightsUri the rights statement URI
     */
    public DcsRights setRightsUri(URI rightsUri) {
        this.rightsUri = rightsUri;
        return this;
    }

    /**
     * The license attached to this rights statement.
     *
     * @return the license
     */
    public DcsLicense getLicense() {
        return license;
    }

    /**
     * The license attached to this rights statement.
     *
     * @param license the license
     */
    public DcsRights setLicense(DcsLicense license) {
        this.license = license;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsRights dcsRights = (DcsRights) o;

        if (description != null ? !description.equals(dcsRights.description) : dcsRights.description != null)
            return false;
        if (license != null ? !license.equals(dcsRights.license) : dcsRights.license != null) return false;
        if (rightsUri != null ? !rightsUri.equals(dcsRights.rightsUri) : dcsRights.rightsUri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (rightsUri != null ? rightsUri.hashCode() : 0);
        result = 31 * result + (license != null ? license.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DcsRights{" +
                "description='" + description + '\'' +
                ", rightsUri=" + rightsUri +
                ", license=" + license +
                '}';
    }
}
