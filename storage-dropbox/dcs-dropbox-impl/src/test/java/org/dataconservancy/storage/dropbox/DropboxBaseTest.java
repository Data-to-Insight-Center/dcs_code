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

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.dataconservancy.storage.dropbox.model.DropboxToken;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.WebAuthSession;

/**
 * a support class for classes which need to connect to the dropbox service
 */
public class DropboxBaseTest {
    // Account key and secret for a linked account to do testing.
    protected static final String ACCOUNT_KEY = "7ki2m96knqc4u78";
    protected static final String ACCOUNT_SECRET = "hb4djilqwjar8r0";

    protected static DropboxAccessor underTest;
    protected static DropboxAPI<WebAuthSession> mDBapi;

    protected static String root;
    protected final int MAX_REVISIONS = 1000;
    protected static DropboxToken token;

    @BeforeClass
    public static void setUp() throws Exception {
        underTest = new DropboxAccessorImpl();
        token = new DropboxToken();
        token.setAppKey(ACCOUNT_KEY);
        token.setAppSecret(ACCOUNT_SECRET);
        underTest.setAccessTokenPairForUser(token);
        //we need a new root folder each time we run tests, as we are unable to purge files from
        //dropbox - deleting leaves previous revisions
        root = "/" + UUID.randomUUID().toString() + "/";
        mDBapi = underTest.getInstance();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        // Delete the created file or directory.
        mDBapi.delete(root);
    }

    /**
     * a protected method to create a new dropbox file
     *
     * @param fileName
     * @return filePath the path of the created file
     * @throws com.dropbox.client2.exception.DropboxException
     */
    protected String createNewDropboxFile(String fileName) throws DropboxException {
        if (fileName == null) {
            fileName = UUID.randomUUID().toString();
        }
        String fileContents = fileName;
        String filePath = root + fileName;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        mDBapi.putFile(filePath, inputStream, fileContents.length(), null, null);
        return (filePath);
    }

}
