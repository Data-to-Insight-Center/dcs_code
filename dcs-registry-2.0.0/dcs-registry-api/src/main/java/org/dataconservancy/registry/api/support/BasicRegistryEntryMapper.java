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
package org.dataconservancy.registry.api.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;


import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.builder.xstream.DcsPullDriver;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.profile.api.DcpMapper;
import org.dataconservancy.registry.api.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;


/**
 * Maps {@link RegistryEntry}ies to {@link Dcp} packages. Domain objects are responsible for mapping domain specific information.
 * <p/>
 * The Dcp packages produced by this mapper are not managed in any way, so care should be taken when attempting to
 * resolve the bytestreams referenced by the DcsFile 'source' attribute.  Future implementations may provide a more
 * robust mechanism for managing these bytestreams.
 * <p/>
 */
public abstract class BasicRegistryEntryMapper<T> implements DcpMapper<RegistryEntry<T>> {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final XStream xstream;
    private static String REGISTRY_TYPES_PREFIX = "dataconservancy.org:types:registry";
    public final static String REGISTRY_ENTRY_DU_TYPE = REGISTRY_TYPES_PREFIX + ":entry";
    public final static String REGISTRY_ENTRY_MAN_TYPE = REGISTRY_TYPES_PREFIX + ":entry:manifestation";

    /**
     * If {@code baseDirectory} is not {@code null}, serializations produced by this mapper will be persisted
     * under this directory, and will not be marked for deletion when the JVM exits.
     */
    protected File baseDirectory;
    
