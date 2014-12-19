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
package org.dataconservancy.registry.impl.metadata.shared;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dataconservancy.model.builder.xstream.DcsPullDriver;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.support.DcpUtil;
import org.dataconservancy.registry.api.support.ResourceResolverUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import org.springframework.core.io.Resource;

import static org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE;
import static org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant.METADATASCHEME_VERSION_ONE;

/**
 * Maps DcsMetadataScheme objects to and from DCP.
 */
public class MetadataSchemeMapper {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final static String SCHEME_SERIALIZATION_MAN_TYPE = "SchemeSerializationManifestation";
    private final static String SCHEME_FILE_MAN_TYPE = "SchemeFileManifestation";

    /**
     * If {@code baseDirectory} is not null, serializations will be persisted under this directory, and
     * will not be marked for deletion when the JVM exits.
     */
    private File baseDirectory;

    private AtomicInteger idPart = new AtomicInteger(0);
    private final XStream xstream;
    
    public MetadataSchemeMapper() {
        final QNameMap qnames = new QNameMap();
        
        final String defaultnsUri = "http://dataconservancy.org/schemas/dcp/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        xstream = new XStream(driver);
        xstream.setMode(XStream.NO_REFERENCES);
        
        // XStream converter, alias, and QName registrations
        xstream.alias(MetadataSchemeConverter.E_SCHEME, DcsMetadataScheme.class);
        xstream.registerConverter(new MetadataSchemeConverter());
        qnames.registerMapping(new QName(defaultnsUri, MetadataSchemeConverter.E_SCHEME), DcsMetadataScheme.class);
    }

    public Set<String> discover(Dcp conformingPackage) {
        Collection<DcsDeliverableUnit> dus = conformingPackage.getDeliverableUnits();
        Set<String> archiveIds = new HashSet<String>();
        for (DcsDeliverableUnit du : dus) {
            if (du.getType().equals(METADATASCHEME_REGISTRY_ENTRY_TYPE)) {
                archiveIds.add(du.getId());
            }
        }

        return archiveIds;
    }

    public String findMasterScheme(Dcp conformingPackage) {
        Collection<DcsDeliverableUnit> dus = conformingPackage.getDeliverableUnits();
        String masterSchemeId = "";
        for (DcsDeliverableUnit du : dus) {
            if (du.getType().equals(METADATASCHEME_REGISTRY_ENTRY_TYPE) && du.getParents().size() == 1) {
                masterSchemeId = du.getId();
                break;
            }
        }

        return masterSchemeId;
    }
    public Dcp to(DcsMetadataScheme scheme, Map<String, Object> context, String masterId, String formatId) {
        Dcp dcp = new Dcp();

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(nextId());
        du.setType(METADATASCHEME_REGISTRY_ENTRY_TYPE);
        du.addFormerExternalRef(scheme.getSchemaUrl());
        du.setTitle(scheme.getName());

        if (!masterId.isEmpty()) {
            du.addParent(new DcsDeliverableUnitRef(masterId));
        }
        
        if (!formatId.isEmpty()) {
            du.addParent(new DcsDeliverableUnitRef(formatId));
        }
        
        DcsFile file = new DcsFile();
        file.setId(nextId());
        file.setSource(scheme.getSource());
        file.setName("schema-source");
        file.setExtant(true);

        DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(file.getId()));
        mf.setPath("/");

        DcsManifestation man = new DcsManifestation();
        man.setId(nextId());
        man.setDeliverableUnit(du.getId());
        man.addManifestationFile(mf);
        man.setType(SCHEME_FILE_MAN_TYPE);
        man.addTechnicalEnvironment(METADATASCHEME_REGISTRY_ENTRY_TYPE + ":" + METADATASCHEME_VERSION_ONE);
        
        DcsFile state_file = new DcsFile();
        state_file.setId(nextId());

        final DcsFormat format = new DcsFormat();
        try {
            File outfile = null;
            if (baseDirectory == null) {
                outfile = File.createTempFile("serializedScheme", null);
                outfile.deleteOnExit();
            } else {
                outfile = File.createTempFile("serializedScheme", null, baseDirectory);
            }

            state_file.setSource(outfile.toURI().toURL().toExternalForm());
            state_file.setExtant(true);
            state_file.setName("java-scheme-serialization");
            format.setFormat("application/xml");
            format.setSchemeUri("http://www.iana.org/assignments/media-types/");
            state_file.addFormat(format);
            
            FileOutputStream fos = new FileOutputStream(outfile);
            xstream.toXML(scheme, fos);
            fos.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);           
        }
        

        DcsManifestationFile state_mf = new DcsManifestationFile();
        state_mf.setRef(new DcsFileRef(state_file.getId()));
        state_mf.setPath("/");

        DcsManifestation state_man = new DcsManifestation();
        state_man.setId(nextId());
        state_man.setDeliverableUnit(du.getId());
        state_man.addManifestationFile(state_mf);
        state_man.setType(SCHEME_SERIALIZATION_MAN_TYPE);
        state_man.addTechnicalEnvironment(METADATASCHEME_REGISTRY_ENTRY_TYPE + ":" + METADATASCHEME_VERSION_ONE);


        dcp.addEntity(du, file, man, state_file, state_man);

        return dcp;
    }

    public DcsMetadataScheme from(String identifier, Dcp dcp, Map<String, Object> context) {
        DcsDeliverableUnit du = (DcsDeliverableUnit) DcpUtil.asMap(dcp).get(identifier);
        DcsManifestation man = null;
        DcsManifestation serializationMan = null;
        
        for (DcsManifestation candidate : dcp.getManifestations()) {
            if (candidate.getDeliverableUnit().equals(du.getId()) && candidate.getType().equals(SCHEME_FILE_MAN_TYPE)) {
                man = candidate;
                break;
            }
        }
          
        for (DcsManifestation candidate : dcp.getManifestations()) {
            if (candidate.getDeliverableUnit().equals(du.getId()) && candidate.getType().equals(SCHEME_SERIALIZATION_MAN_TYPE)) {
                serializationMan = candidate;
                break;
            }
        }
        
        DcsFile serializationFile = (DcsFile) DcpUtil.asMap(dcp).get(serializationMan.getManifestationFiles().iterator().next().getRef().getRef());
        
        DcsMetadataScheme scheme = null;
        InputStream is = null;
        try {
            Resource r = ResourceResolverUtil.resolveFileSource(serializationFile.getSource());
            is = r.getInputStream();

            scheme = (DcsMetadataScheme) xstream.fromXML(is);
        } catch (Exception e) {
            log.error("Error deserializing metadata scheme: " + e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        
        if (scheme == null) {
            scheme = new DcsMetadataScheme();
            scheme.setName(du.getTitle());
            scheme.setSchemaVersion(METADATASCHEME_VERSION_ONE);
        }
        
        DcsFile file = (DcsFile) DcpUtil.asMap(dcp).get(man.getManifestationFiles().iterator().next().getRef().getRef());
       
        scheme.setSource(file.getSource());
        scheme.setSchemaUrl(du.getFormerExternalRefs().iterator().next());
        return scheme;
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

    private String nextId() {
        return String.valueOf(idPart.getAndIncrement());
    }

}
