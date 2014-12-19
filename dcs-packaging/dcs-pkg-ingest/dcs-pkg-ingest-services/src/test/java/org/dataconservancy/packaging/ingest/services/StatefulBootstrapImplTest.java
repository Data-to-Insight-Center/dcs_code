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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestPhase;
import org.dataconservancy.packaging.ingest.api.StatefulIngestService;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.IdentifierImpl;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyInt;


public class StatefulBootstrapImplTest {

    private StatefulBootstrapImpl underTest;
    private ExecutorService executorService;
    private NavigableMap<IngestPhase, List<StatefulIngestService>> ingestServicesMap;

    private StatefulIngestService ingestServiceOne;
    private StatefulIngestService ingestServiceTwo;

    private List<StatefulIngestService> phaseOneServices;

    private StatefulIngestService phaseTwoIngestService;

    private BulkIdCreationService idService;

    private IngestPhase phaseOne;
    private IngestPhase phaseTwo;

    private IngestPhase ingestPhase;
    private IngestWorkflowState state;

    private EventManager eventManager;
    private Set<DcsEvent> eventSet;
    private org.dataconservancy.packaging.model.Package pkg;

    @Before
    public void setup() {
        underTest = new StatefulBootstrapImpl();
        executorService = new SynchronousExecutorService();

        ingestServicesMap = new TreeMap<IngestPhase, List<StatefulIngestService>>();
        ingestServiceOne = mock(StatefulIngestService.class);
        ingestServiceTwo = mock(StatefulIngestService.class);
        phaseTwoIngestService = mock(StatefulIngestService.class);

        phaseOneServices = new ArrayList<StatefulIngestService>();
        phaseOneServices.add(ingestServiceOne);
        phaseOneServices.add(ingestServiceTwo);


        phaseOne = new IngestPhaseImpl(1, true);
        phaseTwo = new IngestPhaseImpl(2, true);

        ingestServicesMap.put(phaseOne, phaseOneServices);

        List<StatefulIngestService> phaseTwoServices = new ArrayList<StatefulIngestService>();
        phaseTwoServices.add(phaseTwoIngestService);

        ingestServicesMap.put(phaseTwo, phaseTwoServices);

        underTest.setIngestServicesMap(ingestServicesMap);
        underTest.setExecutorService(executorService);

        idService = mock(BulkIdCreationService.class);
        PackageDescription description = new DescriptionImpl();
        PackageSerialization serialization = new SerializationImpl();
        pkg = new PackageImpl(description, serialization);

        ingestPhase = null;
        state = mock(IngestWorkflowState.class);

        createMockedState();
    }

    private void createMockedState() {

        doAnswer(new Answer<IngestPhase>() {
            @Override
            public IngestPhase answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument the ingest phase to be set", args);
                assertEquals("Expected one argument the ingest phase to be set",
                       1, args.length);
                assertTrue("Expected argument one to be of type int",
                        args[0] instanceof IngestPhase);
                ingestPhase = (IngestPhase) args[0];
                return ingestPhase;
            }
        }).when(state).setIngestPhase(any(IngestPhase.class));

        doAnswer(new Answer<IngestPhase>() {
            @Override
            public IngestPhase answer(InvocationOnMock invocation) throws Throwable {
                return ingestPhase;
            }
        }).when(state).getIngestPhase();

