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

package org.dataconservancy.storage.dropbox.model.builder.xstream;

import javax.xml.namespace.QName;

import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.Person;
import org.dataconservancy.model.builder.xstream.DcsPullDriver;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;

public class XstreamBusinessObjectFactory {
    
    public static XStream newInstance() {
        
        final QNameMap qnames = new QNameMap();
        
        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        final XStream x = new XStream(driver);
        x.setMode(XStream.NO_REFERENCES);
        
        x.addDefaultImplementation(DropboxModel.class, DropboxModel.class);
        x.alias(DropboxModelConverter.E_DROPBOX_MODEL, DropboxModel.class);
        x.registerConverter(new DropboxModelConverter());
        qnames.registerMapping(new QName(null, DropboxModelConverter.E_DROPBOX_MODEL), DropboxModel.class);
        
        x.addDefaultImplementation(Person.class, Person.class);
        x.alias(PersonConverter.E_PERSON, Person.class);
        x.registerConverter(new PersonConverter());
        qnames.registerMapping(new QName(null, PersonConverter.E_PERSON), Person.class);

        return x;
    }

}
