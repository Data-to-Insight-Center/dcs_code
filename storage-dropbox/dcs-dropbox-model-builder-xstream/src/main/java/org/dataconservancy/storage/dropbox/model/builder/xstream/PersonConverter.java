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

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

import org.dataconservancy.storage.dropbox.model.Person;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PersonConverter extends AbstractEntityConverter implements ConverterConstants {
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        
        final Person person = (Person) source;

        if (person != null) {
            if (!isEmptyOrNull(person.getFirstName())) {
                writer.startNode(E_FIRST_NAME);
                writer.setValue(person.getFirstName());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getLastName())) {
                writer.startNode(E_LAST_NAME);
                writer.setValue(person.getLastName());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getMiddleName())) {
                writer.startNode(E_MIDDLE_NAME);
                writer.setValue(person.getMiddleName());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getPrefix())) {
                writer.startNode(E_PREFIX);
                writer.setValue(person.getPrefix());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getSuffix())) {
                writer.startNode(E_SUFFIX);
                writer.setValue(person.getSuffix());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getOccupation())) {
                writer.startNode(E_OCCUPATION);
                writer.setValue(person.getOccupation());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getOrganization())) {
                writer.startNode(E_ORGANIZATION);
                writer.setValue(person.getOrganization());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getSecretQuestion())) {
                writer.startNode(E_SECRET_QUESTION);
                writer.setValue(person.getSecretQuestion());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getSecretAnswer())) {
                writer.startNode(E_SECRET_ANSWER);
                writer.setValue(person.getSecretAnswer());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getEmail())) {
                writer.startNode(E_EMAIL);
                writer.setValue(person.getEmail());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getUsername())) {
                writer.startNode(E_USERNAME);
                writer.setValue(person.getUsername());
                writer.endNode();
            }
            if (!isEmptyOrNull(person.getPassword())) {
                writer.startNode(E_PASSWORD);
                writer.setValue(person.getPassword());
                writer.endNode();
            }
        }
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Person person = new Person();

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String ename = getElementName(reader);

            if (ename.equals(E_FIRST_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setEmail(value.trim());
                }
            }
            else if (ename.equals(E_LAST_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setLastName(value.trim());
                }
            }
            else if (ename.equals(E_MIDDLE_NAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setMiddleName(value.trim());
                }
            }
            else if (ename.equals(E_PREFIX)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setPrefix(value.trim());
                }
            }
            else if (ename.equals(E_SUFFIX)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setSuffix(value.trim());
                }
            }
            else if (ename.equals(E_OCCUPATION)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setOccupation(value.trim());
                }
            }
            else if (ename.equals(E_ORGANIZATION)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setOrganization(value.trim());
                }
            }
            else if (ename.equals(E_EMAIL)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setEmail(value.trim());
                }
            }
            else if (ename.equals(E_SECRET_QUESTION)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setSecretQuestion(value.trim());
                }
            }
            else if (ename.equals(E_SECRET_ANSWER)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setSecretAnswer(value.trim());
                }
            }
            else if (ename.equals(E_USERNAME)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setUsername(value.trim());
                }
            }
            else if (ename.equals(E_PASSWORD)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    person.setPassword(value.trim());
                }
            }
            reader.moveUp();
        }

        return person;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return type == Person.class;
    }
    
}
