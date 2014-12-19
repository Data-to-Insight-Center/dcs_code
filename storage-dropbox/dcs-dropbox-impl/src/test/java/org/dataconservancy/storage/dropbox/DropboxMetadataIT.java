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

package org.dataconservancy.storage.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.builder.xstream.ConverterConstants;
import org.dataconservancy.storage.dropbox.model.builder.xstream.DropboxModelConverter;
import org.junit.Test;

import java.util.UUID;

/**
 * a simple integration test to show that our DropboxModel constructor, and converter, work as expected with
 * the dropbox service
 */
public class DropboxMetadataIT extends DropboxBaseTest {
    private XStream x = new XStream(new StaxDriver());


    @Test
    public void testCreateDropboxModelFromRetrievedMetadata() throws DropboxException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        DropboxModelConverter dropboxModelConverter = new DropboxModelConverter();
        x.registerConverter(dropboxModelConverter);
        x.alias(ConverterConstants.E_DROPBOX_MODEL, org.dataconservancy.storage.dropbox.model.DropboxModel.class);

        //add a folder with a file in it
        String folderPath = root + UUID.randomUUID().toString();
        mDBapi.createFolder(folderPath);
        final String filePath = folderPath + "/" + UUID.randomUUID().toString();
        final String content = "File Content";

        mDBapi.putFile(filePath, IOUtils.toInputStream(content), content.length(), null, null);

        DropboxAPI.Entry folderMetadata = mDBapi.metadata(folderPath, MAX_REVISIONS, null, true, null);

        //construct a DropboxModel from the Entry returned by metadata
        DropboxModel dm = new DropboxModel(folderMetadata);

        //inspect the folder (note that non-null DateTimes indicate the Joda format
        // setup is correct)
        Assert.assertEquals("0 bytes", dm.getHrFileSize());
        Assert.assertEquals(0, dm.getFileSize());
        Assert.assertEquals(folderPath, dm.getPath());
        Assert.assertTrue(dm.isDir());
        Assert.assertFalse(dm.isDeleted());
        Assert.assertFalse(dm.isThumbExists());
        Assert.assertNotNull(dm.getRev());
        Assert.assertNotNull(dm.getHash());
        Assert.assertNotNull(dm.getRoot());
        Assert.assertNull(dm.getIcon());
        Assert.assertNotNull(dm.getClientMtime());
        Assert.assertNotNull(dm.getModifiedDate());
        Assert.assertNotNull(dm.getContents());
        Assert.assertEquals(1,dm.getContents().size());

        //inspect the contained file
        DropboxModel file = dm.getContents().get(0);
        Assert.assertEquals(content.length(), file.getFileSize());
        Assert.assertEquals(filePath, file.getPath());
        Assert.assertFalse(file.isDir());
        Assert.assertFalse(file.isDeleted());
        Assert.assertFalse(file.isThumbExists());
        Assert.assertNotNull(file.getRev());
        Assert.assertNotNull(file.getHash());
        Assert.assertNotNull(file.getRoot());
        Assert.assertNull(file.getIcon());
        Assert.assertNotNull(file.getClientMtime());
        Assert.assertNotNull(file.getModifiedDate());
        Assert.assertNull(file.getContents());

        //show that converter roundtripping works model->XML->model
        Assert.assertEquals(dm, x.fromXML(x.toXML(dm)));

        //now add a subfolder with content file
        String subFolderPath = folderPath + "/" + "subFolder";
        mDBapi.createFolder(subFolderPath);
        final String subFolderFilePath = subFolderPath + "/" + UUID.randomUUID().toString();
        final String subFolderFileContent = "Subfolder File Content";

        mDBapi.putFile(subFolderFilePath, IOUtils.toInputStream(subFolderFileContent), subFolderFileContent.length(), null, null);

        folderMetadata = mDBapi.metadata(folderPath, MAX_REVISIONS, null, true, null);

        //get the top level folder metadata again
        dm = new DropboxModel(folderMetadata);
        //we have two things in the folder now - the original
        //file, and the subfolder
        Assert.assertEquals(2,dm.getContents().size());

        //inspect the subfolder
        DropboxModel subFolder = dm.getContents().get(1);
        Assert.assertEquals("0 bytes", subFolder.getHrFileSize());
        Assert.assertEquals(0, subFolder.getFileSize());
        Assert.assertEquals(subFolderPath, subFolder.getPath());
        Assert.assertTrue(subFolder.isDir());
        Assert.assertFalse(subFolder.isDeleted());
        Assert.assertFalse(subFolder.isThumbExists());
        Assert.assertNotNull(subFolder.getRev());
        Assert.assertNotNull(subFolder.getHash());
        Assert.assertNotNull(subFolder.getRoot());
        Assert.assertNull(subFolder.getIcon());
        Assert.assertNotNull(subFolder.getClientMtime());
        Assert.assertNotNull(subFolder.getModifiedDate());

        //IMPORTANT: contents are listed only for the top level directory
        //when list is set to true in the metadata call - no list for subfolders
        Assert.assertEquals(2, folderMetadata.contents.size());

        //entry 1 is our subfolder, which has a file in it
        Assert.assertTrue(folderMetadata.contents.get(1).isDir);

        //but the metadata call does not give us contents listing for subfolders
        Assert.assertNull(folderMetadata.contents.get(1).contents);

        //this is reflected in our DropboxModel construction
        Assert.assertNull(subFolder.getContents());

        //roundtripping model->XML->model works for this file structure too
        Assert.assertEquals(dm, x.fromXML(x.toXML(dm)));
    }
}
