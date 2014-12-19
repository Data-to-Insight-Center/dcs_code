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
package org.dataconservancy.reporting.model.builder.xstream;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.reporting.model.IngestReport.Status;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * IngestReportConverter is used by Xstream object builder to serialize and deserialize {@link IngestReport} objects.
 */
public class IngestReportConverter extends AbstractEntityConverter {
    
    protected static final String E_INGEST_REPORT = "IngestReport";
    protected static final String E_TOTAL_PACKAGE_SIZE = "TotalPackageSize";
    protected static final String E_FILE_TYPES = "FileTypes";
    protected static final String E_FILE_TYPE = "FileType";
    protected static final String E_DETAIL = "Detail";
    protected static final String E_TYPE_NAME = "TypeName";
    protected static final String E_COUNT = "Count";
    protected static final String E_COLLECTIONS = "Collections";
    protected static final String E_COLLECTION = "Collection";
    protected static final String E_COLLECTION_ID = "CollectionId";
    protected static final String E_GENERATED_CHECKSUMS = "GeneratedChecksums";
    protected static final String E_VERIFIED_CHECKSUMS = "VerifiedChecksums";
    protected static final String E_UNVERIFIED_CHECKSUMS = "UnVerifiedChecksums";
    protected static final String E_ALGORITHMS = "Algorithms";
    protected static final String E_ALGORITHM = "Algorithm";
    protected static final String E_INVALID_METADATA_FILES = "InvalidMetadataFiles";
    protected static final String E_PATH = "Path";
    protected static final String E_REGISTERED_SCHEMAS = "RegisteredSchemas";
    protected static final String E_REGISTERED_SCHEMA = "RegisteredSchema";
    protected static final String E_UNMATCHED_REGISTERED_SCHEMAS = "UnMatchedRegisteredSchemas";
    protected static final String E_UNREGISTERED_SCHEMAS = "UnRegisteredSchemas";
    protected static final String E_UNREGISTERED_SCHEMA = "UnRegisteredSchema";
    protected static final String E_SCHEMA_NAME = "SchemaName";
    protected static final String E_UNMATCHED_FILE_TYPES = "UnMatchedFileTypes";
    protected static final String E_PACKAGE_CONFORMING = "PackageConforming";
    protected static final String E_PACKAGE_READABLE = "PackageReadable";
    protected static final String E_PACKAGE_HAS_UNRESOLVABLE_IDS = "PackageHasUnresolvableIds";
    protected static final String E_STATUS_MESSAGE = "StatusMessage";
    protected static final String E_STATUS = "Status";
    protected static final String E_CONTENT_DETECTION_TOOLS = "ContentDetectionTools";
    protected static final String E_CONTENT_DETECTION_TOOL = "ContentDetectionTool";
    protected static final String E_NAME = "Name";
    protected static final String E_VERSION = "Version";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        
        IngestReport ingestReport = (IngestReport) source;
        
