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
package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.packaging.ingest.api.*;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.AttributeValueType;
import org.dataconservancy.packaging.model.Metadata;

import java.io.File;
import java.util.*;

/**
 * This class addresses one of functionalities of workflow step "Falcon".
 * This implementation assumes that {@code File} attribute set will be keyed by the file's path & name in the
 * {@link AttributeSetManager}, and that a file's ORE-ReM attribute set ("ORE-ReM-File") would contain a file's full
 * path and name.
 *
 * <p/>
 *
 * OUTPUTS:
 * For each file the service examines (payload files only), it emits:
 * <ul>
 *     <li>ONE SUCCESSFUL ({@code FORMAT_VERIFIED}) event, if any of the file's asserted formats was successfully
 *     verified against system detected formats for the file. </li>
 *     <li>ONE FAILURE ({@code FORMAT_VERIFICATION_FAIL}) event, if any of the file's asserted formats cannot be
 *     verified against the system detected formats for the file.</li>
 * </ul>
 *
 * SUCCESSFUL EVENT:
 * <ul>
 *     <li>Type: {@code FORMAT_VERIFIED}</li>
 *     <li>Target: points to the file which the event is generated for</li>
 *     <li>Details; include the list of detected formats against which asserted formats are verified</li>
 *     <li>Outcome: is the list of formats which were successfully verified</li>
 *
 * </ul>
 *
 * FAILURE EVENT:
 * <ul>
 *     <li>Type: {@code FORMAT_VERIFICATION_FAILURE}</li>
 *     <li>Target: points to the file which the event is generated for</li>
 *     <li>Details; include the list of detected formats against which asserted formats are verified</li>

 * </ul>
 */
