package org.dataconservancy.registry.impl.metadata.shared;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public interface MetadataSchemeSerializer {


    public DcsMetadataScheme deserialize(InputStream in);

    public void serialize(DcsMetadataScheme metadataScheme, OutputStream out);

}
