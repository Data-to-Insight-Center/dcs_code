package org.dataconservancy.storage.dropbox;

import static org.dataconservancy.storage.dropbox.EntryUtil.assertEqualsByValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.junit.Ignore;
import org.junit.Test;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DeltaPage;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;

public class DropboxAccessorImplTest extends DropboxBaseTest{

    @Test
    public void testTestDropboxLink() {
        Assert.assertTrue("Could not test the link to Dropbox.", underTest.testDropboxLink(token));
    }
    
    @Test
    public void testGetDelta() throws DropboxException {
        // Creating a file in prep for testing delta.
        String fileContents = "Hello World! balangdan!";
        String filePath = root + "testing.txt";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        mDBapi.putFile(filePath, inputStream, fileContents.length(), null, null);

        List<DropboxModel> models = underTest.getAllContentFromDropbox(token);
        
        // Check to see delta returns the test file that was created by testCreateFile().
        Assert.assertTrue("Expecting to have at least one file in the delta.", models.size() > 0);
        
    }
    
    /**
     * This should test whether a project is new or not. It should account for renamed projects and all the rules that
     * are mentioned in the wiki page.
     */
    @Ignore
    @Test
    public void testIsProjectNew() {
        
    }
    
    /**
     * This should test whether a collection is new or not. It should account for renamed collections and all the rules
     * that are mentioned in the wiki page.
     */
    @Ignore
    @Test
    public void testIsCollectionNew() {
        
    }
    
    /**
     * This should test whether a file (DataItem) is new or not. It should account for renamed DataItems and all the
     * rules that are mentioned in the wiki page.
     */
    @Ignore
    @Test
    public void testIsFileNew() throws DropboxException {
        
    }
    
    /*********************************************************************************
     * Tests below this section were written to evaluate Dropbox API's functionality.*
     * Currently ignored.                                                            *
     ********************************************************************************/
    @Ignore
    @Test
    public void testCreateFile() throws Exception {
        String fileContents = "Hello World! balangdan!";
        String filePath = root + "testing.txt";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        Entry newEntry = mDBapi.putFile(filePath, inputStream, fileContents.length(), null, null);
        
        // Check to see if the file was created.
        Assert.assertTrue("Failed to create the new file.", newEntry.size.length() > 0);
        Assert.assertEquals(fileContents.length(), newEntry.bytes);
        Assert.assertEquals("text/plain", newEntry.mimeType);
        Assert.assertEquals(filePath, newEntry.path);
        int index = filePath.lastIndexOf("/");
        Assert.assertEquals(filePath.substring(index + 1), newEntry.fileName());
        Assert.assertFalse(newEntry.isDir);
    }

    @Ignore
    @Test
    public void testApiGetDelta() throws Exception {
        String cursor = null;
        DeltaPage<Entry> entries = mDBapi.delta(cursor);
        cursor = (entries.cursor);

        // Check to see delta returns the test file that was created by testCreateFile().
        Assert.assertTrue("Expecting to have at least one file in the delta.", entries.entries.size() > 0);

        entries = mDBapi.delta(cursor);

        // Check to see delta returns nothing after the first run.
        Assert.assertTrue("Expecting delta to return nothing.", entries.entries.size() == 0);
    }

    /**
     * This should test the extraction of metadata from Dropbox files.
     */
    @Ignore
    @Test
    public void testFileMetadataExtraction() throws DropboxException {
        String filePath = createNewDropboxFile(UUID.randomUUID().toString());
        Entry metadataEntry = mDBapi.metadata(filePath, MAX_REVISIONS, null, true, null);
        Assert.assertEquals(filePath, metadataEntry.path);
        int index = filePath.lastIndexOf("/");
        Assert.assertEquals(filePath.substring(index + 1), metadataEntry.fileName());
        Assert.assertFalse(metadataEntry.isDir);
    }

    /**
     * This should test the extraction of metadata from Dropbox folders.
     */
    @Ignore
    @Test
    public void testFolderMetadataExtraction() throws DropboxException {
        String filePath = "/";
        Entry metadataEntry = mDBapi.metadata(filePath, MAX_REVISIONS, null, true, null);
        Assert.assertEquals(filePath, metadataEntry.path);
        Assert.assertTrue(metadataEntry.isDir);
        Assert.assertTrue(metadataEntry.contents.size() > 0);
    }

