
1. This repository contains all the dcs modules needed to build SEAD VA.
2. All modules are built and deployed into SEAD maven repository.
3. To build this source, follow this build order.

        <module>parent</module>
        <module>project</module>
        <module>supplemental-resources</module>
        <module>model</module>
        <module>common-services</module>
        <module>dc-deposit</module>
        <module>dcs-archive</module>
        <module>dcs-index</module>
        <module>dcs-query</module>
        <module>dcs-access</module>
        <module>dcs-profile</module>
        <module>dcs-clients</module>
        <module>dcs-registry</module>
        <module>dcs-transform</module>
        <module>dcs-ingest</module>
        <module>dcs-registry-2.0.0</module>
        <module>dcs-lineage</module>
        <module>storage-dropbox</module>
        <module>dcs-pkg-ui-shared</module>
        <module>dcs-packaging</module>
        <module>dcs-mhf</module>
        <module>dcs-integration</module>
        <module>dcs-ui</module>

4. We have changed the distributionManagement section in the parent pom.
5. After building this, we have to build sead/DataConservencyCode modules and deploy them. That is 
because there are some patches applied to those modules which are needed in SEAD VA code.
