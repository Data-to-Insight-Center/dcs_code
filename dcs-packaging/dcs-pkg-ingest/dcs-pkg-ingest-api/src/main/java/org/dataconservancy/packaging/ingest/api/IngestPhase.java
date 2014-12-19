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
package org.dataconservancy.packaging.ingest.api;

/**
 * Ingest phase is the key used to map ingest phases for the {@code StatefulBootstrap}. A phase consists of an integer that determines execution order. 
 * Lower phase numbers will be executed first, two phases are considered equal if they have the same phase number, which is not allowed. 
 * Phase also contains a boolean flag that says where user feedback is required at the end of the phase. 
 * If true, the {@code StatefulBootstrap} will halt execution at the completion of the phase, and resume will need to be called on the {@code ResumableDepositManager}
 * to complete the ingest.
 *
 */
public interface IngestPhase {

    public void setPhaseNumber(Integer phase);
    
    public Integer getPhaseNumber();
    
    public void setPauseIngest(boolean haltIngest);
    
    public boolean getPauseIngest();
}
