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

import org.apache.commons.io.IOUtils;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.registry.api.support.BasicRegistryEntryMapper;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Sample DCP packages and package fragments that can be used for testing.
 */
class Dcps {

    final String licenseName = "Attribution-NonCommercial-ShareAlike 3.0 Unported";
    final String licenseTag = "CC BY-NC-SA 3.0";
    final String licenseUri = "http://creativecommons.org/licenses/by-nc-sa/3.0/";
    final String licenseVersion = "3.0";
    final String licenseSummary = "This is the summary of the license.";
    final String licenseFulltext = "This is the full text of the license.";

    static final String CONFORMING_DU = "\n" +
            "        <DeliverableUnit id=\"f0220ef0-eb7b-4a51-86c2-cc2c69c967ed\">\n" +
            "            <type>" + LicenseRegistryConstant.LICENSE_REGISTRY_ENTRY_TYPE + "</type>\n" +
            "            <title>Attribution-NonCommercial-ShareAlike 3.0 Unported</title>\n" +
            "            <formerExternalRef>http://creativecommons.org/licenses/by-nc-sa/3.0/</formerExternalRef>\n" +
            "            <digitalSurrogate>false</digitalSurrogate>\n" +
            "        </DeliverableUnit>\n";

    static final String INCORRECT_TYPE_DU = "\n" +
            "        <DeliverableUnit id=\"f0220ef0-eb7b-4a51-86c2-cc2c69c967ed\">\n" +
            "            <type>blah</type>\n" +
            "            <title>License Registry Entry</title>\n" +
            "            <formerExternalRef>http://creativecommons.org/licenses/by-nc-sa/3.0/</formerExternalRef>\n" +
            "            <digitalSurrogate>false</digitalSurrogate>\n" +
            "        </DeliverableUnit>\n";
    
    static final String REGISTRY_ENTRY_DU = "\n" +
            "        <DeliverableUnit id=\"7e212b09-17d4-4758-90fc-d8e8d71f0094\">\n" +
            "            <type>" + BasicRegistryEntryMapper.REGISTRY_ENTRY_DU_TYPE + "</type>\n" +
            "            <title>This is the registry entry for the license</title>\n" +
            "            <digitalSurrogate>false</digitalSurrogate>\n" +
            "            <relationship ref=\"f0220ef0-eb7b-4a51-86c2-cc2c69c967ed\" rel=\"urn:dataconservancy.org:rel/isMetadataFor\"/>\n" +
            "        </DeliverableUnit>\n";

    static final String CONFORMING_TECHENV = "\n" +
            "            <technicalEnvironment>Java Version: 1.6.0_24-b07-334-10M3326</technicalEnvironment>\n" +
            "            <technicalEnvironment>DCS Data Model http://dataconservancy.org/schemas/dcp/1.0</technicalEnvironment>\n" +
            "            <technicalEnvironment>XStream 1.3.1</technicalEnvironment>\n";

    static final String MISSING_DATAMODEL_TECHENV = "\n" +
            "            <technicalEnvironment>Java Version: 1.6.0_24-b07-334-10M3326</technicalEnvironment>\n" +
            "            <technicalEnvironment>XStream 1.3.1</technicalEnvironment>\n";

    static final String CONFORMING_MANIFESTATION = "\n" +
            "        <Manifestation id=\"6a4a7e12-ed73-4722-8a84-1e03d8ee5901\"\n" +
            "            dateCreated=\"2011-07-14T23:45:35.662Z\">\n" +
            "            \n" +
            "            <deliverableUnit ref=\"f0220ef0-eb7b-4a51-86c2-cc2c69c967ed\"/>\n" + CONFORMING_TECHENV +
            "            <manifestationFile ref=\"b64eb1bb-7d27-4106-8e3a-30998fcd9549\">\n" +
            "                <path>/</path>\n" +
            "            </manifestationFile>\n" +
            "        </Manifestation>\n";

    static final String REGISTRY_ENTRY_MANIFESTATION = "\n" +
            "        <Manifestation id=\"d6a93285-f67e-480c-bd0f-097a24d01bac\"\n" +
            "            dateCreated=\"2011-07-14T23:45:35.662Z\">\n" +
            "            \n" +
            "            <deliverableUnit ref=\"7e212b09-17d4-4758-90fc-d8e8d71f0094\"/>\n" +
            "            <type>" + BasicRegistryEntryMapper.REGISTRY_ENTRY_MAN_TYPE + "</type>\n" +
            "            <manifestationFile ref=\"46788232-26b5-42ed-a0a5-4bdd3f077abf\">\n" +
            "                <path>/</path>\n" +
            "            </manifestationFile>\n" +
            "        </Manifestation>\n";
    
