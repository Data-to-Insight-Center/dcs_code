/*
 * Copyright 2012 Johns Hopkins University
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
package org.dataconservancy.packaging.ingest.api;

/**
 * String constants for HTTP header names and mime types.
 */
public interface Http {

    public interface Header {
        public static final String CONTENT_DISPOSITION = "Content-Disposition";
        public static final String LAST_MODIFIED = "Last-Modified";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String X_DCS_AUTHENTICATED_USER = "X-Dcs-Authenticated-User";
    }

    public interface MimeType {
        public static final String TEXT_PLAIN = "text/plain";
        public static final String TEXT_HTML = "text/html";
        public static final String APPLICATION_GZIP = "application/gzip";
        public static final String APPLICATION_XGZIP = "application/x-gzip";
        public static final String APPLICATION_ZIP = "application/zip";
        public static final String APPLICATION_XTAR = "application/x-tar";
        public static final String APPLICATION_XML = "application/xml";
    }

}