        doAnswer(new Answer<List<Identifier>>() {
            @Override
            public List<Identifier> answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the number and the event type to be added", args);
                assertEquals("Expected two arguments: the number and the event type to be added",
                        2, args.length);
                assertTrue("Expected argument one to be of type int",
                        args[0] instanceof Integer);
                assertTrue("Expected argument to be of type String",
                           args[1] instanceof String);
                int number = (Integer) args[0];
                String eventType = (String) args[1];
                List<Identifier> ids = new ArrayList<Identifier>();
                for( int i = 0; i < number; i++) {
                    Identifier id = new IdentifierImpl(eventType, String.valueOf(i));
                    ids.add(id);
                }
                return ids;
            }
        }).when(idService).create(anyInt(), anyString());

        eventSet = new HashSet<DcsEvent>();
        eventManager = mock(EventManager.class);
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {

                // Extract the event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the key and the event to be added", args);
                assertEquals("Expected two arguments: the key and the event to be added",
                        2, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument two to be of type DcsEvent",
                           args[1] instanceof DcsEvent);
                String key = (String) args[0];
                DcsEvent event = (DcsEvent) args[1];
                eventSet.add(event);
                return null;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));

        doAnswer(new Answer<DcsEvent>() {

            @Override
            public DcsEvent answer(InvocationOnMock invocation)
                    throws Throwable {
                // Extract the Event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the type of the event to be generated", args);
                assertEquals("Expected one argument: the type of the event to be retrieved",
                        1, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                String type = (String) args[0];

                DcsEvent dcsEvent = new DcsEvent();
                dcsEvent.setEventType(type);
                dcsEvent.setDate(DateTime.now().toString());
                dcsEvent.setId("foo");
                return dcsEvent;
            }

        }).when(eventManager).newEvent(anyString());

        doAnswer(new Answer<Collection<DcsEvent>>() {
            @Override
            public Collection<DcsEvent> answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the id and the type of the event to be retrieved", args);
                assertEquals("Expected two arguments: the id and the type of the event to be retrieved",
                        2, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument two to be of type string",
                        args[1] instanceof String);
                String key = (String) args[0];
                String type = (String) args[1];

                List<DcsEvent> events = new ArrayList<DcsEvent>();
                for (DcsEvent event : eventSet) {
                    if (event.getEventType().equalsIgnoreCase(type)) {
                        events.add(event);
                    }
                }
                return events;
            }
        }).when(eventManager).getEvents(anyString(), anyString());

        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the id and the type of the event to be retrieved", args);
                assertEquals("Expected two arguments: the id and the type of the event to be retrieved",
                        2, args.length);
                assertTrue("Expected argument one to be of type string",
                        args[0] instanceof String);
                assertTrue("Expected argument two to be of type string",
                        args[1] instanceof String);
                String key = (String) args[0];
                String type = (String) args[1];

                DcsEvent desiredEvent = null;
                for (DcsEvent event : eventSet) {
                    if (event.getEventType().equalsIgnoreCase(type)) {
                        desiredEvent = event;
                        break;
                    }
                }
                return desiredEvent;
            }
        }).when(eventManager).getEventByType(anyString(), anyString());

        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getPackage()).thenReturn(pkg);
    }

    @Test
    public void testBothServicesExecute() throws StatefulIngestServiceException {
        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);
        verify(ingestServiceTwo).execute("foo", state);

        //Since there is a second phase there should not be an ingest success event.
        assertNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_SUCCESS));
    }

    @Test
    public void testHaltEndsLoop() throws StatefulIngestServiceException {
        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);
        verify(ingestServiceTwo).execute("foo", state);
        verify(phaseTwoIngestService, Mockito.times(0)).execute("foo", state);

        //Since there is a second phase there should not be an ingest success event.
        assertNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_SUCCESS));
    }

    @Test
    public void testBothPhasesExecute() throws StatefulIngestServiceException {
        ingestServicesMap.clear();
        IngestPhase phaseOne = new IngestPhaseImpl(1, false);
        ingestServicesMap.put(phaseOne, phaseOneServices);

        List<StatefulIngestService> phaseTwoServices = new ArrayList<StatefulIngestService>();
        phaseTwoServices.add(phaseTwoIngestService);
        ingestServicesMap.put(phaseTwo, phaseTwoServices);

        underTest.setIngestServicesMap(ingestServicesMap);

        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);
        verify(ingestServiceTwo).execute("foo", state);
        verify(phaseTwoIngestService).execute("foo", state);

        //Since there is no a second phase there should be an ingest success event.
        assertNotNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_SUCCESS));
    }

    @Test
    public void testSinglePhaseFiresIngestSuccessEvent() throws StatefulIngestServiceException {
        ingestServicesMap.clear();
        ingestServicesMap.put(phaseOne, phaseOneServices);
        underTest.setIngestServicesMap(ingestServicesMap);

        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);
        verify(ingestServiceTwo).execute("foo", state);
        verify(phaseTwoIngestService, Mockito.times(0)).execute("foo", state);

        //Since there is no a second phase there should be an ingest success event.
        assertNotNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_SUCCESS));
    }

    @Test
    public void testExceptionInServiceOneFailsIngest() throws StatefulIngestServiceException {

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {

                throw new StatefulIngestServiceException();
            }
        }).when(ingestServiceOne).execute("foo", state);

        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);

        assertNotNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_FAIL));

        verify(ingestServiceTwo, Mockito.times(0)).execute("foo", state);
    }

    @Test
    public void testRuntimeExceptionInServiceOneFailsIngest() throws StatefulIngestServiceException {

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {

                throw new RuntimeException();
            }
        }).when(ingestServiceOne).execute("foo", state);

        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);
        assertNotNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_FAIL));

        verify(ingestServiceTwo, Mockito.times(0)).execute("foo", state);
    }

    @Test
    public void testFailureInServiceOneFailsIngest() throws StatefulIngestServiceException {

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the deposit id and the state", args);
                assertEquals("Expected two arguments: the deposit id and the state",
                        2, args.length);
                assertTrue("Expected argument one to be of type String",
                        args[0] instanceof String);
                assertTrue("Expected argument to be of type IngestWorkflowState",
                           args[1] instanceof IngestWorkflowState);
                String depositId = (String) args[0];
                IngestWorkflowState state = (IngestWorkflowState) args[1];

                DcsEvent failEvent = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                failEvent.setDetail("Unable to get attribute set manager from state object for deposit: " + depositId);
                failEvent.setOutcome("Failed to calculate file attributes");
                DcsEntityReference ref = new DcsEntityReference(depositId);
                List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                refs.add(ref);
                failEvent.setTargets(refs);
                state.getEventManager().addEvent(depositId, failEvent);
                return null;
            }
        }).when(ingestServiceOne).execute("foo", state);

        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);

        assertNotNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_FAIL));

        verify(ingestServiceTwo, Mockito.times(0)).execute("foo", state);
    }

    /**
     * Insures that when the current phase is null, nextPhase(...) will increment to the first phase.
     *
     * @throws Exception
     */
    @Test
    public void testNextPhaseWhenCurrentPhaseIsNull() throws Exception {
        final IngestPhase currentPhase = null;
        final IngestPhase expectedPhase = new IngestPhaseImpl(10, false);
        final NavigableMap<IngestPhase, List<StatefulIngestService>> services =
                new TreeMap<IngestPhase, List<StatefulIngestService>>();
        services.put(expectedPhase, Arrays.asList(mock(StatefulIngestService.class)));


        State state = new State();

        state.ingestPhase = currentPhase;
        underTest.setIngestServicesMap(services);

        assertEquals(expectedPhase, underTest.nextPhase(state));
        assertEquals(expectedPhase, state.ingestPhase);
    }

    /**
     * Insures that when the current phase is some integer, nextPhase(...) will increment to the next integer up.
     *
     * @throws Exception
     */
    @Test
    public void testNextPhaseWhenCurrentPhaseIsNotNull() throws Exception {
        final IngestPhase currentPhase = new IngestPhaseImpl(10, false);
        final IngestPhase expectedPhase = new IngestPhaseImpl(20, false);
        final NavigableMap<IngestPhase, List<StatefulIngestService>> services =
                new TreeMap<IngestPhase, List<StatefulIngestService>>();
        services.put(currentPhase, Arrays.asList(mock(StatefulIngestService.class)));
        services.put(expectedPhase, Arrays.asList(mock(StatefulIngestService.class)));


        State state = new State();
        state.ingestPhase = currentPhase;
        underTest.setIngestServicesMap(services);

        assertEquals(expectedPhase, underTest.nextPhase(state));
        assertEquals(expectedPhase, state.ingestPhase);
    }

    /**
     * Insures that when there is only one phase, nextPhase(...) will increment to the first phase, then return null.
     * The state will keep the latest phase.
     *
     * @throws Exception
     */
    @Test
    public void testNextPhaseWhenThereIsOnlyOnePhase() throws Exception {
        final IngestPhase currentPhase = null;
        final IngestPhase expectedPhase = new IngestPhaseImpl(10, false);
        final NavigableMap<IngestPhase, List<StatefulIngestService>> services =
                new TreeMap<IngestPhase, List<StatefulIngestService>>();
        services.put(expectedPhase, Arrays.asList(mock(StatefulIngestService.class)));


        State state = new State();
        state.ingestPhase = currentPhase;
        underTest.setIngestServicesMap(services);

        assertEquals(expectedPhase, underTest.nextPhase(state));
        assertEquals(expectedPhase, state.ingestPhase);

        assertEquals(null, underTest.nextPhase(state));
        assertEquals(expectedPhase, state.ingestPhase);
    }

    /**
     * A test which tests test logic :)
     *
     * Makes sure that using a Lock and a Condition to mimic a long-running ingest service works.
     *
     * @throws Exception
     */
    @Test
    public void testBootstrapBlocks() throws Exception {
        final String depositId = "fooDeposit";
        final Lock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        final BooleanHolder blockerExecuted = new BooleanHolder(false);
        final BooleanHolder probeExecuted = new BooleanHolder(false);
        final IngestPhase ingestPhase1 = new PrivateIngestPhaseImpl(false, 1);
        final IngestPhase ingestPhase2 = new PrivateIngestPhaseImpl(false, 2);

        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(2);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, Long.MAX_VALUE, TimeUnit.MILLISECONDS, queue);
        underTest.setExecutorService(executor);

        /**
         * This is an ingest service that will block forever until this test tells it to unblock.  It is the first
         * and only service in phase 1.  This means that the ingest pipeline will block until this test tells it to
         * move forward.
         */
        final StatefulIngestService blockingSvc = new StatefulIngestService() {
            @Override
            public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
                lock.lock();
                try {
                    c.awaitUninterruptibly();
                    blockerExecuted.value = true;
                } finally {
                    lock.unlock();
                }
            }
        };

        /**
         * This is the one and only ingest service in phase 2. It will execute after phase 1 is unblocked.
         */
        final StatefulIngestService probeSvc = new StatefulIngestService() {
            @Override
            public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
                probeExecuted.value = true;
            }
        };

        NavigableMap<IngestPhase, List<StatefulIngestService>> services =
                new TreeMap<IngestPhase, List<StatefulIngestService>>();
        services.put(ingestPhase1, Arrays.asList(blockingSvc));
        services.put(ingestPhase2, Arrays.asList(probeSvc));
        underTest.setIngestServicesMap(services);

        underTest.startIngest(depositId, state);

        Thread.sleep(2000);
        assertFalse(blockerExecuted.value);
        assertFalse(probeExecuted.value);

        lock.lock();
        c.signal();
        lock.unlock();

        Thread.sleep(2000);
        assertTrue(blockerExecuted.value);
        assertTrue(probeExecuted.value);
    }

    /**
     * Insures that invoking startIngest blocks when an ingest for the same deposit id is already running.
     * 
     * @throws Exception
     */
    @Test
    public void testBootstrapBlocksWhenResumeIsCalledBeforePauseIsReached() throws Exception {
        final String depositId = "fooDeposit";
        final Lock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        final BooleanHolder blockerExecuted = new BooleanHolder(false);
        final BooleanHolder probeExecuted = new BooleanHolder(false);
        final IngestPhase ingestPhase1 = new PrivateIngestPhaseImpl(true, 1);
        final IngestPhase ingestPhase2 = new PrivateIngestPhaseImpl(false, 2);

        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(2);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, Long.MAX_VALUE, TimeUnit.MILLISECONDS, queue);

        underTest.setExecutorService(executor);

        /**
         * This is an ingest service that will block forever until this test tells it to unblock.  It is the first
         * and only service in phase 1.  This means that the ingest pipeline will block until this test tells it to
         * move forward.
         */
        final StatefulIngestService blockingSvc = new StatefulIngestService() {
            @Override
            public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
                lock.lock();
                try {
                    blockerExecuted.value = true;
                    c.awaitUninterruptibly();
                } finally {
                    lock.unlock();
                }
            }
        };

        /**
         * This is the one and only ingest service in phase 2. It will execute after phase 1 is unblocked.
         */
        final StatefulIngestService probeSvc = new StatefulIngestService() {
            @Override
            public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
                probeExecuted.value = true;
            }
        };

        NavigableMap<IngestPhase, List<StatefulIngestService>> services =
                new TreeMap<IngestPhase, List<StatefulIngestService>>();
        services.put(ingestPhase1, Arrays.asList(blockingSvc));
        services.put(ingestPhase2, Arrays.asList(probeSvc));
        underTest.setIngestServicesMap(services);

        final IngestWorkflowState state = new PrivateIngestStateImpl();

        // This call launches the ingest in a new thread, similar to a user submitting a package
        // for ingest from the UI.  This should start an ingest, but it will block until the condition for
        // blockingSvc is signaled.  This ingest thread will block forever until we tell it to unblock.
        Thread ingestStart = new Thread(new Runnable() {
            @Override
            public void run() {
                underTest.startIngest(depositId, state);
            }
        });

        ingestStart.setName("Ingest Start");
        ingestStart.start();

        // Give a chance for the thread to run
        Thread.sleep(5000);

        assertTrue(blockerExecuted.value);  // the blockingSvc execute method has been reached.
        assertFalse(probeExecuted.value);   // the probeSvc execute method has not been reached, because blockingSvc is
                                            // blocked forever.

        // This call simulates what happens when the user resumes an ingest.  We call startIngest in a
        // separate thread.  This thread should block because StatefulBootstrapImpl will only execute one set of
        // phases at a time
        Thread ingestResume = new Thread(new Runnable() {
            @Override
            public void run() {
                underTest.startIngest(depositId, state);
            }
        });

        ingestResume.setName("Ingest Resume");
        ingestResume.start();

        Thread.sleep(5000);

        assertFalse(probeExecuted.value);   // the probeSvc execute method has not been reached, because blockingSvc is
                                            // blocked forever.

        // Obtain the monitor and signal the phase 1 ingest service to unblock.
        // The 'Ingest Start' thread should complete after the phase 1 ingest service completes, because
        // the phase 1 ingest service is set to pause.
        // After phase 1 has been unblocked, phase 2 will be invoked, because we already started the "Ingest Resume"
        // thread above.
        lock.lock();
        c.signal();
        lock.unlock();

        // at this point, the "Ingest Resume" thread is moving forward

        ingestStart.join();  // just to prove that "Ingest Start" is complete, we wait for it here

        Thread.sleep(5000);  // Give the "Ingest Resume" thread time to complete

        assertTrue(probeExecuted.value);  // After unblocking phase 1 ingest service, phase 2 will have executed and
                                          // set this flag
    }

    /**
     * Insures extracted files are cleaned up when a service emits an ingest.fail event.
     *
     * @throws Exception
     */
    @Test
    public void testCleanupWhenIngestFailEmitted() throws Exception {
        final File extractDir = File.createTempFile("StatefulBootstrapImplTest-", ".dir");
        FileUtils.forceDelete(extractDir);
        FileUtils.forceMkdir(extractDir);
        final File baseDir = new File("foo");
        FileUtils.forceMkdir(new File(extractDir, baseDir.getName()));
        final File absPackageDir = new File(extractDir, baseDir.getName());
        assertTrue(absPackageDir.exists());

        pkg.getSerialization().setExtractDir(extractDir);
        pkg.getSerialization().setBaseDir(baseDir);

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                final DcsEvent failureEvent = new DcsEvent();
                failureEvent.setEventType(Package.Events.INGEST_FAIL);
                state.getEventManager().addEvent("foo", failureEvent);
                return null;
            }
        }).when(ingestServiceOne).execute("foo", state);

        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);
        assertNotNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_FAIL));
        verify(ingestServiceTwo, Mockito.times(0)).execute("foo", state);
        assertFalse(absPackageDir.exists());
    }

    /**
     * Insures extracted files are cleaned up when a service throws a StatefulIngestServiceException.
     *
     * @throws Exception
     */
    @Test
    public void testCleanupWhenStatefulIngestServiceExceptionThrown() throws Exception {
        final File extractDir = File.createTempFile("StatefulBootstrapImplTest-", ".dir");
        FileUtils.forceDelete(extractDir);
        FileUtils.forceMkdir(extractDir);
        final File baseDir = new File("foo");
        FileUtils.forceMkdir(new File(extractDir, baseDir.getName()));
        final File absPackageDir = new File(extractDir, baseDir.getName());
        assertTrue(absPackageDir.exists());

        pkg.getSerialization().setExtractDir(extractDir);
        pkg.getSerialization().setBaseDir(baseDir);

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                throw new StatefulIngestServiceException();
            }
        }).when(ingestServiceOne).execute("foo", state);

        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);
        assertNotNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_FAIL));
        verify(ingestServiceTwo, Mockito.times(0)).execute("foo", state);
        assertFalse(absPackageDir.exists());
    }

    /**
     * Insures extracted files are cleaned up when a service throws any Throwable
     *
     * @throws Exception
     */
    @Test
    public void testCleanupWhenThrowableThrown() throws Exception {
        final File extractDir = File.createTempFile("StatefulBootstrapImplTest-", ".dir");
        FileUtils.forceDelete(extractDir);
        FileUtils.forceMkdir(extractDir);
        final File baseDir = new File("foo");
        FileUtils.forceMkdir(new File(extractDir, baseDir.getName()));
        final File absPackageDir = new File(extractDir, baseDir.getName());
        assertTrue(absPackageDir.exists());

        pkg.getSerialization().setExtractDir(extractDir);
        pkg.getSerialization().setBaseDir(baseDir);

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                throw new AssertionError();
            }
        }).when(ingestServiceOne).execute("foo", state);

        underTest.startIngest("foo", state);

        verify(ingestServiceOne).execute("foo", state);
        assertNotNull(state.getEventManager().getEventByType("foo", Package.Events.INGEST_FAIL));
        verify(ingestServiceTwo, Mockito.times(0)).execute("foo", state);
        assertFalse(absPackageDir.exists());
    }

    /**
     * Demonstrates that the bootstrap does <em>not</em> clean up extracted files upon a successful ingest.  It is
     * the responsibility of the CleanupIngestService.
     *
     * @throws Exception
     */
    @Test
    public void testCleanupWhenSuccessful() throws Exception {
        final File extractDir = File.createTempFile("StatefulBootstrapImplTest-", ".dir");
        FileUtils.forceDelete(extractDir);
        FileUtils.forceMkdir(extractDir);
        final File baseDir = new File("foo");
        FileUtils.forceMkdir(new File(extractDir, baseDir.getName()));
        final File absPackageDir = new File(extractDir, baseDir.getName());
        assertTrue(absPackageDir.exists());

        pkg.getSerialization().setExtractDir(extractDir);
        pkg.getSerialization().setBaseDir(baseDir);

        underTest.startIngest("foo", state);

        // The bootstrap impl does _not_ clean up the extracted files on success.  the cleanupservice does this.
        assertTrue(absPackageDir.exists());
    }

    public class SynchronousExecutorService extends AbstractExecutorService {
        private boolean shutdown;

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            shutdown = true;
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void execute(final Runnable command) {
            command.run();
        }
    }

    private class State implements IngestWorkflowState {

        private AttributeSetManager attributeSetManager;

        private BusinessObjectManager businessObjectManager;

        private EventManager eventManager;

        private org.dataconservancy.packaging.model.Package thePackage;

        private IngestPhase ingestPhase;
        
        private String userId;

        private boolean isCancelled = false;

        @Override
        public AttributeSetManager getAttributeSetManager() {
            return attributeSetManager;
        }

        @Override
        public BusinessObjectManager getBusinessObjectManager() {
            return businessObjectManager;
        }

        @Override
        public EventManager getEventManager() {
            return eventManager;
        }

        @Override
        public org.dataconservancy.packaging.model.Package getPackage() {
            return thePackage;
        }

        @Override
        public IngestPhase getIngestPhase() {
            return ingestPhase;
        }

        @Override
        public void setIngestPhase(IngestPhase ingestPhase) {
            this.ingestPhase = ingestPhase;
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public void setCancelled() {
            this.isCancelled = true;
        }

        @Override
        public void setIngestUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public String getIngestUserId() {
            return userId;
        }
    }

    /**
     * Holds a boolean value so anonymous inner classes can modify the state.
     */
    private class BooleanHolder {
        boolean value = false;

        private BooleanHolder(boolean value) {
            this.value = value;
        }
    }

    /**
     *
     */
    private class PrivateIngestPhaseImpl implements IngestPhase, Comparable<IngestPhase> {
        private boolean pauseIngest;
        private Integer phaseNumber;

        private PrivateIngestPhaseImpl() {

        }

        private PrivateIngestPhaseImpl(boolean pauseIngest, Integer phaseNumber) {
            this.pauseIngest = pauseIngest;
            this.phaseNumber = phaseNumber;
        }

        @Override
        public boolean getPauseIngest() {
            return pauseIngest;
        }

        @Override
        public void setPhaseNumber(Integer phase) {
            this.phaseNumber = phase;

        }

        @Override
        public Integer getPhaseNumber() {
            return this.phaseNumber;
        }

        @Override
        public void setPauseIngest(boolean haltIngest) {
            this.pauseIngest = haltIngest;

        }

        @Override
        public int compareTo(IngestPhase ingestPhase) {
            return this.phaseNumber.compareTo(ingestPhase.getPhaseNumber());
        }

        @Override
        public String toString() {
            return String.valueOf(phaseNumber);
        }
    }

    private class PrivateIngestStateImpl implements IngestWorkflowState {
        private IngestPhase phase;

        private boolean isCancelled = false;
        private String userId;

        @Override
        public AttributeSetManager getAttributeSetManager() {
            return null;
        }

        @Override
        public EventManager getEventManager() {
            return eventManager;
        }

        @Override
        public BusinessObjectManager getBusinessObjectManager() {
            // Default method body
            return null;
        }

        @Override
        public org.dataconservancy.packaging.model.Package getPackage() {
            return pkg;
        }

        @Override
        public IngestPhase getIngestPhase() {
            return phase;
        }

        @Override
        public void setIngestPhase(IngestPhase ingestPhase) {
            this.phase = ingestPhase;
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public void setCancelled() {
            isCancelled = true;
        }

        @Override
        public void setIngestUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public String getIngestUserId() {
            return userId;
        }
    }
}