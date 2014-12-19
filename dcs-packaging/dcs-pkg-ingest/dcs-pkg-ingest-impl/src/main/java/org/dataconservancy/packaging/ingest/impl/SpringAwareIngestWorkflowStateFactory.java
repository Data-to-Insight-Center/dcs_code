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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * An IngestWorkflowStateFactory which looks up instances of IngestWorkflowState from a Spring container.  Typically
 * the Spring container will contain a {@code prototype} bean definition for the IngestWorkflowState bean, so that
 * {@link #newInstance()} returns a new instance each time the Spring container is consulted.
 * <p/>
 * Configuration:
 * <dl>
 * <dd>ingestWorkflowStateBeanName</dd>
 * <dt>the identifier or name of the IngestWorkflowState bean definition in the Spring container</dt>
 * </dl>
 */
class SpringAwareIngestWorkflowStateFactory implements IngestWorkflowStateFactory, ApplicationContextAware {

    private ApplicationContext appCtx;

    private String ingestWorkflowStateBeanName;

    @Override
    public IngestWorkflowState newInstance() {
        if (appCtx == null) {
            throw new IllegalStateException("Spring Application Context is required!");
        }

        if (ingestWorkflowStateBeanName == null || ingestWorkflowStateBeanName.trim().length() == 0) {
            throw new IllegalStateException("The name or id of a Spring Bean implementing IngestWorkflowState " +
                    "must be supplied!");
        }

        final IngestWorkflowState state = appCtx.getBean(ingestWorkflowStateBeanName, IngestWorkflowState.class);
        if (state == null) {
            throw new IllegalStateException("Spring container did not have a bean named '" +
                    ingestWorkflowStateBeanName + "'");
        }

        return state;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext == null) {
            throw new IllegalArgumentException("Application Context must not be null.");
        }

        this.appCtx = applicationContext;
    }

    /**
     * The name or identifier of the IngestWorkflowState bean in the Spring container.
     *
     * @return the name or identifier of the IngestWorkflowState bean in the Spring container
     */
    public String getIngestWorkflowStateBeanName() {
        return ingestWorkflowStateBeanName;
    }

    /**
     * The name or identifier of the IngestWorkflowState bean in the Spring container.
     *
     * @param ingestWorkflowStateBeanName the name or identifier of the IngestWorkflowState bean in the Spring container
     */
    public void setIngestWorkflowStateBeanName(String ingestWorkflowStateBeanName) {
        this.ingestWorkflowStateBeanName = ingestWorkflowStateBeanName;
    }
}
