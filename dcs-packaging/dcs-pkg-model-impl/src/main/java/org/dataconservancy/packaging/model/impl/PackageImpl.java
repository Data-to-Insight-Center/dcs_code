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
package org.dataconservancy.packaging.model.impl;

import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PackageImpl implements Package {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private PackageDescription description;
    private PackageSerialization serialization;
    
    public PackageImpl(PackageDescription description, PackageSerialization serialization) {
        this.description = description;
        this.serialization = serialization;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PackageDescription getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PackageSerialization getSerialization() {
        return serialization;
    }

    @Override
    public String toString() {
        return "PackageImpl{" +
                "description=" + description +
                ", serialization=" + serialization +
                '}';
    }

    public String toString(HierarchicalPrettyPrinter hpp) {

        if (!hasHpp(description)) {
            hpp.appendWithIndentAndNewLine("Description: " + description);
        } else {
            hpp.appendWithIndentAndNewLine("Description:");
            hpp.incrementDepth();
            invokeHpp(hpp, description);
            hpp.decrementDepth();
        }

        if (!hasHpp(serialization)) {
            hpp.appendWithIndentAndNewLine("Serialization: " + serialization);
        } else {
            hpp.appendWithIndentAndNewLine("Serialization: ");
            hpp.incrementDepth();
            invokeHpp(hpp, serialization);
            hpp.decrementDepth();
        }

        return hpp.toString();
    }

    /**
     * Uses reflection to determine if the supplied object has a {@code toString(HierarchicalPrettyPrinter)} method.
     *
     * @param o any object
     * @return true if {@code toString(HierarchicalPrettyPrinter)} is present
     */
    private boolean hasHpp(Object o) {
        try {
            return o.getClass().getDeclaredMethod("toString", HierarchicalPrettyPrinter.class) != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Invokes {@code toString(HierarchicalPrettyPrinter)} on the supplied object using reflection.
     *
     * @param hpp a HierarchicalPrettyPrinter instance
     * @param o   the object to invoke {@code toString(HierarchicalPrettyPrinter)} on
     */
    private void invokeHpp(HierarchicalPrettyPrinter hpp, Object o) {
        try {
            Method hppToString = o.getClass().getDeclaredMethod("toString", HierarchicalPrettyPrinter.class);
            hppToString.invoke(o, hpp);
        } catch (NoSuchMethodException e) {
            log.warn(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.warn(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.warn(e.getMessage(), e);
        }
    }
}