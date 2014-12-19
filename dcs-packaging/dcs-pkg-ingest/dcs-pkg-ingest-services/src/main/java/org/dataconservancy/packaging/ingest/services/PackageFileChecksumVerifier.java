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

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.model.impl.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Ingest service to verify checksums provided for files in the manifest file or in
 * tag files, with those calculated by the PackageFileAnalyzer
 * workflow step "Falcon", card DC-1438
 */
public class PackageFileChecksumVerifier extends BaseIngestService {

    @Override
    public void execute(String depositId, IngestWorkflowState state)
            throws StatefulIngestServiceException {
        super.execute(depositId, state);
            verifyChecksums(depositId, state);
    }

    private void verifyChecksums(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        //create a list of references in case checksums don't verify
        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
        DcsEntityReference ref;

        //get all the checksums from the manifest file
        AttributeSet bagitManifestAttributeSet = state.getAttributeSetManager().getAttributeSet(AttributeSetName.BAGIT);
        if (bagitManifestAttributeSet == null || bagitManifestAttributeSet.getAttributes().isEmpty()) {
            throw new StatefulIngestServiceException("PackageFileChecksumVerifier: BagIt-Manifest AttributeSet could not be found.");
        }
        Collection<Attribute> bagitAttributes = bagitManifestAttributeSet.getAttributes();

        // we need to make sure that there are no conflicts
        // between what is provided, and what is calculated. each provided checksum must agree with
        // calculated checksums of the same algorithm
        for(Attribute bagitAttr : bagitAttributes){
             if (bagitAttr.getName().equals(Metadata.BAGIT_CHECKSUM)){
                 //grab the value for the provided checksum and parse it
                 String attrValue = bagitAttr.getValue();
                 Pair<String, Checksum> manifestChecksumPair = parsePairString(attrValue);
                 String thisFile = manifestChecksumPair.getKey();
                 File absBaseDirectory = new File(state.getPackage().getSerialization().getExtractDir(),
                         state.getPackage().getSerialization().getBaseDir().getPath());
                 File fileFullPath = new File(absBaseDirectory, thisFile);
                 String providedAlgorithm = manifestChecksumPair.getValue().getAlgorithm();
                 String providedValue = manifestChecksumPair.getValue().getValue();

                 //get all file attributes for the file referenced by the manifest attribute
                 //this is where the calculated check sums live
                 AttributeSet matchingAttributeSet = state.getAttributeSetManager().getAttributeSet(fileFullPath.getAbsolutePath());
                 if (matchingAttributeSet == null) {
                     ref = new DcsEntityReference(thisFile);
                     refs.add(ref);
                 }
                 else
                 {
                     Collection<Attribute> fileAttributes = matchingAttributeSet.getAttributes();
    
                     //compare provided value against candidates for the calculated checksum
                     boolean checked = false;
                     for(Attribute fileAttr : fileAttributes){
                          if (fileAttr.getName().equals(Metadata.CALCULATED_CHECKSUM)) {
                               Pair<String, Checksum> fileChecksumPair = parsePairString(fileAttr.getValue());
                               String calculatedAlgorithm = fileChecksumPair.getValue().getAlgorithm();
                               String calculatedValue = fileChecksumPair.getValue().getValue();
                               //if it is the same algorithm, the values must agree, else we add a reference
                               //for an ingest fail event
                               if(providedAlgorithm.equals(calculatedAlgorithm)){
                                   checked = true;
                                   if(!providedValue.equals(calculatedValue)){
                                        ref = new DcsEntityReference(thisFile);
                                        refs.add(ref);
                                   }
                               }
                          }
                     }
                     if(!checked){//a calculated value for this provided value was missing, so we did not verify
                        ref = new DcsEntityReference(thisFile);
                        refs.add(ref);
                     }
                 }
             }
        }
        //we have checked all the provided checksums. if we have a failure, log an IngestFail event
        //with names of failed files as targets
        if( refs.size() > 0){
            DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
            event.setDetail("Checksum verification failure for deposit: " + depositId);
            event.setOutcome("Checksum verification failure");
            event.setTargets(refs);
            state.getEventManager().addEvent(depositId, event);
        }
    }

    /**
     * This method takes a serialized Pair of a String filename and Checksum for the file
     * and returns the deserialized Pair
     * @param serializedPair
     */
    private static Pair<String, Checksum> parsePairString(String serializedPair){
        String filename = null;
        Checksum checksum = null;

        if (!serializedPair.contains("key=") && !serializedPair.contains("value=")) {
            return new Pair<String, Checksum>(filename, checksum);
        }

        //Go to the start of the first filename character
        int startIndex = serializedPair.indexOf('=') + 2;
        filename = "";
        for (int i = startIndex; i < serializedPair.length(); i++) {
            char car = serializedPair.charAt(i);
            if (car != '\'') {
                filename += car;
            } else {
                startIndex = i;
                break;
            }
        }

        //now go to the start of the serialized checksum
        startIndex = serializedPair.indexOf('=', startIndex) + 2;
        String checksumString = "";
        for (int i = startIndex; i < serializedPair.length(); i++) {
            char car = serializedPair.charAt(i);
            if (i != serializedPair.lastIndexOf('\'')) {
                checksumString += car;
            } else {
                break;
            }
        }
        checksum = ChecksumImpl.parse(checksumString);

        return new Pair<String, Checksum>(filename, checksum);
    }

}
