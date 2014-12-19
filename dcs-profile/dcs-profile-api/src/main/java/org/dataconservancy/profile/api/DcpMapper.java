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

package org.dataconservancy.profile.api;

import org.dataconservancy.model.dcp.Dcp;

import java.util.Map;
import java.util.Set;

/**
 * Interprets the content of a Data Conservancy Package, providing mechanisms to map a DCP to business objects, and
 * business objects back to a DCP.
 */
public interface DcpMapper<T> extends ModelMapper<Dcp, T> {

    /**
     * For conforming packages (that is, {@link DcpProfile#conforms(org.dataconservancy.model.dcp.Dcp)} returns {@code true}),
     * this method will introspect over the package and return the archival (i.e. DCS)  identifiers of the business
     * objects represented in the package.  Identifiers contained in the returned {@code Set} can then be used in calls
     * to {@link #from(String, org.dataconservancy.model.dcp.Dcp, java.util.Map)}.
     *
     * @param conformingPackage a package that conforms to this profile
     * @return a {@code Set} of archival identifiers for the business objects contained in the package
     */
    public Set<String> discover(Dcp conformingPackage);

    /**
     * Serialize a domain object as a DCP.  May return null if serialization is not possible.
     *
     * @param domainObject the domain object
     * @param context      additional context for the implementation to compose the DCP, may be {@code null}
     * @return the DCP, or null if serialization is not possible
     */
    public Dcp to(T domainObject, Map<String, Object> context);

    /**
     * Deserialize a domain object from a DCP.  May return null if de-serialization is not possible.
     * <p/>
     * For conforming packages (that is, {@link DcpProfile#conforms(org.dataconservancy.model.dcp.Dcp)} returns
     * {@code true}), this method will introspect over the package, compose, and return the business object.  It may be
     * the case that the archival package does not fully encode all of the properties of the business object, so the
     * caller may supply additional {@code context}.
     * <p/>
     * It is expected that each identifier returned by {@link #discover(org.dataconservancy.model.dcp.Dcp)} is a valid
     * parameter to this method, and should result in a business object being returned.  If an identifier cannot be
     * resolved to a business object, this method may return {@code null}.
     *
     * @param identifier the archival (DCS entity) identifier of a business object in the supplied {@code Dcp} to deserialize
     * @param dcp        the Data Conservancy Package containing the identified object
     * @param context    additional context for the implementation to compose the object, may be {@code null}
     * @return the domain object, or null if deserialization is not possible
     */
    public T from(String identifier, Dcp dcp, Map<String, Object> context);

}
