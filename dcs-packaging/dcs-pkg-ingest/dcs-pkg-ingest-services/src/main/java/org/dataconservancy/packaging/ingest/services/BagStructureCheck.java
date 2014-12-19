package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.BagUtil;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.packaging.model.PackageSerialization;

import java.io.File;

/**
 * Performs a minimal sanity check regarding the structure of the extracted Bag.
 * This class verifies:
 * <ol>
 *     <li>That {@code extractDir} exists and can be read</li>
 *     <li>That {@code baseDir} exists and can be read</li>
 *     <li>That the {@code data/} directory of the bag exists directly below {@code baseDir}, and can be read</li>
 *     <li>That the {@code baginfo.txt} file exists directly below {@code baseDir}, and can be read</li>
 *     <li>That the {@code baginfo.txt} file exists in the Package Serialization</li>
 * </ol>
 * There are two directories this class is concerned with:
 * <dl>
 *     <dt>extractDir</dt>
 *     <dd>The root directory that all bags will be extracted under.  Typically a systems administrator will allocate
 *         enough storage under {@code extractDir} to contain uploaded and extracted bags. This directory is always
 *         absolute.  For example: {@code /storage/bags}</dd>
 *     <dt>baseDir</dt>
 *     <dd>The directory <em>relative</em> to {@code extractDir} that a particular bag will be extracted under.
 *         Typically this will be derived from a deposit identifier and the name of the uploaded bag.  For example, if
 *         the uploaded bag is named {@code mybag.tar} and the deposit identifier is {@code http://localhost/deposit/1},
 *         then an example {@code baseDir} would be {@code 1/mybag}</dd>
 * </dl>
 *
 * @see <a href="https://datatracker.ietf.org/doc/draft-kunze-bagit/?include_text=1">The BagIt File Packaging Format (V0.97)</a>
 */
public class BagStructureCheck extends BaseIngestService {

    private static final String FAILURE_MESSAGE = "Ingest (id: %s) failed, invalid bag structure: %s";

    private static final String NON_EXISTENT_FILE_OR_CANT_READ = "%s does not exist or cannot be read.";

    private static final String NOT_A_DIRECTORY = "%s is not a directory.";

    private static final String PACKAGE_SERIALIZATION_MISSING_FILE = "Package is missing file %s";

    private File extractDir;

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {

        super.execute(depositId, state);

        /*
         * Expected (i.e. correct) directory structure is:
         *   /<extractDir>/<depositId>/<bag>/data
         *
         * In the above example, "<depositId>/<bag>" is the base directory.
         *
         * So if a correctly formed bag named 'bag.tar' is uploaded to the system, it will be exploded into
         * /<extractDir>/<depositId> (where <depositId> is a unique directory derived from the deposit id), resulting
         * in /<extractDir>/<depositId>/bag begin created:
         * <pre>
         *     <extractDir>/<depositId>/bag
         *    |-- bagit.txt
         *    `-- data/
         *
         *    1 directory, 1 file
         * </pre>
         *
         */

        final Package thePackage = state.getPackage();
        final PackageSerialization serialization = thePackage.getSerialization();

        // Verify the extraction directory exists and is readable.  It should be an absolute directory.
        verifyDir(depositId, state, serialization.getExtractDir());

        // Re-base the relative package base directory on the extract directory.
        final File absBaseDir = new File(serialization.getExtractDir(), serialization.getBaseDir().getPath());  // the absolute base directory

        // The absolute package base directory should exist and be readable
        verifyDir(depositId, state, absBaseDir);

        // Create absolute File instances for the required 'bagit.txt' file and the 'data/' directory
        final File baginfotxt = new File(absBaseDir, BagUtil.BAGIT_TXT.getName());
        final File datadir = new File(absBaseDir, BagUtil.DATA_DIR.getName());

        // Verify that the 'bagit.txt' and 'data/' directory are present and can be read
        verifyFile(depositId, state, baginfotxt);
        verifyDir(depositId, state, datadir);

        // The package serialization should carry the exact same 'bagit.txt' File
        if (!serialization.getFiles(false).contains(baginfotxt)) {
            fail(depositId, state, String.format(PACKAGE_SERIALIZATION_MISSING_FILE, baginfotxt));
        }

        // The PackageSerialization doesn't contain directories on their own, so we can't test for this
        if (!serialization.getFiles(false).contains(datadir)) {
            fail(depositId, state, String.format(PACKAGE_SERIALIZATION_MISSING_FILE, datadir));
        }
    }

    /**
     * The directory with an absolute path, typically allocated enough storage by a systems administrator, that all bags
     * will be extracted to.
     *
     * @return the absolute extract directory
     */
    public File getExtractDir() {
        return extractDir;
    }

    /**
     * The directory with an absolute path, typically allocated enough storage by a systems administrator, that all bags
     * will be extracted to.
     *
     * @param extractDir the absolute extract directory
     * @throws IllegalArgumentException if the extract directory is not an absolute path
     */
    public void setExtractDir(File extractDir) {
        if (!extractDir.isAbsolute()) {
            throw new IllegalArgumentException("The extraction directory must be absolute!");
        }
        this.extractDir = extractDir;
    }

    /**
     * Composes a failure message and throws a {@code StatefulIngestServiceException}, essentially failing the ingest.
     *
     * @param depositId the deposit identifier
     * @param state the state associated with the deposit attempt
     * @param message the specific reason for the failure
     * @throws StatefulIngestServiceException using the supplied message
     */
    private void fail(String depositId, IngestWorkflowState state, String message)
            throws StatefulIngestServiceException {
        throw new StatefulIngestServiceException(String.format(FAILURE_MESSAGE, depositId, message));
    }

    /**
     * Asserts that the supplied file exists and can be read.  Throws a {@code StatefulIngestServiceException} if not.
     *
     * @param depositId the deposit identifier
     * @param state the state associated with the deposit attempt
     * @param f the file to verify
     * @throws StatefulIngestServiceException if the file cannot be read or doesn't exist
     */
    private void verifyFile(String depositId, IngestWorkflowState state, File f)
            throws StatefulIngestServiceException {
        if (!f.exists()) {
            fail(depositId, state, String.format(NON_EXISTENT_FILE_OR_CANT_READ, f.getAbsolutePath()));
        }

        if (!f.canRead()) {
            fail(depositId, state, String.format(NON_EXISTENT_FILE_OR_CANT_READ, f.getAbsolutePath()));
        }
    }

    /**
     * Asserts that the supplied directory exists, can be read, and is a directory.  Throws a
     * {@code StatefulIngestServiceException} if not.
     *
     * @param depositId the deposit identifier
     * @param state the state associated with the deposit attempt
     * @param dir the directory to verify
     * @throws StatefulIngestServiceException if the directory cannot be read, doesn't exist, or is not a directory
     */
    private void verifyDir(String depositId, IngestWorkflowState state, File dir)
            throws StatefulIngestServiceException {
        verifyFile(depositId, state, dir);
        if (!dir.isDirectory()) {
            fail(depositId, state, String.format(NOT_A_DIRECTORY, dir.getAbsolutePath()));
        }
    }
}
