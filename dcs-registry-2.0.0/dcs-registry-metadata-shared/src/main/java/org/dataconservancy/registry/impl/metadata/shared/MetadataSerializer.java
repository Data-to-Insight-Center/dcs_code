package org.dataconservancy.registry.impl.metadata.shared;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public interface MetadataSerializer<T> {

    public T deserialize(InputStream in);

    public void serialize(T object, OutputStream out);

}
