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
package org.dataconservancy.packaging.ingest.impl;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;

/**
 * A factory for producing instances of {@link IngestWorkflowState}.
 */
interface IngestWorkflowStateFactory {

    /**
     * Produces a new instance of {@link IngestWorkflowState}, properly initialized, but empty.  That is, the
     * objects composing the {@link IngestWorkflowState} should be available and ready to be used, but they shouldn't
     * contain any state at this point.
     *
     * @return a new instance of {@code IngestWorkflowState}, ready to be used by the caller
     */
    public IngestWorkflowState newInstance();

}
