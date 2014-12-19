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

package org.dataconservancy.storage.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.DropboxToken;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.WebAuthSession;

public interface DropboxAccessor {
    boolean setAccessTokenPairForUser(DropboxToken token);
    
    DropboxAPI<WebAuthSession> getInstance();
    
    String getDropboxOAuthUrl();
    
    Boolean testDropboxLink(DropboxToken token);
    
    void upload(DropboxToken token, InputStream inputStream) throws DropboxException;
    
    List<DropboxModel> getAllContentFromDropbox(DropboxToken token);
    
    DropboxToken getUserAccessTokenPair() throws MalformedURLException, IOException, URISyntaxException,
            DropboxException;
}
