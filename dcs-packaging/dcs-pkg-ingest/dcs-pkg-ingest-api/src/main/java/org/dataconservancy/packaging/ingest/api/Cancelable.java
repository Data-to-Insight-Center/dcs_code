package org.dataconservancy.packaging.ingest.api;

/**
 * Deposit managers implementing this interface support cancellation of executing deposits in some fashion.
 */
public interface Cancelable {

    /**
     * Indicates that the processes and resources for the supplied deposit identifier are no longer needed
     * and should be stopped and cleaned up as soon as possible.
     *
     * @param depositId the deposit identifier
     */
    public void cancel(String depositId);

}
