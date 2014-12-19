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

import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.IngestPhase;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulBootstrap;
import org.dataconservancy.packaging.ingest.api.StatefulIngestService;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.BagUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Bootstrap implementation that has the ability to start or resume an ingest process.  Ultimately this implementation
 * coordinates the execution of {@link StatefulIngestService}s.
 * <p/>
 * Configuration:
 * <dl>
 *   <dt>Required: ExecutorService</dt>
 *   <dd>The executor service allows different threading models to be used.</dd>
 *   <dt>Required: IngestServicesMap&lt;Integer, List&lt;StatefulIngestService>></dt>
 *   <dd>Maps phases to the ingest services</dd>
 * </dl>
 */
public class StatefulBootstrapImpl implements StatefulBootstrap {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The executor service which provides the thread model used when executing ingest phases.  For example, the
     * executor could be backed by a thread pool, which would allow multiple ingest phases to execute in parallel (if
     * the pool as <em>n</em> threads, then <em>n</em> ingest phases could execute simultaneously).  If only one ingest
     * phase should be executed at any given time, then the executor could be configured to use a single thread.
     */
    private ExecutorService executorService;

    /**
     * A Map of ingest phases and their associated ingest services.
     */
    private NavigableMap<IngestPhase, List<StatefulIngestService>> ingestServicesMap;

