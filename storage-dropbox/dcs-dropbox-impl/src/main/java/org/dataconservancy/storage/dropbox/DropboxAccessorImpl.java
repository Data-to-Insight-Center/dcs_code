/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.storage.dropbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.dataconservancy.storage.dropbox.model.DropboxDelta;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.DropboxToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DeltaEntry;
import com.dropbox.client2.DropboxAPI.DeltaPage;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

public class DropboxAccessorImpl implements DropboxAccessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DropboxAccessorImpl.class);

    private static String DROPBOX_PROPERTIES = "org/dataconservancy/lite/dcs-dropbox.properties";

    private static final AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    private static DropboxAPI<WebAuthSession> mDBApi;

    private String appKey;

    private String appSecret;

    private WebAuthSession session;

    private WebAuthInfo authInfo;

    private AccessTokenPair atp;
    
    public DropboxAccessorImpl() {
        try {
            Properties properties = loadPropertiesFile(DROPBOX_PROPERTIES);
            appKey = properties.getProperty("appKey");
            appSecret = properties.getProperty("appSecret");
            AppKeyPair appKeys = new AppKeyPair(appKey, appSecret);
            session = new WebAuthSession(appKeys, ACCESS_TYPE);
            authInfo = session.getAuthInfo();
        }
        catch (Exception e) {
            LOGGER.error("Could not instantiate a new Dropbox Session.", e);
        }
    }

    /**
     * Assigns the user specific app key/secret to be used for this session.
     * 
     * @param tokens
     * @return boolean
     */
    @Override
    public boolean setAccessTokenPairForUser(DropboxToken token) {
        if (token != null) {
            atp = new AccessTokenPair(token.getAppKey(), token.getAppSecret());
            session.setAccessTokenPair(atp);
            getInstance();
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Helper method to restart the session.
     */
    private void restartSession() {
        AppKeyPair appKeys = new AppKeyPair(appKey, appSecret);
        session = new WebAuthSession(appKeys, ACCESS_TYPE);
        LOGGER.debug("Created a new session!");
    }

    /**
     * Instantiates a Dropbox API access.
     */
    @Override
    public DropboxAPI<WebAuthSession> getInstance() {
        if (mDBApi == null) {
            mDBApi = new DropboxAPI<WebAuthSession>(session);
            return mDBApi;
        }
        else {
            return mDBApi;
        }
    }

    /**
     * Returns an app specific URL so the user can link their Dropbox account to DCS-Lite.
     * 
     * @return String
     */
    @Override
    public String getDropboxOAuthUrl() {
        try {
            // Have to restart the session as it may have expired.
            restartSession();
            authInfo = session.getAuthInfo();
            return authInfo.url;
        } catch (Exception e) {
            LOGGER.error("Could not retrieve the oauth URL.", e);
            return null;
        }
    }

    /**
     * Assigns the initial user specific app key/secret and updates the database.
     * 
     * @param username
     * @throws MalformedURLException
     * @throws IOException
     * @throws URISyntaxException
     * @throws DropboxException
     */
    @Override
    public DropboxToken getUserAccessTokenPair() {
        try {
            session.retrieveWebAccessToken(authInfo.requestTokenPair);
            atp = session.getAccessTokenPair();
            DropboxToken token = new DropboxToken(atp.key, atp.secret);
            return token;
        }
        catch (DropboxException e) {
            LOGGER.error("Could not get the user access token pair from the session", e);
            return null;
        }
    }

    /**
     * Creates a file and then deletes it in order to test the dropbox link via the UI.
     */
    @Override
    public Boolean testDropboxLink(DropboxToken token) {
        try {
            if (setAccessTokenPairForUser(token)) {
                String fileContents = "Hello World! balangdan!";
                ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
                Entry newEntry = mDBApi.putFile("/testing.txt", inputStream, fileContents.length(), null, null);
                if (newEntry.size.length() > 0) {
                    mDBApi.delete("/testing.txt");
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                LOGGER.error("Session connection could not be established.");
                return false;
            }
        } catch (DropboxException e) {
            LOGGER.debug("Could not create the folder.", e);
            return false;
        }
    }

    @Override
    public void upload(DropboxToken token, InputStream inputStream)
            throws DropboxException {
        LOGGER.debug("Uploading file...");
        if (setAccessTokenPairForUser(token)) {
            Entry newEntry = mDBApi.putFile("/testing.txt", inputStream, 1000L, null, null);
            LOGGER.debug("Uploade is done.");
            LOGGER.debug("Revision of file: " + newEntry.rev);
        } else {
            LOGGER.error("Session connection could not be established.");
        }
    }

    /**
     * Polls dropbox and returns everything in it each time it's called.
     * 
     * @return models
     */
    @Override
    public List<DropboxModel> getAllContentFromDropbox(DropboxToken token) {
        try {
            if (setAccessTokenPairForUser(token)) {
                List<DropboxModel> models = new ArrayList<DropboxModel>();
                DeltaPage<Entry> entries = mDBApi.delta(null);
                for (DeltaEntry<Entry> entry : entries.entries) {
                    DropboxModel model = new DropboxModel(new DropboxDelta(entry).getMetadata());
                    models.add(model);
                }
                return models;
            }
            else {
                LOGGER.error("Session connection could not be established.");
                return null;
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not get the delta.", e);
            return null;
        }
    }

    /**
     * Loads the properties file.
     *
     * @param filename
     * @return Properties
     * @throws IOException
     */
    public Properties loadPropertiesFile(String filename) throws IOException {
        Properties props = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
        if (in != null) {
            props.load(in);
            in.close();
        } else {
            throw new IOException("Unable to resolve classpath resource '" + filename + "'");
        }
        return props;
    }
    
}
