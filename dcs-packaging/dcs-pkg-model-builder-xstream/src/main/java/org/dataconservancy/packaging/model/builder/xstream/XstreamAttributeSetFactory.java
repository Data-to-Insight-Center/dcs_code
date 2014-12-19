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
package org.dataconservancy.packaging.model.builder.xstream;

import javax.xml.namespace.QName;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.builder.xstream.DcsPullDriver;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;

/**
 * XstreamAttributeSetFactory provides a Factory method that instantiates an XStream to be used by
 * {@code XstreamAttributeSetBuilder}
 */
public class XstreamAttributeSetFactory {
    
    public static XStream newInstance() {
        
        final QNameMap qnames = new QNameMap();
        
        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        final XStream x = new XStream(driver);
        x.setMode(XStream.NO_REFERENCES);
        
        x.addDefaultImplementation(AttributeSetImpl.class, AttributeSet.class);
        x.alias(AttributeSetConverter.E_ATTRIBUTE_SET, AttributeSet.class);
        x.registerConverter(new AttributeSetConverter());
        qnames.registerMapping(new QName(null, AttributeSetConverter.E_ATTRIBUTE_SET), AttributeSet.class);
        
        return x;
    }

}