    /**
     * Invokes the next phase of the ingest process for the identified deposit.  If there are no more phases to be
     * executed, this method returns without throwing an exception.  The current phase is stored in the {@code state}.
     * <p/>
     * The deposit identifier is associated with an ingest process.  An ingest process is made up of ingest phases, and
     * each phase contains the {@link StatefulIngestService}s to be executed.
     * <p/>
     * An ingest phase is executed by a single thread.  The phases are executed in ascending order according to the
     * key of the {@link #getIngestServicesMap() ingest services map}. {@code StatefulIngestService}s for each phase are
     * executed in the order they appear in the {@code List&lt;StatefulIngestService>}.
     *
     * @param depositId the identifier representing the deposit
     * @param state the current state of the ingest process
     */
    @Override
    public void startIngest(final String depositId, final IngestWorkflowState state) {

        // This lock insures that only one set of ingest phases is running at any point in time for a given deposit.
        // It prevents an ingest from being accidentally "resumed" while its previous phases are still being executed.
        //
        // The lock is used twice: once in this thread, and once in the thread launched by the executor service, because
        // there are critical sections in both threads.
        //
        // There are alternate ways to implement this.  For example, the events in the state could be examined, and
        // a determination could be made whether or not an ingest is ready to be resumed.  However, the EventManager is
        // not thread-safe; this seemed the easiest thing to do to prevent accidental resumption.
        //
        // Note: we don't lock only on the depositId because that is already used as a lock to synchronize writes to
        // the InMemoryEventManager.  The lock is namespaced with 'bootstrap-ingest-lock-' and then the deposit id is
        // appended. This insures that there won't be any blocking for deposits with different ids.
        final String lock = "bootstrap-ingest-lock-" + depositId;
        synchronized (lock.intern()) {
           /*
            * Logic to get the next phase
            */

            final IngestPhase currentPhase = nextPhase(state);
            if (currentPhase == null) {
                // we're done
                log.info("No remaining ingest phases for deposit id {}", depositId);
                return;
            }

            if (currentPhase.equals(ingestServicesMap.firstKey())) {
                // Add an INGEST_START event
                addStartEvent(depositId, state);
            }

            // Execute all of the ingest services for the phase in a single thread.
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    // (re)acquire the lock
                    synchronized (lock.intern()) {
                        long start = System.currentTimeMillis();
                        IngestPhase nextPhase = currentPhase;
                        while (nextPhase != null) {
                            log.info("Executing ingest phase {} (of {}) for deposit id {}",
                                    new Object[] {nextPhase, ingestServicesMap.keySet().size(), depositId});
                            addPhaseStart(depositId, state);
                            List<Class<? extends StatefulIngestService>> executedServices =
                                    new ArrayList<Class<? extends StatefulIngestService>>();

                            // Look up the List of ingest services to run in this phase.
                            List<StatefulIngestService> services = ingestServicesMap.get(nextPhase);

                            for (StatefulIngestService ingestService : services) {
                                executedServices.add(ingestService.getClass());
                                try {
                                    ingestService.execute(depositId, state);
                                    if (ingestFailed(depositId, state)) {
                                        addPhaseComplete(start, depositId, state, executedServices);
                                        failIngest(depositId, state, null);
                                        BagUtil.deleteDepositDirectory(depositId, state.getPackage());
                                        return;
                                    }
                                } catch (StatefulIngestServiceException e) {
                                    addPhaseComplete(start, depositId, state, executedServices);
                                    failIngest(depositId, state, e);
                                    BagUtil.deleteDepositDirectory(depositId, state.getPackage());
                                    return;
                                } catch (Throwable e) {
                                    addPhaseComplete(start, depositId, state, executedServices);
                                    failIngest(depositId, state, e);
                                    BagUtil.deleteDepositDirectory(depositId, state.getPackage());
                                    return;
                                }
                            }

                            addPhaseComplete(start, depositId, state, executedServices);


                            if (nextPhase.getPauseIngest()) {
                                break;
                            } else {
                                //Only call next phase if we are moving to that phase, this method sets the current phase on the state.
                                nextPhase = nextPhase(state);
                            }
                        }

                        addSuccessEvent(start, state, depositId);
                    }
                }
            });
        }
    }

    /**
     * Returns an IngestPhase indicating the next phase of ingest.  If there are no more phases, {@code null} is returned.
     * <p/>
     * Ingest phases are keyed by an {@code IngestPhase}, and are returned in ascending order.
     *
     * @param state the current ingest state
     * @return an Integer indicating the next phase of ingest, or {@code null} if no more phases exist
     */
    IngestPhase nextPhase(IngestWorkflowState state) {
        final IngestPhase nextPhase;
        if (state.getIngestPhase() == null) {
            // This is the fist time we've seen this depositId, so we return the first phase.
            state.setIngestPhase(ingestServicesMap.firstKey());
            nextPhase = ingestServicesMap.firstKey();
        } else {
            IngestPhase previousPhase = state.getIngestPhase();
            if (ingestServicesMap.higherKey(previousPhase) != null) {
                // There are more phases
                nextPhase = ingestServicesMap.higherKey(previousPhase);
                state.setIngestPhase(nextPhase);
            } else {
                // There are no more phases
                // Don't set the state phase to null as this would cause the ingest to repeat so leave it set to last
                // phase
                nextPhase = null;
            }
        }

        return nextPhase;
    }

    /**
     * Returns true if there are additional phases, without modifying the current phase.
     *
     * @param state
     * @return
     */
    boolean hasNextPhase(IngestWorkflowState state) {
        if (state.getIngestPhase() == null) {
            return ingestServicesMap.firstKey() != null;
        }

        return ingestServicesMap.higherKey(state.getIngestPhase()) != null;
    }

    /**
     * Composes and adds a phase start event to the state.
     *
     * @param depositId the deposit id
     * @param state the state associated with the deposit
     */
    private void addPhaseStart(String depositId, IngestWorkflowState state) {
        DcsEvent e = state.getEventManager().newEvent(Package.Events.INGEST_PHASE_START);

        // outcome
        e.setOutcome(String.valueOf(state.getIngestPhase().getPhaseNumber()));

        // detail
        e.setDetail("Started ingest phase " + state.getIngestPhase().getPhaseNumber() + " for deposit id " + depositId);

        // target
        for (StatefulIngestService service : ingestServicesMap.get(state.getIngestPhase())) {
            e.addTargets(new DcsEntityReference(service.getClass().getName()));
        }

        state.getEventManager().addEvent(depositId, e);
    }

    /**
     * Composes and adds a phase complete event to the state.
     *
     * @param depositId the deposit id
     * @param state the state associated with the deposit
     */
    private void addPhaseComplete(long start, String depositId, IngestWorkflowState state,
                                  List<Class<? extends StatefulIngestService>> completedServices) {
        DcsEvent e = state.getEventManager().newEvent(Package.Events.INGEST_PHASE_COMPLETE);

        // outcome
        e.setOutcome(String.valueOf(state.getIngestPhase().getPhaseNumber()));

        // detail
        e.setDetail("Completed ingest phase " + state.getIngestPhase().getPhaseNumber() + " for deposit id " + depositId);

        // target
        for (Class<? extends StatefulIngestService> service : completedServices) {
            e.addTargets(new DcsEntityReference(service.getName()));
        }

        state.getEventManager().addEvent(depositId, e);

        log.info("Ingest phase {} (of {}) for deposit id {} completed in {} ms",
                new Object[]{state.getIngestPhase(), ingestServicesMap.keySet().size(), depositId,
                        (System.currentTimeMillis() - start)});
    }

    /**
     * Composes an {@link Package.Events#INGEST_FAIL INGEST_FAIL} event, setting the event outcome to the first line of
     * the supplied {@code Exception}, and putting the full stack trace of the {@code Exception} in the event detail.
     * The target of the event is set to the {@code depositId}.
     * <p/>
     * This method does <em>not</em> add the event to the ingest state.
     *
     * @param depositId the id of the deposit that failed
     * @param state the ingest state associated with the deposit, used to create the failure event
     * @param t the exception that caused the deposit to fail
     * @return the failure event, with the date, outcome, and detail populated
     */
    private DcsEvent failureEvent(String depositId, IngestWorkflowState state, Throwable t) {
        final DcsEvent failureEvent = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
        failureEvent.setOutcome("Ingest " + depositId + " failed: " + t.getMessage());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        t.printStackTrace(ps);
        failureEvent.setDetail(new String(baos.toByteArray()));
        failureEvent.addTargets(new DcsEntityReference(depositId));
        return failureEvent;
    }

    /**
     * Composes an {@link Package.Events#INGEST_START} event and adds it to the workflow state.
     *
     * @param depositId the deposit ID of the ingest being started
     * @param state the ingest state associated with the deposit ID
     */
    private void addStartEvent(String depositId, IngestWorkflowState state) {
        DcsEvent ingestStart = state.getEventManager().newEvent(Package.Events.INGEST_START);
        ingestStart.setOutcome(depositId);
        ingestStart.setDetail("Ingest " + depositId + " started");

        for (File file : state.getPackage().getSerialization().getFiles()) {
            ingestStart.addTargets(new DcsEntityReference(file.getPath()));
        }

        state.getEventManager().addEvent(depositId, ingestStart);
    }

    /**
     * Composes an {@link Package.Events#INGEST_SUCCESS} event and adds it to the workflow state.  The event is only
     * added if the last phase has been completed, otherwise this method simply returns.
     *
     * @param start the start time in milliseconds of the ingest
     * @param state the ingest state associated with the deposit ID
     * @param depositId the deposit DI of the successful ingest
     */
    private void addSuccessEvent(long start, IngestWorkflowState state, String depositId) {
        if (hasNextPhase(state)) {
            // we aren't at the last phase.  Only add the event if we're done.
            return;
        }

        DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_SUCCESS);
        event.setDetail("Ingest of deposit: " + depositId + " successful");
        event.setOutcome(depositId);
        // TODO DC-1477: The target should probably enumerate the business ids of the objects that were added to the system.
        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
        DcsEntityReference ref = new DcsEntityReference(depositId);
        refs.add(ref);
        event.setTargets(refs);
        state.getEventManager().addEvent(depositId, event);
    }


    /**
     * Looks for an INGEST_FAIL event in the state.
     *
     * @param depositId the deposit ID
     * @param state the ingest state associated with the deposit ID
     * @return true if an INGEST_FAIL event exists in the state
     */
    private boolean ingestFailed(String depositId, IngestWorkflowState state) {
        return state.getEventManager().getEventByType(depositId, Package.Events.INGEST_FAIL) != null;
    }

    /**
     * Fails the ingest by creating an INGEST_FAIL event if one doesn't already exist.  Logs the failure.  Sets the
     * current ingest phase to the last phase.
     *
     * @param depositId the deposit ID for the failed ingest
     * @param state the ingest state associated with the deposit ID
     * @param cause the cause of the failure
     */
    private void failIngest(String depositId, IngestWorkflowState state, Throwable cause) {
        DcsEvent failureEvent = state.getEventManager().getEventByType(depositId, Package.Events.INGEST_FAIL);
        if (failureEvent == null) {
            if (cause == null) {
                cause = new Exception("Unknown cause.");
            }
            failureEvent = failureEvent(depositId, state, cause);
            state.getEventManager().addEvent(depositId, failureEvent);
        }

        if (cause == null) {
            log.error("Failed ingest (" + depositId + "): " + failureEvent.getOutcome() + "\n" +
                    failureEvent.getDetail());
        } else {
            log.error("Failed ingest (" + depositId + "): " + cause.getMessage(), cause);
        }

        state.setIngestPhase(ingestServicesMap.lastKey());
    }

    /**
     * The executor service used when running {@link StatefulIngestService}s
     *
     * @return the executor service used to run ingest services
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * The executor service used when running {@link StatefulIngestService}s
     *
     * @param executorService the executor service used to run ingest services
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * The ingest services being executed by this {@code StatefulBootstrap}.  The map is keyed by an integer
     * representing the ingest phase, and the value is a list of ingest services associated with the phase.
     *
     * @return the ingest services, keyed by an integer representing the
     */
    public NavigableMap<IngestPhase, List<StatefulIngestService>> getIngestServicesMap() {
        return ingestServicesMap;
    }

    /**
     * The ingest services being executed by this {@code StatefulBootstrap}.  The map is keyed by an integer
     * representing the ingest phase, and the value is a list of ingest services associated with the phase.
     *
     * @param ingestServicesMap the ingest services, keyed by an integer representing the
     */
    public void setIngestServicesMap(NavigableMap<IngestPhase, List<StatefulIngestService>> ingestServicesMap) {
        this.ingestServicesMap = ingestServicesMap;
    }
    
    /**
     * Adds a new phase to the bootstrap
     * @param phase The number key of the phase to be added
     * @param phaseServices A list of the services to be added for the phase.
     */
    public void addIngestPhase(IngestPhase phase, List<StatefulIngestService> phaseServices) {
        ingestServicesMap.put(phase, phaseServices);
    }
}
