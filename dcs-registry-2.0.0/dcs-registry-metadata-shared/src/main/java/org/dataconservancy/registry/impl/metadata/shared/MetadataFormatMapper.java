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
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.support.DcpUtil;
import org.dataconservancy.registry.api.support.BasicRegistryEntryMapper;
import org.dataconservancy.registry.api.support.ResourceResolverUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant.METADATAFORMAT_REGISTRY_ENTRY_TYPE;
import static org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant.METADATAFORMAT_VERSION_ONE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import org.springframework.core.io.Resource;

/**
 *
 */
public class MetadataFormatMapper extends BasicRegistryEntryMapper<DcsMetadataFormat> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MetadataSchemeMapper schemeMapper;

    private final XStream xstream;
    
    public MetadataFormatMapper(MetadataSchemeMapper schemeMapper) {
        final QNameMap qnames = new QNameMap();
        
        final String defaultnsUri = "http://dataconservancy.org/schemas/dcp/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        xstream = new XStream(driver);
        xstream.setMode(XStream.NO_REFERENCES);
        
        // XStream converter, alias, and QName registrations
        xstream.alias(MetadataFormatConverter.E_FORMAT, DcsMetadataFormat.class);
        xstream.registerConverter(new MetadataFormatConverter());
        qnames.registerMapping(new QName(defaultnsUri, MetadataFormatConverter.E_FORMAT), DcsMetadataFormat.class);
        
        this.schemeMapper = schemeMapper;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DcsMetadataFormat deserializeObjectState(Dcp dcp)
            throws IOException {
        DcsDeliverableUnit du = retrieveObjectDu(dcp);
        if (du == null) {
            log.error("Could not find DcsMetadataFormat du");
            return null;
        }
        DcsManifestation man = null;

        for (DcsManifestation candidate : dcp.getManifestations()) {
            if (candidate.getDeliverableUnit().equals(du.getId())) {
                man = candidate;
                break;
            }
        }

        DcsFile file = (DcsFile) DcpUtil.asMap(dcp).get(man.getManifestationFiles().iterator().next().getRef().getRef());

        Resource fileSource = ResourceResolverUtil.resolveFileSource(file.getSource());
        InputStream is = fileSource.getInputStream();
        DcsMetadataFormat format = (DcsMetadataFormat) xstream.fromXML(is);
        
        if (format == null) {
            log.error("Error deserializing DcsMetadataFormat serialization.");
            format = new DcsMetadataFormat();
            format.setName(du.getTitle());
            format.setVersion(METADATAFORMAT_VERSION_ONE);
            if (!du.getFormerExternalRefs().isEmpty()) {
                format.setId(du.getFormerExternalRefs().iterator().next());
            }
        }

        //Retrieve all the mapper object graphs from the scheme mapper
        Set<String> schemeIds = schemeMapper.discover(dcp);
        
        //Add the master scheme id first
        String masterSchemeId = schemeMapper.findMasterScheme(dcp);
        DcsMetadataScheme masterScheme = schemeMapper.from(masterSchemeId, dcp, null);
        format.addScheme(masterScheme);
        
        //Remove the master scheme id from the list of schemes.
        schemeIds.remove(masterSchemeId);
        
        //Loop through all the remaining scheme ids to add all the schemes to the format
        for (String id : schemeIds) {
            DcsMetadataScheme scheme = schemeMapper.from(id, dcp, null);
            format.addScheme(scheme);
        }
        return format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void serializeObjectState(DcsMetadataFormat object, Dcp dcp)
            throws IOException {

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(UUID.randomUUID().toString());
        du.setType(METADATAFORMAT_REGISTRY_ENTRY_TYPE);
        du.setTitle(object.getName());
        du.addFormerExternalRef(object.getId());
        
        DcsFile file = new DcsFile();
        file.setId(UUID.randomUUID().toString());

        final DcsFormat format = new DcsFormat();
        try {

            File outFile = null;
            if (baseDirectory == null) {
                outFile = File.createTempFile("serializedFormat", null);
                outFile.deleteOnExit();
            } else {
                outFile = File.createTempFile("serializedFormat", null, baseDirectory);
            }

            file.setSource(outFile.toURI().toURL().toExternalForm());
            file.setExtant(true);
            file.setName("java-format-serialization");
            format.setFormat("application/xml");
            format.setSchemeUri("http://www.iana.org/assignments/media-types/");
            file.addFormat(format);
            
            FileOutputStream fos = new FileOutputStream(outFile);
            xstream.toXML(object, fos);
            fos.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);           
        }
        

        DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(file.getId()));
        mf.setPath("/");

        DcsManifestation man = new DcsManifestation();
        man.setId(UUID.randomUUID().toString());
        man.setDeliverableUnit(du.getId());
        man.addManifestationFile(mf);
        man.addTechnicalEnvironment(METADATAFORMAT_REGISTRY_ENTRY_TYPE + ":" + METADATAFORMAT_VERSION_ONE);

        dcp.addEntity(du, file, man);
        
        String parentId = "";
        
        for (DcsMetadataScheme scheme : object.getSchemes()) {
            if (parentId.isEmpty()) {
                Dcp schemeDcp = schemeMapper.to(scheme, null, parentId, du.getId());
                if (!schemeDcp.getDeliverableUnits().isEmpty()) {
                    DcsDeliverableUnit schemeDu = schemeDcp.getDeliverableUnits().iterator().next();
                    parentId = schemeDu.getId();
                }
                DcpUtil.add(dcp, DcpUtil.asList(schemeDcp));
            } else {
                Dcp schemeDcp = schemeMapper.to(scheme, null, parentId, du.getId());            
                DcpUtil.add(dcp, DcpUtil.asList(schemeDcp));
            }
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DcsDeliverableUnit retrieveObjectDu(Dcp dcp) {
        DcsDeliverableUnit objectDu = null;
        
        Set<String> objectIds = discover(dcp);
        String objectId = null;
        Iterator<String> idIter = objectIds.iterator();
        if (idIter.hasNext()) {
            objectId = idIter.next();
        }    
        
        if (objectId != null) {
            for (DcsDeliverableUnit du : dcp.getDeliverableUnits()) {
                if (du.getId().equals(objectId)) {
                    objectDu = du;
                    break;
                }
            }
        }
        
        return objectDu;
    }
    

    @Override
    public Set<String> discover(Dcp conformingPackage) {
        Collection<DcsDeliverableUnit> dus = conformingPackage.getDeliverableUnits();
        Set<String> archiveIds = new HashSet<String>();
        for (DcsDeliverableUnit du : dus) {
            if (du.getType().equals(METADATAFORMAT_REGISTRY_ENTRY_TYPE)) {
                archiveIds.add(du.getId());
            }
        }

        return archiveIds;
    }

}
