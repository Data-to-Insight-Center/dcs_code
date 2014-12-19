/*
 * Copyright 2013 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.storage.dropbox.model.builder.xstream;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DropboxModelConverter extends AbstractEntityConverter implements ConverterConstants {

    private DateTimeFormatter fmt = DateTimeFormat.forPattern(DropboxModel.JODA_DATE_TIME_FORMATTER_STRING);

    /**
     * This method creates the xml serialization of a DropboxModel object. Because the metadata endpoint of
     * the Dropbox REST service does not provide a contents field for child folders of folders, we do not
     * look for it in child elements.
     * @param source
     * @param writer
     * @param context
     */
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        
        final DropboxModel dropboxModel = (DropboxModel) source;

        if (dropboxModel != null) {
            if (!isEmptyOrNull(dropboxModel.getId())) {
                writer.startNode(E_ID);
                writer.setValue(dropboxModel.getId());
                writer.endNode();
            }
            if (!isEmptyOrNull(fmt.print(dropboxModel.getClientMtime()))) {
                writer.startNode(E_CLIENT_M_TIME);
                writer.setValue(fmt.print(dropboxModel.getClientMtime()));
                writer.endNode();
            }
            if (!isEmptyOrNull(Long.toString(dropboxModel.getFileSize()))) {
                writer.startNode(E_FILESIZE);
                writer.setValue(Long.toString(dropboxModel.getFileSize()));
                writer.endNode();
            }
            if (null != dropboxModel.getContents() && dropboxModel.getContents().size() > 0) {
                writer.startNode(E_CONTENTS);
                for (DropboxModel contentEntry : dropboxModel.getContents()) {
                    createChild(writer, contentEntry);
                }
                writer.endNode();
            }
            if (!isEmptyOrNull(dropboxModel.getHash())) {
                writer.startNode(E_HASH);
                writer.setValue(dropboxModel.getHash());
                writer.endNode();
            }
            if (!isEmptyOrNull(dropboxModel.getIcon())) {
                writer.startNode(E_ICON);
                writer.setValue(dropboxModel.getIcon());
                writer.endNode();
            }
            if (dropboxModel.isDeleted()) {
                writer.startNode(E_IS_DELETED);
                writer.setValue("true");
                writer.endNode();
            }
            else {
                writer.startNode(E_IS_DELETED);
                writer.setValue("false");
                writer.endNode();
            }
            if (dropboxModel.isDir()) {
                writer.startNode(E_IS_DIR);
                writer.setValue("true");
                writer.endNode();
            }
            else {
                writer.startNode(E_IS_DIR);
                writer.setValue("false");
                writer.endNode();
            }
            if (!isEmptyOrNull(dropboxModel.getMimeType())) {
                writer.startNode(E_MIME_TYPE);
                writer.setValue(dropboxModel.getMimeType());
                writer.endNode();
            }
            if(!isEmptyOrNull(fmt.print(dropboxModel.getModifiedDate()))) {
                writer.startNode(E_MODIFIED_DATE);
                writer.setValue(fmt.print(dropboxModel.getModifiedDate()));
                writer.endNode();
            }
            if (!isEmptyOrNull(dropboxModel.getPath())) {
                writer.startNode(E_PATH);
                writer.setValue(dropboxModel.getPath());
                writer.endNode();
            }
            if (!isEmptyOrNull(dropboxModel.getRev())) {
                writer.startNode(E_REV);
                writer.setValue(dropboxModel.getRev());
                writer.endNode();
            }
            if (!isEmptyOrNull(dropboxModel.getRoot())) {
                writer.startNode(E_ROOT);
                writer.setValue(dropboxModel.getRoot());
                writer.endNode();
            }
            if (!isEmptyOrNull(dropboxModel.getHrFileSize())) {
                writer.startNode(E_HR_SIZE);
                writer.setValue(dropboxModel.getHrFileSize());
                writer.endNode();
            }
            if (dropboxModel.isThumbExists()) {
                writer.startNode(E_THUMB_EXISTS);
                writer.setValue("true");
                writer.endNode();
            }
            else {
                writer.startNode(E_THUMB_EXISTS);
                writer.setValue("false");
                writer.endNode();
            }
        }
    }

    private void createChild(HierarchicalStreamWriter writer, DropboxModel dm) {
        writer.startNode(E_CONTENT);
        writer.startNode(E_ID);
        writer.setValue(dm.getId());
        writer.endNode();
        writer.startNode(E_CLIENT_M_TIME);
        writer.setValue(fmt.print(dm.getClientMtime()));
        writer.endNode();
        writer.startNode(E_FILESIZE);
        writer.setValue(Long.toString(dm.getFileSize()));
        writer.endNode();
        if (dm.isDir()){
            writer.startNode(E_HASH);
            writer.setValue(dm.getHash());
            writer.endNode();
        }
        writer.startNode(E_ICON);
        writer.setValue(dm.getIcon());
        writer.endNode();
        writer.startNode(E_IS_DELETED);
        if (dm.isDeleted()) {
            writer.setValue("true");
        }
        else {
            writer.setValue("false");
        }
        writer.endNode();
        writer.startNode(E_IS_DIR);
        if (dm.isDir()) {
            writer.setValue("true");
        }
        else {
            writer.setValue("false");
        }
        writer.endNode();
        writer.startNode(E_MIME_TYPE);
        writer.setValue(dm.getMimeType());
        writer.endNode();
        writer.startNode(E_MODIFIED_DATE);
        writer.setValue(fmt.print(dm.getModifiedDate()));
        writer.endNode();
        writer.startNode(E_PATH);
        writer.setValue(dm.getPath());
        writer.endNode();
        writer.startNode(E_REV);
        writer.setValue(dm.getRev());
        writer.endNode();
        writer.startNode(E_ROOT);
        writer.setValue(dm.getRoot());
        writer.endNode();
        writer.startNode(E_HR_SIZE);
        writer.setValue(dm.getHrFileSize());
        writer.endNode();
        writer.startNode(E_THUMB_EXISTS);
        if (dm.isThumbExists()) {
            writer.setValue("true");
        }
        else {
            writer.setValue("false");
        }
        writer.endNode();
        writer.endNode();
    }

    /**
     *This method creates a DropboxModel object from the xml serialization. Because the metadata endpoint of
     * the Dropbox REST service does not provide a contents field for child folders of folders, we do not
     * look for it in child elements.
     * @param reader
     * @param context
     * @return
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        DropboxModel dropBoxModel = new DropboxModel();

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);

            if (ename.equals(E_ID)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setId(value.trim());
                }
            }
            else if (ename.equals(E_CLIENT_M_TIME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setClientMtime(fmt.parseDateTime(value.trim()));
                }
            }
            else if (ename.equals(E_FILESIZE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setFileSize(Long.parseLong(value));
                }
            }

            else if (ename.equals(E_CONTENTS)) {
                List<DropboxModel> contents = new ArrayList<DropboxModel>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    //get the next content DropboxModel
                    if (getElementName(reader).equals(E_CONTENT)) {
                        DropboxModel dm = new DropboxModel();
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            final String enameChild = getElementName(reader);
                            if (enameChild.equals(E_ID)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setId(value.trim());
                                }
                            }
                            else if (enameChild.equals(E_CLIENT_M_TIME)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setClientMtime(fmt.parseDateTime(value.trim()));
                                }
                            }
                            else if (enameChild.equals(E_FILESIZE)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setFileSize(Long.parseLong(value));
                                }
                            }
                            else if (enameChild.equals(E_HASH)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setHash(value.trim());
                                }
                            }
                            else if (enameChild.equals(E_ICON)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                   dm.setIcon(value.trim());
                                }
                            }
                            else if (enameChild.equals(E_IS_DELETED)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                   dm.setDeleted(value.equals("true"));
                                }
                            }
                            else if (enameChild.equals(E_IS_DIR)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setDir(value.equals("true"));
                                }
                            }
                            else if (enameChild.equals(E_MIME_TYPE)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setMimeType(value.trim());
                                }
                            }
                            else if (enameChild.equals(E_PATH)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setPath(value.trim());
                                }
                            }
                            else if (enameChild.equals(E_REV)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                   dm.setRev(value.trim());
                                }
                            }
                            else if (enameChild.equals(E_ROOT)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setRoot(value.trim());
                                }
                            }
                            else if (enameChild.equals(E_HR_SIZE)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setHrFileSize(value.trim());
                                }
                            }
                            else if (enameChild.equals(E_THUMB_EXISTS)) {
                                final String value = reader.getValue();
                                if (!isEmptyOrNull(value)) {
                                    dm.setThumbExists(value.equals("true"));
                                }
                            }
                            reader.moveUp();
                        }
                        //have finished getting all fields for this DropboxModel
                        contents.add(dm);
                    }
                    reader.moveUp();
                }
                //have finished getting all the contents
                //put the List on the top level DropboxModel
                dropBoxModel.setContents(contents);
            }
            else if (ename.equals(E_HASH)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setHash(value.trim());
                }
            }
            else if (ename.equals(E_ICON)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setIcon(value.trim());
                }
            }
            else if (ename.equals(E_IS_DELETED)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setDeleted(value.equals("true"));
                }
            }
            else if (ename.equals(E_IS_DIR)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setDir(value.equals("true"));
                }
            }
            else if (ename.equals(E_MIME_TYPE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setMimeType(value.trim());
                }
            }
            else if (ename.equals(E_PATH)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                   dropBoxModel.setPath(value.trim());
                }
            }
            else if (ename.equals(E_REV)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                   dropBoxModel.setRev(value.trim());
                }
            }
            else if (ename.equals(E_ROOT)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                   dropBoxModel.setRoot(value.trim());
                }
            }
            else if (ename.equals(E_HR_SIZE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setHrFileSize(value.trim());
                }
            }
            else if (ename.equals(E_THUMB_EXISTS)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    dropBoxModel.setThumbExists(value.equals("true"));
                }
            }
            reader.moveUp();
        }

        return dropBoxModel;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return type == DropboxModel.class;
    }
    
}
