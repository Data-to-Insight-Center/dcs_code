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

package org.dataconservancy.storage.dropbox.model.builder.xstream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.dropbox.client2.DropboxAPI;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 *
 */
public class DropboxModelConverterTest implements ConverterConstants {
    private XStream x = new XStream(new StaxDriver());
    private String FILE_XML;
    private String DIR_XML;
    private DropboxModel fileDropboxModel;
    private DropboxModel dirDropboxModel;
    private DateTimeFormatter fmt;

    @Before
    public void setup() {

        fmt = DateTimeFormat.forPattern(DropboxModel.JODA_DATE_TIME_FORMATTER_STRING);

        DropboxAPI.Entry fileEntry = new DropboxAPI.Entry();
        fileEntry.size = "100 bytes";
        fileEntry.rev = "35c1f029684fe";
        fileEntry.thumbExists = false;
        fileEntry.bytes = 100;
        fileEntry.modified = "Mon, 18 Jul 2011 20:13:43 +0000";
        fileEntry.clientMtime = "Wed, 20 Apr 2011 16:20:19 +0000";
        fileEntry.path = "/Public/latest.txt";
        fileEntry.isDir = false;
        fileEntry.icon = "page_white_text";
        fileEntry.root = "dropbox";
        fileEntry.mimeType = "text/plain";
        fileEntry.isDeleted = false;

        fileDropboxModel = new DropboxModel(fileEntry);

        List<DropboxAPI.Entry> contentsList= new ArrayList<DropboxAPI.Entry>();
        contentsList.add(fileEntry);

        DropboxAPI.Entry dirEntry = new DropboxAPI.Entry();
        dirEntry.clientMtime = "Wed, 27 Apr 2011 22:18:51 +0000";
        dirEntry.bytes = 0;
        dirEntry.contents = contentsList;
        dirEntry.hash = "37eb1ba1849d4b0fb0b28caf7ef3af52";
        dirEntry.icon = "folder_public";
        dirEntry.isDeleted = false;
        dirEntry.isDir = true;
        dirEntry.modified = "Wed, 27 Apr 2011 22:18:51 +0000";
        dirEntry.path = "/Public";
        dirEntry.rev = "714f029684fe";
        dirEntry.root = "dropbox";
        dirEntry.size = "100 bytes";
        dirEntry.thumbExists = false;

        dirDropboxModel = new DropboxModel(dirEntry);

        FILE_XML =
                "            <" + E_DROPBOX_MODEL + ">\n" +
                "                <" + E_ID + ">" + fileDropboxModel.getId() + "</" + E_ID + ">" +
                "                <" + E_CLIENT_M_TIME + ">" + fmt.print(fileDropboxModel.getClientMtime()) + "</" + E_CLIENT_M_TIME + ">\n" +
                "                <" + E_FILESIZE + ">" + Long.toString(fileDropboxModel.getFileSize()) + "</" + E_FILESIZE + ">\n" +
                "                <" + E_MODIFIED_DATE + ">" + fmt.print(fileDropboxModel.getModifiedDate()) + "</" + E_MODIFIED_DATE + ">\n" +
                "                <" + E_ICON + ">" + fileDropboxModel.getIcon() + "</" + E_ICON + ">\n" +
                "                <" + E_IS_DELETED + ">" + fileDropboxModel.isDeleted() + "</" + E_IS_DELETED + ">\n" +
                "                <" + E_IS_DIR + ">" + fileDropboxModel.isDir() + "</" + E_IS_DIR + ">\n" +
                "                <" + E_MIME_TYPE + ">" + fileDropboxModel.getMimeType() + "</" + E_MIME_TYPE + ">\n" +
                "                <" + E_PATH + ">" + fileDropboxModel.getPath() + "</" + E_PATH + ">\n" +
                "                <" + E_REV + ">" + fileDropboxModel.getRev() + "</" + E_REV + ">\n" +
                "                <" + E_ROOT + ">" + fileDropboxModel.getRoot() + "</" + E_ROOT + ">\n" +
                "                <" + E_HR_SIZE + ">" + fileDropboxModel.getHrFileSize() + "</" + E_HR_SIZE + ">\n" +
                "                <" + E_THUMB_EXISTS + ">" + fileDropboxModel.isThumbExists() + "</" + E_THUMB_EXISTS + ">\n" +
                "            </" + E_DROPBOX_MODEL + ">\n";

        DIR_XML =
                "            <" + E_DROPBOX_MODEL + ">\n" +
                "                <" + E_ID + ">" + dirDropboxModel.getId() + "</" + E_ID + ">" +
                "                <" + E_CLIENT_M_TIME + ">" + fmt.print(dirDropboxModel.getClientMtime()) + "</" + E_CLIENT_M_TIME + ">\n" +
                "                <" + E_FILESIZE + ">" + Long.toString(dirDropboxModel.getFileSize()) + "</" + E_FILESIZE + ">\n" +
                "                <" + E_CONTENTS + ">\n" +
                "                    <" + E_CONTENT + ">\n" +
                "                        <" + E_ID + ">" + fileDropboxModel.getId() + "</" + E_ID + ">" +
                "                        <" + E_CLIENT_M_TIME + ">" + fmt.print(fileDropboxModel.getClientMtime()) + "</" + E_CLIENT_M_TIME + ">\n" +
                "                        <" + E_FILESIZE + ">" + Long.toString(fileDropboxModel.getFileSize()) + "</" + E_FILESIZE + ">\n" +
                "                        <" + E_ICON + ">" + fileDropboxModel.getIcon() + "</" + E_ICON + ">\n" +
                "                        <" + E_IS_DELETED + ">" + fileDropboxModel.isDeleted() + "</" + E_IS_DELETED + ">\n" +
                "                        <" + E_IS_DIR + ">" + fileDropboxModel.isDir() + "</" + E_IS_DIR + ">\n" +
                "                        <" + E_MIME_TYPE + ">" + fileDropboxModel.getMimeType() + "</" + E_MIME_TYPE + ">\n" +
                "                        <" + E_MODIFIED_DATE + ">" + fmt.print(fileDropboxModel.getModifiedDate()) + "</" + E_MODIFIED_DATE + ">\n" +
                "                        <" + E_PATH + ">" + fileDropboxModel.getPath() + "</" + E_PATH + ">\n" +
                "                        <" + E_REV + ">" + fileDropboxModel.getRev() + "</" + E_REV + ">\n" +
                "                        <" + E_ROOT + ">" + fileDropboxModel.getRoot() + "</" + E_ROOT + ">\n" +
                "                        <" + E_HR_SIZE + ">" + fileDropboxModel.getHrFileSize() + "</" + E_HR_SIZE + ">\n" +
                "                        <" + E_THUMB_EXISTS + ">" + fileDropboxModel.isThumbExists() + "</" + E_THUMB_EXISTS + ">\n" +
                "                    </" + E_CONTENT + ">\n" +
                "                </" + E_CONTENTS + ">\n" +
                "                <" + E_HASH + ">" + dirDropboxModel.getHash() + "</" + E_HASH + ">\n" +
                "                <" + E_ICON + ">" + dirDropboxModel.getIcon() + "</" + E_ICON + ">\n" +
                "                <" + E_IS_DELETED + ">" + dirDropboxModel.isDeleted() + "</" + E_IS_DELETED + ">\n" +
                "                <" + E_IS_DIR + ">" + dirDropboxModel.isDir() + "</" + E_IS_DIR + ">\n" +
                "                <" + E_MODIFIED_DATE + ">" + fmt.print(dirDropboxModel.getModifiedDate()) + "</" + E_MODIFIED_DATE + ">\n" +
                "                <" + E_PATH + ">" + dirDropboxModel.getPath() + "</" + E_PATH + ">\n" +
                "                <" + E_REV + ">" + dirDropboxModel.getRev() + "</" + E_REV + ">\n" +
                "                <" + E_ROOT + ">" + dirDropboxModel.getRoot() + "</" + E_ROOT + ">\n" +
                "                <" + E_HR_SIZE + ">" + dirDropboxModel.getHrFileSize() + "</" + E_HR_SIZE + ">\n" +
                "                <" + E_THUMB_EXISTS + ">" +  dirDropboxModel.isThumbExists() + "</" + E_THUMB_EXISTS + ">\n" +
                "            </" + E_DROPBOX_MODEL + ">\n";


    }

