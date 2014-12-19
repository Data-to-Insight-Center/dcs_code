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

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.packaging.ingest.api.*;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.AttributeValueType;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * BusinessObjectBuilder takes the Attribute sets in a package and builds the corresponding business objects.
 * Only relationships which are expressed through fields on the created objects are captured.
 */
public class BusinessObjectBuilder extends BaseIngestService {

    private IdService idService;

    protected static final String SUCCESS_EVENT_DETAIL = "Business objects described in the package were transformed into their corresponding BusinessObject representations.";

    protected static final String SUCCESS_EVENT_OUTCOME = "BusinessObjects building process was completed. ";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {

        super.execute(depositId, state);

        List<DcsEntityReference> businessObjectRefs = buildBusinessObjects(state, depositId);

        //Emit event indication that business objects had been created from attribute sets
        DcsEvent event = state.getEventManager().newEvent(Package.Events.BUSINESS_OBJECT_BUILT);
        event.setDetail(SUCCESS_EVENT_DETAIL);
        event.setOutcome(SUCCESS_EVENT_OUTCOME + businessObjectRefs.size() + " objects created.");
        event.setTargets(businessObjectRefs);
        state.getEventManager().addEvent(depositId, event);
    }

    /**
     * Build the business objects from the attribute sets, and add them to the business object manager
     *
     * @return  List<DcsEntityReference>
     * @throws StatefulIngestServiceException
     */
    private List<DcsEntityReference>  buildBusinessObjects(IngestWorkflowState state, String depositId) throws StatefulIngestServiceException{

        AttributeSetManager attributeSetManager = state.getAttributeSetManager();
        BusinessObjectManager businessObjectManager = state.getBusinessObjectManager();

        List<DcsEntityReference> businessObjectRefs = new ArrayList<DcsEntityReference>();
        
        //Map of all the parents in the package with the child as the key, and business object id of the parent as the value
        Map<String, String> parentMap = new HashMap<String, String>();
        
        //Map of files to their data items keyed by the data item business id, with a list of the data file local ids as values.
        Map<String, ArrayList<String>> dataItemFiles = new HashMap<String, ArrayList<String>>();
        
        //We need to hydrate collections after they all have minted ids so we store the attribute sets for later minting
        Map<String, AttributeSet> collectionAttributeSets = new HashMap<String, AttributeSet>();
        
        //We need to hydraate metadata files last so we save their attribute sets for the end;
        Map<String, AttributeSet> metadataFileAttributeSets = new HashMap<String, AttributeSet>();
        
        //A map of all the business object ids to their business object manager keys
        Map<String, String> businessIdToKeyMap = new HashMap<String, String>();
        
        //Go through all the projects and map their aggregated collections to them
        Map<String, String> collectionProjectMap = new HashMap<String, String>();
        
        for (AttributeSet projectAttributeSet : attributeSetManager.matches(AttributeSetUtil.ORE_PROJECT_MATCHER)) {
            String projectId = "";
            if (projectAttributeSet.getAttributesByName(Metadata.PROJECT_IDENTIFIER).iterator().hasNext()) {
                projectId = projectAttributeSet.getAttributesByName(Metadata.PROJECT_IDENTIFIER).iterator().next().getValue();
            } else if (projectAttributeSet.getAttributesByName(Metadata.PROJECT_RESOURCEID).iterator().hasNext()) {
                projectId = projectAttributeSet.getAttributesByName(Metadata.PROJECT_RESOURCEID).iterator().next().getValue();
            } else {
                throw new StatefulIngestServiceException("BusinessObjectBuilder failed Project attribute set must have either project identifier or project resource id.");     
            }
            
            for (Attribute collectionAggregation : projectAttributeSet.getAttributesByName(Metadata.PROJECT_AGGREGATES_COLLECTION)) {
                collectionProjectMap.put(collectionAggregation.getValue(), projectId);
            } 
            
            for (Attribute fileAggregation : projectAttributeSet.getAttributesByName(Metadata.PROJECT_AGGREGATES_FILE)) {
                parentMap.put(fileAggregation.getValue(), projectId);
            }
        }
        
        for (AttributeSet collectionAttributeSet : attributeSetManager.matches(AttributeSetUtil.ORE_COLLECTION_MATCHER)) {
            String businessId = null;
            String localId = null;
            
            if (collectionAttributeSet.getAttributesByName(Metadata.COLLECTION_RESOURCEID).iterator().hasNext()){
                localId = collectionAttributeSet.getAttributesByName(Metadata.COLLECTION_RESOURCEID).iterator().next().getValue();
            }//every BO attribute set must have a resourceId attribute

            businessId = idService.create(Types.COLLECTION.name()).getUrl().toString();
            Collection collection = new Collection();
            collection.setId(businessId);
            businessIdToKeyMap.put(businessId, localId);
            collectionAttributeSets.put(businessId, collectionAttributeSet);
            
            //Check if we have a project aggregating this collection and if so add it as a parent project
            if (collectionProjectMap.containsKey(localId)) {
                collection.setParentProjectId(collectionProjectMap.get(localId));
            }
                        
            for (Attribute dataItemAggregation : collectionAttributeSet.getAttributesByName(Metadata.COLLECTION_AGGREGATES_DATAITEM)) {
                Set<AttributeSet> dataItemAttributeSets = attributeSetManager.matches(AttributeSetName.ORE_REM_DATAITEM, 
                                                                                new AttributeImpl(Metadata.DATAITEM_RESOURCEID, AttributeValueType.STRING, dataItemAggregation.getValue()));

                if (dataItemAttributeSets == null || dataItemAttributeSets.isEmpty()){
                    String msg =
                            String.format("BusinessObjectBuilder failed:  aggregated DataItem attribute set %s is missing",
                                          dataItemAggregation.getValue());
                    throw new StatefulIngestServiceException(msg);                        
                }
                
                AttributeSet dataItemAttributeSet = dataItemAttributeSets.iterator().next();
                String pkgId = dataItemAttributeSet.getAttributesByName(Metadata.DATAITEM_RESOURCEID).iterator().next().getValue();
                
                parentMap.put(pkgId, businessId);
            }
            
            for (Attribute collectionAggregationAttribute : collectionAttributeSet.getAttributesByName(Metadata.COLLECTION_AGGREGATES_COLLECTION)) {
                Set<AttributeSet> aggregatedCollectionAttributeSets = attributeSetManager.matches(AttributeSetName.ORE_REM_COLLECTION, 
                                                                                      new AttributeImpl(Metadata.COLLECTION_RESOURCEID, AttributeValueType.STRING, collectionAggregationAttribute.getValue()));

                if (aggregatedCollectionAttributeSets == null || aggregatedCollectionAttributeSets.isEmpty()){
                    String msg =
                            String.format("BusinessObjectBuilder failed:  aggregated Collection attribute set %s is missing",
                                          collectionAggregationAttribute.getValue());
                    throw new StatefulIngestServiceException(msg);                        
                }

                AttributeSet subCollectionAttributeSet = aggregatedCollectionAttributeSets.iterator().next();
                String pkgId = subCollectionAttributeSet.getAttributesByName(Metadata.COLLECTION_RESOURCEID).iterator().next().getValue();
                parentMap.put(pkgId, businessId);
            }
            
            for (Attribute fileAggregation : collectionAttributeSet.getAttributesByName(Metadata.COLLECTION_AGGREGATES_FILE)) {
                parentMap.put(fileAggregation.getValue(), businessId);
            }
            businessObjectManager.add(localId, collection, Collection.class);

            businessObjectRefs.add(new DcsEntityReference(businessId));
        }
        
        for (AttributeSet dataItemAttributeSet : attributeSetManager.matches(AttributeSetUtil.ORE_DATAITEM_MATCHER)) {
            String businessId = null;
            String localId = null;
            
            if (dataItemAttributeSet.getAttributesByName(Metadata.DATAITEM_RESOURCEID).iterator().hasNext()){
                localId = dataItemAttributeSet.getAttributesByName(Metadata.DATAITEM_RESOURCEID).iterator().next().getValue();
            }//every BO attribute set must have a resourceId attribute

            //Check if the data was already added by a collection, if not add it
            businessId = idService.create(Types.DATA_SET.name()).getUrl().toString();
                
            DataItem dataItem = new DataItem();
            dataItem.setId(businessId);
            businessIdToKeyMap.put(businessId, localId);

            //Check for parent id
            if (!dataItemAttributeSet.getAttributesByName(Metadata.DATA_ITEM_IS_PART_OF_COLLECTION).isEmpty()) {
                dataItem.setParentId(dataItemAttributeSet.getAttributesByName(Metadata.DATA_ITEM_IS_PART_OF_COLLECTION).iterator().next().getValue());
            } else {
                if (parentMap.containsKey(localId)) {
                    dataItem.setParentId(parentMap.get(localId));
                }
            }
            
            dataItemFiles.put(businessId, new ArrayList<String>());

            hydrateDataItem(state, dataItemFiles, localId, dataItemAttributeSet, dataItem);
            businessObjectRefs.add(new DcsEntityReference(businessId));
        }
        
        for (AttributeSet fileAttributeSet : attributeSetManager.matches(AttributeSetUtil.ORE_FILE_MATCHER)) {
            String businessId = null;
            String localId = null;
            
            boolean isMetadataFile = false;
            
            if (fileAttributeSet.getAttributesByName(Metadata.FILE_RESOURCEID).iterator().hasNext()){
                localId = fileAttributeSet.getAttributesByName(Metadata.FILE_RESOURCEID).iterator().next().getValue();
            }//every BO attribute set must have a resourceId attribute

            if (fileAttributeSet.getAttributesByName(Metadata.FILE_IS_METADATA_FOR).size() > 0) {
                isMetadataFile = true;
            }
            
            //Data Items are stored differently so if the file is in here it's a metadata file 
            String parentId = "";
            if (parentMap.containsKey(localId)) {
                isMetadataFile = true;
                parentId = parentMap.get(localId);
            }
            
            if (isMetadataFile) {
                businessId = idService.create(Types.METADATA_FILE.name()).getUrl().toString();
                MetadataFile metadataFile = new MetadataFile();
                metadataFile.setId(businessId);
                businessIdToKeyMap.put(businessId, localId);

                metadataFile.setParentId(parentId);
                businessObjectManager.add(localId, metadataFile, MetadataFile.class);
                metadataFileAttributeSets.put(businessId, fileAttributeSet);
            } else {
                businessId = idService.create(Types.DATA_FILE.name()).getUrl().toString();
                DataFile dataFile = new DataFile();
                dataFile.setId(businessId);
                businessIdToKeyMap.put(businessId, localId);

                businessObjectManager.add(localId, dataFile, DataFile.class);
                hydrateDataFile(state, localId, fileAttributeSet, dataFile);
            }
                         
            businessObjectRefs.add(new DcsEntityReference(businessId));
        }

        for (Collection collection : businessObjectManager.getInstancesOf(Collection.class)) {
            AttributeSet collectionAttributeSet = collectionAttributeSets.get(collection.getId());
            if (collectionAttributeSet == null) {
                throw new StatefulIngestServiceException("Couldn't find attribute set for " + collection.getId());
            }
            
            String localId = businessIdToKeyMap.get(collection.getId());    
            if (localId == null || localId.isEmpty()) {
                throw new StatefulIngestServiceException("Couldn't find local id for " + collection.getId());
            }
            hydrateCollection(businessObjectManager, localId, collectionAttributeSet, parentMap, collection);
        }
        
        for (MetadataFile metadataFile : businessObjectManager.getInstancesOf(MetadataFile.class)) {
            String localId = businessIdToKeyMap.get(metadataFile.getId());    
            if (localId == null || localId.isEmpty()) {
                throw new StatefulIngestServiceException("Couldn't find local id for " + metadataFile.getId());
            }
            hydrateMetadataFile(state, localId, metadataFileAttributeSets.get(metadataFile.getId()), metadataFile);                

        }
        for (DataItem dataItem : businessObjectManager.getInstancesOf(DataItem.class)) {
            for (String fileId : dataItemFiles.get(dataItem.getId())) {
                DataFile file = (DataFile) businessObjectManager.get(fileId, DataFile.class);
                if (file == null) {
                    throw new StatefulIngestServiceException("Couldn't find file for data item: " + dataItem.getName());
                }
                dataItem.addFile(file);
                file.setParentId(dataItem.getId());
                String fileLocalId = businessIdToKeyMap.get(file.getId());
                businessObjectManager.update(fileLocalId, file, DataFile.class);
            }
            
            String key = businessIdToKeyMap.get(dataItem.getId());
            if (key == null || key.isEmpty()) {
                throw new StatefulIngestServiceException("Couldn't find key for data item: " + dataItem.getName());
            }
            businessObjectManager.update(key,  dataItem, DataItem.class);
        }
        
        return businessObjectRefs;
    }

