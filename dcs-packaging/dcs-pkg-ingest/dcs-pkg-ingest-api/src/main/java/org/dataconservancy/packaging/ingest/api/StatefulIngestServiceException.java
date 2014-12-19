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
 *
 */
public class StatefulIngestServiceException extends Exception {

    private String depositId;

    private IngestWorkflowState state;

    public StatefulIngestServiceException() {

    }

    public StatefulIngestServiceException(String msg) {
        super(msg);
    }

    public StatefulIngestServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public StatefulIngestServiceException(Throwable cause) {
        super(cause);
    }

    public IngestWorkflowState getState() {
        return state;
    }

    public void setState(IngestWorkflowState state) {
        this.state = state;
    }

    public String getDepositId() {
        return depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }
}