    static final String MISSING_TECHENV_MANIFESTATION = "\n" +
            "        <Manifestation id=\"6a4a7e12-ed73-4722-8a84-1e03d8ee5901\"\n" +
            "            dateCreated=\"2011-07-14T23:45:35.662Z\">\n" +
            "            <deliverableUnit ref=\"f0220ef0-eb7b-4a51-86c2-cc2c69c967ed\"/>\n" +
            "            <manifestationFile ref=\"b64eb1bb-7d27-4106-8e3a-30998fcd9549\">\n" +
            "                <path>/</path>\n" +
            "            </manifestationFile>\n" +
            "        </Manifestation>\n";

    static final String MISSING_TECHENV_DATA_MODEL_MANIFESTATION = "\n" +
            "        <Manifestation id=\"6a4a7e12-ed73-4722-8a84-1e03d8ee5901\"\n" +
            "            dateCreated=\"2011-07-14T23:45:35.662Z\">\n" +
            "            <deliverableUnit ref=\"f0220ef0-eb7b-4a51-86c2-cc2c69c967ed\"/>\n" + MISSING_DATAMODEL_TECHENV +
            "            <manifestationFile ref=\"b64eb1bb-7d27-4106-8e3a-30998fcd9549\">\n" +
            "                <path>/</path>\n" +
            "            </manifestationFile>\n" +
            "        </Manifestation>\n";

    static final String CONFORMING_FILE = "\n" +
            "        <File id=\"b64eb1bb-7d27-4106-8e3a-30998fcd9549\"\n" +
            "            src=\"" + Dcps.class.getResource("/org/dataconservancy/registry/impl/license/shared/serialized_license.xml") + "\">\n" +
            "            <fileName>java-entry-serialization</fileName>\n" +
            "            <extant>true</extant>\n" +
            "            <size>546</size>\n" +
            "            <fixity algorithm=\"MD5\">4dec79387493ae3a3728c73912871799</fixity>\n" +
            "            <fixity algorithm=\"SHA-1\">3eb42341488976d42a5ac7a54f3d63a557b186b</fixity>\n" +
            "            <format>\n" +
            "                <id scheme=\"http://www.iana.org/assignments/media-types/\">application/xml</id>\n" +
            "            </format>\n" +
            "        </File>\n";
    
    static final String REGISTRY_FILE = "\n" +
            "        <File id=\"46788232-26b5-42ed-a0a5-4bdd3f077abf\"\n" +
            "            src=\"file:/var/folders/El/EldIZ5LOGOyQ9tUh-WCEYk+++TI/-Tmp-/registryEntryMap1522678615449486719.tmp\">\n" +
            "            <fileName>java-entry-serialization</fileName>\n" +
            "            <extant>true</extant>\n" +
            "            <size>100</size>\n" +
            "            <format>\n" +
            "                <id scheme=\"http://www.iana.org/assignments/media-types/\">application/xml</id>\n" +
            "            </format>\n" +
            "        </File>\n";
    
    static final String CONFORMING_DCP = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xsi:schemaLocation=\"http://dataconservancy.org/schemas/dcp/1.0 http://dataconservancy.org/schemas/dcp/1.0\">\n" +
        "    <DeliverableUnits>" + CONFORMING_DU + REGISTRY_ENTRY_DU +
        "    </DeliverableUnits>\n" +
        "    <Manifestations>" + CONFORMING_MANIFESTATION + REGISTRY_ENTRY_MANIFESTATION +
        "    </Manifestations>\n" +
        "    <Files>" + CONFORMING_FILE + REGISTRY_FILE +
        "    </Files>\n" +
        "</dcp>";

    static Dcp getConformingDcp() throws InvalidXmlException {
        return new DcsXstreamStaxModelBuilder().buildSip(IOUtils.toInputStream(CONFORMING_DCP));
    }

