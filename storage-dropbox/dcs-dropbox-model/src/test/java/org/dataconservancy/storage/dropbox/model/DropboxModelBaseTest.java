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

package org.dataconservancy.storage.dropbox.model;

import com.dropbox.client2.DropboxAPI;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * A support class for testing DropboxModel test classes.
 */
public class DropboxModelBaseTest {

    protected DropboxModel dirOne = new DropboxModel();
    protected DropboxModel dirOnePrime = new DropboxModel();
    protected DropboxModel fileOne = new DropboxModel();
    protected List<DropboxModel> contents = new ArrayList<DropboxModel>();

    protected DropboxAPI.Entry fileEntry = new DropboxAPI.Entry();
    protected DropboxAPI.Entry dirEntry = new DropboxAPI.Entry();
    protected List<DropboxAPI.Entry> entryContents  = new ArrayList<DropboxAPI.Entry>();

    //milliseconds must be set to zero, since the formatter does not recognize milliseconds
    //(granularity of the date provided by dropbox is seconds)
    //if milliseconds are not zero, translation of date-time to string and back will not
    //produce a good roundtrip. this will cause testModelEvenModelConstructor() to fail
    protected DateTime now = new DateTime(2013, 5, 4, 3, 2, 1, 0);
    protected DateTime then = now.minusHours(1);

    protected DateTimeFormatter fmt =  DateTimeFormat.forPattern(DropboxModel.JODA_DATE_TIME_FORMATTER_STRING);
    protected String nowString = fmt.print(now);
    protected String thenString = fmt.print(then);

    @Before
    public void setUp() throws Exception {

        fileOne.setId("fileonepath_115");
        fileOne.setClientMtime(now);
        fileOne.setDeleted(false);
        fileOne.setDir(false);
        fileOne.setFileSize(115);
        fileOne.setHrFileSize("115 bytes");
        fileOne.setIcon("/path/to/icon.gif");
        fileOne.setMimeType("image");
        fileOne.setModifiedDate(now);
        fileOne.setPath("fileOnePath");
        fileOne.setRev("2");
        fileOne.setRoot("dropbox");
        fileOne.setThumbExists(false);

        contents.add(fileOne);

        dirOne.setId("dironepath_0");
        dirOne.setClientMtime(then);
        dirOne.setContents(contents);
        dirOne.setDeleted(false);
        dirOne.setDir(true);
        dirOne.setFileSize(0);
        dirOne.setHash("thisIsDirOnesHash");
        dirOne.setHrFileSize("0 bytes");
        dirOne.setIcon("/path/to/icon.gif");
        dirOne.setModifiedDate(then);
        dirOne.setPath("dirOnePath");
        dirOne.setRev("1");
        dirOne.setRoot("dropbox");
        dirOne.setThumbExists(false);

        dirOnePrime.setId("dironepath_0");
        dirOnePrime.setClientMtime(then);
        dirOnePrime.setContents(contents);
        dirOnePrime.setDeleted(false);
        dirOnePrime.setDir(true);
        dirOnePrime.setFileSize(0);
        dirOnePrime.setHash("thisIsDirOnesHash");
        dirOnePrime.setHrFileSize("0 bytes");
        dirOnePrime.setIcon("/path/to/icon.gif");
        dirOnePrime.setModifiedDate(then);
        dirOnePrime.setPath("dirOnePath");
        dirOnePrime.setRev("1");
        dirOnePrime.setRoot("dropbox");
        dirOnePrime.setThumbExists(false);

        //construct an entry corresponding dirOne with a list
        //containing an entry corresponding to fileOne as contents
        fileEntry.size = "115 bytes";
        fileEntry.rev = "2";
        fileEntry.thumbExists = false;
        fileEntry.bytes = 115;
        fileEntry.modified = nowString;
        fileEntry.clientMtime = nowString;
        fileEntry.path = "fileOnePath";
        fileEntry.isDir = false;
        fileEntry.icon = "/path/to/icon.gif";
        fileEntry.root = "dropbox";
        fileEntry.mimeType = "image";
        fileEntry.isDeleted = false;

        entryContents.add(fileEntry);

        dirEntry.clientMtime = thenString;
        dirEntry.bytes = 0;
        dirEntry.contents = entryContents;
        dirEntry.hash = "thisIsDirOnesHash";
        dirEntry.icon = "/path/to/icon.gif";
        dirEntry.isDeleted = false;
        dirEntry.isDir = true;
        dirEntry.modified = thenString;
        dirEntry.path = "dirOnePath";
        dirEntry.rev = "1";
        dirEntry.root = "dropbox";
        dirEntry.size = "0 bytes";
        dirEntry.thumbExists = false;
    }

}
