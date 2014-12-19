/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.ingest.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestService;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ingest service that ensures a BagIt Package is valid. The requirements are:
 * 
 * <p>
 * 1. Data directory should exist.
 * </p>
 * <p>
 * 2. Required elements should exist (bagit.txt, bag-info.txt, manifest-alg.txt and tagmanifest-alg.txt
 * </p>
 * <p>
 * 3. Required attributes should have been created.
 * </p>
 */
public class BagItPackageValidator extends BaseIngestService implements StatefulIngestService {
    
    private static final String TAGMANIFEST_SHA1_TXT = "tagmanifest-sha1.txt";
    private static final String TAGMANIFEST_MD5_TXT = "tagmanifest-md5.txt";
    private static final String MANIFEST_SHA1_TXT = "manifest-sha1.txt";
    private static final String MANIFEST_MD5_TXT = "manifest-md5.txt";
    private static final String BAG_INFO_TXT = "bag-info.txt";
    private static final String BAGIT_TXT = "bagit.txt";
    private static final String DATA = "data";
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);
        AttributeSetManager attSetManager = state.getAttributeSetManager();
        EventManager eventManager = state.getEventManager();
        org.dataconservancy.packaging.model.Package pkg = state.getPackage();
        
        if (!attSetManager.contains(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)) {
            throw new StatefulIngestServiceException(getClass().getSimpleName() + ": AttributeSetName '"
                    + AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA + "' is required, but it's missing.");
        }
        
        if (!attSetManager.contains(AttributeSetName.BAGIT)) {
            throw new StatefulIngestServiceException(getClass().getSimpleName() + ": AttributeSetName '"
                    + AttributeSetName.BAGIT + "' is required, but it's missing.");
        }
        
        List<List<String>> analysisResults = analyzePackage(pkg);
        if (analysisResults == null || analysisResults.size() == 0) {
            throw new StatefulIngestServiceException(getClass().getSimpleName()
                    + ": Package does not include any files.");
        }

        if (!dataDirExists(pkg, analysisResults.get(0))) {
            throw new StatefulIngestServiceException(getClass().getSimpleName()
                    + ": Package does not include data/ directory.");
        }

        String missingReqElement = checkMissingRequiredElements(pkg, analysisResults.get(1));
        if (missingReqElement != null) {
            throw new StatefulIngestServiceException(getClass().getSimpleName()
                    + ": Package does not include required element(s) '" + missingReqElement.trim() + "'.");
        }
        
        String missingReqAttribute = checkMissingRequiredAttributes(attSetManager, analysisResults.get(2));
        if (missingReqAttribute != null) {
            throw new StatefulIngestServiceException(getClass().getSimpleName()
                    + ": Package does not include any or all required attribute(s) of name '" + missingReqAttribute
                    + "'.");
        }
        
        DcsEvent event = eventManager.newEvent(
                org.dataconservancy.packaging.ingest.api.Package.Events.PACKAGE_VALIDATED);
        event.setDetail("BagIt package was successfully validated with deposit ID: " + depositId);
        event.setOutcome("Succeeded in validating the BagIt Package.");
        DcsEntityReference ref = new DcsEntityReference(depositId);
        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
        refs.add(ref);
        event.setTargets(refs);
        state.getEventManager().addEvent(depositId, event);

    }
    
    /**
     * Makes sure all the required attributes exist which validates the contents of required elements. Returns null if
     * they do and a string containing the missing attribute otherwise.
     * 
     * @param attSetManager
     * @return String
     */
    private String checkMissingRequiredAttributes(AttributeSetManager attSetManager, List<String> allBagFiles) {
        AttributeSet attSet = attSetManager.getAttributeSet(AttributeSetName.BAGIT);
        String missingReqAttribute = "";
        // Ensure all manifest entries exist from manifest-alg.txt and tagmanifest-alg.txt.
        if (attSet.getAttributesByName(Metadata.BAGIT_CHECKSUM).size() != allBagFiles.size()) {
            missingReqAttribute = Metadata.BAGIT_CHECKSUM;
            log.error("The total number of " + missingReqAttribute
                    + " attributes does not match the total number of files in the bag.");
            return missingReqAttribute;
        }
        
        // Ensure required properties exist.
        // attributes from bag-info.txt
        if (attSet.getAttributesByName(Metadata.CONTACT_EMAIL).size() == 0) {
            missingReqAttribute = Metadata.CONTACT_EMAIL;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.CONTACT_NAME).size() == 0) {
            missingReqAttribute = Metadata.CONTACT_NAME;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.CONTACT_PHONE).size() == 0) {
            missingReqAttribute = Metadata.CONTACT_PHONE;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.EXTERNAL_IDENTIFIER).size() == 0) {
            missingReqAttribute = Metadata.EXTERNAL_IDENTIFIER;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.BAG_SIZE).size() == 0) {
            missingReqAttribute = Metadata.BAG_SIZE;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.PAYLOAD_OXUM).size() == 0) {
            missingReqAttribute = Metadata.PAYLOAD_OXUM;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.BAG_COUNT).size() == 0) {
            missingReqAttribute = Metadata.BAG_COUNT;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.BAG_GROUP_IDENTIFIER).size() == 0) {
            missingReqAttribute = Metadata.BAG_GROUP_IDENTIFIER;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.BAGGING_DATE).size() == 0) {
            missingReqAttribute = Metadata.BAGGING_DATE;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        // Attributes from bagit.txt
        if (attSet.getAttributesByName(Metadata.BAGIT_VERSION).size() == 0) {
            missingReqAttribute = Metadata.BAGIT_VERSION;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        if (attSet.getAttributesByName(Metadata.TAG_FILE_CHARACTER_ENCODING).size() == 0) {
            missingReqAttribute = Metadata.TAG_FILE_CHARACTER_ENCODING;
            log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
            return missingReqAttribute;
        }
        
        attSet = attSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);

        if (attSet.getAttributes().size() > 0) {
            // Ensure required attributes for a DCS Package exist.
            if (attSet.getAttributesByName(Metadata.BAGIT_PROFILE_IDENTIFIER).size() == 0) {
                missingReqAttribute = Metadata.BAGIT_PROFILE_IDENTIFIER;
                log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
                return missingReqAttribute;
            }
            
            // Determine if DCS Package has an Ore-Rem, per current spec, this set would only have more than
            // one attribute if it has an Ore-Rem.
            if (attSet.getAttributes().size() > 1) {
                // Ensure DCS Package has Ore-ReM attributes
                if (attSet.getAttributesByName(Metadata.PKG_BAG_DIR).size() == 0) {
                    missingReqAttribute = Metadata.PKG_BAG_DIR;
                    log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
                    return missingReqAttribute;
                }
                
                if (attSet.getAttributesByName(Metadata.PKG_ORE_REM).size() == 0) {
                    missingReqAttribute = Metadata.PKG_ORE_REM;
                    log.error("Required Attribute " + missingReqAttribute + " doesn't exist.");
                    return missingReqAttribute;
                }
            }
            else {
                log.debug("The provided DCS package does not have an Ore-ReM");
            }
        }
        else {
            log.debug("The provided bag is not a DCS Package.");
        }
        
        return null;
    }

    /**
     * Checks to see whether the required elements i.e. bagit.txt, bag-info.txt, manifest-alg.txt and
     * tagmanifest-alg.txt exist or not. Returns null if they do, a string of missing elements otherwise.
     * 
     * @param pkg
     * @return String
     */
    private String checkMissingRequiredElements(Package pkg, List<String> requiredElements) {
        String missingReqElement = "";
        if (requiredElements.size() > 0) {
            if (hasAllRequiredElements(requiredElements)) {
                return null;
            }
            else {
                if (!requiredElements.contains(BAGIT_TXT)) {
                    missingReqElement += BAGIT_TXT;
                }
                if (!requiredElements.contains(BAG_INFO_TXT)) {
                    missingReqElement += BAG_INFO_TXT;
                }
                if (!requiredElements.contains(MANIFEST_MD5_TXT)) {
                    missingReqElement += MANIFEST_MD5_TXT;
                }
                else if (!requiredElements.contains(MANIFEST_SHA1_TXT)) {
                    missingReqElement += MANIFEST_SHA1_TXT;
                }
                if (!requiredElements.contains(TAGMANIFEST_MD5_TXT)) {
                    missingReqElement += TAGMANIFEST_MD5_TXT;
                }
                else if (!requiredElements.contains(TAGMANIFEST_SHA1_TXT)) {
                    missingReqElement += TAGMANIFEST_SHA1_TXT;
                }
                if (missingReqElement.length() > 0) {
                    missingReqElement += "; ";
                }
                log.error("Package does not include required element(s) '" + missingReqElement.trim() + "'.");
                return missingReqElement;
            }
        }
        else {
            missingReqElement = "All of the required elements";
            return missingReqElement;
        }
    }

    /**
     * Checks to make sure the minimum required elements are available.
     * 
     * @param requiredElements
     * @return boolean
     */
    private boolean hasAllRequiredElements(List<String> requiredElements) {
        if (requiredElements.contains(BAGIT_TXT) && requiredElements.contains(BAG_INFO_TXT)
                && (requiredElements.contains(MANIFEST_MD5_TXT) || requiredElements.contains(MANIFEST_SHA1_TXT))
                && (requiredElements.contains(TAGMANIFEST_MD5_TXT) || requiredElements.contains(TAGMANIFEST_SHA1_TXT))) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see whether the data/ directory exists or not.
     * 
     * @param pkg
     * @return boolean
     */
    private boolean dataDirExists(Package pkg, List<String> dataDir) {
        if (dataDir.size() > 0) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Iterates through all the files in the package and creates needed lists for further validation. Returns null if
     * package has no files.
     * 
     * @param pkg
     * @return List<List<String>>
     */
    private List<List<String>> analyzePackage(Package pkg) {
        List<List<String>> results = new ArrayList<List<String>>();
        List<String> requiredElements = new ArrayList<String>();
        List<String> allBagFiles = new ArrayList<String>();
        List<String> dataDir = new ArrayList<String>();
        
        if (pkg.getSerialization().getFiles().size() > 0) {
            for (File file : pkg.getSerialization().getFiles()) {
                if (file.isDirectory() && file.getName().equals(DATA)) {
                    dataDir.add(file.getName());
                }
                else if (file.isFile() && file.getName().equals(BAGIT_TXT)) {
                    requiredElements.add(file.getName());
                    allBagFiles.add(file.getName());
                }
                else if (file.isFile() && file.getName().equals(BAG_INFO_TXT)) {
                    requiredElements.add(file.getName());
                    allBagFiles.add(file.getName());
                }
                else if (file.isFile()
                        && (file.getName().equals(MANIFEST_MD5_TXT) || file.getName().equals(MANIFEST_SHA1_TXT))) {
                    requiredElements.add(file.getName());
                    allBagFiles.add(file.getName());
                }
                else if (file.isFile()
                        && (file.getName().equals(TAGMANIFEST_MD5_TXT) || file.getName().equals(TAGMANIFEST_SHA1_TXT))) {
                    requiredElements.add(file.getName());
                }
                else if (file.isFile()) {
                    // adding pay load files to check against manifest attributes.
                    allBagFiles.add(file.getName());
                }
            }
            results.add(0, dataDir);
            results.add(1, requiredElements);
            results.add(2, allBagFiles);
            return results;
        }
        else {
            return null;
        }
    }

}
