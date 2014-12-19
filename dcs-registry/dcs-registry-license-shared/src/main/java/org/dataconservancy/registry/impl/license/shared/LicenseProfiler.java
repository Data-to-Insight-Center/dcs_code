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

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.profile.api.DcpProfile;
import org.dataconservancy.profile.support.CollectionMatchStrategy;
import org.dataconservancy.profile.support.ProfileStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

/**
 * Determines whether or not a given DCP contains a license registry entry.
 * <p/>
 * Note that this class is really an experiment right now.  While {@link #conformsTo(org.dataconservancy.model.dcp.Dcp)}
 * is a reasonable method to be exposed via some interface, there are improvements which could be made.  For
 * example, the idea of a profiler being able to select entities of interest from a package.  One pain point right now
 * especially is the injection of profile statements for various portions of the DCP.
 */
public class LicenseProfiler implements DcpProfile {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Set<ProfileStatement> technicalEnvironmentProfile;
    private ProfileStatement deliverableUnitProfile;
    private ProfileStatement fileProfile;

    /**
     * Selects the Deliverable Unit from the package which has the appropriate &lt;type> (as supplied by
     * {@link #setDeliverableUnitProfile(ProfileStatement)}).
     *
     * @param dcp the package
     * @return the deliverable unit, or null if it can't be found
     */
    DcsDeliverableUnit selectDeliverableUnit(Dcp dcp) {
        DcsDeliverableUnit du = null;

        for (DcsDeliverableUnit candidateDu : dcp.getDeliverableUnits()) {
            if (deliverableUnitProfile.evaluate(candidateDu.getType())) {
                du = candidateDu;
            }
        }

        return du;
    }

    /**
     * Selects the Manifestation from the package which has the appropriate &lt;technicalEnvironment> (as supplied
     * by {@link #setTechnicalEnvironmentProfile(java.util.Set)}).
     *
     * @param dcp the package
     * @return the manifestation, or null if it can't be found.
     */
    DcsManifestation selectManifestation(Dcp dcp) {
        final Collection<DcsManifestation> manifestations = dcp.getManifestations();
        DcsManifestation manifestation = null;

        for (DcsManifestation candidateManifestation : manifestations) {
            Collection<String> techEnv = candidateManifestation.getTechnicalEnvironment();
            boolean match = true;
            for (ProfileStatement statement : technicalEnvironmentProfile) {
                match &= statement.evaluate(techEnv, CollectionMatchStrategy.AT_LEAST_ONE);
            }

            if (match) {
                manifestation = candidateManifestation;
                break;
            }
        }
        return manifestation;
    }

    /**
     * Selects the File from the package which contains the serialized license
     * (as supplied by {@link #setFileProfile(ProfileStatement)}).
     *
     * @param dcp the package
     * @return the fule, or null if it can't be found
     */
    DcsFile selectFile(Dcp dcp) {
        final DcsManifestation manifestation = selectManifestation(dcp);

        if (manifestation == null) {
            return null;
        }

        DcsFile licenseStream = null;

        for (DcsManifestationFile mf : manifestation.getManifestationFiles()) {
            DcsFileRef fileRef = mf.getRef();
            if (fileRef == null) {
                continue;
            }

            for (DcsFile candidateFile : dcp.getFiles()) {
                if (candidateFile.getId().equals(fileRef.getRef())) {
                    if (fileProfile.evaluate(candidateFile.getName())) {
                        licenseStream = candidateFile;
                    }
                }
            }
        }
        return licenseStream;
    }

    /**
     * Determines if the supplied package conforms to this profile.
     *
     * @param dcp the package
     * @return true if the package is conformant to this profile
     * @throws IllegalStateException if the required profiles are not present
     * @see #setDeliverableUnitProfile(ProfileStatement)
     * @see #setFileProfile(ProfileStatement)
     * @see #setTechnicalEnvironmentProfile(java.util.Set)
     */
    public boolean conformsTo(Dcp dcp) {
        if (technicalEnvironmentProfile == null) {
            throw new IllegalStateException("A technical environment profile has not been set.");
        }

        if (deliverableUnitProfile == null) {
            throw new IllegalStateException("A deliverable unit profile has not been set.");
        }

        if (fileProfile == null) {
            throw new IllegalStateException("A file profile has not been set.");
        }

        if (dcp == null) {
            log.debug("DCP was null.");
            return false;
        }

        if (dcp.getDeliverableUnits() == null || !dcp.getDeliverableUnits().iterator().hasNext()) {
            log.debug("Malformed package: DCP has no Deliverable Units.");
            return false;
        }

        if (dcp.getManifestations() == null || !dcp.getManifestations().iterator().hasNext()) {
            log.debug("Malformed package: DCP has no Manifestations");
            return false;
        }

        if (dcp.getFiles() == null || !dcp.getFiles().iterator().hasNext()) {
            log.debug("Malformed package: DCP has no Files");
            return false;
        }

        if (selectDeliverableUnit(dcp) == null) {
            log.debug("Malformed package: No Deliverable Unit with the correct type could be found.");
            return false;
        }

        if (selectManifestation(dcp) == null) {
            log.debug("Malformed package: No manifestation with the correct technical profile could be found.");
            return false;
        }

        DcsFile licenseStream = selectFile(dcp);

        if (licenseStream == null) {
            log.debug("Malformed package: No file containing the license stream could be found.");
            return false;
        }

        return true;
    }

    @Override
    public boolean conforms(Dcp candidatePackage) {
        return conformsTo(candidatePackage);
    }

    @Override
    public String getType() {
        return LicenseRegistryConstant.LICENSE_REGISTRY_ENTRY_TYPE;
    }

    @Override
    public String getVersion() {
        return LicenseRegistryConstant.VERSION_ONE;
    }

    /**
     * Obtain the technical environment profile that is required of conformant packages.
     *
     * @return the technical environment profile
     */
    public Set<ProfileStatement> getTechnicalEnvironmentProfile() {
        return technicalEnvironmentProfile;
    }

    /**
     * Set the technical environment profile that is required of conformant packages.
     *
     * @param technicalEnvironmentProfile a set of profile statements used to evaluate the a manifestation's
     *                                    technical environment
     */
    public void setTechnicalEnvironmentProfile(Set<ProfileStatement> technicalEnvironmentProfile) {
        this.technicalEnvironmentProfile = technicalEnvironmentProfile;
    }

    /**
     * Obtain the deliverable unit profile that is required of conformant packages.
     *
     * @return the technical environment profile
     */
    public ProfileStatement getDeliverableUnitProfile() {
        return deliverableUnitProfile;
    }

    /**
     * Set the deliverable unit profile that is required of conformant packages.
     *
     * @param deliverableUnitProfile a profile statement used to evaluate the type of the deliverable unit
     */
    public void setDeliverableUnitProfile(ProfileStatement deliverableUnitProfile) {
        this.deliverableUnitProfile = deliverableUnitProfile;
    }

    /**
     * Obtain the file profile that is required of conformant packages.
     *
     * @return the technical environment profile
     */
    public ProfileStatement getFileProfile() {
        return fileProfile;
    }

    /**
     * Set the file profile that is required of conformant packages.
     *
     * @param fileProfile a profile statement used to evaluate the name of a file
     */
    public void setFileProfile(ProfileStatement fileProfile) {
        this.fileProfile = fileProfile;
    }

    @Override
    public String toString() {
        return "LicenseProfiler{" +
                "fileProfile=" + fileProfile +
                ", technicalEnvironmentProfile=" + technicalEnvironmentProfile +
                ", deliverableUnitProfile=" + deliverableUnitProfile +
                '}';
    }
}