    /**
     * Take an attribute set representing a collection and populate fields in the corresponding
     * business object
     * @param localId
     * @param oreRemAttributeSet
     * @throws StatefulIngestServiceException
     */
    private void hydrateCollection(BusinessObjectManager businessObjectManager, String localId, AttributeSet oreRemAttributeSet, Map<String, String> parentMap, Collection collection) throws StatefulIngestServiceException {

        if(collection == null){
             throw new StatefulIngestServiceException("Error occurred retrieving a Collection from the BusinessObjectManager.");
        }

        Set<Attribute> collectionTitles = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.COLLECTION_TITLE);
        for(Attribute attr : collectionTitles){
            if(collection.getTitle() == null){
                collection.setTitle(attr.getValue());
            }
        }

        Set<Attribute> collectionDescriptions = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.COLLECTION_DESCRIPTION);
        for(Attribute attr : collectionDescriptions){
            if(collection.getSummary() == null){
                collection.setSummary(attr.getValue());
            }
        }

        Set<Attribute> subcollections = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.COLLECTION_AGGREGATES_COLLECTION);
        List<String> childrenIds = new ArrayList<String>();
        for(Attribute attr : subcollections){
            childrenIds.add(businessObjectManager.get(attr.getValue(), Collection.class).getId());
        }
        
        if(childrenIds.size() > 0){
            collection.setChildrenIds(childrenIds);
        }
        
        String parentBusinessId = parentMap.get(localId);
        if (parentBusinessId != null && !parentBusinessId.isEmpty()) {
            collection.setParentId(parentBusinessId);
        }

        //If a collection is aggregated by a project or collection in the project that's it we ignore any ispart of relationships.
        if ((collection.getParentId() == null || collection.getParentId().isEmpty()) &&
                (collection.getParentProjectId() == null || collection.getParentProjectId().isEmpty())) {
            Set<Attribute> parentIds = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.COLLECTION_IS_PART_OF_COLLECTION);
            for(Attribute attr : parentIds) {
                //A collection can't be both aggregated and is part of a collection. So if we already have an aggregated parent set, then this must be a part of a project.
                //Note this behavior is asserted by a prior service
                if (collection.getParentId() == null) {
                    Collection parentCollection = (Collection) businessObjectManager.get(attr.getValue(), Collection.class);
                    if (parentCollection != null) {
                        collection.setParentId(parentCollection.getId());
                    }
                    else {       
                        try {
                            if (idService.fromUrl(new URL(attr.getValue())).getType() == Types.COLLECTION.getTypeName()) {
                                collection.setParentId(attr.getValue());
                            } else {
                                collection.setParentProjectId(attr.getValue());
                            }
                        } catch (Exception e) {
                            //This should never happen as this should be flagged by the external reference checker
                            log.warn("Attribute isPartOf for collection wasn't an actual id. " + e.getMessage());
                        } 
                    }
                    
                } else {
                    collection.setParentProjectId(attr.getValue());
                }
            }
        }


        
        Set<Attribute> collectionCreators = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.COLLECTION_CREATOR_NAME);
        List<PersonName> creators = new ArrayList<PersonName>();
        for(Attribute attr : collectionCreators){
           creators.add(parseCreatorName(attr.getValue()));
        }
        if(creators.size() > 0){
            collection.setCreators(creators);
        }

        Set<Attribute> publicationDates = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.COLLECTION_CREATED);
        for(Attribute attr : publicationDates){
            try {
                DateTime publicationDate = DateTime.parse(attr.getValue());
                if(collection.getPublicationDate() == null && publicationDate != null){
                    collection.setPublicationDate(publicationDate);
                }
            } catch (IllegalArgumentException e){
                log.warn(e.getMessage(), e);
            }
        }
        try {
            businessObjectManager.update(localId, collection, Collection.class);
        } catch (NonexistentBusinessObjectException e) {
            throw new StatefulIngestServiceException("Exception occurred when a Collection object was being updated in. " +
                    e.getMessage());
        }
    }

    /**
     * Take an attribute set representing a data item and populate fields in the corresponding
     * business object
     * @param localId
     * @param oreRemAttributeSet
     * @throws StatefulIngestServiceException
     */
    private void hydrateDataItem(IngestWorkflowState state, Map<String, ArrayList<String>> dataItemFiles,
                                 String localId, AttributeSet oreRemAttributeSet, DataItem dataItem) throws StatefulIngestServiceException {

        if(dataItem == null){
            throw new StatefulIngestServiceException("Error occurred retrieving a DataItem from the BusinessObjectManager.");
        }

        Set<Attribute> dataItemTitles = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.DATA_ITEM_TITLE);
        for(Attribute attr : dataItemTitles){
            if(dataItem.getName() == null){
                dataItem.setName(attr.getValue());
            }
        }

        Set<Attribute> dataItemDescriptions = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.DATA_ITEM_DESCRIPTION);
        for(Attribute attr : dataItemDescriptions){
            if(dataItem.getDescription() == null){
                dataItem.setDescription(attr.getValue());
            }
        }

        //we need to save the keys for the DataFiles, and add them to the
        // DataItem later when we are sure they are fully hydrated
        Set<Attribute> files = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.DATA_ITEM_AGGREGATES_FILE);
        for(Attribute attr : files){
            dataItemFiles.get(dataItem.getId()).add(attr.getValue());
        }
        
        dataItem.setDepositDate(new DateTime());
        
        if (state.getIngestUserId() != null && !state.getIngestUserId().isEmpty()) {
            dataItem.setDepositorId(state.getIngestUserId());
        }

        try {
            state.getBusinessObjectManager().add(localId, dataItem, DataItem.class);
        } catch (NonexistentBusinessObjectException e) {
            throw new StatefulIngestServiceException("Exception occurred when a DataItem object was being updated. " +
                    e.getMessage());
        }
    }

    /**
     * Take an attribute set representing a data file and populate fields in the corresponding
     * business object
     * @param localId
     * @param oreRemAttributeSet
     * @throws StatefulIngestServiceException
     */
    private void hydrateDataFile(IngestWorkflowState state, String localId, AttributeSet oreRemAttributeSet, DataFile dataFile) throws StatefulIngestServiceException {
        BusinessObjectManager businessObjectManager = state.getBusinessObjectManager();

        if(dataFile == null){
              throw new StatefulIngestServiceException("Error occurred retrieving a DataFile from the BusinessObjectManager.");
        }

        Set<Attribute> dataFileTitles = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.FILE_TITLE);
        for(Attribute attr : dataFileTitles){
            if(dataFile.getName() == null){
                dataFile.setName(attr.getValue());
            }
        }/*

        Set<Attribute> dataFileFormats = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.FILE_FORMAT);
        for(Attribute attr: dataFileFormats){
            if(dataFileToUpdate.getFormat() == null){
                dataFileToUpdate.setFormat(attr.getValue());
            }
        }*/

        //get file's set and source from File AttributeSet
        setFileSizeAndSource(state, oreRemAttributeSet, dataFile);

        try {
            businessObjectManager.update(localId, dataFile, DataFile.class);
        } catch (NonexistentBusinessObjectException e) {
            throw new StatefulIngestServiceException("Exception occurred when a DataFile object was being updated. " +
                    e.getMessage());
        }
    }

    /**
     * Take an attribute set representing a metadata file and populate fields in the corresponding
     * business object
     * @param localId
     * @param oreRemAttributeSet
     * @throws StatefulIngestServiceException
     */
    private void hydrateMetadataFile(IngestWorkflowState state, String localId, AttributeSet oreRemAttributeSet, MetadataFile metadataFile)
            throws StatefulIngestServiceException {
        BusinessObjectManager businessObjectManager = state.getBusinessObjectManager();

        if(metadataFile == null){
              throw new StatefulIngestServiceException("Error occurred retrieving a MetadataFile from the BusinessObjectManager.");
        }

        Set<Attribute> metadataFileTitles = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.FILE_TITLE);
        for(Attribute attr : metadataFileTitles){
            if(metadataFile.getName() == null){
                metadataFile.setName(attr.getValue());
            }
        }

     /*   Set<Attribute> metadataFileFormats = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.FILE_FORMAT);
        for(Attribute attr: metadataFileFormats){
            if(metadataFileToUpdate.getFormat() == null){
                metadataFileToUpdate.setFormat(attr.getValue());
            }
        }*/

        if (metadataFile.getParentId() == null || metadataFile.getParentId().isEmpty()) {
            Set<Attribute> isMetadataFors = (Set<Attribute>) oreRemAttributeSet.getAttributesByName(Metadata.FILE_IS_METADATA_FOR);
            for(Attribute attr : isMetadataFors){
                String targetId = attr.getValue();
    
                if (targetId == null) {
                    continue;
                }
    
                if (businessObjectManager.getType(targetId) != null) {
                    BusinessObject parent = businessObjectManager.get(targetId, businessObjectManager.getType(targetId));
                    if (parent != null) {
                      metadataFile.setParentId(parent.getId());  
                    }
                    
                    break;
                }
            }
        }

        //get file's set and source from File AttributeSet
        setFileSizeAndSource(state, oreRemAttributeSet, metadataFile);

        try {
            businessObjectManager.update(localId, metadataFile, MetadataFile.class);
        } catch (NonexistentBusinessObjectException e) {
            throw new StatefulIngestServiceException("Exception occurred when a MetadataFile object was being updated. " +
                    e.getMessage());
        }
    }

    private void setFileSizeAndSource(IngestWorkflowState state, AttributeSet fileAttributeSet, DataFile file) throws StatefulIngestServiceException{

        Attribute filePathAttribute;
        long fileSize;
        String fileAbsolutePath = "";
        //retrieve local file-uri from the file's ORE-ReM AttributeSet
        if (fileAttributeSet.getAttributesByName(Metadata.FILE_RESOURCEID).iterator().hasNext()) {
            //There should only be ONE file-resource id attribute in the AttributeSet.
            filePathAttribute = fileAttributeSet.getAttributesByName(Metadata.FILE_RESOURCEID).iterator().next();
            //retrieve the the decoded file-path value from the local file-uri
            try {
                String relativeFilePath = new URI(filePathAttribute.getValue()).getPath();
                File payloadFileBaseDir =  new File( state.getPackage().getSerialization().getExtractDir(),
                                                     state.getPackage().getSerialization().getBaseDir().getPath())
                                                     .getParentFile();
                File payloadFile = new File(payloadFileBaseDir, relativeFilePath);
                fileAbsolutePath = payloadFile.getPath();
                file.setSource(payloadFile.toURI().toURL().toExternalForm());
            } catch (URISyntaxException e) {
                throw new StatefulIngestServiceException("Error generating file uri: " + filePathAttribute.getValue(), e);
            } catch (MalformedURLException e) {
                throw new StatefulIngestServiceException("Error generating file url: " + filePathAttribute.getValue(), e);
            }
        }
        
           
        /**
         * TODO: DC-1441 card specs: If a given item in the payload (i.e. a file) has metadata
         * associated with it that specifies it's format, then
         * (a) if the system was able to determine a format itself ((in DCS-STY-108), a check will be
         * done between the system determined format and the Package asserted format.  If these are
         * different, a warning will be created for later reporting to the user, and the system will
         * specify the format to be that which it determined itself, and the file will continue to be
         * imported.
         * (b) if the system was unable to determine a format, then the Package asserted format will be
         * used to specify the format of the file on ingest.
         */
        //If we have the file absolute path we're set we can get the file attribute set and get the file format
        String fileFormat = "";
        if (!fileAbsolutePath.isEmpty()) {
            AttributeSet fileDetailsAttributeSet = state.getAttributeSetManager().getAttributeSet(fileAbsolutePath);
            if (fileDetailsAttributeSet != null) {
                //Loop through the formats just in case the first one can't be parsed
                for (Attribute formatAttribute : fileDetailsAttributeSet.getAttributesByName(Metadata.FILE_FORMAT)) {
                    try {
                        DcsFormat format = DcsFormat.parseDcsFormat(formatAttribute.getValue());
                        if (format.getSchemeUri().equals("http://www.nationalarchives.gov.uk/PRONOM/")) {
                            fileFormat = "info:pronom/" + format.getFormat();
                        } else {
                            fileFormat = format.getFormat();
                        }
                        
                        //We can only have one format on the object so break after the first one.
                        break;
                    } catch (IllegalArgumentException e) {
                        //In this case just move on to the next format
                    }   
                }
                
                if (fileDetailsAttributeSet.getAttributesByName(Metadata.FILE_SIZE).iterator().hasNext()) {
                    fileSize = Long.parseLong(fileDetailsAttributeSet.getAttributesByName(Metadata.FILE_SIZE)
                            .iterator().next().getValue());
                    file.setSize(fileSize);
                }
            }
        } 
        
        //If we still have found the file format pull it from the ore rem file attribute set
        if (fileFormat.isEmpty()) {
            if (!fileAttributeSet.getAttributesByName(Metadata.FILE_FORMAT).isEmpty()) {
                Attribute formatAttribute = fileAttributeSet.getAttributesByName(Metadata.FILE_FORMAT).iterator().next();
                fileFormat = formatAttribute.getValue();
            } else if (!fileAttributeSet.getAttributesByName(Metadata.FILE_CONFORMS_TO).isEmpty()) {
                Attribute formatAttribute = fileAttributeSet.getAttributesByName(Metadata.FILE_CONFORMS_TO).iterator().next();
                fileFormat = formatAttribute.getValue();
            }
        }
        
        file.setFormat(fileFormat);

    }

    private PersonName parseCreatorName(String name){
        PersonName personName = new PersonName();
        String[] nameFields= name.split("\\s");
        int length = nameFields.length;
        int contentStarts = 0;
        int contentEnds = length -1;
        String firstField = nameFields[contentStarts];
        String lastField = nameFields[contentEnds];

        //institutional creators go in the family name slot
        if(lastField.toUpperCase().equals("UNIVERSITY") ||
                lastField.toUpperCase().equals("AGENCY") ||
                lastField.toUpperCase().equals("FOUNDATION") ||
                lastField.toUpperCase().equals("CENTER") ||
                lastField.toUpperCase().equals("CENTRE") ||
               length == 1){
           personName.setFamilyNames(name);
           return personName;
        }

        if(lastField.equals("I") ||
                lastField.equals("II") ||
                lastField.equals("III") ||
                lastField.equals("Jr") ||
                lastField.equals("Jr.") ||
                lastField.toUpperCase().equals("PHD") ||
                lastField.toUpperCase().equals("PHD.") ||
                lastField.toUpperCase().equals("PH.D")){
            personName.setSuffixes(lastField);
            contentEnds--;
        }

        if(firstField.equals("Dr") ||
                firstField.equals("Dr.") ||
                firstField.equals("Mr") ||
                firstField.equals("Mr.") ||
                firstField.equals("Ms") ||
                firstField.equals("Ms.") ||
                firstField.equals("Mrs") ||
                firstField.equals("Mrs.") ||
                firstField.equals("Miss")){
            personName.setPrefixes(firstField);
            contentStarts ++;
        }

        personName.setFamilyNames(nameFields[contentEnds]);

        if(contentStarts < contentEnds){
            personName.setGivenNames(nameFields[contentStarts]);
        }

        if(contentStarts < contentEnds - 1){
            String middleNames = "";
            for(int i = contentStarts + 1; i < contentEnds; i++){
                middleNames = middleNames + nameFields[i] + " ";
            }
            personName.setMiddleNames(middleNames.trim());
        }

        return personName;
    }

    public void setIdService(IdService idService){
        this.idService = idService;
    }

}



