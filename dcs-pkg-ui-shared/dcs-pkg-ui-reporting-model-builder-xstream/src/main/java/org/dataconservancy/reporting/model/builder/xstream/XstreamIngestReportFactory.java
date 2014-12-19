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

import javax.xml.namespace.QName;

import org.dataconservancy.model.builder.xstream.DcsPullDriver;
import org.dataconservancy.reporting.model.IngestReport;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;

/**
 * XstreamIngestReportFactory provides a Factory method that instantiates an XStream to be used by
 * {@code XstreamIngestReportBuilder}
 */
public class XstreamIngestReportFactory {
    
    public static XStream newInstance() {
        
        final QNameMap qnames = new QNameMap();
        
        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        final XStream x = new XStream(driver);
        x.setMode(XStream.NO_REFERENCES);
        
        x.addDefaultImplementation(IngestReport.class, IngestReport.class);
        x.alias(IngestReportConverter.E_INGEST_REPORT, IngestReport.class);
        x.registerConverter(new IngestReportConverter());
        qnames.registerMapping(new QName(null, IngestReportConverter.E_INGEST_REPORT), IngestReport.class);
        
        return x;
    }

}