    static DcsLicense getConformingLicense() {
        final String licenseName = "Attribution-NonCommercial-ShareAlike 3.0 Unported";
        final String licenseTag = "CC BY-NC-SA 3.0";
        final String licenseUri = "http://creativecommons.org/licenses/by-nc-sa/3.0/";
        final String licenseVersion = "3.0";
        final String licenseSummary = "This is the summary of the license.";
        final String licenseFulltext = "This is the full text of the license.";

        final DcsLicense license = new DcsLicense();
        try {
            license.addUris(new URI(licenseUri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        license.setTag(licenseTag);
        license.setVersion(licenseVersion);
        license.setName(licenseName);
        license.setSummary(licenseSummary);
        license.setFullText(licenseFulltext);

        return license;
    }

    static DcsLicense getUnexpectedLicense() {
        final String licenseName = "Foo Name";
        final String licenseTag = "Foo Tag";
        final String licenseUri = "http://creativecommons.org/foo/uri/";
        final String licenseVersion = "3.0";
        final String licenseSummary = "Foo summary";
        final String licenseFulltext = "Foo full text";

        final DcsLicense license = new DcsLicense();
        try {
            license.addUris(new URI(licenseUri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        license.setTag(licenseTag);
        license.setVersion(licenseVersion);
        license.setName(licenseName);
        license.setSummary(licenseSummary);
        license.setFullText(licenseFulltext);

        return license;
    }

    static DcsLicense getMalformedLicense() {

        final String licenseTag = "Foo Tag";
        final String licenseVersion = "3.0";
        final String licenseSummary = "Foo summary";
        final String licenseFulltext = "Foo full text";

        final DcsLicense license = new DcsLicense();
        license.setTag(licenseTag);
        license.setVersion(licenseVersion);
        license.setSummary(licenseSummary);
        license.setFullText(licenseFulltext);

        return license;
    }

    static final String MISSING_DU = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xsi:schemaLocation=\"http://dataconservancy.org/schemas/dcp/1.0 http://dataconservancy.org/schemas/dcp/1.0\">\n" +
        "    <Manifestations>" + CONFORMING_MANIFESTATION +
        "    </Manifestations>\n" +
        "    <Files>" + CONFORMING_FILE +
        "    </Files>\n" +
        "</dcp>";

    static Dcp getMissingDuDcp() throws InvalidXmlException {
        return new DcsXstreamStaxModelBuilder().buildSip(IOUtils.toInputStream(MISSING_DU));
    }

    static final String MISSING_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xsi:schemaLocation=\"http://dataconservancy.org/schemas/dcp/1.0 http://dataconservancy.org/schemas/dcp/1.0\">\n" +
        "    <DeliverableUnits>" + CONFORMING_DU +
        "    </DeliverableUnits>\n" +
        "    <Manifestations>" + CONFORMING_MANIFESTATION +
        "    </Manifestations>\n" +
        "</dcp>";

    static Dcp getMissingFileDcp() throws InvalidXmlException {
        return new DcsXstreamStaxModelBuilder().buildSip(IOUtils.toInputStream(MISSING_FILE));
    }

    static final String MISSING_MANIFESTATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xsi:schemaLocation=\"http://dataconservancy.org/schemas/dcp/1.0 http://dataconservancy.org/schemas/dcp/1.0\">\n" +
        "    <DeliverableUnits>" + CONFORMING_DU +
        "    </DeliverableUnits>\n" +
        "    <Files>" + CONFORMING_FILE +
        "    </Files>\n" +
        "</dcp>";

    static Dcp getMissingManifestationDcp() throws InvalidXmlException {
        return new DcsXstreamStaxModelBuilder().buildSip(IOUtils.toInputStream(MISSING_MANIFESTATION));
    }

    static final String MISSING_TECHENV = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xsi:schemaLocation=\"http://dataconservancy.org/schemas/dcp/1.0 http://dataconservancy.org/schemas/dcp/1.0\">\n" +
        "    <DeliverableUnits>" + CONFORMING_DU +
        "    </DeliverableUnits>\n" +
        "    <Manifestations>" + MISSING_TECHENV_MANIFESTATION +
        "    </Manifestations>\n" +
        "    <Files>" + CONFORMING_FILE +
        "    </Files>\n" +
        "</dcp>";

    static Dcp getMissingTechenvDcp() throws InvalidXmlException {
        return new DcsXstreamStaxModelBuilder().buildSip(IOUtils.toInputStream(MISSING_TECHENV));
    }

    static final String MISSING_DATAMODEL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xsi:schemaLocation=\"http://dataconservancy.org/schemas/dcp/1.0 http://dataconservancy.org/schemas/dcp/1.0\">\n" +
        "    <DeliverableUnits>" + CONFORMING_DU +
        "    </DeliverableUnits>\n" +
        "    <Manifestations>" + MISSING_TECHENV_DATA_MODEL_MANIFESTATION +
        "    </Manifestations>\n" +
        "    <Files>" + CONFORMING_FILE +
        "    </Files>\n" +
        "</dcp>";

    static Dcp getMissingDataModelDcp() throws InvalidXmlException {
        return new DcsXstreamStaxModelBuilder().buildSip(IOUtils.toInputStream(MISSING_DATAMODEL));
    }

    static final String INCORRECT_DU_TYPE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xsi:schemaLocation=\"http://dataconservancy.org/schemas/dcp/1.0 http://dataconservancy.org/schemas/dcp/1.0\">\n" +
        "    <DeliverableUnits>" + INCORRECT_TYPE_DU +
        "    </DeliverableUnits>\n" +
        "    <Manifestations>" + CONFORMING_MANIFESTATION +
        "    </Manifestations>\n" +
        "    <Files>" + CONFORMING_FILE +
        "    </Files>\n" +
        "</dcp>";

    static Dcp getIncorrectDuTypeDcp() throws InvalidXmlException {
        return new DcsXstreamStaxModelBuilder().buildSip(IOUtils.toInputStream(INCORRECT_DU_TYPE));
    }
}
