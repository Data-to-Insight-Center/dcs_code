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

import org.junit.Assert;
import org.junit.Test;

/**
 *  This class tests our model for the Dropbox Entry.
 */
public class DropboxModelTest extends DropboxModelBaseTest{

    /**
     * test that that equal objects are evaluated as equal by
     * DropboxModel.equals()
     */
    @Test
    public void testSameModelsLookEqual() {
        Assert.assertTrue(dirOne.equals(dirOnePrime));
    }

    /**
     * test that DropboxModel.equals() recognizes differences betwneen
     * different objects
     */
    @Test
    public void testDifferentModelsLookUnequal(){
        DropboxModel testDir = new DropboxModel(dirOne);

        //change ClientMtime
        Assert.assertTrue(dirOne.equals(testDir));
        testDir.setClientMtime(now);
        Assert.assertFalse(dirOne.equals(testDir));

        //change contents
        testDir = new DropboxModel(dirOne);
        Assert.assertTrue(dirOne.equals(testDir));
        testDir.setContents(null);
        Assert.assertFalse(dirOne.equals(testDir));

        //check unequal boolean formulations
        testDir = new DropboxModel(dirOne);
        Assert.assertTrue(dirOne.equals(testDir));
        testDir.setDeleted(true);
        Assert.assertFalse(dirOne.equals(testDir));

        testDir = new DropboxModel(dirOne);
        Assert.assertTrue(dirOne.equals(testDir));
        testDir.setDir(false);
        Assert.assertFalse(dirOne.equals(testDir));
    }

    /**
     * test that using the DropboxModel(Event) constructor gives the same result as
     * constructing the object directly. we use a directory with a file in it for the Entry
     */
    @Test
    public void testModelEventModelConstructor(){
        DropboxModel testEventModelConstructed = new DropboxModel(dirEntry);
        Assert.assertTrue(dirOne.equals(testEventModelConstructed));
    }

}
