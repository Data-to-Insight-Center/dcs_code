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

/**
 * Determines if a Data Conservancy Package conforms to a specific structure, identified by the DcpProfile
 * {@link #getType() type} and {@link #getVersion() version}.  A {@code DcpProfile} examines the structure of a DCP and the
 * properties of the entities contained therein (e.g. relationships between entities, technical environment of
 * manifestations, names of files).  It is not concerned about evaluating or interpreting content that may be referenced
 * or serialized in the package (for example, the content of a DCS File).
 * <p/>
 * Therefore, a version of a DcpProfile represents a structure of a DCP.  New versions of a DcpProfile should only be created
 * if the structure of the DCP has changed (e.g. to accommodate new business requirements).
 * <p/>
 * A DCP may conform to multiple Profiles.
 */
public interface DcpProfile extends Profile<Dcp> {

    /**
     * {@inheritDoc}
     * <p/>
     * A single DCP may conform to multiple profiles.
     *
     * @param candidatePackage an instance of the model
     * @return true if the package conforms to this profile, false otherwise
     */
    public boolean conforms(Dcp candidatePackage);

}
