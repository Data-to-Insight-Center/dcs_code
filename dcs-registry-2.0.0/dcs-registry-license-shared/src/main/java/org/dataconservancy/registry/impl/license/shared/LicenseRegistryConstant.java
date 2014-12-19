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
package org.dataconservancy.registry.impl.license.shared;

/**
 * Constants used throughout the License Registry
 */
public class LicenseRegistryConstant {

    /**
     * The string used to identify license entry types.  It is used in response to
     * {@link org.dataconservancy.registry.api.Registry#getEntryType()}. In the query framework implementation, this is
     * mapped to a Deliverable Unit &lt;type> (that is, each license entry Dcp contains a Deliverable Unit with this
     * &lt;type>).
     */
    public static final String LICENSE_REGISTRY_ENTRY_TYPE = "dataconservancy:types:registry-entry:license";

    public static final String VERSION_ONE = "1.0";

}