    /**
     * This is in case we need to add a hidden metadata file for any tracking purposes. So uploading a file to dropbox
     * should be tested here.
     *
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testUploadMetadata() throws DropboxException {
        String file = createNewDropboxFile(".metadata.txt");
        List<Entry> entries = mDBapi.revisions(file, MAX_REVISIONS);
        Assert.assertTrue("There are no revisions of the created file.", entries.size() > 0);
        for (Entry entry : entries) {
            Assert.assertTrue("Failed to create the new file", entry.rev.length() > 0);
        }
    }

    @Ignore
    @Test
    public void testDeletingFileAddsRevision() throws DropboxException {
        String filePath = createNewDropboxFile(UUID.randomUUID().toString());
        int index = filePath.lastIndexOf("/");
        String fileName = filePath.substring(index + 1);

        List<Entry> entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        mDBapi.delete(filePath);
        entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(2, entries.size());

        for (Entry entry : entries) {
            Assert.assertEquals(root, entry.parentPath());
            Assert.assertEquals(fileName, entry.fileName());
            Assert.assertEquals((entry.bytes == 0), entry.isDeleted);
        }
    }

    /**
     * this test shows that changing a file's name from oldFile to newFile adds a
     * version to a oldFile's revision history. in fact, the new zero-byte entry corresponds to the
     * deletion event of the original version
     *
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testRenamingFileAddsRevision() throws DropboxException {
        String filePath = createNewDropboxFile(UUID.randomUUID().toString());
        int index = filePath.lastIndexOf("/");
        String fileName = filePath.substring(index + 1);

        List<Entry> entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        boolean hasDeletedFile = false;
        for (Entry entry : entries) {
            Assert.assertEquals(root, entry.parentPath());
            Assert.assertEquals(fileName, entry.fileName());
            if (entry.isDeleted) {
                hasDeletedFile = true;
            }
            Assert.assertEquals((entry.bytes == 0), entry.isDeleted);
        }
        Assert.assertFalse(hasDeletedFile);

        mDBapi.move(filePath, filePath + "-renamed");
        entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(2, entries.size());

        for (Entry entry : entries) {
            Assert.assertEquals(root, entry.parentPath());
            Assert.assertEquals(fileName, entry.fileName());
            if (entry.isDeleted) {
                hasDeletedFile = true;
            }
            Assert.assertEquals((entry.bytes == 0), entry.isDeleted);
        }
        Assert.assertTrue(hasDeletedFile);

        entries = mDBapi.revisions(filePath + "-renamed", MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        for (Entry entry : entries) {
            Assert.assertEquals(root, entry.parentPath());
            Assert.assertEquals(fileName + "-renamed", entry.fileName());
            Assert.assertEquals((entry.bytes == 0), entry.isDeleted);
        }
    }

    /**
     * this test shows that the revision lists may return unexpected entries if a file
     * uses a previously used filename. a user may not be aware that the file name has
     * been previously used.
     *
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testReusingOldNamesCausesConfusion() throws DropboxException {
        String filePath1 = createNewDropboxFile(UUID.randomUUID().toString());
        String filePath2 = filePath1 + "-renamed-1";
        String filePath3 = filePath1 + "-renamed-2";

        mDBapi.move(filePath1, filePath2);
        List<Entry> entries = mDBapi.revisions(filePath2, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        mDBapi.move(filePath2, filePath3);
        entries = mDBapi.revisions(filePath2, MAX_REVISIONS);
        Assert.assertEquals(2, entries.size());

        String newFilePath1 = createNewDropboxFile(UUID.randomUUID().toString());

        mDBapi.move(newFilePath1, filePath2);
        entries = mDBapi.revisions(filePath2, MAX_REVISIONS);
        //we expect to see a 1 here since if don't expect the new
        //filename to have any history - but here it does. this might cause
        //confusion when relying on revision history to say something
        //about a file
        Assert.assertEquals(3, entries.size());
    }

    /**
     * this test shows that hash and revision information changes after a file is moved,
     * and so are not useful in trying to recover this connection between new and old file names
     *
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testRenamingFileDoesNotPreserveHashCodeOrRevison() throws DropboxException {
        String filePath = createNewDropboxFile(UUID.randomUUID().toString());
        Entry entry = mDBapi.metadata(filePath, MAX_REVISIONS, null, true, null);
        int originalHashCode = entry.hashCode();
        Assert.assertNotNull(originalHashCode);
        String originalRevision = entry.rev;
        Assert.assertNotNull(originalRevision);

        String newFilePath = filePath + "-renamed";

        mDBapi.move(filePath, newFilePath);
        entry = mDBapi.metadata(newFilePath, MAX_REVISIONS, null, true, null);
        int newHashCode = entry.hashCode();
        Assert.assertNotNull(newHashCode);
        String newRevision = entry.rev;
        Assert.assertNotNull(newRevision);

        Assert.assertFalse(originalHashCode == newHashCode);
        Assert.assertFalse(originalRevision.equals(newRevision));
    }

    /**
     * this test shows that hash and revision information changes after a folder is moved,
     * and so are not useful in trying to recover this connection between new and old folder names
     * contents information is unchanged, but this fact may not be useful
     *
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testRenamingFoldersDoesNotPreserveHashOrHashcodeOrRevisionOrContents() throws DropboxException, IllegalAccessException {
        String folderPath = root + UUID.randomUUID().toString();
        mDBapi.createFolder(folderPath);
        Entry entry = mDBapi.metadata(folderPath, MAX_REVISIONS, null, true, null);
        int originalHashCode = entry.hashCode();
        Assert.assertNotNull(originalHashCode);
        String originalHash = entry.hash;
        Assert.assertNotNull(originalHash);
        String originalRevision = entry.rev;
        Assert.assertNotNull(originalRevision);
        List<Entry> originalContents = entry.contents;

        String newFolderPath = folderPath + "-renamed-1";
        mDBapi.move(folderPath, newFolderPath);
        entry = mDBapi.metadata(newFolderPath, MAX_REVISIONS, null, true, null);
        int newHashCode = entry.hashCode();
        Assert.assertNotNull(newHashCode);
        String newHash = entry.hash;
        Assert.assertNotNull(newHash);
        String newRevision = entry.rev;
        Assert.assertNotNull(newRevision);
        List<Entry> newContents = entry.contents;

        Assert.assertFalse(originalHashCode == newHashCode);
        Assert.assertFalse(originalHash.equals(newHash));
        Assert.assertFalse(originalRevision.equals(newRevision));
        //just for fun, we can see that at least the contents are the same - empty
        Assert.assertEquals(originalContents, newContents);

        //lets try adding a file
        String fileContents="new file contents";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        mDBapi.putFile(newFolderPath + "/" + UUID.randomUUID().toString(), inputStream, fileContents.length(), null, null);
        entry = mDBapi.metadata(newFolderPath, MAX_REVISIONS, null, true, null);
        originalContents = entry.contents;
        String newerFolderPath = folderPath + "-renamed-2";
        mDBapi.move(newFolderPath, newerFolderPath);
        entry = mDBapi.metadata(newerFolderPath, MAX_REVISIONS, null, true, null);
        newContents = entry.contents;
        //these specified fields differ between old contents and new contents - the rest are unchanged
        assertEqualsByValue(originalContents, newContents, "modified", "path", "rev");
    }

    /**
     * this test shows that when a file is deleted and then re-added, the new file is
     * contained in the revision history of the original file
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testDeletedFileReaddedIsSameFile() throws DropboxException {
        String fileName = UUID.randomUUID().toString();
        String filePath = createNewDropboxFile(fileName);
        List<Entry> entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        mDBapi.delete(filePath);
        entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(2, entries.size());

        createNewDropboxFile(fileName);
        entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(3, entries.size());
        //new version shows up in the revisions history of the original path

    }

    /**
     * this test show that when content of a file is changed, the new version shows up
     * in the revision history of the original file
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testChangedContentIsSameFile() throws DropboxException {
        String fileName = UUID.randomUUID().toString();
        String filePath = createNewDropboxFile(fileName);
        List<Entry> entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        //get parent revision to overwrite
        Entry entry = mDBapi.metadata(filePath, MAX_REVISIONS, null, true, null);
        String parent_rev = entry.rev;

        String fileContents = "different content";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        mDBapi.putFile(filePath, inputStream, fileContents.length(), parent_rev, null);
        entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(2, entries.size());
        // new version of original file path

    }

    /**
     * this test shows that renaming a file creates a new file with a revision history disjoint from that of
     * the original file
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testSamePathDifferentNameIsDifferentFile() throws DropboxException {
        String fileName = UUID.randomUUID().toString();
        String filePath = createNewDropboxFile(fileName);
        List<Entry> entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        mDBapi.move(filePath, filePath + "-renamed");
        entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(2, entries.size());
        //two entries, one for deletion event

        entries = mDBapi.revisions(filePath + "-renamed", MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());
    }

    /**
     * this test shows that copying a file to a different folder creates a new file with a revision
     * history disjoint from that of the original file
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testCopiedFileDifferentFolderIsDifferentFile() throws DropboxException {
        String fileName = UUID.randomUUID().toString();
        String originalFolder = root + UUID.randomUUID().toString() + "/";
        String filePath = originalFolder + fileName;

        String fileContents="file contents";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        mDBapi.putFile(filePath, inputStream, fileContents.length(), null, null);

        List<Entry> entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        String newFolder = root + UUID.randomUUID().toString() + "/";
        String copiedFilePath = newFolder + fileName;

        mDBapi.copy(filePath, copiedFilePath);

        entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        entries = mDBapi.revisions(copiedFilePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());
    }

    /**
     * this test shows that moving a file to a different folder creates a new file with a revision
     * history disjoint from that of the original file
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testMovedFileToNewFolderIsDifferentFile() throws DropboxException {
        String fileName = UUID.randomUUID().toString();
        String filePath = createNewDropboxFile(fileName);
        List<Entry> entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

        String newFolder = root + UUID.randomUUID().toString() + "/";
        String movedFilePath = newFolder + fileName;

        mDBapi.move(filePath, movedFilePath);

        entries = mDBapi.revisions(filePath, MAX_REVISIONS);
        Assert.assertEquals(2, entries.size());
        //one entry for deletion event

        entries = mDBapi.revisions(movedFilePath, MAX_REVISIONS);
        Assert.assertEquals(1, entries.size());

    }

    /**
     * Illustrates that new files don't have hashes.
     *
     * @throws DropboxException
     * @throws IllegalAccessException
     */
    @Ignore
    @Test
    public void testNewFileHashIsNull() throws DropboxException, IllegalAccessException {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String content = "File Content";

        // creates a new file at 'path'
        final Entry entry = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entry);
    }

    /**
     * Illustrates that a new file created by overwriting an existing file doesn't have a hash.  In this test case
     * the second file's content differs from the first.
     *
     * @throws DropboxException
     * @throws IllegalAccessException
     */
    @Ignore
    @Test
    public void testOverwritingFileHashIsNull() throws DropboxException, IllegalAccessException {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String content = "File Content";

        // creates a new file at 'path'
        final Entry entry = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entry);

        // creates another new file at the same 'path'
        final Entry overwrittenEntry = mDBapi.putFile(path,
                IOUtils.toInputStream(content + " modified"), content.length() + " modified".length(), null, null);
        assertNotNull(overwrittenEntry);

        assertNull(entry.hash);
        assertNull(overwrittenEntry.hash);
    }

    /**
     * Illustrates that a new file created by <em>updating</em> an existing file doesn't have a hash.
     *
     * @throws DropboxException
     * @throws IllegalAccessException
     */
    @Ignore
    @Test
    public void testUpdatingFileHashIsNull() throws DropboxException, IllegalAccessException {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String content = "File Content";

        // creates a new file at 'path'
        final Entry entry = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entry);

        // updates file at the same 'path' (specifies a parent rev)
        final Entry updatedEntry = mDBapi.putFile(path,
                IOUtils.toInputStream(content + " modified"), content.length() + " modified".length(), entry.rev, null);
        assertNotNull(updatedEntry);

        assertNull(entry.hash);
        assertNull(updatedEntry.hash);
    }

    /**
     * Illustrates that putting two identical files with the {@code putFile(...)} call, and 'null' parentRevs produces
     * two different files in dropbox. E.g. putting filename "/foo/bar" and then putting "/foo/bar" again will leave two
     * different files in dropbox:
     * <ul>
     *     <li>/foo/bar</li>
     *     <li>/foo/bar (1)</li>
     * </ul>
     * Essentially this test shows you can't overwrite a file using {@code putFile(...)}
     * @throws Exception
     */
    @Ignore
    @Test
    public void testPutFileSamePathNullParentRevs() throws Exception {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String content = "File Content";

        // put a new file at 'path'
        final Entry entryOne = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryOne);

        // put the same file at at the same 'path', using a null parentRev
        final Entry entryTwo = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryTwo);

        // At this point, what's actually happened is that on Dropbox there are two files:
        // - the file represented by entryOne
        // - the file represented by entryTwo
        // So these two files will:
        // - *not* share revision history
        // - *not* share file names or file paths
        assertFalse(entryOne.path.equals(entryTwo.path));
        assertFalse(entryOne.fileName().equals(entryTwo.fileName()));
        assertFalse(entryOne.rev.equals(entryTwo.rev));

        // expect a single revision for entryOne.path, because entryOne hasn't been updated at all by the second call to
        // putFile(...).
        assertEquals(1, mDBapi.revisions(entryOne.path, MAX_REVISIONS).size());
        assertEqualsByValue(entryOne, mDBapi.revisions(entryOne.path, MAX_REVISIONS).get(0));

        // expect a single revision for entryTwo.path, because entryTwo was created as a new file by the second call to
        // putFile(...).
        assertEquals(1, mDBapi.revisions(entryTwo.path, MAX_REVISIONS).size());
        assertEqualsByValue(entryTwo, mDBapi.revisions(entryTwo.path, MAX_REVISIONS).get(0));
    }

    /**
     * Illustrates that putting two identical files (overwriting the second with the {@code putFileOverwrite(...)} call,
     * and using a 'null' parentRev for the first {@code putFile(...)} call) produces a single file in Dropbox.
     * E.g. putting filename "/foo/bar" and then putting (with overwrite semantics) "/foo/bar" again will leave a single
     * file in Dropbox.
     * <ul>
     *   <li>/foo/bar</li>
     * </ul>
     * Essentially this test shows that attempting to overwrite an existing file in dropbox using identical content is
     * ignored.
     * @throws Exception
     */
    @Ignore
    @Test
    public void testPutFileOverwriteSamePathNullParentRevs() throws Exception {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String content = "File Content";

        // put a new file at 'path'
        final Entry entryOne = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryOne);

        // Sleep two seconds to insure that modification timestamps could differ between the updates
        Thread.sleep(2000l);

        // put the same file at at the same 'path', using putFileOverwrite
        final Entry entryTwo = mDBapi.putFileOverwrite(path, IOUtils.toInputStream(content), content.length(), null);
        assertNotNull(entryTwo);

        // At this point, we expect a single file on Dropbox, because we used putFileOverwrite(...)
        // So the entries should:
        // - share file names and file paths
        assertEquals(entryOne.path, entryTwo.path);
        assertEquals(entryOne.fileName(), entryTwo.fileName());
        // - the revs are equal because the content and metadata are equal
        assertEquals(entryOne.rev, entryTwo.rev);

        // in fact, the two entries are equivalent; they have the same content, and have the same metadata.
        // the second call to putFileOverwrite(...) is essentially ignored (note: no timestamps have changed)
        assertEqualsByValue(entryOne, entryTwo);

        // expect a single revision for entryOne.path, because entryOne and entryTwo are equivalent objects (have the
        // same path)
        assertEquals(1, mDBapi.revisions(entryOne.path, MAX_REVISIONS).size());
        assertEqualsByValue(entryOne, mDBapi.revisions(entryOne.path, MAX_REVISIONS).get(0));

        // expect a single revision for entryTwo.path, because entryOne and entryTwo are equivalent objects (have the
        // same path)
        assertEquals(1, mDBapi.revisions(entryTwo.path, MAX_REVISIONS).size());
        assertEqualsByValue(entryTwo, mDBapi.revisions(entryTwo.path, MAX_REVISIONS).get(0));

        // This illustrates that for all intents and purposes, overwriting a file with the same content is ignored.
    }

    /**
     * Illustrates that putting a file, then overwriting it with updated content (overwriting the second with the
     * {@code putFileOverwrite(...)} call, and using a 'null' parentRev for the first {@code putFile(...)} call)
     * produces a single file in Dropbox. E.g. putting filename "/foo/bar" and then putting "/foo/bar" again will leave
     * a single file in Dropbox:
     * <ul>
     *   <li>/foo/bar</li>
     * </ul>
     * This illustrates that overwriting an existing file with content that differs from the original results in a
     * shared revision history between the two entries, and different 'rev's of the same file.
     * @throws Exception
     */
    @Ignore
    @Test
    public void testPutFileOverwriteDifferentContentNullParentRevs() throws Exception {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String content = "File Content";

        // put a new file at 'path'
        final Entry entryOne = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryOne);

        // Sleep two seconds to insure that modification timestamps could differ between the updates
        Thread.sleep(2000l);

        // put the file at at the same 'path', but with modified content, using putFileOverwrite
        final Entry entryTwo = mDBapi.putFileOverwrite(path, IOUtils.toInputStream(content + " modified"),
                (content.length() + " modified".length()), null);
        assertNotNull(entryTwo);

        // At this point, we expect a single file on Dropbox, because we used putFileOverwrite(...)
        // So the entries should:
        // - share file names and file paths
        // - share revision history
        // But we updated the content so the 'rev' should differ
        assertEquals(entryOne.path, entryTwo.path);
        assertEquals(entryOne.fileName(), entryTwo.fileName());
        assertFalse(entryOne.rev.equals(entryTwo.rev));

        // expect two revisions for entryOne.path, because entryOne has been updated at all by the second call to
        // putFile(...).
        // The oldest entry is at the tail of the list.
        assertEquals(2, mDBapi.revisions(entryOne.path, MAX_REVISIONS).size());
        assertEqualsByValue(entryOne, mDBapi.revisions(entryOne.path, MAX_REVISIONS).get(1));

        // expect two revisions for entryTwo.path, because entryTwo was created as a new file by the second call to
        // putFileOverwrite(...).
        // The newest entry is at the head of the list.
        assertEquals(2, mDBapi.revisions(entryTwo.path, MAX_REVISIONS).size());
        assertEqualsByValue(entryTwo, mDBapi.revisions(entryTwo.path, MAX_REVISIONS).get(0));

        // in fact the revision history for entryOne.path and entryTwo.path are the same
        assertEqualsByValue(mDBapi.revisions(entryOne.path, MAX_REVISIONS),
                mDBapi.revisions(entryTwo.path, MAX_REVISIONS));

        // This illustrates that for all intents and purposes, overwriting a file with different content will produce
        // a new revision of that file.
    }

    /**
     * Illustrates that updating a file (with different content) and specifying a parentRev changes its revision,
     * producing a history containing two entries.
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testPutFileWithParentRevUpdatedContent() throws Exception {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String content = "File Content";

        // creates a new file at 'path'
        final Entry entry = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entry);

        // updates the file at the same 'path' (note parentRev is supplied)
        final Entry updatedEntry = mDBapi.putFile(path,
                IOUtils.toInputStream(content + " modified"), content.length() + " modified".length(), entry.rev, null);
        assertNotNull(updatedEntry);

        assertNotNull(entry.rev);
        assertNotNull(updatedEntry.rev);
        assertFalse(entry.rev.equals(updatedEntry.rev));

        // we expect two revisions
        assertEquals(2, mDBapi.revisions(path, MAX_REVISIONS).size());

        // The oldest at the tail...
        assertEqualsByValue(entry, mDBapi.revisions(path, MAX_REVISIONS).get(1));

        // The newest at the head...
        assertEqualsByValue(updatedEntry, mDBapi.revisions(path, MAX_REVISIONS).get(0));
    }

    /**
     * Illustrates that updating a file (with different content, and a different name) changes its revision, but history
     * is truncated (doesn't survive renames)
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testPutFileWithParentRevUpdatedContentAndPath() throws Exception {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String newPath = path + "foo";
        final String content = "File Content";
        final String modifiedContent = content + " modified";

        // creates a new file at 'path'
        final Entry entry = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entry);

        // updates the file at a new 'path' (note parentRev is supplied)
        final Entry updatedEntry = mDBapi.putFile(newPath,
                IOUtils.toInputStream(modifiedContent), modifiedContent.length(), entry.rev, null);
        assertNotNull(updatedEntry);

        assertNotNull(entry.rev);
        assertNotNull(updatedEntry.rev);
        assertFalse(entry.rev.equals(updatedEntry.rev));

        // we expect one revision for 'path'
        assertEquals(1, mDBapi.revisions(path, MAX_REVISIONS).size());

        // we get one revision for 'pathFoo'
        assertEquals(1, mDBapi.revisions(newPath, MAX_REVISIONS).size());

        // The oldest at the tail...
        assertEqualsByValue(entry, mDBapi.revisions(path, MAX_REVISIONS).get(0));

        // The newest at the head...
        assertEqualsByValue(updatedEntry, mDBapi.revisions(newPath, MAX_REVISIONS).get(0));

        // illustrates that updating a file path, even when specifying a parent rev, fails to track history across renames.
    }

    /**
     * Illustrates that moving a file (without changing its content) changes its revision, but history is truncated
     * (doesn't survive renames)
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testPutFileAndMove() throws Exception {
        final String name = UUID.randomUUID().toString();
        final String path = root + name;
        final String newPath = path + "foo";
        final String content = "File Content";

        // creates a new file at 'path'
        final Entry entry = mDBapi.putFile(path, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entry);

        // moves the file to 'newPath'
        final Entry updatedEntry = mDBapi.move(path, newPath);
        assertNotNull(updatedEntry);

        // The files don't share the same rev
        assertNotNull(entry.rev);
        assertNotNull(updatedEntry.rev);
        assertFalse(entry.rev.equals(updatedEntry.rev));

        // we expect two revisions for 'path': the original entry and an entry that represents the deletion.
        assertEquals(2, mDBapi.revisions(path, MAX_REVISIONS).size());
        final Entry deletedEntry = mDBapi.revisions(path, MAX_REVISIONS).get(0);
        assertEquals(0, deletedEntry.bytes);
        assertEqualsByValue(entry, mDBapi.revisions(path, MAX_REVISIONS).get(1));

        // we expect one revision for 'newPath'.  We don't see the original entry before the rename.
        assertEquals(1, mDBapi.revisions(newPath, MAX_REVISIONS).size());
        assertEqualsByValue(updatedEntry, mDBapi.revisions(newPath, MAX_REVISIONS).get(0));

        // illustrates that moving a file (a mechanism that could be leveraged for renames) truncates history, so
        // moving a file is not a useful strategy for preserving history.
    }

    /**
     * Illustrates that identicial content in different folders doesn't share revision numbers.
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testPutIdenticalContentInDifferentFoldersDoesntShareRev() throws Exception {
        final String nameOne = UUID.randomUUID().toString();
        final String nameTwo = UUID.randomUUID().toString();
        final String pathOne = root + nameOne;
        final String pathTwo = root + nameTwo;
        final String content = "File Content";

        // creates a new file at 'pathOne'
        final Entry entryOne = mDBapi.putFile(pathOne, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryOne);

        // creates a new file at 'pathTwo', identical content
        final Entry entryTwo = mDBapi.putFile(pathTwo, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryTwo);

        // Assert the two files have different revs
        assertFalse(entryOne.rev.equals(entryTwo.rev));
    }

    /**
     * test that updating a file with the same content is ignored, if the submitted filename is the same
     * @throws DropboxException
     * @throws IllegalAccessException
     */
    @Ignore
    @Test
    public void testSameContentUpdateSameFilenameIsIgnored() throws DropboxException, IllegalAccessException {
        final String basename =  UUID.randomUUID().toString();
        final String nameOne = basename + "NAME";
        final String nameTwo = basename + "NAME";
        final String pathOne = root + nameOne;
        final String pathTwo = root + nameTwo;
        final String content = "zippity-doo-dah";

        // creates a new file at 'pathOne'
        final Entry entryOne = mDBapi.putFile(pathOne, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryOne);

        // updates file at the same 'path' (specifies a parent rev)
        final Entry updatedEntry = mDBapi.putFile(pathTwo,
                IOUtils.toInputStream(content), content.length(), entryOne.rev, null);
        assertNotNull(updatedEntry);

        List<Entry> listOne = mDBapi.revisions(pathOne, MAX_REVISIONS);
        List<Entry> listTwo = mDBapi.revisions(pathTwo, MAX_REVISIONS);
        Assert.assertEquals(1,listOne.size());
        Assert.assertEquals(1,listTwo.size());
        assertEqualsByValue(listOne.get(0), listTwo.get(0));
    }


    /**
     * test that updating a file with the same content is ignored, even if the filename differs by case only
     * @throws DropboxException
     * @throws IllegalAccessException
     */
    @Ignore
    @Test
    public void testSameContentUpdateDifferentCaseFilenameIsIgnored() throws DropboxException, IllegalAccessException {
        final String basename =  UUID.randomUUID().toString();
        final String nameOne = basename +  "NAME";
        final String nameTwo = basename + "naMe";
        final String pathOne = root + nameOne;
        final String pathTwo = root + nameTwo;
        final String content = "zippity-doo-dah";

        // creates a new file at 'pathOne'
        final Entry entryOne = mDBapi.putFile(pathOne, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryOne);

        // updates file at the same 'path' (specifies a parent rev)
        final Entry updatedEntry = mDBapi.putFile(pathTwo,
                IOUtils.toInputStream(content), content.length(), entryOne.rev, null);
        assertNotNull(updatedEntry);

        List<Entry> listOne = mDBapi.revisions(pathOne, MAX_REVISIONS);
        List<Entry> listTwo = mDBapi.revisions(pathTwo, MAX_REVISIONS);
        Assert.assertEquals(1,listOne.size());
        Assert.assertEquals(1,listTwo.size());
        assertEqualsByValue(listOne.get(0), listTwo.get(0));
    }

    /**
     * test that we cannot move a file to a path with the same lower case resolution - get a
     * 403 Forbidden (A file with that name already exists at path (pathTwo)
     * @throws DropboxException
     */
    @Ignore
    @Test(expected= DropboxServerException.class)
    public void testCannotMoveOldFileToNewFileWithSameLowerCaseNameResolution() throws DropboxException {
        final String basename =  UUID.randomUUID().toString();
        final String nameOne = basename +  "NAME";
        final String nameTwo = basename + "naMe";
        final String pathOne = root + nameOne;
        final String pathTwo = root + nameTwo;
        final String content = "zippity-doo-dah";

        // creates a new file at 'pathOne'
        final Entry entryOne = mDBapi.putFile(pathOne, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryOne);

        // try to move file to pathTwo, expect an error
        final Entry updatedEntry = mDBapi.move(pathOne, pathTwo);
    }

    /**
     * test that we cannot copy a file to a path with the same lower case resolution - get a
     * 403 Forbidden (A file with that name already exists at path (pathTwo)
     * @throws DropboxException
     */
    @Ignore
    @Test(expected= DropboxServerException.class)
    public void testCannotCopyToFileWithSameLowerCaseNameResolutionAsOldFile() throws DropboxException {
        final String basename =  UUID.randomUUID().toString();
        final String nameOne = basename +  "NAME";
        final String nameTwo = basename + "naMe";
        final String pathOne = root + nameOne;
        final String pathTwo = root + nameTwo;
        final String content = "zippity-doo-dah";

        // creates a new file at 'pathOne'
        final Entry entryOne = mDBapi.putFile(pathOne, IOUtils.toInputStream(content), content.length(), null, null);
        assertNotNull(entryOne);

        // copy the file to a path with same lower case resolution
        final Entry updatedEntry = mDBapi.copy(pathOne, pathTwo);
    }

    /**
     * tests that delta will return a change for a file, even if the content of the file is in the same state between
     * the last poll and the current poll
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testDeltaPicksUpChangesForFileEvenIfNetEffectIsNoChangeForFile() throws DropboxException {
        final String basename =  UUID.randomUUID().toString();

        final String nameOne=basename + "original";
        final String nameTwo=basename + "second";

        final String pathOne = root + nameOne;
        final String pathTwo = root + nameTwo;

        final String content1 = "zippity-doo-dah";
        final String content2 = "zippity-ay";
        final String content3 = "my, oh my";

        // creates a new file at 'pathOne'
        final Entry entryOne = mDBapi.putFile(pathOne, IOUtils.toInputStream(content1), content1.length(), null, null);
        assertNotNull(entryOne);

        String cursor = null;
        DeltaPage<Entry> entries = mDBapi.delta(cursor);
        cursor = (entries.cursor);
        // at this point there is nothing at pathTwo, content for pathOne is content1

        final Entry entryTwo = mDBapi.move(pathOne, pathTwo);
        final Entry entryThree = mDBapi.putFile(pathTwo, IOUtils.toInputStream(content2), content2.length(), entryTwo.rev, null);
        final Entry entryFour = mDBapi.putFile(pathTwo, IOUtils.toInputStream(content3), content3.length(), entryThree.rev, null);
        final Entry entryFive = mDBapi.move(pathTwo, pathOne);
        //pathTwo now has no file again

        Assert.assertEquals(3,mDBapi.revisions(pathOne, MAX_REVISIONS).size());
        Assert.assertEquals(4,mDBapi.revisions(pathTwo, MAX_REVISIONS).size());

        //now there is nothing at pathTwo, but we picked up revisions for pathTwo
        entries = mDBapi.delta(cursor);
        Assert.assertEquals(2, entries.entries.size());

        DropboxAPI.DeltaEntry<Entry> pathTwoNewDelta = null;

        for(DropboxAPI.DeltaEntry<Entry> entry : entries.entries){
            if(pathTwo.equals(entry.lcPath)){
                pathTwoNewDelta = entry;
            }
        }

        //now there has been no change in the (no) file at pathTwo, but revisions for pathTwo
        //have affected the delta
        Assert.assertNotNull(pathTwoNewDelta);
        Assert.assertEquals(pathTwo, pathTwoNewDelta.lcPath);

    }

    /**
     * if we create a folder, add a file to the folder, then delete the file, then a delta entry for that folder will
     * not be returned, a delta entry for the file will be returned.
     * @throws DropboxException
     */
    @Ignore
    @Test
    public void testDeltaDoesNotPickUpRevisionsForFolderIfNetEffectForFolderIsNoChangeForFolder() throws DropboxException {
        final String nameOne= UUID.randomUUID().toString() + "originalfolder";
        final String content1 = "zippity-doo-dah";

        final String folderPath = root + nameOne;
        final String filePath = folderPath + "/" + "file";

        final Entry folderEntryOne = mDBapi.createFolder(folderPath);

        String cursor = null;
        DeltaPage<Entry> entries = mDBapi.delta(cursor);
        cursor = (entries.cursor);

        // creates a new file at filePath (under folderPath)
        final Entry fileEntryOne = mDBapi.putFile(filePath, IOUtils.toInputStream(content1), content1.length(), null, null);
        mDBapi.delete(filePath);

        entries = mDBapi.delta(cursor);
        Assert.assertEquals(1, entries.entries.size());

        //the file is the only thing in the delta - the folder does not get a delta entry
        for(DropboxAPI.DeltaEntry<Entry> entry : entries.entries){
            Assert.assertEquals(filePath, entry.lcPath);
        }

    }

    @Ignore
    @Test
    public void testMetadataForDirectoriesWithSubdirectoriesWithContentDoesNotReturnContentsRecursively() throws DropboxException {
        final String folderOne = root + UUID.randomUUID().toString();
        final String subFolderOne = folderOne + "/" + UUID.randomUUID().toString();
        final String subSubFolderOne = subFolderOne + "/" + UUID.randomUUID().toString();
        mDBapi.createFolder(folderOne);
        mDBapi.createFolder(subFolderOne);
        mDBapi.createFolder(subSubFolderOne);

        Entry metadataEntry = mDBapi.metadata(folderOne, MAX_REVISIONS, null, true, null);

        Assert.assertEquals(folderOne,  metadataEntry.path);
        Assert.assertEquals(subFolderOne, metadataEntry.contents.get(0).path);
        // subfolderOne has contents, but are not returned
        Assert.assertNull(metadataEntry.contents.get(0).contents);

        // if we go one level down, we will see that subSubFolder is in subFolder's contents
        metadataEntry = mDBapi.metadata(subFolderOne, MAX_REVISIONS, null, true, null);
        Assert.assertEquals(subFolderOne,  metadataEntry.path);
        Assert.assertNotNull(metadataEntry.contents);
        Assert.assertEquals(subSubFolderOne, metadataEntry.contents.get(0).path);
    }


}
