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

import java.io.InputStream;
import java.io.OutputStream;

import org.dataconservancy.storage.dropbox.model.Bop;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.Person;
import org.dataconservancy.storage.dropbox.model.builder.BusinessObjectBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

public class XstreamBusinessObjectBuilder implements BusinessObjectBuilder {
    
    /**
     * Error deserializing a stream. Parameters: reason
     */
    private final static String DESER_ERR = "Error encountered deserializing a stream: %s";
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private XStream x;
    
    public XstreamBusinessObjectBuilder() {
        x = XstreamBusinessObjectFactory.newInstance();
    }

    @Override
    public DropboxModel buildDropboxModel(InputStream in) throws InvalidXmlException {
        if (in != null) {
            final DropboxModel dropboxModel;
            try {
                dropboxModel = (DropboxModel) x.fromXML(in);
            }
            catch (StreamException e) {
                log.debug(String.format(DESER_ERR, e.getMessage()), e);
                throw new InvalidXmlException(e);
            }
            return dropboxModel;
        }
        else {
            return null;
        }
    }
    
    @Override
    public void buildDropboxModel(DropboxModel dbm, OutputStream sink) {
        if (dbm != null && sink != null) {
            x.toXML(dbm, sink);
        }

    }
    
    @Override
    public Person buildPerson(InputStream in) throws InvalidXmlException {
        if (in != null) {
            final Person person;
            try {
                person = (Person) x.fromXML(in);
            }
            catch (StreamException e) {
                log.debug(String.format(DESER_ERR, e.getMessage()), e);
                throw new InvalidXmlException(e);
            }
            return person;
        }
        else {
            return null;
        }
    }
    
    @Override
    public void buildPerson(Person person, OutputStream sink) {
        if (person != null && sink != null) {
            x.toXML(person, sink);
        }

    }
    
    @Override
    public Bop buildBop(InputStream in) throws InvalidXmlException {
        if (in != null) {
            final Bop bop;
            try {
                bop = (Bop) x.fromXML(in);
            }
            catch (StreamException e) {
                log.debug(String.format(DESER_ERR, e.getMessage()), e);
                throw new InvalidXmlException(e);
            }
            return bop;
        }
        else {
            return null;
        }
    }
    
    @Override
    public void buildBop(Bop bop, OutputStream sink) {
        if (bop != null && sink != null) {
            x.toXML(bop, sink);
        }
    }

}