public class PayloadFileFormatVerifier extends BaseIngestService {
    private static String pronomSchemaUri = "http://www.nationalarchives.gov.uk/PRONOM/";

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);
        verifyFormat(depositId, state);
    }

    private void verifyFormat(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        DcsEntityReference currentRef;

        //get all package file
        List<File> packageFiles = state.getPackage().getSerialization().getFiles();

        //get only payload file by checking file path
        AttributeSet fileAttributeSet = null;
        Collection<Attribute> formatAttributes = null;
        Attribute fileFormatToVerify;
        Set<AttributeSet> matchingOREAttributeSets;
        Set<String> detectedFormatURIs;
        Attribute fileNameAttribute;
        String detectedFormatAttributeValue;
        String detectedFormatUriString;
        Set<String> failedVerificationFormats;
        Set<String> verifiedFormats;
        File baseDir = new File( state.getPackage().getSerialization().getExtractDir(),  state.getPackage().getSerialization().getBaseDir().getPath()).getParentFile();

        //loop through package file for verify payload files' formats
        for (File file : packageFiles) {
            failedVerificationFormats = new HashSet<String>();
            verifiedFormats = new HashSet<String>();
            //only process files that are in the payload
            if (file.getPath().contains( state.getPackage().getSerialization().getBaseDir().getPath()
                    + File.separator + "data" + File.separator) && !file.isDirectory()) {
                //look up file attribute set which contains the DETECTED file formats
                fileAttributeSet = state.getAttributeSetManager().getAttributeSet(file.getPath());

                //for each file in the payload, pulls it file attribute set if
                //get all the format assertion from the existing attributeSets
                if (fileAttributeSet == null || fileAttributeSet.getAttributes().isEmpty()) {
                    throw new StatefulIngestServiceException("PayloadFileFormatVerifier: File AttributeSet is " +
                     "required but could not be found.");
                }

                //get file's format attributes. There could be multiple detected formats.
                formatAttributes = fileAttributeSet.getAttributesByName(Metadata.FILE_FORMAT);
                //get the format-id strings
                detectedFormatURIs = new HashSet<String>();
                for (Attribute formatAttribute : formatAttributes) {
                    detectedFormatAttributeValue = formatAttribute.getValue();
                    if (formatAttribute.getType().equals(AttributeValueType.DCS_FORMAT)) {
                        try {
                            detectedFormatUriString = createFormatURIString(DcsFormat.parseDcsFormat(detectedFormatAttributeValue));
                            if(detectedFormatUriString != null) {
                                detectedFormatURIs.add(detectedFormatUriString);
                            }
                        } catch (IllegalArgumentException e) {
                            log.warn("Unable to parse DcsFormat for format string value. " + e.getMessage());
                        }
                    }
                }

                String fileURI = toFileURI(baseDir.getPath(), file.getPath());
                //Create an attribute expression to used for searching for a particular attribute set in the ASManager
                fileNameAttribute = new AttributeImpl(Metadata.FILE_PATH, AttributeValueType.STRING, fileURI);


                //Search for ORE-REM-FILE attribute set which contains matching file-path attribute
                //ie ORE-REM-FILE attribute set of the file in specified file-path
                matchingOREAttributeSets =
                        state.getAttributeSetManager().matches(AttributeSetName.ORE_REM_FILE, fileNameAttribute);

                //Skip iteration when there's no ORE-REM-File attribute set found for current file
                if (matchingOREAttributeSets == null || matchingOREAttributeSets.size() == 0) {
                    log.info("File <" + fileNameAttribute.getValue() + "> did not have any matching ORE-ReM-File " +
                            "AttributeSet. No format verification will be performed for this file. ");
                    continue;
                }

                //assuming there's only ONE ORE-REM-FILE attribute set per file
                Collection<Attribute> fileConformsToAttributes =
                        matchingOREAttributeSets.iterator().next().getAttributesByName(Metadata.FILE_CONFORMS_TO);

                Collection<Attribute> fileFormatAttributes =
                        matchingOREAttributeSets.iterator().next().getAttributesByName(Metadata.FILE_FORMAT);

                Collection<Attribute> fileFormatsToVerify = new HashSet<Attribute>();
                fileFormatsToVerify.addAll(fileConformsToAttributes);
                fileFormatsToVerify.addAll(fileFormatAttributes);

                Iterator iterator = fileFormatsToVerify.iterator();
                //looping through  asserted formats
                while (iterator.hasNext()) {
                    fileFormatToVerify = (Attribute)iterator.next();
                    if (detectedFormatURIs.contains(fileFormatToVerify.getValue())) {
                        verifiedFormats.add(fileFormatToVerify.getValue());
                    } else {
                        failedVerificationFormats.add(fileFormatToVerify.getValue());
                    }
                }
                currentRef = new DcsEntityReference(file.getPath().substring(baseDir.getPath().length()));

                if (verifiedFormats.size() > 0) {
                    DcsEvent event = state.getEventManager().newEvent(Package.Events.FORMAT_VERIFIED);
                    event.setOutcome(verifiedFormats.toString());
                    event.setDetail("One or more asserted formats for file " + file.getPath().substring(baseDir.getPath().length()) +
                            " was successfully verified against detected formats: " + detectedFormatURIs);
                    event.addTargets(currentRef);
                    state.getEventManager().addEvent(depositId, event);
                }
                if (failedVerificationFormats.size() > 0) {
                    DcsEvent failureEvent = state.getEventManager().newEvent(Package.Events.FORMAT_VERIFICATION_FAILED);
                    failureEvent.setOutcome(failedVerificationFormats.toString());
                    failureEvent.setDetail("One or more asserted formats for file " + file.getPath().substring(baseDir.getPath().length()) +
                            " was not amongst detected formats: " + detectedFormatURIs);
                    failureEvent.addTargets(currentRef);
                    state.getEventManager().addEvent(depositId, failureEvent);
                }
            }
        }
    }

    /**
     * Build file URIs similar to one expressed in the Ore-Rem files and captured in Ore-Rem AttributeSet.
     * @param baseDirString
     * @param filePath
     * @return file URI string
     * @return null if a URI could not be created from the file's path and base directory.
     */
    private String toFileURI(String baseDirString, String filePath) {      
       return "file://" + filePath.substring(baseDirString.length()).replace(" ", "%20");
    }

    /**
     * Converts format id from the DcsFormat objects into formatURI string with qualifying namespace. Only applicable
     * to pronom format identifier at this point.
     * @param dcsFormat
     * @return
     */
    private String createFormatURIString(DcsFormat dcsFormat) {
        if (dcsFormat.getSchemeUri().equals(pronomSchemaUri)) {
            return "info:pronom/" + dcsFormat.getFormat();
        } else {
            log.info("Format URI conversion is not supported for: " + dcsFormat.getSchemeUri() +
                    ". Format URI conversion is supported for " + pronomSchemaUri +
                    ". Returning format string as format URI");
            return dcsFormat.getFormat();
        }
    }

    public static String getPronomSchemaUri() {
        return pronomSchemaUri;
    }

    public static void setPronomSchemaUri(String pronomSchemaUri) {
        PayloadFileFormatVerifier.pronomSchemaUri = pronomSchemaUri;
    }
}
