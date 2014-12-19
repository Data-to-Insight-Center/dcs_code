/*
 * Copyright 2013 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.packaging.ingest.api.IngestPhase;

/**
 * Basic implementation of {@code IngestPhase} used in the ingest bootstrap. 
 *
 */
public class IngestPhaseImpl
        implements IngestPhase, Comparable<IngestPhaseImpl> {

    private Integer phaseNumber;
    private boolean haltIngest;
    
    public IngestPhaseImpl(Integer phaseNumber, boolean haltIngest) {
        this.phaseNumber = phaseNumber;
        this.haltIngest = haltIngest;
    }
    
    public IngestPhaseImpl(Integer phaseNumber) {
        this.phaseNumber = phaseNumber;
        haltIngest = false;
    }
    
    @Override
    public void setPhaseNumber(Integer phase) {
        phaseNumber = phase;
    }

    @Override
    public Integer getPhaseNumber() {
        return phaseNumber;
    }

    @Override
    public void setPauseIngest(boolean haltIngest) {
        this.haltIngest = haltIngest;
    }

    @Override
    public boolean getPauseIngest() {
        return haltIngest;
    }

    /**
     * Two phases are considered equal if they have the same phase number. 
     */
    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }else if (obj instanceof IngestPhase) {
            IngestPhase other = (IngestPhase)obj;
            return phaseNumber.equals(other.getPhaseNumber()); 
        } else {
            return false;
        }        
       
    }

    @Override
    public int compareTo(IngestPhaseImpl o) {
        return phaseNumber.compareTo(o.getPhaseNumber());
    }

    @Override
    public int hashCode() {
        return phaseNumber.hashCode();
    } 
}