        if (ingestReport != null) {
            writer.startNode(E_TOTAL_PACKAGE_SIZE);
            writer.setValue(Long.toString(ingestReport.getTotalPackageSize()));
            writer.endNode();
            if (ingestReport.getFileTypeCount() != null) {
                Map<String, Integer> fileTypeCount = ingestReport.getFileTypeCount();
                writer.startNode(E_FILE_TYPES);
                for (String name : fileTypeCount.keySet()) {
                    writer.startNode(E_FILE_TYPE);
                    writer.startNode(E_TYPE_NAME);
                    writer.setValue(name);
                    writer.endNode();
                    writer.startNode(E_COUNT);
                    writer.setValue(Integer.toString(fileTypeCount.get(name)));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getDataItemsPerCollectionCount() != null) {
                Map<String, Integer> dataItemsPerCollectionCount = ingestReport.getDataItemsPerCollectionCount();
                writer.startNode(E_COLLECTIONS);
                for (String collectionId : dataItemsPerCollectionCount.keySet()) {
                    writer.startNode(E_COLLECTION);
                    writer.startNode(E_COLLECTION_ID);
                    writer.setValue(collectionId);
                    writer.endNode();
                    writer.startNode(E_COUNT);
                    writer.setValue(Integer.toString(dataItemsPerCollectionCount.get(collectionId)));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getGeneratedChecksumsCount() != null) {
                Map<String, Integer> generatedChecksumsCount = ingestReport.getGeneratedChecksumsCount();
                writer.startNode(E_GENERATED_CHECKSUMS);
                for (String algorithm : generatedChecksumsCount.keySet()) {
                    writer.startNode(E_ALGORITHMS);
                    writer.startNode(E_ALGORITHM);
                    writer.setValue(algorithm);
                    writer.endNode();
                    writer.startNode(E_COUNT);
                    writer.setValue(Integer.toString(generatedChecksumsCount.get(algorithm)));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getVerifiedChecksumsCount() != null) {
                Map<String, Integer> verifiedChecksumsCount = ingestReport.getVerifiedChecksumsCount();
                writer.startNode(E_VERIFIED_CHECKSUMS);
                for (String algorithm : verifiedChecksumsCount.keySet()) {
                    writer.startNode(E_ALGORITHMS);
                    writer.startNode(E_ALGORITHM);
                    writer.setValue(algorithm);
                    writer.endNode();
                    writer.startNode(E_COUNT);
                    writer.setValue(Integer.toString(verifiedChecksumsCount.get(algorithm)));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getUnverifiedChecksumsCount() != null) {
                Map<String, Integer> unverifiedChecksumsCount = ingestReport.getUnverifiedChecksumsCount();
                writer.startNode(E_UNVERIFIED_CHECKSUMS);
                for (String algorithm : unverifiedChecksumsCount.keySet()) {
                    writer.startNode(E_ALGORITHMS);
                    writer.startNode(E_ALGORITHM);
                    writer.setValue(algorithm);
                    writer.endNode();
                    writer.startNode(E_COUNT);
                    writer.setValue(Integer.toString(unverifiedChecksumsCount.get(algorithm)));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getRegisteredSchemasCount() != null) {
                Map<String, Integer> registeredSchemasCount = ingestReport.getRegisteredSchemasCount();
                writer.startNode(E_REGISTERED_SCHEMAS);
                for (String name : registeredSchemasCount.keySet()) {
                    writer.startNode(E_REGISTERED_SCHEMA);
                    writer.startNode(E_SCHEMA_NAME);
                    writer.setValue(name);
                    writer.endNode();
                    writer.startNode(E_COUNT);
                    writer.setValue(Integer.toString(registeredSchemasCount.get(name)));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getUnRegisteredSchemasCount() != null) {
                Map<String, Integer> unregisteredSchemasCount = ingestReport.getUnRegisteredSchemasCount();
                writer.startNode(E_UNREGISTERED_SCHEMAS);
                for (String name : unregisteredSchemasCount.keySet()) {
                    writer.startNode(E_UNREGISTERED_SCHEMA);
                    writer.startNode(E_SCHEMA_NAME);
                    writer.setValue(name);
                    writer.endNode();
                    writer.startNode(E_COUNT);
                    writer.setValue(Integer.toString(unregisteredSchemasCount.get(name)));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getInvalidMetadataFileWRegisteredSchemas() != null) {
                List<String> invalidMetadataFileWRegisteredSchemas = ingestReport
                        .getInvalidMetadataFileWRegisteredSchemas();
                writer.startNode(E_INVALID_METADATA_FILES);
                for (String path : invalidMetadataFileWRegisteredSchemas) {
                    writer.startNode(E_PATH);
                    writer.setValue(path);
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getUnmatchedFileTypes() != null) {
                Map<String, String> unmatchedFileTypesCount = ingestReport.getUnmatchedFileTypes();
                writer.startNode(E_UNMATCHED_FILE_TYPES);
                for (String name : unmatchedFileTypesCount.keySet()) {
                    writer.startNode(E_FILE_TYPE);
                    writer.startNode(E_TYPE_NAME);
                    writer.setValue(name);
                    writer.endNode();
                    writer.startNode(E_DETAIL);
                    writer.setValue(unmatchedFileTypesCount.get(name));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getUnmatchedRegisteredSchemasCount() != null) {
                Map<String, Integer> unmatchedRegisteredSchemasCount = ingestReport.getUnmatchedRegisteredSchemasCount();
                writer.startNode(E_UNMATCHED_REGISTERED_SCHEMAS);
                for (String name : unmatchedRegisteredSchemasCount.keySet()) {
                    writer.startNode(E_REGISTERED_SCHEMA);
                    writer.startNode(E_SCHEMA_NAME);
                    writer.setValue(name);
                    writer.endNode();
                    writer.startNode(E_COUNT);
                    writer.setValue(Integer.toString(unmatchedRegisteredSchemasCount.get(name)));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getContentDetectionTools() != null) {
                Map<String, String> contentDetectionTools = ingestReport.getContentDetectionTools();
                writer.startNode(E_CONTENT_DETECTION_TOOLS);
                for (String name : contentDetectionTools.keySet()) {
                    writer.startNode(E_CONTENT_DETECTION_TOOL);
                    writer.startNode(E_NAME);
                    writer.setValue(name);
                    writer.endNode();
                    writer.startNode(E_VERSION);
                    writer.setValue(contentDetectionTools.get(name));
                    writer.endNode();
                    writer.endNode();
                }
                writer.endNode();
            }
            if (ingestReport.getStatusMessage() != null) {
                writer.startNode(E_STATUS_MESSAGE);
                writer.setValue(ingestReport.getStatusMessage());
                writer.endNode();
            }
            if (ingestReport.isPackageConforming()) {
                writer.startNode(E_PACKAGE_CONFORMING);
                writer.setValue("True");
                writer.endNode();
            }
            else {
                writer.startNode(E_PACKAGE_CONFORMING);
                writer.setValue("False");
                writer.endNode();
            }
            if (ingestReport.isPackageReadable()) {
                writer.startNode(E_PACKAGE_READABLE);
                writer.setValue("True");
                writer.endNode();
            }
            else {
                writer.startNode(E_PACKAGE_READABLE);
                writer.setValue("False");
                writer.endNode();
            }
            if (ingestReport.packageHasUnresolvableIds()) {
                writer.startNode(E_PACKAGE_HAS_UNRESOLVABLE_IDS);
                writer.setValue("True");
                writer.endNode();
            }
            else {
                writer.startNode(E_PACKAGE_HAS_UNRESOLVABLE_IDS);
                writer.setValue("False");
                writer.endNode();
            }
            if (ingestReport.getStatus() != null) {
                writer.startNode(E_STATUS);
                writer.setValue(ingestReport.getStatus().toString());
                writer.endNode();
            }
        }
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        IngestReport ingestReport = new IngestReport();
        
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String name = getElementName(reader);
            if (name.equals(E_TOTAL_PACKAGE_SIZE)) {
                String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    ingestReport.setTotalPackageSize(Long.parseLong(value.trim()));
                }
            }
            else if (name.equals(E_FILE_TYPES)) {
                Map<String, Integer> matchedFileTypes = new HashMap<String, Integer>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_FILE_TYPE)) {
                        String type = null;
                        String count = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_TYPE_NAME)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    type = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_COUNT)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    count = value.trim();
                                }
                            }
                            if (type != null && count != null) {
                                matchedFileTypes.put(type, Integer.parseInt(count));
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setFileTypeCount(matchedFileTypes);
            }
            else if (name.equals(E_COLLECTIONS)) {
                Map<String, Integer> dataItemsPerCollectionCount = new HashMap<String, Integer>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_COLLECTION)) {
                        String collectionId = null;
                        int count = 0;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_COLLECTION_ID)) {
                                collectionId = reader.getValue().trim();
                            }
                            if (getElementName(reader).equals(E_COUNT)) {
                                count = Integer.parseInt(reader.getValue().trim());
                            }
                            reader.moveUp();
                        }
                        dataItemsPerCollectionCount.put(collectionId, count);
                    }
                    reader.moveUp();
                }
                ingestReport.setDataItemsPerCollectionCount(dataItemsPerCollectionCount);
            }
            else if (name.equals(E_GENERATED_CHECKSUMS)) {
                Map<String, Integer> generatedChecksums = new HashMap<String, Integer>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_ALGORITHMS)) {
                        String algorithm = null;
                        String count = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_ALGORITHM)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    algorithm = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_COUNT)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    count = value.trim();
                                }
                            }
                            if (algorithm != null && count != null) {
                                generatedChecksums.put(algorithm, Integer.parseInt(count));
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setGeneratedChecksumsCount(generatedChecksums);
            }
            else if (name.equals(E_VERIFIED_CHECKSUMS)) {
                Map<String, Integer> verifiedChecksums = new HashMap<String, Integer>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_ALGORITHMS)) {
                        String algorithm = null;
                        String count = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_ALGORITHM)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    algorithm = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_COUNT)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    count = value.trim();
                                }
                            }
                            if (algorithm != null && count != null) {
                                verifiedChecksums.put(algorithm, Integer.parseInt(count));
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setVerifiedChecksumsCount(verifiedChecksums);
            }
            else if (name.equals(E_UNVERIFIED_CHECKSUMS)) {
                Map<String, Integer> unverifiedChecksums = new HashMap<String, Integer>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_ALGORITHMS)) {
                        String algorithm = null;
                        String count = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_ALGORITHM)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    algorithm = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_COUNT)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    count = value.trim();
                                }
                            }
                            if (algorithm != null && count != null) {
                                unverifiedChecksums.put(algorithm, Integer.parseInt(count));
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setUnverifiedChecksumsCount(unverifiedChecksums);
            }
            else if (name.equals(E_REGISTERED_SCHEMAS)) {
                Map<String, Integer> registeredSchemas = new HashMap<String, Integer>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_REGISTERED_SCHEMA)) {
                        String schemaName = null;
                        String count = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_SCHEMA_NAME)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    schemaName = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_COUNT)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    count = value.trim();
                                }
                            }
                            if (schemaName != null && count != null) {
                                registeredSchemas.put(schemaName, Integer.parseInt(count));
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setRegisteredSchemasCount(registeredSchemas);
            }
            else if (name.equals(E_UNREGISTERED_SCHEMAS)) {
                Map<String, Integer> unregisteredSchemas = new HashMap<String, Integer>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_UNREGISTERED_SCHEMA)) {
                        String schemaName = null;
                        String count = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_SCHEMA_NAME)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    schemaName = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_COUNT)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    count = value.trim();
                                }
                            }
                            if (schemaName != null && count != null) {
                                unregisteredSchemas.put(schemaName, Integer.parseInt(count));
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setUnRegisteredSchemasCount(unregisteredSchemas);
            }
            else if (name.equals(E_INVALID_METADATA_FILES)) {
                List<String> invalidMetadataFiles = new ArrayList<String>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_PATH)) {
                        invalidMetadataFiles.add(reader.getValue().trim());
                    }
                    reader.moveUp();
                }
                ingestReport.setInvalidMetadataFileWRegisteredSchemas(invalidMetadataFiles);
            }
            else if (name.equals(E_UNMATCHED_FILE_TYPES)) {
                Map<String, String> unmatchedFileTypes = new HashMap<String, String>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_FILE_TYPE)) {
                        String typeName = null;
                        String detail = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_TYPE_NAME)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    typeName = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_DETAIL)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    detail = value.trim();
                                }
                            }
                            if (typeName != null && detail != null) {
                                unmatchedFileTypes.put(typeName, detail);
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setUnmatchedFileTypes(unmatchedFileTypes);
            }
            else if (name.equals(E_UNMATCHED_REGISTERED_SCHEMAS)) {
                Map<String, Integer> unmatchedRegisteredSchemas = new HashMap<String, Integer>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_REGISTERED_SCHEMA)) {
                        String schemaName = null;
                        String count = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_SCHEMA_NAME)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    schemaName = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_COUNT)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    count = value.trim();
                                }
                            }
                            if (schemaName != null && count != null) {
                                unmatchedRegisteredSchemas.put(schemaName, Integer.parseInt(count));
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setUnmatchedRegisteredSchemasCount(unmatchedRegisteredSchemas);
            }
            else if (name.equals(E_CONTENT_DETECTION_TOOLS)) {
                Map<String, String> contentDetectionTools = new HashMap<String, String>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_CONTENT_DETECTION_TOOL)) {
                        String nameElem = null;
                        String version = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if (getElementName(reader).equals(E_NAME)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    nameElem = value.trim();
                                }
                            }
                            else if (getElementName(reader).equals(E_VERSION)) {
                                String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    version = value.trim();
                                }
                            }
                            if (nameElem != null && version != null) {
                                contentDetectionTools.put(nameElem, version);
                            }
                            reader.moveUp();
                        }
                    }
                    reader.moveUp();
                }
                ingestReport.setContentDetectionTools(contentDetectionTools);
            }
            else if (name.equals(E_STATUS_MESSAGE)) {
                String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    ingestReport.setStatusMessage(value.trim());
                }
            }
            else if (name.equals(E_PACKAGE_CONFORMING)) {
                String value = reader.getValue();
                if (value.equalsIgnoreCase("true")) {
                    ingestReport.setPackageConforming(true);
                }
                else {
                    ingestReport.setPackageConforming(false);
                }
            }
            else if (name.equals(E_PACKAGE_READABLE)) {
                String value = reader.getValue();
                if (value.equalsIgnoreCase("true")) {
                    ingestReport.setPackageReadable(true);
                }
                else {
                    ingestReport.setPackageReadable(false);
                }
            }
            else if (name.equals(E_PACKAGE_HAS_UNRESOLVABLE_IDS)) {
                String value = reader.getValue();
                if (value.equalsIgnoreCase("true")) {
                    ingestReport.setPackageHasUnresolvableIds(true);
                }
                else {
                    ingestReport.setPackageHasUnresolvableIds(false);
                }
            }
            else if (name.equals(E_STATUS)) {
                String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    if (value.equalsIgnoreCase("successful")) {
                        ingestReport.setStatus(Status.SUCCESSFUL);
                    }
                    else if (value.equalsIgnoreCase("warnings")) {
                        ingestReport.setStatus(Status.WARNINGS);
                    }
                    else if (value.equalsIgnoreCase("errors")) {
                        ingestReport.setStatus(Status.ERRORS);
                    }
                }
            }
            reader.moveUp();
        }

        return ingestReport;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return type == IngestReport.class;
    }
}
