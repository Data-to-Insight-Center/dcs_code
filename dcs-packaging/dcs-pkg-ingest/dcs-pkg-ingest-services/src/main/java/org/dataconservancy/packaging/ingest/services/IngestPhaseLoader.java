/*
 * Copyright 2012 Johns Hopkins University
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

import java.util.List;

import org.dataconservancy.packaging.ingest.api.IngestPhase;
import org.dataconservancy.packaging.ingest.api.StatefulIngestService;


public class IngestPhaseLoader {
    StatefulBootstrapImpl bootstrap;
    IngestPhase phase;
    List<StatefulIngestService> phaseServices;
    
    public void addPhase() {
        bootstrap.addIngestPhase(phase, phaseServices);
    }
    
    public void setBootstrap(StatefulBootstrapImpl bootstrap) {
        this.bootstrap = bootstrap;
    }
    
    public void setPhase(IngestPhase phase) {
        this.phase = phase;
    }
    
    public void setPhaseServices(List<StatefulIngestService> phaseServices) {
        this.phaseServices = phaseServices;
    }
    
}