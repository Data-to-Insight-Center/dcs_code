package org.dataconservancy.registry.http;

import java.net.URL;
import java.util.Set;

/**
 * Holds a Set of URL references to individual entries, or to the registry itself.  The purpose of this class is to
 * provide a place for a HTTP response code to be associated with a Set of references.  In some cases, the response code
 * should be 200, in other cases it should be 300.  The RegistryController will set the proper status code and place an
 * instance of this in the model.
 */
class ReferencesHolder {

    /** The HTTP status code to be used in the response */
    int statusCode;

    /** The URL references to serialize in the response */
    Set<URL> references;

}
