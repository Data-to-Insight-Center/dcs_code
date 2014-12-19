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
package org.dataconservancy.packaging.ingest.impl;

import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.packaging.ingest.api.Http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PreIngestProcessingReportDocument implements DepositDocument {

    private String mimeType;
    private long lastModified;
    private InputStream inputStream;
    private Map<String, String> metadata;

    public PreIngestProcessingReportDocument() {
        this.mimeType = Http.MimeType.TEXT_PLAIN;
        this.metadata = new HashMap<String, String>();
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public String toString() {
        return "PreIngestProcessingReportDocument{" +
                "mimeType='" + mimeType + '\'' +
                ", lastModified=" + lastModified +
                ", inputStream=" + inputStream +
                ", metadata=" + metadata +
                '}';
    }

}
