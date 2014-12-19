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
package org.dataconservancy.packaging.ingest.api;

/**
 * Bootstraps an ingest process.  Implementations are expected to reason over the current state of the ingest to
 * determine the ingest process(es) to execute.
 */
public interface StatefulBootstrap {

    /**
     * Start an ingest process.
     *
     * @param depositId the identifier representing the deposit
     * @param state the current state of the ingest process
     */
    public void startIngest(String depositId, IngestWorkflowState state);

}