    public BasicRegistryEntryMapper() {
        final QNameMap qnames = new QNameMap();
        
        final String defaultnsUri = "http://dataconservancy.org/schemas/dcp/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        xstream = new XStream(driver);
        xstream.setMode(XStream.NO_REFERENCES);
        
        // XStream converter, alias, and QName registrations
        xstream.alias(BasicRegistryEntryConverter.E_ENTRY, BasicRegistryEntryImpl.class);
        xstream.registerConverter(new BasicRegistryEntryConverter());
        qnames.registerMapping(new QName(defaultnsUri, BasicRegistryEntryConverter.E_ENTRY), BasicRegistryEntryImpl.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> discover(Dcp conformingPackage) {
        Set<String> ids = new HashSet<String>();
        
        for (DcsDeliverableUnit du : conformingPackage.getDeliverableUnits()) {
            if (du.getType().equals(REGISTRY_ENTRY_DU_TYPE)) {
                ids.add(du.getId());
            }
        }
        
        return ids;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public RegistryEntry<T> from(String identifier, Dcp dcp, Map<String, Object> context) {
        log.debug("Obtaining a RegistryEntry using id {} from DCP {}",
                new Object[] { identifier, dcp });
        BasicRegistryEntryImpl<T> result = null;
       
        DcsDeliverableUnit entryDu = null;
        for (DcsDeliverableUnit du : dcp.getDeliverableUnits()) {
            if (du.getType().equalsIgnoreCase(REGISTRY_ENTRY_DU_TYPE)) {
                entryDu = du;
                break;
            }
        }
        
        if (entryDu == null) {
            log.error("Unable to find registry entry du.");
            return null;
        }

        DcsManifestation entryMan = null;
        for (DcsManifestation man : dcp.getManifestations()) {
            if (man.getType() != null && man.getDeliverableUnit() != null) {
                if (man.getType().equalsIgnoreCase(REGISTRY_ENTRY_MAN_TYPE) && man.getDeliverableUnit().equals(entryDu.getId())) {
                    entryMan = man;
                    break;
                }
            }
        }
        
        if (entryMan == null) {
            log.error("Unable to find registry entry manifestation.");
            return null;
        }
        
        
        DcsManifestationFile dcs_mf = entryMan.getManifestationFiles()
                .iterator().next();

        if (dcs_mf.getRef() == null) {
            log.error("Registry Entry manifestation file, missing file reference.");
            return null;
        }

        String entryFileId = dcs_mf.getRef().getRef();

        if (entryFileId == null) {
            log.error("Registry Entry File id is null.");
            return null;
        }

        DcsFile file = null;

        for (DcsFile df : dcp.getFiles()) {
            if (df.getId() != null && df.getId().equals(entryFileId)) {
                file = df;
                break;
            }
        }
        
        if (file == null) {
            log.error("Unable to find registry entry file.");
            return null;
        }
        
        try {
            final String source = file.getSource();
            Resource r = ResourceResolverUtil.resolveFileSource(source);
            InputStream is = r.getInputStream();
            result = (BasicRegistryEntryImpl<T>) deserializeEntryDetails(is);
            
            is.close();
        } catch (Exception e) {
            log.error("Exception reading registry entry source {}: {}",
                    new Object[] { (file.getSource() == null) ? null : file.getSource(), e });
            return null;
        }
        
        try {
            T object = deserializeObjectState(dcp);
            result.setEntry(object);
        } catch (IOException e) {
            log.error("Exception deserializing object: " + e.getMessage(), e);
            return null;
        }        
       
        return result;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Serializes a <code>BasicRegistryEntry</code> to a <code>Dcp</code> according to a profile.  The RegistryEntry is represented
     * as a single Deliverable Unit with a single Manifestation and File.  Elements specific to the profile should be created and populated
     * by the object's specific mapper.  The object is serialized as a bytestream, stored
     * in a temporary file, and a File is created and named according to the profile.
     * Gotcha: the returned <code>Dcp</code> is not managed in any way (unlike the way SIPs are managed upon ingest).
     * This means that serialized bytestream (the 'source' of the File) may not be available if this Dcp is moved from
     * system to system.
     * 
     * @param domainObject {@inheritDoc}
     * @return {@inheritDoc}
     * @throws IllegalArgumentException if <code>domainObject</code> is null.*/
    @Override
    final public Dcp to(RegistryEntry<T> registryEntry, Map<String, Object> context) {
        if (registryEntry == null) {
            throw new IllegalArgumentException("Domain Object must not be null.");
        }

        //TODO: Determine if one or both of these are going to be required to be considered valid
        /*
        if (registryEntry.getId() == null || registryEntry.getId().trim().equals("")) {
            return null;
        }
        
        if (registryEntry.getKeys() == null || registryEntry.getKeys().isEmpty()) {
            return null;
        }
        */
   
        final DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(UUID.randomUUID().toString());

        du.setTitle(registryEntry.getDescription() == null ? "Title Not Supplied" : registryEntry.getDescription());
        du.setType(REGISTRY_ENTRY_DU_TYPE);
        du.addFormerExternalRef(registryEntry.getId());
        du.setDigitalSurrogate(false);
        
        for (String key : registryEntry.getKeys()) {
            du.addAlternateId(new DcsResourceIdentifier("", key, registryEntry.getType()));
        }
        
        final DcsManifestation man = new DcsManifestation();
        man.setDateCreated(DateUtility.toIso8601(Calendar.getInstance().getTimeInMillis()));
        man.setType(REGISTRY_ENTRY_MAN_TYPE);
        final DcsManifestationFile mf = new DcsManifestationFile();
        final DcsFormat format = new DcsFormat();
        DcsFile entry_file = new DcsFile();

        man.setId(UUID.randomUUID().toString());
        entry_file.setId(UUID.randomUUID().toString());

        man.setDeliverableUnit(du.getId());
        mf.setRef(new DcsFileRef(entry_file.getId()));
        man.addManifestationFile(mf);

        try {
            File outfile = null;
            if (getBaseDirectory() == null) {
                outfile = File.createTempFile("registryEntryMap", null);
                outfile.deleteOnExit();
            } else {
                outfile = File.createTempFile("registryEntryMap", null, getBaseDirectory());
            }
            entry_file.setSource(outfile.toURI().toURL().toExternalForm());
            entry_file.setExtant(true);
            entry_file.setName("java-entry-serialization");
            format.setFormat("application/xml");
            format.setSchemeUri("http://www.iana.org/assignments/media-types/");
            entry_file.addFormat(format);
            
            FileOutputStream fos = new FileOutputStream(outfile);
            serializeEntryDetails(registryEntry, fos);
            fos.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            //If we threw an exception serializing the object just exit.
            return null;
        }
        
        try {
            String xml = xstream.toXML(registryEntry);
            entry_file.setSizeBytes(xml.getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        
        final Dcp dcp = new Dcp();
        dcp.addDeliverableUnit(du);
        dcp.addManifestation(man);
        dcp.addFile(entry_file);   
        
        try {
            serializeObjectState(registryEntry.getEntry(), dcp);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            //If we threw an exception serializing the object just exit.
            return null;
        }

        DcsDeliverableUnit objectDu = retrieveObjectDu(dcp);
       
        if (objectDu != null) {
            DcsRelation isRegistryEntryFor = new DcsRelation(DcsRelationship.IS_REGISTRY_ENTRY_FOR, objectDu.getId());
            du.addRelation(isRegistryEntryFor);
            
            DcsRelation hasRegistryEntry = new DcsRelation(DcsRelationship.HAS_REGISTRY_ENTRY, du.getId());
            objectDu.addRelation(hasRegistryEntry);   
        }
        
        return dcp;
    }

    /**
     * If {@code baseDirectory} is not null, serializations will be persisted under this directory, and
     * will not be marked for deletion when the JVM exits.
     */
    public File getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * If {@code baseDirectory} is not null, serializations will be persisted under this directory, and
     * will not be marked for deletion when the JVM exits.
     */
    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    private void serializeEntryDetails(RegistryEntry<T> entry, OutputStream os)
            throws IOException {
        xstream.toXML(entry, os);  
    }
    
    private RegistryEntry<T> deserializeEntryDetails(InputStream is) 
            throws IOException {
        return (BasicRegistryEntryImpl<T>) xstream.fromXML(is);
    }
    
    /**
     * Map the object to the supplied dcs objects. All objects will be empty outside of having an id provided.
     * 
     * @param object The object to be serialized 
     * @param du The dcsDeliverableUnit that will represent the head of the objects graph
     * @param file The dcs_file that will represent the serialized object state.
     * @param man The manifestation for the dcsDeliverableUnit that will link to the file.
     */
    protected abstract void serializeObjectState(T object, Dcp dcp)
            throws IOException;

    /**
     * Unmap the given dcp into an object representation
     * 
     * @param Dcp dcp - The dcp to determine if it contains the object
     * @throws IOException
     */
    protected abstract T deserializeObjectState(Dcp dcp)
            throws IOException;
    
    /**
     * Retrieves the root du for the object graph given the current dcp.
     * @param dcp The dcp to find the object du in.
     * @return The object deliverable unit or null if non can be found.
     */
    protected abstract DcsDeliverableUnit retrieveObjectDu(Dcp dcp);
    
}