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

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.dcs.util.DigestListenerImpl;
import org.dataconservancy.dcs.util.DigestNotificationOutputStream;
import org.dataconservancy.dcs.util.stream.api.StreamSource;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.registry.api.support.RegistryEntry;
import org.dataconservancy.profile.api.DcpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Maps {@link DcsLicense} domain objects to {@link Dcp} packages and {@link RegistryEntry}ies according to a profile.
 * <p/>
 * The Dcp packages produced by this mapper are not managed in any way, so care should be taken when attempting to
 * resolve the bytestreams referenced by the DcsFile 'source' attribute.  Future implementations may provide a more
 * robust mechanism for managing these bytestreams.
 * <p/>
 * The DCP package profile is not defined in a rigorous way: there is currently no XSD, Schematron, or other definition
 * of the profile.  There is, however, a {@link LicenseProfiler} which can inspect a DCP package and determines if if
 * it conforms to the profile.
 */
public class DcpLicenseMapper implements DcpMapper<DcsLicense> {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private LicenseProfiler profiler;
    private StreamSource streamSource;
    private XStream xstream;
    private DcsModelBuilder builder;

    /**
     * {@inheritDoc}
     * <p/>
     * Serializes a <code>DcsLicense</code> to a <code>Dcp</code> according to a profile.  The DcsLicense is represented
     * as a single Deliverable Unit with a single Manifestation and File.  Elements specific to the profile are created
     * and populated by this method, including setting the appropriate &lt;type> of the Deliverable Unit, and the proper
     * &lt;technicalEnvironment> elements for the Manifestation.  The license is serialized as a bytestream, stored
     * in a temporary file, and a File is created and named according to the profile.
     * <p/>
     * This implementation will refuse to serialize the <code>domainObject</code> if it is incomplete.  It must have
     * a {@link DcsLicense#getName() name}.
     * <p/>
     * Gotcha: the returned <code>Dcp</code> is not managed in any way (unlike the way SIPs are managed upon ingest).
     * This means that serialized bytestream (the 'source' of the File) may not be available if this Dcp is moved from
     * system to system.
     * 
     * @param domainObject {@inheritDoc}
     * @return {@inheritDoc}
     * @throws IllegalArgumentException if <code>domainObject</code> is null.
     */
    @Override
    public Dcp to(DcsLicense domainObject, Map<String, Object> context) {
        if (domainObject == null) {
            throw new IllegalArgumentException("Domain Object must not be null.");
        }

        if (domainObject.getName() == null || domainObject.getName().trim().equals("")) {
            return null;
        }

        if (xstream == null) {
            throw new IllegalStateException("No XStream instance has been provided.");
        }

        File temp;
        try {
            temp = File.createTempFile("DcpLicenseMapper", ".xml");
        } catch (IOException e) {
            log.info(e.getMessage(), e);
            return null;
        }

        final String serializedLicense = xstream.toXML(domainObject);
        final DcsFixity sha1Fixity = new DcsFixity();
        final DcsFixity md5Fixity = new DcsFixity();

        try {
            final DigestListenerImpl sha1listener = new DigestListenerImpl();
            final DigestListenerImpl md5listener = new DigestListenerImpl();
            final DigestNotificationOutputStream out =
                    new DigestNotificationOutputStream(
                            new DigestNotificationOutputStream(new FileOutputStream(temp), MessageDigest.getInstance("MD5"), md5listener),
                            MessageDigest.getInstance("SHA-1"), sha1listener);
            IOUtils.write(serializedLicense, out);
            out.close();

            sha1Fixity.setAlgorithm("SHA-1");
            sha1Fixity.setValue(sha1listener.asHex());
            md5Fixity.setAlgorithm("MD5");
            md5Fixity.setValue(md5listener.asHex());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }

        final DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(UUID.randomUUID().toString());

        du.setTitle(domainObject.getName() == null ? "Title Not Supplied" : domainObject.getName());
        du.setType(LicenseRegistryConstant.LICENSE_REGISTRY_ENTRY_TYPE);
        du.setDigitalSurrogate(false);
        for (URI u : domainObject.getUris()) {
            du.addFormerExternalRef(u.toString());
        }

        final DcsManifestation man = new DcsManifestation();
        man.setId(UUID.randomUUID().toString());
        man.setDateCreated(DateUtility.toIso8601(Calendar.getInstance().getTimeInMillis()));
        final DcsManifestationFile mf = new DcsManifestationFile();
        final DcsFormat format = new DcsFormat();
        final DcsFile file = new DcsFile();
        file.setId(UUID.randomUUID().toString());

        try {
            file.setSizeBytes(serializedLicense.getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }

        file.setExtant(true);
        file.setName("java-entry-serialization");
        format.setFormat("application/xml");
        format.setSchemeUri("http://www.iana.org/assignments/media-types/");
        file.addFormat(format);
        file.setSource(temp.toURI().toString());
        file.addFixity(sha1Fixity, md5Fixity);


        mf.setPath("/");
        mf.setRef(new DcsFileRef(file.getId()));

        man.addManifestationFile(mf);
        man.addTechnicalEnvironment("XStream 1.3.1", "DCS Data Model " + DcpModelVersion.VERSION_1_0.getXmlns(),
                "Java Version: " + System.getProperty("java.runtime.version"));
        man.setDeliverableUnit(du.getId());

        final Dcp dcp = new Dcp();
        dcp.addDeliverableUnit(du);
        dcp.addManifestation(man);
        dcp.addFile(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        builder.buildSip(dcp, out);

        try {
            temp = File.createTempFile("DcsEntityLicenceMapper", ".txt");
            FileUtils.writeByteArrayToFile(temp, out.toByteArray());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return dcp;
    }

    @Override
    public DcsLicense from(String identifier, Dcp dcp, Map<String, Object> context) {

        if (profiler == null) {
            throw new IllegalStateException("No License Profiler has been provided.");
        }

        if (xstream == null) {
            throw new IllegalStateException("No XStream instance has been provided.");
        }

        if (!profiler.conformsTo(dcp)) {
            log.debug("DCP does not conform to the profile.");
            return null;
        }
        
        final DcsFile dcsFile = profiler.selectFile(dcp);

        DcsLicense license = null;
        try {
            license = (DcsLicense) xstream.fromXML(streamSource.getStream(dcsFile.getId()));
        } catch (IOException e) {
            log.error("Error retrieving license stream " + dcsFile.getId() + " from stream source " + streamSource + ": " + e.getMessage(), e);
        }
        return license;
    }

    @Override
    public Set<String> discover(Dcp conformingPackage) {
        if (profiler == null) {
            throw new IllegalStateException("No License Profiler has been provided.");
        }

        DcsDeliverableUnit du = profiler.selectDeliverableUnit(conformingPackage);

        Set<String> ids = new HashSet<String>();
        ids.add(du.getId());
        return ids;
    }

    public RegistryEntry<DcsLicense> toRegistryEntry(Dcp dcp) {

        DcsLicense license = from("someId", dcp, null);
        LicenseRegistryEntry entry = new LicenseRegistryEntry();
        
        // TODO: should this be getIds()?
        if (license.getUris() != null && !license.getUris().isEmpty()) {
            entry.id = license.getUris().iterator().next().toString();
        } else {
            entry.id = profiler.selectDeliverableUnit(dcp).getId();
        }

        entry.name = license.getName();
        entry.domainObject = license;

        return entry;
    }

    public LicenseProfiler getProfiler() {
        return profiler;
    }

    public void setProfiler(LicenseProfiler profiler) {
        this.profiler = profiler;
    }

    public StreamSource getStreamSource() {
        return streamSource;
    }

    public void setStreamSource(StreamSource streamSource) {
        this.streamSource = streamSource;
    }

    public XStream getXstream() {
        return xstream;
    }

    public void setXstream(XStream xstream) {
        this.xstream = xstream;
    }

    public DcsModelBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(DcsModelBuilder builder) {
        this.builder = builder;
    }

    private class LicenseRegistryEntry implements RegistryEntry<DcsLicense> {
        private String name;
        private String id;
        private DcsLicense domainObject;

//        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public DcsLicense getEntry() {
            return domainObject;
        }

        @Override
        public String getEntryType() {
            return LicenseRegistryConstant.LICENSE_REGISTRY_ENTRY_TYPE;
        }
    }

}
