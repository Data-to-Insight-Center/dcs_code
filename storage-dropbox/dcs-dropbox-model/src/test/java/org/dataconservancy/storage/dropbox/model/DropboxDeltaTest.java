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

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;

/**
 * This class tests our model for the Dropbox DeltaEntry.
 */
public class DropboxDeltaTest extends DropboxModelBaseTest{

    // TODO: Update these tests to use the actual delta from DropboxAccessor.
    /**
     * test that the Entry representing the metadata for our file is processed
     * correctly by our DropboxDelta constructor
     */
    @Ignore
    @Test
    public void testDeltaMetadataForFile(){
        DropboxAPI.DeltaEntry<Entry> APIDeltaEntry = new DropboxAPI.DeltaEntry<Entry>("fileonepath", fileEntry);
        DropboxDelta deltaTest = new DropboxDelta(APIDeltaEntry);
        Assert.assertEquals(deltaTest.getLcPath(), "fileonepath");
        Assert.assertEquals(deltaTest.getMetadata(), fileOne);
    }

    /**
     * tests our equals() method
     */
    @Ignore
    @Test
    public void testDifferentDeltasAreNotEqual(){
        DropboxAPI.DeltaEntry<Entry> deltaOne = new DropboxAPI.DeltaEntry<Entry>("fileonepath", fileEntry);
        DropboxAPI.DeltaEntry<Entry> deltaTwo = new DropboxAPI.DeltaEntry<Entry>("otherpathname", fileEntry);
        DropboxDelta dropboxDeltaOne =  new DropboxDelta(deltaOne);
        Assert.assertFalse(deltaOne.equals(deltaTwo));
        DropboxAPI.DeltaEntry<Entry> deltaThree = new DropboxAPI.DeltaEntry<Entry>("fileonepath", null);
        DropboxDelta dropboxDeltaThree =  new DropboxDelta(deltaThree);
        Assert.assertFalse(dropboxDeltaOne.equals(dropboxDeltaThree));
        Assert.assertFalse(dropboxDeltaThree.equals(dropboxDeltaOne));
    }
}
