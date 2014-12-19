package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.BagUtil;

/**
 * Executes clean up operations after a successful ingest.  If an ingest terminates early - either by user cancellation
 * or due to an exceptional condition - other objects will be responsible for cleanup.
 *
 * @see BagItDepositManagerImpl
 * @see StatefulBootstrapImpl
 */
public class CleanupIngestService extends BaseIngestService {

    /**
     * Recursively deletes the <em>deposit directory</em>.
     * <p/>
     * If the <em>extract directory</em> is {@code /tmp/package-extraction}, and a package is ingested with deposit
     * identifier {@code 1234}, then the <em>deposit directory</em> will be {@code /tmp/package-extraction/1234}.  If
     * the package is named {@code sample bag.tar.gz}, the tar.gz will be extracted to the file
     * {@code /tmp/package-extraction/1234/sample bag.tar}, and the files in that tar will be extracted to the directory
     * {@code /tmp/package-extraction/1234/sample bag/}.  This method recursively deletes the <em>deposit directory</em>
     * {@code /tmp/package-extraction/1234}.
     *
     * @param depositId the deposit identifier, must not be empty or {@code null}
     * @param state the state associated with identified deposit, must not be {@code null}, and must have its components
     *              set
     * @throws StatefulIngestServiceException
     */
    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);

        BagUtil.deleteDepositDirectory(depositId, state.getPackage());
    }

}
