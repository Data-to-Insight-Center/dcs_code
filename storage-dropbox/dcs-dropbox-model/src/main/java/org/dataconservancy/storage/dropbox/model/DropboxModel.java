/*
 * Copyright 2013 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.storage.dropbox.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.dropbox.client2.DropboxAPI.Entry;

/**
 * This class models the Dropbox Entry object.
 */
public class DropboxModel {
    
    private String id;
    private long fileSize;
    private DateTime clientMtime;
    private List<DropboxModel> contents;
    private String hash;
    private String icon;
    private boolean isDeleted;
    private boolean isDir;
    private String mimeType;
    private DateTime modifiedDate;
    private String path;
    private String rev;
    private String root;
    private String hrFileSize;
    private boolean thumbExists;

    public static final String JODA_DATE_TIME_FORMATTER_STRING = "E, dd MMM yyyy HH:mm:ss Z";
    
    public DropboxModel() {
    }

    /**
     * Creates a new DropboxModel from a Dropbox Entry
     * 
     * @param e
     */
    public DropboxModel(Entry e) {
        this.id = generateId(e.path, e.bytes);
        this.fileSize = e.bytes;
        this.clientMtime = formatDateTime(e.clientMtime);
        if(e.contents != null){
            this.contents = convertEntries(e.contents);
        }
        this.hash = e.hash;
        this.icon = e.icon;
        this.isDeleted = e.isDeleted;
        this.isDir = e.isDir;
        this.mimeType = e.mimeType;
        this.modifiedDate = formatDateTime(e.modified);
        this.path = e.path;
        this.rev = e.rev;
        this.root = e.root;
        this.hrFileSize = e.size;
        this.thumbExists = e.thumbExists;
    }

    /**
     * Creates a new DropboxModel {@link #equals(Object) equal} to {@code toCopy}.
     * 
     * @param toCopy
     *            the DropboxModel to copy, must not be {@code null}
     * @throws UnsupportedEncodingException
     *             if {@code toCopy} is {@code null}.
     */
    public DropboxModel(DropboxModel toCopy) {
        this.id = generateId(toCopy.getPath(), toCopy.getFileSize());
        this.fileSize = toCopy.getFileSize();
        this.clientMtime = toCopy.getClientMtime();
        this.contents = toCopy.getContents();
        this.hash = toCopy.getHash();
        this.icon = toCopy.getIcon();
        this.isDeleted = toCopy.isDeleted();
        this.isDir = toCopy.isDir();
        this.mimeType = toCopy.getMimeType();
        this.modifiedDate = toCopy.getModifiedDate();
        this.path = toCopy.getPath();
        this.rev = toCopy.getRev();
        this.root = toCopy.getRoot();
        this.hrFileSize = toCopy.getHrFileSize();
        this.thumbExists = toCopy.isThumbExists();
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }
    
    /**
     * @param fileSize
     *            the fileSize to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    /**
     * @return the clientMtime
     */
    public DateTime getClientMtime() {
        return clientMtime;
    }
    
    /**
     * @param clientMtime
     *            the clientMtime to set
     */
    public void setClientMtime(DateTime clientMtime) {
        this.clientMtime = clientMtime;
    }
    
    /**
     * @return the contents
     */
    public List<DropboxModel> getContents() {
        return contents;
    }
    
