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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.packaging.model.builder.AttributeSetBuilder;
import org.dataconservancy.packaging.model.builder.xstream.XstreamAttributeSetBuilder;
import org.dataconservancy.packaging.model.builder.xstream.XstreamAttributeSetFactory;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.model.impl.Pair;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Created with IntelliJ IDEA.
 * User: hanh
 * Date: 8/15/13
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ASMetadataFileBuilder extends BaseIngestService {

    private static final String SUCCESS_EVENT_DETAIL = "Generated/extracted AttributeSets about BusinessObjects were " +
            "serialized into MetadataFiles. ";
    private static final String SUCCESS_EVENT_OUTCOME = "Serialization process was successfully completed. ";
    private IdService idService;

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {

        super.execute(depositId, state);

        Set<DcsEntityReference> metadataFilesCreated = buildAttributeSetMetadataFile(state);

        //Emit event indication that business objects had been created from attribute sets
        DcsEvent event = state.getEventManager().newEvent(org.dataconservancy.packaging.ingest.api.Package.Events.BUSINESS_OBJECT_BUILT);
        event.setDetail(SUCCESS_EVENT_DETAIL);
        event.setOutcome(SUCCESS_EVENT_OUTCOME + metadataFilesCreated.size() + " MetadataFile objects created.");
        event.setTargets(metadataFilesCreated);
        state.getEventManager().addEvent(depositId, event);
    }

    private Set<DcsEntityReference> buildAttributeSetMetadataFile(IngestWorkflowState state) throws StatefulIngestServiceException {
        AttributeSetManager attributeSetManager = state.getAttributeSetManager();
        BusinessObjectManager businessObjectManager = state.getBusinessObjectManager();
        Set<String> attributeSetManagerKeys = attributeSetManager.getKeys();
        Set<DcsEntityReference> metadataFileRefs = new HashSet<DcsEntityReference>();
        String metadataFileId = null;
        Map<String, Attribute> assertedChecksumMap =  getAssertedFileChecksumMap(attributeSetManager.getAttributeSet(AttributeSetName.BAGIT));

        for (String key: attributeSetManagerKeys) {
            AttributeSet attSet = attributeSetManager.getAttributeSet(key);

            if(attSet.getName().equals(AttributeSetName.ORE_REM_COLLECTION)) {
                //handles collection AS
                metadataFileId = handleCollection(businessObjectManager, attSet);
            } else if (attSet.getName().equals(AttributeSetName.ORE_REM_DATAITEM)) {
                //handles data item AS
                metadataFileId = handleDataItem(businessObjectManager, attSet);
            } else if (attSet.getName().equals(AttributeSetName.ORE_REM_FILE)) {
                metadataFileId = handleFile(state, assertedChecksumMap, attSet);
            }
            if(metadataFileId != null) {
                metadataFileRefs.add(new DcsEntityReference(metadataFileId));
                metadataFileId = null;
            }
        }
        return metadataFileRefs;
    }

    /**
     * Goes through BagIt attribute set, get check sum Attributes and store in a map, keyed by file's path for easy looking
     * when attribute sets for files are collected.
     */
    private Map<String, Attribute> getAssertedFileChecksumMap(AttributeSet attributeSet) throws StatefulIngestServiceException {
        if (!attributeSet.getName().equals(AttributeSetName.BAGIT)) {
            throw new StatefulIngestServiceException("Expected " + AttributeSetName.BAGIT + " attribute set, but given " +
                   attributeSet.getName() + ".");
        }
        Map<String, Attribute> assertedFileChecksumMap = new HashMap<String, Attribute>();
        java.util.Collection<Attribute> checksumAttributes = attributeSet.getAttributesByName(Metadata.BAGIT_CHECKSUM);
        for (Attribute attribute : checksumAttributes) {
            Pair<String, Checksum> checksumKVPair = parsePairString(attribute.getValue());
            //file path value used as key = data/.../.../<file-name>
            assertedFileChecksumMap.put((String) checksumKVPair.getKey(), attribute);
        }
        return assertedFileChecksumMap;
    }

    private String handleFile(IngestWorkflowState state, Map<String, Attribute> assertedFileChecksumMap,
                              AttributeSet attributeSet) throws StatefulIngestServiceException {
        AttributeSetManager attributeSetManager = state.getAttributeSetManager();
        Package pkg = state.getPackage();
        BusinessObjectManager businessObjectManager = state.getBusinessObjectManager();

        //extract dir           = /.../.../.../package-extraction
        //base dir              = <deposit-id>/<bag-name>
        //payload file base dir = /.../.../.../package-extraction/<deposit-id>
        File payloadFileBaseDir =  new File( state.getPackage().getSerialization().getExtractDir(),
                state.getPackage().getSerialization().getBaseDir().getPath())
                .getParentFile();

        if (!attributeSet.getName().equals(AttributeSetName.ORE_REM_FILE)) {
            throw new StatefulIngestServiceException("Expected attribute set named " + AttributeSetName.ORE_REM_FILE +
                    ". But was given " + attributeSet.getName());
        }
        //Create a set to hold generated attribute set for a file.
        Set<AttributeSet> attributeSetsForFile = new HashSet<AttributeSet>();

        //***Add ORE-ReM-FILE attribute set
        attributeSetsForFile.add(attributeSet);

        java.util.Collection<Attribute> filePathAttributes = attributeSet.getAttributesByName(Metadata.FILE_PATH);
        if (filePathAttributes.iterator().hasNext()) {
            String fileURI = filePathAttributes.iterator().next().getValue();
            try {
                //local file path = <bag-name>/data/.../.../<file-name>
                String localFilePath = new URI(fileURI).getPath();

                //absolute file path = /.../.../.../package-extraction/<deposit-id>/<bag-name>/data/.../.../<file-name>
                File fileAbsolutePath = new File(payloadFileBaseDir, localFilePath);

                //****Add FILE attribute set of the file   (File AttributeSet is keyed by absolute file path)
                AttributeSet fileAS = attributeSetManager.getAttributeSet(fileAbsolutePath.getPath());
                if (fileAS == null) {
                    throw new StatefulIngestServiceException("Could not find attribute set for " + fileAbsolutePath.getPath());
                }

                attributeSetsForFile.add(fileAS);
                
                 //package root dir = /.../.../.../package-extraction/<deposit-id>/<bag-name>
                String packageRootDir = new File(pkg.getSerialization().getExtractDir(), pkg.getSerialization().getBaseDir().getPath()).getPath();
                //file path in package = data/.../.../<file-name>
                String filePathInPackage = fileAbsolutePath.getPath().substring(packageRootDir.length()+1);

                //get File's checksum
                Attribute fileChecksum = assertedFileChecksumMap.get(filePathInPackage);

                //Create a BagIt AS to contain file's check sum
                AttributeSet fileBagItAS = new AttributeSetImpl(AttributeSetName.BAGIT);
                fileBagItAS.getAttributes().add(fileChecksum);

                //****add BagIt AS to the collection of AS for file
                attributeSetsForFile.add(fileBagItAS);

                //serialize all AS related to the current file
                MetadataFile metadataFile = buildMetadataFile(attributeSetsForFile);

                //Look up concerning file.
                java.util.Collection<Attribute> resourceIdAttributes =
                        attributeSet.getAttributesByName(Metadata.FILE_RESOURCEID);

                if (resourceIdAttributes.iterator().hasNext()) {
                    String fileResourceId = resourceIdAttributes.iterator().next().getValue();
                    DataFile file = (DataFile)businessObjectManager.get(fileResourceId, DataFile.class);
                    if (file != null) {
                        //set metadataFile parent ID
                        metadataFile.setParentId(file.getId());
                    } else {
                        MetadataFile metadataFileBO = (MetadataFile)businessObjectManager.get(fileResourceId, MetadataFile.class);
                        if (metadataFileBO != null) {
                            //set metadataFile parent ID
                            metadataFile.setParentId(metadataFileBO.getId());
                        }
                    }

                    businessObjectManager.add(metadataFile.getSource(), metadataFile, MetadataFile.class);
                } else {
                    throw new StatefulIngestServiceException("Encountered Ore-Rem-File AttributeSet with no " +
                        "File-ResourceId attribute.");
                }

                return metadataFile.getId();
            } catch (URISyntaxException e) {
                throw new StatefulIngestServiceException("Could not look up all Attribute Sets for file " +
                        fileURI + ".");
            }
        } else {
            throw new StatefulIngestServiceException("Could not find File-Path attribute in Ore-Rem-File AttributeSet" +
                    " keyed as " );
        }

    }

    /**
     * Serialize ORE-ReM-Collection attribute set, create a {@code MetadataFile} object to represent the serialized
     * attribute set and attached the {@code MetadataFile} to the appropriate {@code Collection}
     * @param attributeSet ORE-ReM-Collection
     * @throws StatefulIngestServiceException
     */
    private String handleCollection(BusinessObjectManager businessObjectManager, AttributeSet attributeSet) throws StatefulIngestServiceException {
        //create metadata file from attribute
        Set<AttributeSet> collectionAttributeSets = new HashSet<AttributeSet>();
        collectionAttributeSets.add(attributeSet);
        MetadataFile metadataFile = buildMetadataFile(collectionAttributeSets);

        //Find matching collection
        java.util.Collection<Attribute> resourceIdAttributes =
                attributeSet.getAttributesByName(Metadata.COLLECTION_RESOURCEID);
        if (resourceIdAttributes.iterator().hasNext()) {
            String collectionResourceId = resourceIdAttributes.iterator().next().getValue();
            Collection collection = (Collection)businessObjectManager.get(collectionResourceId, Collection.class);
            if (collection != null) {
                //set metadataFile's parent id
                metadataFile.setParentId(collection.getId());
                businessObjectManager.add(metadataFile.getSource(), metadataFile, MetadataFile.class);
            }
        } else {
            throw new StatefulIngestServiceException("Encountered Ore-Rem-Collection AttributeSet with no " +
                    "Collection-ResourceId attribute.");
        }

        return metadataFile.getId();
    }

    /**
     * Serialize ORE-ReM-DataItem attribute set, create a {@code MetadataFile} object to represent the serialized
     * attribute set and attached the {@code MetadataFile} to the appropriate {@code DataItem}
     * @param attributeSet
     * @throws StatefulIngestServiceException
     */
    private String handleDataItem(BusinessObjectManager businessObjectManager, AttributeSet attributeSet) throws StatefulIngestServiceException {
        //create metadata file from attribute
        Set<AttributeSet> dataItemAttributeSets = new HashSet<AttributeSet>();
        dataItemAttributeSets.add(attributeSet);
        MetadataFile metadataFile = buildMetadataFile(dataItemAttributeSets);

        //Find matching collection
        String dataItemResourceId = null;
        DataItem dataItem = null;
        java.util.Collection<Attribute> resourceIdAttributes =
                attributeSet.getAttributesByName(Metadata.DATAITEM_RESOURCEID);

        if (resourceIdAttributes.iterator().hasNext()) {
            dataItemResourceId = resourceIdAttributes.iterator().next().getValue();
            dataItem = (DataItem)businessObjectManager.get(dataItemResourceId, DataItem.class);
            if (dataItem != null) {
                //set metadataFile's parent id
                metadataFile.setParentId(dataItem.getId());
                businessObjectManager.add(metadataFile.getSource(), metadataFile, MetadataFile.class);
            }
        } else {
            throw new StatefulIngestServiceException("Encountered Ore-Rem-DataItem AttributeSet with no " +
                    "DataItem-ResourceId attribute.");
        }
        return metadataFile.getId();
    }

    /**
     * Helper method which except a set of {@code AttributeSet}, create a {@code MetadataFile} for it.
     * @param attributeSets
     * @return
     * @throws StatefulIngestServiceException
     */
    private MetadataFile buildMetadataFile (Set<AttributeSet> attributeSets)
            throws StatefulIngestServiceException {

        //create temp file with serialized attribute sets as content
        try {
            File tempMetadataFile = File.createTempFile("AttributeSetsMetadataFile", ".asmf");
            tempMetadataFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMetadataFile);
            // Setting xml header for the Xstream.
            fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>".getBytes("UTF-8"));
            AttributeSetBuilder builder = new XstreamAttributeSetBuilder(XstreamAttributeSetFactory.newInstance());
            builder.buildAttributeSets(attributeSets, fos);

            //create metadataFile object
            MetadataFile metadataFile = new MetadataFile();
            //Assigned business id
            String businessId = idService.create(Types.METADATA_FILE.name()).getUrl().toString();
            metadataFile.setId(businessId);
            metadataFile.setSource(tempMetadataFile.toURI().toURL().toExternalForm());
            metadataFile.setName("Metadata File from serialized AttributeSets");
            metadataFile.setFormat(MetadataFormatId.ATTRIBUTE_SETS_METADATA_FORMAT_ID);
            Resource r = new UrlResource(metadataFile.getSource());
            metadataFile.setSize(r.contentLength());            

            return metadataFile;
        } catch (IOException e) {
            throw new StatefulIngestServiceException("MetadataFile from generated AttributeSets could not be created. "
                    + e.getMessage());
        }
    }

    //TODO: refactor this method, which is a duplication of a method of the same name in PackageFileChecksumVerifier
    //class into some place more logical.
    /**
     * This method takes a serialized Pair of a String filename and Checksum for the file
     * and returns the deserialized Pair
     * @param serializedPair
     */
    public static Pair<String, Checksum> parsePairString(String serializedPair){
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
    public void setIdService (IdService idService) {
        this.idService = idService;
    }
}
