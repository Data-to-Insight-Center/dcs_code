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

import org.dataconservancy.model.dcs.support.Assertion;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates a license associated with an entity in the Data Conservancy.  The license has a number of properties,
 * some of which may move off of this object in the future, to a license registry or service.
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
 * @see DcsRights
 */
public class DcsLicense {

    /**
     * The human-readable name of this license.  For example:
     * "Attribution-NonCommercial-ShareAlike 3.0 Unported"
     */
    private String name;

    /**
     * The license tag.  For example:
     * "CC BY-NC-SA 3.0"
     */
    private String tag;

    /**
     * The particular version of the license.  For example:
     * "3.0"
     */
    private String version;

    /**
     * A human-readable summary of the full legal code of the license.
     */
    private String summary;

    /**
     * The full legal code of the license.
     */
    private String fullText;

    /**
     * URIs that this license is referenced as.  May be internal or external URIs.  For example:
     * "http://creativecommons.org/licenses/by-nc-sa/3.0/" or "urn:dataconservancy.org:license/by-nc-sa/3.0"
     */
    private Set<URI> uris = new HashSet<URI>();

    /**
     * The human-readable name of this license.  For example:
     * "Attribution-NonCommercial-ShareAlike 3.0 Unported"
     *
     * @return the human-readable name
     */
    public String getName() {
        return name;
    }

    /**
     * The human-readable name of this license.  For example:
     * "Attribution-NonCommercial-ShareAlike 3.0 Unported"
     *
     * @param name the human-readable name
     */
    public DcsLicense setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The license tag.  For example:
     * "CC BY-NC-SA 3.0"
     *
     * @return the license tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * The license tag.  For example:
     * "CC BY-NC-SA 3.0"
     *
     * @param tag the license tag
     */
    public DcsLicense setTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * The particular version of the license.  For example:
     * "3.0"
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * The particular version of the license.  For example:
     * "3.0"
     *
     * @param version the version
     */
    public DcsLicense setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * A human-readable summary of the full legal code of the license.
     *
     * @return the human-readable summary of the license
     */
    public String getSummary() {
        return summary;
    }

    /**
     * A human-readable summary of the full legal code of the license.
     *
     * @param summary the human-readable summary of the license
     */
    public DcsLicense setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * The full, human-readable, legal code of the license.
     *
     * @return the full, human-readable, legal code of the license.
     */
    public String getFullText() {
        return fullText;
    }

    /**
     * The full legal code of the license.
     *
     * @param fullText the full, human-readable, legal code of the license.
     */
    public DcsLicense setFullText(String fullText) {
        this.fullText = fullText;
        return this;
    }

    /**
     * URIs that this license is referenced as.  May be internal or external URIs.  For example:
     * "http://creativecommons.org/licenses/by-nc-sa/3.0/" or "urn:dataconservancy.org:license/by-nc-sa/3.0"
     *
     * @return the uris
     */
    public Set<URI> getUris() {
        return uris;
    }

    /**
     * URIs that this license is referenced as.  May be internal or external URIs.  For example:
     * "http://creativecommons.org/licenses/by-nc-sa/3.0/" or "urn:dataconservancy.org:license/by-nc-sa/3.0"
     *
     * @param uris the uris
     */
    public DcsLicense setUris(Set<URI> uris) {
        Assertion.notNull(uris);
        this.uris = uris;
        return this;
    }

    /**
     * URIs that this license is referenced as.  May be internal or external URIs.  For example:
     * "http://creativecommons.org/licenses/by-nc-sa/3.0/" or "urn:dataconservancy.org:license/by-nc-sa/3.0"
     *
     * @param uris the uris
     */
    public void addUris(URI... uris) {
        Assertion.notNull(uris);
        for (int i = 0; i < uris.length; i++) {
            this.uris.add(uris[i]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsLicense that = (DcsLicense) o;

        if (fullText != null ? !fullText.equals(that.fullText) : that.fullText != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (summary != null ? !summary.equals(that.summary) : that.summary != null) return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        if (uris != null ? !uris.equals(that.uris) : that.uris != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (fullText != null ? fullText.hashCode() : 0);
        result = 31 * result + (uris != null ? uris.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DcsLicense{" +
                "name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", version='" + version + '\'' +
                ", summary='" + summary + '\'' +
                ", fullText='" + fullText + '\'' +
                ", uris=" + uris +
                '}';
    }
}
