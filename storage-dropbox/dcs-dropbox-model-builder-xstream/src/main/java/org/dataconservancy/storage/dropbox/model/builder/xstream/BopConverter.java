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

import java.util.Set;

import javax.xml.XMLConstants;

import org.dataconservancy.storage.dropbox.model.Bop;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.Person;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class BopConverter extends AbstractEntityConverter implements ConverterConstants {
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final Bop businessObjectPackage = (Bop) source;
        final Set<DropboxModel> dropboxModels = businessObjectPackage.getDropboxModels();
        final Set<Person> persons = businessObjectPackage.getPersons();
        
        writer.addAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);

        if (!persons.isEmpty()) {
            writer.startNode(E_PERSONS);
            for (Person person : persons) {
                writer.startNode(PersonConverter.E_PERSON);
                context.convertAnother(person);
                writer.endNode();
            }
            writer.endNode();
        }
        
        if (!dropboxModels.isEmpty()) {
            writer.startNode(E_DROPBOX_MODELS);
            for (DropboxModel dm : dropboxModels) {
                writer.startNode(DropboxModelConverter.E_DROPBOX_MODEL);
                context.convertAnother(dm);
                writer.endNode();
            }
            writer.endNode();
        }

    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final Bop businessObjectPackage = new Bop();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            
            final String containerElementName = getElementName(reader);
            
            if (containerElementName.equals(E_DROPBOX_MODELS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(DropboxModelConverter.E_DROPBOX_MODEL)) {
                        final DropboxModel dm = (DropboxModel) context.convertAnother(businessObjectPackage,
                                DropboxModel.class);
                        businessObjectPackage.addDropboxModel(dm);
                    }
                    reader.moveUp();
                }
            }
            
            if (containerElementName.equals(E_PERSONS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(DropboxModelConverter.E_PERSON)) {
                        final Person person = (Person) context.convertAnother(businessObjectPackage, Person.class);
                        businessObjectPackage.addPerson(person);
                    }
                    reader.moveUp();
                }
            }
            reader.moveUp();
        }
        
        return businessObjectPackage;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return type == Bop.class;
    }
    
}
