package org.dataconservancy.packaging.ingest.shared;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MimeDetector;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

/**
 * Utility class supporting various constants and utility methods for operating on bags, and bag storage.
 */
public class BagUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BagUtil.class);

    /**
     * A string representing BagIt version 0.97
     */
    public static final String BAGIT_VERSION_097 = "0.97";

    /**
     * A <em>relative</em> file representing the {@code baginfo.txt} file.
     */
    public static final File BAGINFO_TXT = new File("baginfo.txt");

    /**
     * A <em>relative</em> file representing the {@code bagit.txt} file.  {@code bagit.txt} is a required file.
     */
    public static final File BAGIT_TXT = new File("bagit.txt");

    /**
     * A <em>relative</em> file representing the {@code data/} directory.  {@code data/} is a required directory.
     */
    public static final File DATA_DIR = new File("data");

    /**
     * Derives the <em>relative</em> base directory of a Bag provided a deposit identifier and a File representing the
     * Bag.  Including the deposit identifier in the generation of a base directory helps insure that the base directory
     * will be unique.  Because the returned file is relative, it is not guaranteed to exist;
     * {@link java.io.File#isDirectory()} may return {@code false}.
     *
     * @param depositId the unique deposit identifier associated with the bag
     * @param bagFile the File representing the Bag.  It doesn't need to exist.
     * @return a File representing the <em>relative</em> base directory of the bag.  The returned File is not guaranteed
     *         to exist.
     */
    public static File deriveBaseDirectory(String depositId, File bagFile) {
        File depositFile = sanitizeStringForFile(depositId);
        String bagName = detectAndStripExtensions(bagFile).getName();

        // TODO: Is the result of File.getName() ever going to contain File.pathSeparator or File.separator?  If not, throw these loops out...
        while (bagName.contains(File.pathSeparator)) {
            bagName = bagName.substring(bagFile.getName().indexOf(File.pathSeparator) + 1);
        }

        while (bagName.contains(File.separator)) {
            bagName = bagName.substring(bagFile.getName().indexOf(File.separator) + 1);
        }

        return new File(depositFile, bagName);
    }

    /**
     * Derives a <em>relative</em> File from a string.  Essentially this method will sanitize the supplied string (e.g.
     * a deposit identifier), removing any special characters, making it suitable for using a directory or file name.
     * If the same deposit identifier is supplied to this method multiple times, the same {@code File} will be returned.
     * Because the returned file is relative, it isn't guaranteed to exist.
     *
     * @param depositId a deposit identifier
     * @return a <em>relative</em> File derived from the deposit identifier, not guaranteed to exist
     */
    public static File sanitizeStringForFile(String depositId) {
        StringBuilder fileName = new StringBuilder();
        for (int i = 0; i < depositId.length(); i++) {
            Character c = depositId.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                fileName.append(c);
            }
        }

        return new File(fileName.toString().toLowerCase());
    }

    /**
     * Strips the last extension (and period) off of a file name.  For example: {@code foo.tar.gz} becomes
     * {@code foo.tar}, and passing in {@code foo.tar} becomes {@code foo}.  If a file with no extension is passed in,
     * the method simply returns the file.  For example, passing in {@code foo} will return {@code foo}.
     *
     * @param toStrip the File object to strip the extension off, must not be null
     * @return a File with the last extension stripped off, or the same file if no extension is found
     * @throws IllegalArgumentException if the supplied file is null
     */
    static File stripLastExtension(File toStrip) {
        if (toStrip == null) {
            throw new IllegalArgumentException("The supplied file must not be null.");
        }

        if (!toStrip.getName().contains(".")) {
            return toStrip;
        }

        final String[] parts = toStrip.getName().split("\\.");

        final StringBuilder strippedName = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            strippedName.append(parts[i]);
            // If we aren't on the last extension in the array, append a "."
            if ((parts.length - 2) != i) {
                strippedName.append(".");
            }
        }

        // This will be the file stripped of its last extension
        // For example, if 'toStrip' is 'foo.tar.gz', 'strippedName' will be 'foo.tar'
        return new File(strippedName.toString());
    }

    /**
     * Detects all file extensions on the supplied File, strips them off, and returns a File without any file extension,
     * including periods.  This method differentiates between a file extension and a portion of a file name that
     * contains periods.  For example: if {@code foo.tar.gz} is passed in, {@code foo} will be returned, as expected.
     * But if {@code foo.bar.tar.gz} is passed in, {@code foo.bar} will be returned, because {@code bar} is not a
     * recognized file extension.
     *
     * @param toStrip the file to strip extensions off of, must not be null
     * @return the file, without extensions
     * @throws IllegalArgumentException if {@code toStrip} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static File detectAndStripExtensions(final File toStrip) {
        if (toStrip == null) {
            throw new IllegalArgumentException("The file to strip must not be null.");
        }

        // Avoid noisy DEBUG messages by only registering the ExtensionMimeDetector once
        MimeDetector extDetector;

        // Not sure if registering a MimeDetector is thread-safe or not
        synchronized (MimeUtil.class) {
            extDetector = MimeUtil.getMimeDetector(ExtensionMimeDetector.class.getName());
            if (extDetector == null) {
                MimeUtil.registerMimeDetector(ExtensionMimeDetector.class.getName());
                extDetector = MimeUtil.getMimeDetector(ExtensionMimeDetector.class.getName());
            }
        }

        Collection<MimeType> types = extDetector.getMimeTypes(toStrip);

        for (MimeType t : types) {
            LOG.trace("Detected initial type {} for file name {}", t, toStrip);
        }

        File stripped = new File(toStrip.getName());
        while (types.size() > 0) {
            stripped = stripLastExtension(stripped);
            types = extDetector.getMimeTypes(stripped);
            for (MimeType t : types) {
                LOG.trace("Detected type {} for file name {}", t, stripped);
            }
        }

        LOG.debug("Original file '{}' was stripped of known extensions to '{}'", toStrip, stripped);
        return stripped;
    }

    /**
     * Attempts to delete the directory that the Package was extracted to.  If deletion fails, a message is logged.
     * <p/>
     * This method deletes the files that were extracted from a Package, but it doesn't delete any intermediate
     * package files, or the originally uploaded package file.  For example, if a user uploads the file
     * {@code package.tar.gz}, the system will extract the intermediate file {@code package.tar}, and then extract
     * the files from {@code package.tar}.  This method will delete the files extracted from {@code package.tar}, but
     * it won't delete {@code package.tar} or {@code package.tar.gz}.
     * See the {@link #deleteDepositDirectory} method, which would delete intermediate files like {@code package.tar}.
     *
     * @param thePackage the Package whose filesystem serialization is being deleted
     * @return true if the files were successfully deleted, false otherwise
     */
    public static boolean deleteExtractedFiles(org.dataconservancy.packaging.model.Package thePackage) {
        final String msg = "Unable to remove package extraction directory: ";

        if (thePackage == null || thePackage.getSerialization() == null) {
            LOG.debug(msg + "the Package or its Serialization was null.");
            return false;
        }

        final File extractDir = thePackage.getSerialization().getExtractDir();
        final File baseDir = thePackage.getSerialization().getBaseDir();

        if (extractDir == null) {
            LOG.debug(msg + "extract directory was null.");
            return false;
        }

        if (baseDir == null) {
            LOG.debug(msg + "base directory was null.");
            return false;
        }

        if (!extractDir.isAbsolute()) {
            LOG.debug(msg + "extract directory '" + extractDir + "' is not absolute.");
            return false;
        }

        final File absPackageDir = new File(extractDir, baseDir.getPath());

        if (!absPackageDir.exists()) {
            LOG.debug(msg + "'" + absPackageDir + "' doesn't exist.");
            return false;
        }

        if (!FileUtils.deleteQuietly(absPackageDir)) {
            LOG.warn(msg + "'" + absPackageDir + "'");
            return false;
        }

        return true;
    }

    /**
     * Attempts to delete the directory that was created for a specific deposit.  This method should not be called
     * until after a deposit has completed.  If deletion fails, a message will be logged.
     * <p/>
     * This method differs from {@link #deleteExtractedFiles(org.dataconservancy.packaging.model.Package)} by deleting
     * the directory allocated to the {@code depositId}.  Deleting this directory would remove any intermediate
     * package files <em>and</em> any extracted files from the package.  If a package is comprised of multiple bag
     * files, this method would delete all of those files.
     *
     * @param thePackage the Package whose filesystem serialization is being deleted
     * @return true if the files were successfully deleted, false otherwise
     */
    public static boolean deleteDepositDirectory(String depositId,
                                                 org.dataconservancy.packaging.model.Package thePackage) {
        final String msg = "Unable to remove deposit directory for deposit ID '" + depositId + "': ";

        if (thePackage == null || thePackage.getSerialization() == null) {
            LOG.debug(msg + "the Package or its Serialization was null.");
            return false;
        }

        if (depositId == null || depositId.trim().length() == 0) {
            LOG.debug(msg + "deposit id was null or empty");
        }

        final File extractDir = thePackage.getSerialization().getExtractDir();

        if (extractDir == null) {
            LOG.debug(msg + "extract directory was null.");
            return false;
        }

        if (!extractDir.isAbsolute()) {
            LOG.debug(msg + "extract directory '" + extractDir + "' is not absolute.");
            return false;
        }

        final File depositDir = new File(extractDir, sanitizeStringForFile(depositId).getPath());

        if (!depositDir.exists()) {
            LOG.debug(msg + "'" + depositDir + "' doesn't exist.");
            return false;
        }

        if (!FileUtils.deleteQuietly(depositDir)) {
            LOG.warn(msg + "'" + depositDir + "'");
            return false;
        }

        return true;
    }

}
