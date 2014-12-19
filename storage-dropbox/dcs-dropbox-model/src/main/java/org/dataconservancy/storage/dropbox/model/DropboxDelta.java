package org.dataconservancy.storage.dropbox.model;

import java.util.ArrayList;
import java.util.List;

import com.dropbox.client2.DropboxAPI.DeltaEntry;
import com.dropbox.client2.DropboxAPI.Entry;

/**
 * This class models the Dropbox DeltaEntry object.
 */
public class DropboxDelta {
    private String lcpath;
    private DropboxModel metadata;
    private List<DropboxModel> dropboxModels;

    public DropboxDelta(){
        dropboxModels = new ArrayList<DropboxModel>();
    }

    /**
     * Constructor to create a DropboxDelta object from a DeltaEntry object
     * 
     * @param deltaEntry
     */
    public DropboxDelta(DeltaEntry<Entry> deltaEntry) {
        this.lcpath = deltaEntry.lcPath;
        if (deltaEntry.metadata != null){
            this.metadata = new DropboxModel(deltaEntry.metadata);
        }
    }

    public void addDropboxModel(DropboxModel model) {
        dropboxModels.add(model);
    }
    
    public List<DropboxModel> getDropboxModels() {
        return dropboxModels;
    }

    /**
     *
     * @return the lcpath
     */
    public String getLcPath(){
        return this.lcpath;
    }

    /**
     * set the lcpath
     * @param lcpath
     */
    public void setLcPath(String lcpath){
        this.lcpath = lcpath;
    }

    /**
     *
     * @return the metadata
     */
    public DropboxModel getMetadata(){
        return this.metadata;
    }

    /**
     * set the metadata
     * @param metadata
     */
    public void setMetadata(DropboxModel metadata){
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof DropboxDelta)) {
            return false;
        }

        DropboxDelta other = (DropboxDelta)obj;

        if (lcpath == null) {
            if (other.lcpath != null) {
                return false;
            }
        } else if (!lcpath.equals(other.lcpath)) {
            return false;
        }

        if(metadata == null){
            if (other.metadata != null){
                return false;
            }
        }else if(other.metadata == null) {
                return false;
        } else {
            if(!metadata.equals(other.metadata)) {
                return false;
            }
        }
        return true;
    }
}

