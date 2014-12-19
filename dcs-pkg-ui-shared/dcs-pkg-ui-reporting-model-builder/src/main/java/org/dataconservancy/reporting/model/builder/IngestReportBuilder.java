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
package org.dataconservancy.reporting.model.builder;

import java.io.InputStream;
import java.io.OutputStream;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.reporting.model.IngestReport;

/**
 * 
 * IngestReportBuilder is an abstraction that provides methods to serialize and deserialize an {@link IngestReport}
 * object
 * 
 */
public interface IngestReportBuilder {
    
    /**
     * Builds a {@link IngestReport } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * IngestReport serialization specification. The <code>InputStream</code> will be deserialized into the
     * corresponding IngestReport java object and returned.
     * 
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @throws {@link InvalidXmlException}
     * @return the {@link IngestReport } object
     */
    public IngestReport buildIngestReport(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link IngestReport} to XML, formatted according to the IngestReport serialization
     * specification.
     * 
     * @param ingestReport the {@link IngestReport} to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     */
    public void buildIngestReport(IngestReport ingestReport, OutputStream sink);
}
