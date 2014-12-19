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

package org.dataconservancy.storage.dropbox.model.builder;

import java.io.InputStream;
import java.io.OutputStream;

import org.dataconservancy.storage.dropbox.model.Bop;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.Person;
import org.dataconservancy.model.builder.InvalidXmlException;

public interface BusinessObjectBuilder {
    
    /**
     * Builds a {@link org.dataconservancy.storage.dropbox.model.Bop } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the Bop
     * serialization specification. The <code>InputStream</code> will be deserialized into the corresponding Data
     * Conservancy java object and returned.
     * 
     * @param in
     * @return the deserialized Bop object
     * @throws InvalidXmlException
     */
    public Bop buildBop(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.ui.model.Bop } to XML, formatted according to the Bop
     * serialization specification.
     * 
     * @param bop
     * @param sink
     */
    public void buildBop(Bop bop, OutputStream sink);

    /**
     * Builds a {@link org.dataconservancy.storage.dropbox.model.DropboxModel } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * DropboxModel serialization specification. The <code>InputStream</code> will be deserialized into the
     * corresponding Data Conservancy java object and returned.
     * 
     * @param in
     * @return the deserialized DropboxModel object
     * @throws InvalidXmlException
     */
    public DropboxModel buildDropboxModel(InputStream in) throws InvalidXmlException;

    /**
     * Serializes the supplied {@link org.dataconservancy.ui.model.DropboxModel } to XML, formatted according to the
     * DropboxModel serialization specification.
     * 
     * @param dbm
     * @param sink
     */
    public void buildDropboxModel(DropboxModel dbm, OutputStream sink);
    
    /**
     * Builds a {@link org.dataconservancy.storage.dropbox.model.Person } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the Person
     * serialization specification. The <code>InputStream</code> will be deserialized into the corresponding Data
     * Conservancy java object and returned.
     * 
     * @param in
     * @return the deserialized Person object
     * @throws InvalidXmlException
     */
    public Person buildPerson(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.ui.model.Person } to XML, formatted according to the Person
     * serialization specification.
     * 
     * @param person
     * @param sink
     */
    public void buildPerson(Person person, OutputStream sink);
}
