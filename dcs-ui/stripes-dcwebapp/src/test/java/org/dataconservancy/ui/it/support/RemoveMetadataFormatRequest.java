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
package org.dataconservancy.ui.it.support;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A request which remove a MetadataFormat from the user interface.
 */
public class RemoveMetadataFormatRequest {

    private final UiUrlConfig urlConfig;

    private static final String STRIPES_EVENT = "removeFormat";

    public RemoveMetadataFormatRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
    }

    public HttpPost asHttpPost(String idToRemove) {
        if (idToRemove == null) {
            throw new IllegalArgumentException("The Id of Metadata Format to be removed has to be provided first. " +
                    "idToRemove cannot be null");
        }

        final HttpPost form = new HttpPost(urlConfig.getUiConfigUrl().toExternalForm());

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("metadataFormatId", idToRemove));
        params.add(new BasicNameValuePair(STRIPES_EVENT, "removeFormat"));

        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        form.setEntity(entity);
        return form;
    }
}
