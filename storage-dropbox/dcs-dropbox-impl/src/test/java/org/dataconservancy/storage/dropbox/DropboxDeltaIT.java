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
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.storage.dropbox.model.DropboxDelta;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.builder.xstream.ConverterConstants;
import org.dataconservancy.storage.dropbox.model.builder.xstream.DropboxModelConverter;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * a simple integration test to show that our DropboxDelta constructor, and converter, work as expected with
 * the dropbox service
 */
public class DropboxDeltaIT  extends DropboxBaseTest{
   private XStream x = new XStream(new StaxDriver());

    /**
     * this method tests that when we deposit a file, the delta entry it creates is what we expect
     * and that when it is deleted, the metadata on the delta entry is null, as it should be
     * @throws DropboxException
     */
   @Test
   public void testCreateDropboxDeltaFromRetrievedDelta() throws DropboxException {
       XMLUnit.setIgnoreWhitespace(true);
       XMLUnit.setIgnoreComments(true);
       DropboxModelConverter dropboxModelConverter = new DropboxModelConverter();
       x.registerConverter(dropboxModelConverter);
       x.alias(ConverterConstants.E_DROPBOX_MODEL, org.dataconservancy.storage.dropbox.model.DropboxModel.class);

       //create a folder with a file in it
       String folderPath = root + UUID.randomUUID().toString();
       mDBapi.createFolder(folderPath);

       String cursor = null;
       DropboxAPI.DeltaPage<DropboxAPI.Entry> entries = mDBapi.delta(cursor);
       cursor = (entries.cursor);
       final String filePath = folderPath + "/" + UUID.randomUUID().toString();
       final String content = "File Content";

       mDBapi.putFile(filePath, IOUtils.toInputStream(content), content.length(), null, null);

       //get the delta now, should just have our file in it
       entries = mDBapi.delta(cursor);
       Assert.assertEquals(1, entries.entries.size());

       //get the DeltaEntry, and create our DropboxDelta object
       DropboxAPI.DeltaEntry deltaEntry = entries.entries.get(0);
       DropboxDelta ourDelta = new DropboxDelta(deltaEntry);

       //check to see that our lcpath is what it is supposed to be
       Assert.assertNotNull(ourDelta);
       Assert.assertEquals(ourDelta.getLcPath(), filePath.toLowerCase());

       DropboxModel ourMetadata = ourDelta.getMetadata();
       //examine the metadata on the delta for our file
       Assert.assertEquals(ourMetadata.getFileSize(), content.length());
       Assert.assertEquals(ourMetadata.getPath(), filePath);
       Assert.assertNull(ourMetadata.getHash());
       Assert.assertNull(ourMetadata.getContents());
       Assert.assertNotNull(ourMetadata.getHrFileSize());
       Assert.assertNull(ourMetadata.getIcon());
       Assert.assertNull(ourMetadata.getMimeType());
       Assert.assertNotNull(ourMetadata.getClientMtime());
       Assert.assertFalse(new DateTime().isBefore(ourMetadata.getClientMtime()));
       Assert.assertNotNull(ourMetadata.getModifiedDate());
       Assert.assertFalse(new DateTime().isBefore(ourMetadata.getModifiedDate()));
       Assert.assertNotNull(ourMetadata.getRev());
       Assert.assertNotNull(ourMetadata.getRoot());
       Assert.assertFalse(ourMetadata.isDeleted());
       Assert.assertFalse(ourMetadata.isDir());
       Assert.assertFalse(ourMetadata.isThumbExists());

       //show that converter roundtripping works model->XML->model
       Assert.assertEquals(ourMetadata, x.fromXML(x.toXML(ourMetadata)));

       //now delete the file and get a new delta
       mDBapi.delete(filePath);

       entries = mDBapi.delta(cursor);
       Assert.assertEquals(1, entries.entries.size());

       //get the DeltaEntry, and create our DropboxDelta object
       deltaEntry = entries.entries.get(0);
       ourDelta = new DropboxDelta(deltaEntry);

       //check to see that our lcpath is what it is supposed to be
       Assert.assertNotNull(ourDelta);
       Assert.assertEquals(ourDelta.getLcPath(), filePath.toLowerCase());

       //deleted file should have null metadata
       Assert.assertNull(ourDelta.getMetadata());

       //show that converter roundtripping works model->XML->model
       //even tho model is null
       Assert.assertEquals(ourMetadata, x.fromXML(x.toXML(ourMetadata)));

   }
}
