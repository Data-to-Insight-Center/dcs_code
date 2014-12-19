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
package org.dataconservancy.reporting.model.builder.xstream;

import java.io.InputStream;
import java.io.OutputStream;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.reporting.model.builder.IngestReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

/**
 * XStream impl of IngestReportBuilder
 */
public class XstreamIngestReportBuilder implements IngestReportBuilder {
    
    private static final Logger log = LoggerFactory.getLogger(XstreamIngestReportBuilder.class);

    private final static String DESER_ERR = "Error encountered deserializing a stream: %s";

    private XStream x;
    
    public XstreamIngestReportBuilder() {
        x = XstreamIngestReportFactory.newInstance();
    }

    @Override
    public IngestReport buildIngestReport(InputStream is) throws InvalidXmlException {
        if (is != null) {
            final IngestReport ingestReport;
            try {
                ingestReport = (IngestReport) x.fromXML(is);
            }
            catch (StreamException e) {
                log.debug(String.format(DESER_ERR, e.getMessage()), e);
                throw new InvalidXmlException(e);
            }
            return ingestReport;
        }
        else {
            return null;
        }
    }
    
    @Override
    public void buildIngestReport(IngestReport ingestReport, OutputStream sink) {
        if (ingestReport != null && sink != null) {
            x.toXML(ingestReport, sink);
        }
    }
    
}
