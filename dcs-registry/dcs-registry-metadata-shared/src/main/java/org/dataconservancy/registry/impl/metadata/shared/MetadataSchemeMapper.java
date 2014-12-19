package org.dataconservancy.registry.impl.metadata.shared;

import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.support.DcpUtil;
import org.dataconservancy.profile.api.DcpMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE;
import static org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant.METADATASCHEME_VERSION_ONE;

/**
 * Maps DcsMetadataScheme objects to and from DCP.
 */
public class MetadataSchemeMapper implements DcpMapper<DcsMetadataScheme> {

    private AtomicInteger idPart = new AtomicInteger(0);

    public MetadataSchemeMapper() {

    }

    @Override
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

    @Override
    public Dcp to(DcsMetadataScheme domainObject, Map<String, Object> context) {
        Dcp dcp = new Dcp();

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(nextId());
        du.setType(METADATASCHEME_REGISTRY_ENTRY_TYPE);
        du.addFormerExternalRef(domainObject.getSchemaUrl());
        du.setTitle(domainObject.getName());

        DcsFile file = new DcsFile();
        file.setId(nextId());
        file.setSource(domainObject.getSource());
        file.setName("schema-source");

        DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(file.getId()));
        mf.setPath("/");

        DcsManifestation man = new DcsManifestation();
        man.setId(nextId());
        man.setDeliverableUnit(du.getId());
        man.addManifestationFile(mf);
        man.addTechnicalEnvironment(METADATASCHEME_REGISTRY_ENTRY_TYPE + ":" + METADATASCHEME_VERSION_ONE);

        dcp.addEntity(du, file, man);

        return dcp;
    }

    @Override
    public DcsMetadataScheme from(String identifier, Dcp dcp, Map<String, Object> context) {
        DcsDeliverableUnit du = (DcsDeliverableUnit) DcpUtil.asMap(dcp).get(identifier);
        DcsManifestation man = null;

        for (DcsManifestation candidate : dcp.getManifestations()) {
            if (candidate.getDeliverableUnit().equals(du.getId())) {
                man = candidate;
                break;
            }
        }

        DcsFile file = (DcsFile) DcpUtil.asMap(dcp).get(man.getManifestationFiles().iterator().next().getRef().getRef());

        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName(du.getTitle());
        scheme.setSchemaVersion(METADATASCHEME_VERSION_ONE);
        scheme.setSource(file.getSource());
        scheme.setSchemaUrl(du.getFormerExternalRefs().iterator().next());
        return scheme;
    }

    private String nextId() {
        return String.valueOf(idPart.getAndIncrement());
    }

}