    /**
     * @param contents
     *            the contents to set
     */
    public void setContents(List<DropboxModel> contents) {
        this.contents = contents;
    }
    
    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }
    
    /**
     * @param hash
     *            the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * @param icon
     *            the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    /**
     * @return the isDeleted
     */
    public boolean isDeleted() {
        return isDeleted;
    }
    
    /**
     * @param isDeleted
     *            the isDeleted to set
     */
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    /**
     * @return the isDir
     */
    public boolean isDir() {
        return isDir;
    }
    
    /**
     * @param isDir
     *            the isDir to set
     */
    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * @param mimeType
     *            the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    /**
     * @return the modifiedDate
     */
    public DateTime getModifiedDate() {
        return modifiedDate;
    }
    
    /**
     * @param modifiedDate
     *            the modifiedDate to set
     */
    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * @return the rev
     */
    public String getRev() {
        return rev;
    }
    
    /**
     * @param rev
     *            the rev to set
     */
    public void setRev(String rev) {
        this.rev = rev;
    }
    
    /**
     * @return the root
     */
    public String getRoot() {
        return root;
    }
    
    /**
     * @param root
     *            the root to set
     */
    public void setRoot(String root) {
        this.root = root;
    }
    
    /**
     * @return the hrFileSize
     */
    public String getHrFileSize() {
        return hrFileSize;
    }
    
    /**
     * @param hrFileSize
     *            the hrFileSize to set
     */
    public void setHrFileSize(String hrFileSize) {
        this.hrFileSize = hrFileSize;
    }
    
    /**
     * @return the thumbExists
     */
    public boolean isThumbExists() {
        return thumbExists;
    }
    
    /**
     * @param thumbExists
     *            the thumbExists to set
     */
    public void setThumbExists(boolean thumbExists) {
        this.thumbExists = thumbExists;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof DropboxModel)) {
            return false;
        }

        DropboxModel other = (DropboxModel) obj;

        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        
        if (clientMtime == null) {
            if (other.clientMtime != null) {
                return false;
            }
        }
        else if (!clientMtime.equals(other.clientMtime)) {
            return false;
        }

        if(contents == null){
            if (other.contents != null){
                return false;
            }
        }
        else if (other.contents == null) {
            return false;
        } else {
            List<DropboxModel> thisContentsList = contents;
            List<DropboxModel> otherContentsList = other.getContents();
            if(thisContentsList.size() != other.getContents().size()){
                return false;
            }
            for(int i=0; i < thisContentsList.size(); i++){
                if(!thisContentsList.get(i).equals(otherContentsList.get(i))){
                    return false;
                }
            }
        }

        if (hash == null){
            if(other.hash != null){
                return false;
            }
        }
        else if (!hash.equals(other.getHash())) {
            return false;
        }

        if (icon == null){
            if(other.icon != null){
                return false;
            }
        }
        else if (!icon.equals(other.getIcon())) {
            return false;
        }

        if((isDeleted && !other.isDeleted()) || (!isDeleted && other.isDeleted())){
            return false;
        }

        if((isDir && !other.isDir()) || (!isDir && other.isDir())){
            return false;
        }

        if (mimeType == null){
            if(other.mimeType != null){
                return false;
            }
        }
        else if (!mimeType.equals(other.getMimeType())) {
            return false;
        }

        if (path == null){
            if(other.path != null){
                return false;
            }
        }
        else if (!path.equals(other.getPath())) {
            return false;
        }

        if (rev == null){
            if(other.rev != null){
                return false;
            }
        }
        else if (!rev.equals(other.getRev())) {
            return false;
        }

        if (root == null){
            if(other.root != null){
                return false;
            }
        }
        else if (!root.equals(other.getRoot())) {
            return false;
        }

        if (hrFileSize == null){
            if(other.hrFileSize != null){
                return false;
            }
        }
        else if (!hrFileSize.equals(other.getHrFileSize())) {
            return false;
        }

        if((thumbExists && !other.isThumbExists()) || (!thumbExists && other.isThumbExists())){
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "DropboxModel{ id=" + id + ", clienMTime=" + clientMtime + ", contents=" + contents + ", hash" + hash
                + ", icon="
                + icon + ", isDeleted=" + isDeleted + ", isDir=" + isDir + ", mimeType=" + mimeType + ", modifiedDate="
                + modifiedDate + ", path=" + path + ", rev=" + rev + ", root=" + root + ", hrFileSize=" + hrFileSize
                + ", thumbExists=" + thumbExists;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((clientMtime == null) ? 0 : clientMtime.hashCode());
        result = prime * result + ((contents == null) ? 0 : contents.hashCode());
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + ((icon == null) ? 0 : icon.hashCode());
        result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
        result = prime * result + ((modifiedDate == null) ? 0 : modifiedDate.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((rev == null) ? 0 : rev.hashCode());
        result = prime * result + ((root == null) ? 0 : root.hashCode());
        result = prime * result + ((hrFileSize == null) ? 0 : hrFileSize.hashCode());
        return result;
    }

    /**
     * A helper method to enable the construction of a DropboxModel from an Entry Converts a List of Entrys to a List of
     * Dropbox Models
     * 
     * @param entriesList
     * @return
     */
    private List<DropboxModel> convertEntries(List<Entry> entriesList) {
        List<DropboxModel> contentsList = new ArrayList<DropboxModel>();
        for(Entry e : entriesList){
           DropboxModel dropboxModel = new DropboxModel(e);
            contentsList.add(dropboxModel);
        }
        return contentsList;
    }

    /**
     * A helper method to enable the construction of a DropboxModel from an Entry
     * Converts an Entry date string to a DateTime object
     * @param stringDate
     * @return
     */
    private DateTime formatDateTime(String stringDate){
        if(stringDate == null){
            return null;
        }
        DateTimeFormatter fmt = DateTimeFormat.forPattern(JODA_DATE_TIME_FORMATTER_STRING);
        DateTime dateTime = fmt.parseDateTime(stringDate);
        return dateTime;
    }
    
    /**
     * A helper method that generates an ID for the model based on file's full path and its size. Size is specifically
     * used to determine whether a file was modified in which case it could be marked as update.
     * 
     * FIXME: Think of a better way to create IDs as right now if for instance a top directory is renamed, that would
     * also mark everything under it as new. This may or may not be expected.
     * 
     * @param path
     * @param fileSize
     * @return
     */
    private String generateId(String path, long fileSize) {
        String id = path + "_" + Long.toString(fileSize);
        id = id.replaceAll("\\s", "").toLowerCase();
        return id;
    }
    
}