    @Test
    public void testMarshalFile() throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        DropboxModelConverter dropboxModelConverter = new DropboxModelConverter();
        x.registerConverter(dropboxModelConverter);
        x.alias(E_DROPBOX_MODEL, org.dataconservancy.storage.dropbox.model.DropboxModel.class);
        XMLAssert.assertXMLEqual(FILE_XML, x.toXML(fileDropboxModel));
    }

    @Test
    public void testMarshalDirectory() throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        DropboxModelConverter dropboxModelConverter = new DropboxModelConverter();
        x.registerConverter(dropboxModelConverter);
        x.alias(E_DROPBOX_MODEL, org.dataconservancy.storage.dropbox.model.DropboxModel.class);
        XMLAssert.assertXMLEqual(DIR_XML, x.toXML(dirDropboxModel));
    }

    @Test
    public void testUnmarshalFile(){
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        DropboxModelConverter dropboxModelConverter = new DropboxModelConverter();
        x.registerConverter(dropboxModelConverter);
        x.alias(E_DROPBOX_MODEL, org.dataconservancy.storage.dropbox.model.DropboxModel.class);
        DropboxModel actual = (DropboxModel)x.fromXML(FILE_XML);
        Assert.assertEquals(fileDropboxModel, actual);
        Assert.assertEquals(fileDropboxModel, x.fromXML(x.toXML(fileDropboxModel)));
    }

    @Test
    public void testUnmarshalDirectory(){
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        DropboxModelConverter dropboxModelConverter = new DropboxModelConverter();
        x.registerConverter(dropboxModelConverter);
        x.alias(E_DROPBOX_MODEL, org.dataconservancy.storage.dropbox.model.DropboxModel.class);
        DropboxModel actual = (DropboxModel)x.fromXML(DIR_XML);
        Assert.assertEquals(dirDropboxModel, actual);
        Assert.assertEquals(dirDropboxModel, x.fromXML(x.toXML(dirDropboxModel)));
    }

}
