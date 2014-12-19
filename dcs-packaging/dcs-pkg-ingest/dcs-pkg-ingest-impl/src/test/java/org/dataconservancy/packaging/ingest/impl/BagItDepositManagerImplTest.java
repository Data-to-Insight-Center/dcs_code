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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;

import org.dataconservancy.dcs.contentdetection.api.ContentDetectionService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.packaging.ingest.api.Http;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulBootstrap;
import org.dataconservancy.packaging.ingest.shared.BagUtil;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.dataconservancy.ui.exceptions.UnpackException;
import org.dataconservancy.ui.util.PackageExtractor;
import org.dataconservancy.ui.util.PackageSelector;
import org.dataconservancy.ui.util.PackageSelectorImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 *
 */
public class BagItDepositManagerImplTest {

    private BagItDepositManagerImpl underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new BagItDepositManagerImpl();
    }

    /**
     * This is a test for the {@link BagItDepositManagerImpl#deposit(java.io.InputStream, String, String, java.util.Map)}
     * method.  This method is testing for successful execution of the {@code deposit} method - we are not testing how
     * {@code deposit} responds to exceptional conditions; therefore, all of the mock instances are configured to
     * succeed.
     * <p/>
     * The {@code BagItDepositManagerImpl deposit} method relies on a bunch of supporting methods to execute its
     * responsibilities.  If there are failures in this test class, it is probably better to insure that the tests for
     * the supporting methods are passing before you attempt to troubleshoot failures in this class.  If the supporting
     * methods are failing their tests, then that means that this test method will probably fail too.
     * <p/>
     * The deposit(...) method:
     * <ol>
     *  <li>Generates a Deposit identifier</li>
     *  <li>Obtains State from the IngestWorkflowStateFactory</li>
     *  <li>Adds Deposit event</li>
     *  <li>Extracts the Package and Populates PackageSerialization with files.</li>
     *  <li>Adds Extraction event</li>
     *  <li>Starts Ingest</li>
     *  <li>Composes and returns Deposit Info</li>
     * </ol>
     *
     * @throws Exception
     */
    @Test
    public void testDeposit() throws Exception {
        int idCounter = 0;
        final String depositId = "foo";
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();
        final List<DcsEvent> events = new ArrayList<DcsEvent>();
        final File extractedFileOne = new File("extractedFileOne.txt");
        final File extractedFileTwo = new File("extractedFileTwo.txt");
        final List<File> extractedFiles = Arrays.asList(extractedFileOne, extractedFileTwo);
        final String archiveName = "foo";
        final String archiveExt = ".tar.gz";
        final String archiveFileName = archiveName + archiveExt;
        final File expectedBaseDirectory = new File(new File("foo"), archiveName);
        final String packageExtractDirectory = System.getProperty("java.io.tmpdir");

        /*
         * Mock the IdService
         * - to create an Identifier for the Deposit.
         */
        final IdService idService = mock(IdService.class);
        when(idService.create(IdTypes.DEPOSIT)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                // Default method body
                return null;
            }

            @Override
            public String getUid() {
                return depositId;
            }

            @Override
            public String getType() {
                return IdTypes.DEPOSIT;
            }
        });

        /*
         * Mock the State Manager
         * - verifying that invoking the deposit(...) method will eventually
         * - add the proper state to the state manager.  After we invoke deposit(...) we must verify that
         * - the 'stateManagerPutCalled' was flipped to true.
         */
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        final BooleanHolder stateManagerPutCalled = new BooleanHolder();
        assertFalse(stateManagerPutCalled.value); // verify assumptions

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals(depositId, invocationOnMock.getArguments()[0]);
                assertNotNull(invocationOnMock.getArguments()[1]);
                assertEquals(state, invocationOnMock.getArguments()[1]);
                stateManagerPutCalled.value = true;
                return null;
            }
        }).when(stateManager).put(depositId, state);

        when(stateManager.get(depositId)).thenReturn(state);

        /*
         * Mock the Stateful Bootstrap
         * - verifying that invoking the deposit(...) method will eventually invoke the bootstrapper
         * - with the proper deposit id and state.  After invoking deposit(...) we must verify that the
         * - 'bootstrapStartIngestCalled' was flipped to true.
         */
        final StatefulBootstrap bootstrap = mock(StatefulBootstrap.class);
        final BooleanHolder bootstrapStartIngestCalled = new BooleanHolder();
        assertFalse(bootstrapStartIngestCalled.value); // verify assumptions

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals(depositId, invocationOnMock.getArguments()[0]);
                assertNotNull(invocationOnMock.getArguments()[1]);
                assertEquals(state, invocationOnMock.getArguments()[1]);
                bootstrapStartIngestCalled.value = true;
                return null;
            }
        }).when(bootstrap).startIngest(depositId, state);

        /*
         * Mock the State Factory
         * - To return the locally created instance of IngestWorkflowStateImpl.  We attach our mocked collaborators
         * - to the local instance of IngestWorkflowStateImpl.
         */
        final IngestWorkflowStateFactory stateFactory = mock(IngestWorkflowStateFactory.class);
        when(stateFactory.newInstance()).thenReturn(state);

        /*
         * Mock the Event Manager
         * - We capture the events added to the Event Manager using a local List<DcsEvent>.  After invoking
         * - deposit(...) we can look at the list to make sure the proper events were fired.
         * - We mock the creation of new events: a Deposit event and a File Extraction event.
         */
        final EventManager em = mock(EventManager.class);

        // Capture the events added to the event manager in the 'events' list.
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals("Unexpected value for depositId", depositId, invocationOnMock.getArguments()[0]);
                events.add((DcsEvent) invocationOnMock.getArguments()[1]);
                return null;
            }
        }).when(em).addEvent(anyString(), (DcsEvent) anyObject());

        final DcsEvent depositEvent = new DcsEvent();
        depositEvent.setId("depositEventId" + String.valueOf(idCounter++));
        depositEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        depositEvent.setEventType(Package.Events.DEPOSIT);
        when(em.newEvent(Package.Events.DEPOSIT)).thenReturn(depositEvent);

        final DcsEvent extractionEvent = new DcsEvent();
        extractionEvent.setId("extractionEventId" + String.valueOf(idCounter++));
        extractionEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        extractionEvent.setEventType(Package.Events.FILE_EXTRACTION);
        when(em.newEvent(Package.Events.FILE_EXTRACTION)).thenReturn(extractionEvent);

        when(em.getEventByType(depositId, Package.Events.DEPOSIT)).thenReturn(depositEvent);

        /*
         * Mock the PackageSelector to return a PackageExtractor
         * - to return a local list of Files
         */
        final PackageSelector selector = mock(PackageSelector.class);
        final PackageExtractor extractor = mock(PackageExtractor.class);
        when(selector.selectPackageExtractor((InputStream)any(), (Map)any())).thenReturn(extractor);
        when(extractor.getFilesFromPackageStream(anyString(), anyString(), (InputStream) anyObject())).thenReturn(extractedFiles);
        when(extractor.getExtractDirectory()).thenReturn(packageExtractDirectory);

        /*
         * Create an empty Package instance.  After invoking deposit(...) the extracted files should be added to the
         * Package Serialization.
         */
        final org.dataconservancy.packaging.model.Package pkg = new PackageImpl(new DescriptionImpl(),
                new SerializationImpl());

        /*
         * Attach the Event Manager and the Package to the IngestWorkflowState.  Remember the state is being returned
         * by the mocked State Factory.
         */
        state.setEventManager(em);
        state.setPackage(pkg);

        /*
         * Attach our mocked State Factory, State Manager, Id Service, Package Selector, and Bootstrap to the
         * deposit manager under test.
         */
        underTest.setStateFactory(stateFactory);
        underTest.setIdService(idService);
        underTest.setPackageSelector(selector);
        underTest.setStateManager(stateManager);
        underTest.setBootstrap(bootstrap);

        final String user = "user";
        /*
         * Populate the metadata map with the required metadata
         */
        Map<String, String> metadata = new HashMap<String, String>();
                metadata.put(Http.Header.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archiveFileName + "\"");
                metadata.put(Http.Header.X_DCS_AUTHENTICATED_USER, user);

        /*
         * Invoke the method under test.
         */
        final DepositInfo di =
                underTest.deposit(new NullInputStream(1024, false, false), Http.MimeType.APPLICATION_GZIP,
                        Package.Types.BAGIT_DCS_10, metadata);

        /*
         * Verify that the mocks were called.  If they weren't called, it's likely that the rest of the assertions
         * will fail.
         */
        verify(idService).create(IdTypes.DEPOSIT);

        verify(stateManager).put(depositId, state);
        assertTrue(stateManagerPutCalled.value);
        verify(stateManager).get(depositId);

        verify(bootstrap).startIngest(depositId, state);
        assertTrue(bootstrapStartIngestCalled.value);

        verify(stateFactory).newInstance();

        verify(em).newEvent(Package.Events.DEPOSIT);
        verify(em).newEvent(Package.Events.FILE_EXTRACTION);
        verify(em).addEvent(depositId, depositEvent);
        verify(em).addEvent(depositId, extractionEvent);
        verify(em, atLeastOnce()).getEventByType(depositId, Package.Events.DEPOSIT);

        verify(extractor).getFilesFromPackageStream(anyString(), anyString(), (InputStream) anyObject());
        verify(extractor).getExtractDirectory();

        // Generates Deposit identifier

        // Obtains State from the IngestWorkflowStateFactory
        // - Event Manager
        // - Package
        // - Attribute Set Manager

        // Adds Deposit event

        assertTrue(events.contains(depositEvent));
        assertEquals(depositId, depositEvent.getOutcome());
        assertEquals(1, depositEvent.getTargets().size());
        assertTrue(depositEvent.getTargets().contains(new DcsEntityReference(archiveFileName)));

        // Extracts the Package and Populates PackageSerialization with files.

        assertEquals(extractedFiles.size(), pkg.getSerialization().getFiles().size());
        for (File f : extractedFiles) {
            assertTrue(pkg.getSerialization().getFiles().contains(f));
        }

        //Since we've mocked the extractor to return the list of files right away we'll only get foo.tar instead of foo.
        assertEquals(expectedBaseDirectory, pkg.getSerialization().getBaseDir());

        assertTrue(events.contains(extractionEvent));
        assertEquals(String.valueOf(extractedFiles.size()), extractionEvent.getOutcome());
        assertEquals(extractedFiles.size(), extractionEvent.getTargets().size());
        for (File f : extractedFiles) {
            assertTrue(extractionEvent.getTargets().contains(new DcsEntityReference(f.getName())));
        }
        
        //Check the user was set
        String stateUser = state.getIngestUserId();
        assertEquals(user, stateUser);


        // Starts Ingest

        // Composes and returns Deposit Info

        assertNotNull(di);

    }

    @Test
    public void testNestedExtractionGeneratesCorrectDirectories() throws Exception {
        int idCounter = 0;
        final String depositId = "foo";
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();
        final List<DcsEvent> events = new ArrayList<DcsEvent>();
        final File extractedFileOne = File.createTempFile("foo", ".tar");
        final List<File> extractedFiles = Arrays.asList(extractedFileOne);
        final String archiveName = "foo";
        final String archiveExt = ".tar.gz";
        final String archiveFileName = archiveName + archiveExt;
        final File expectedBaseDirectory = new File(new File(depositId), archiveName);
        final String packageExtractDirectory = System.getProperty("java.io.tmpdir");


        /*
         * Mock the IdService
         * - to create an Identifier for the Deposit.
         */
        final IdService idService = mock(IdService.class);
        when(idService.create(IdTypes.DEPOSIT)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                // Default method body
                return null;
            }

            @Override
            public String getUid() {
                return depositId;
            }

            @Override
            public String getType() {
                return IdTypes.DEPOSIT;
            }
        });

        /*
         * Mock the State Manager
         * - verifying that invoking the deposit(...) method will eventually
         * - add the proper state to the state manager.  After we invoke deposit(...) we must verify that
         * - the 'stateManagerPutCalled' was flipped to true.
         */
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        final BooleanHolder stateManagerPutCalled = new BooleanHolder();
        assertFalse(stateManagerPutCalled.value); // verify assumptions

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals(depositId, invocationOnMock.getArguments()[0]);
                assertNotNull(invocationOnMock.getArguments()[1]);
                assertEquals(state, invocationOnMock.getArguments()[1]);
                stateManagerPutCalled.value = true;
                return null;
            }
        }).when(stateManager).put(depositId, state);

        when(stateManager.get(depositId)).thenReturn(state);

        /*
         * Mock the Stateful Bootstrap
         * - verifying that invoking the deposit(...) method will eventually invoke the bootstrapper
         * - with the proper deposit id and state.  After invoking deposit(...) we must verify that the
         * - 'bootstrapStartIngestCalled' was flipped to true.
         */
        final StatefulBootstrap bootstrap = mock(StatefulBootstrap.class);
        final BooleanHolder bootstrapStartIngestCalled = new BooleanHolder();
        assertFalse(bootstrapStartIngestCalled.value); // verify assumptions

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals(depositId, invocationOnMock.getArguments()[0]);
                assertNotNull(invocationOnMock.getArguments()[1]);
                assertEquals(state, invocationOnMock.getArguments()[1]);
                bootstrapStartIngestCalled.value = true;
                return null;
            }
        }).when(bootstrap).startIngest(depositId, state);

        /*
         * Mock the State Factory
         * - To return the locally created instance of IngestWorkflowStateImpl.  We attach our mocked collaborators
         * - to the local instance of IngestWorkflowStateImpl.
         */
        final IngestWorkflowStateFactory stateFactory = mock(IngestWorkflowStateFactory.class);
        when(stateFactory.newInstance()).thenReturn(state);

        /*
         * Mock the Event Manager
         * - We capture the events added to the Event Manager using a local List<DcsEvent>.  After invoking
         * - deposit(...) we can look at the list to make sure the proper events were fired.
         * - We mock the creation of new events: a Deposit event and a File Extraction event.
         */
        final EventManager em = mock(EventManager.class);

        // Capture the events added to the event manager in the 'events' list.
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals("Unexpected value for depositId", depositId, invocationOnMock.getArguments()[0]);
                events.add((DcsEvent) invocationOnMock.getArguments()[1]);
                return null;
            }
        }).when(em).addEvent(anyString(), (DcsEvent) anyObject());

        final DcsEvent depositEvent = new DcsEvent();
        depositEvent.setId("depositEventId" + String.valueOf(idCounter++));
        depositEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        depositEvent.setEventType(Package.Events.DEPOSIT);
        when(em.newEvent(Package.Events.DEPOSIT)).thenReturn(depositEvent);

        final DcsEvent extractionEvent = new DcsEvent();
        extractionEvent.setId("extractionEventId" + String.valueOf(idCounter++));
        extractionEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        extractionEvent.setEventType(Package.Events.FILE_EXTRACTION);
        when(em.newEvent(Package.Events.FILE_EXTRACTION)).thenReturn(extractionEvent);

        when(em.getEventByType(depositId, Package.Events.DEPOSIT)).thenReturn(depositEvent);

        /*
         * Mock the PackageSelector to return a PackageExtractor
         * - to return a local list of Files
         */
        final PackageSelector selector = mock(PackageSelector.class);
        final PackageExtractor extractor = mock(PackageExtractor.class);
        when(selector.selectPackageExtractor((InputStream)any(), (Map)any())).thenReturn(extractor);
        when(extractor.getFilesFromPackageStream(anyString(), anyString(), (InputStream) anyObject())).thenReturn(extractedFiles);
        when(extractor.getExtractDirectory()).thenReturn(packageExtractDirectory);

        /*
         * Create an empty Package instance.  After invoking deposit(...) the extracted files should be added to the
         * Package Serialization.
         */
        final org.dataconservancy.packaging.model.Package pkg = new PackageImpl(new DescriptionImpl(),
                new SerializationImpl());

        /*
         * Attach the Event Manager and the Package to the IngestWorkflowState.  Remember the state is being returned
         * by the mocked State Factory.
         */
        state.setEventManager(em);
        state.setPackage(pkg);

        ContentDetectionService detectionService = mock(ContentDetectionService.class);
        when(detectionService.detectFormats(any(File.class))).thenReturn(new ArrayList<DcsFormat>());
        
        /*
         * Attach our mocked State Factory, State Manager, Id Service, Package Selector, and Bootstrap to the
         * deposit manager under test.
         */
        underTest.setStateFactory(stateFactory);
        underTest.setIdService(idService);
        underTest.setPackageSelector(selector);
        underTest.setStateManager(stateManager);
        underTest.setBootstrap(bootstrap);
        underTest.setContentDetectionService(detectionService);

        /*
         * Populate the metadata map with the required metadata
         */
        Map<String, String> metadata = new HashMap<String, String>();
                metadata.put(Http.Header.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archiveFileName + "\"");

        /*
         * Invoke the method under test.
         */
        final DepositInfo di =
                underTest.deposit(new NullInputStream(1024, false, false), Http.MimeType.APPLICATION_GZIP,
                        Package.Types.BAGIT_DCS_10, metadata);


        //Since we've mocked the extractor to return the list of files right away we'll only get foo.tar instead of foo.
        assertEquals(expectedBaseDirectory, pkg.getSerialization().getBaseDir());

    }
    
    /**
     * Insures a null object is returned when a {@code null} deposit id is provided to the
     * getDepositInfo(...) method.
     *
     * @throws Exception
     */
    @Test
    public void testGetDepositInfoNullId() throws Exception {
        final String nullId = null;
        final InMemoryDepositStateManager stateMgr = new InMemoryDepositStateManager();
        assertNull(stateMgr.get(nullId));

        underTest.setStateManager(stateMgr);

        assertNull(underTest.getDepositInfo(nullId));
    }

    /**
     * Insures a null object is returned when a non-existent deposit id is provided to the
     * getDepositInfo(...) method.
     *
     * @throws Exception
     */
    @Test
    public void testGetDepositInfoNonExistentId() throws Exception {
        final String nonExistentId = "foo";

        // Verify assumptions
        final InMemoryDepositStateManager stateMgr = new InMemoryDepositStateManager();
        assertNull(stateMgr.get(nonExistentId));

        // Set the StateManager on the DepositManager
        underTest.setStateManager(stateMgr);

        assertNull("Expected a null DepositInfo object.", underTest.getDepositInfo(nonExistentId));
    }

    /**
     * Insures a DepositInfo object is returned when an existing deposit id is provided to the
     * getDepositInfo(...) method.
     *
     * @throws Exception
     */
    @Test
    public void testGetDepositInfoExistingId() throws Exception {
        final String existingId = "foo";
        final InMemoryDepositStateManager stateMgr = new InMemoryDepositStateManager();

        // Set up the State and State Manager
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();
        // Add the State to the Manager
        stateMgr.put(existingId, state);
        // Verify assumptions
        assertNotNull(stateMgr.get(existingId));

        // Set the StateManager on the DepositManager
        underTest.setStateManager(stateMgr);

        // Set up the Event Manager to contain a INGEST_SUCCESS event
        EventManager em = mock(EventManager.class);
        // Set up the INGEST_SUCCESS event
        final DcsEvent successEvent = new DcsEvent();
        successEvent.setId("successEventId");
        successEvent.setEventType(Package.Events.INGEST_SUCCESS);
        successEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        // Mock behaviors on the EventManager to return the event when asked.
        when(em.getEventByType(existingId, Package.Events.INGEST_SUCCESS)).thenReturn(successEvent);
        when(em.getEvents(existingId, null)).thenReturn(Arrays.asList(successEvent));
        // Set the EventManager on the IngestState
        state.setEventManager(em);

        assertNotNull("Expected a DepositInfo object to be returned for a valid deposit identifier.",
                underTest.getDepositInfo(existingId));
    }

    /**
     * Insures that the DepositInfo from a failed ingest has the proper information:
     * <dl>
     * <dd>completed</dd>
     * <dt>true</dt>
     * <dd>successful</dd>
     * <dt>false</dt>
     * <dd>summary</dd>
     * <dt>contains the string 'errors' and the event date</dt>
     * <dd>depositStatus document</dd>
     * <dt>is greater than zero bytes</dt>
     * </dl>
     *
     * @throws Exception
     */
    @Test
    public void testGetDepositInfoContentFailedIngest() throws Exception {
        final String depositId = "foo";
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        when(stateManager.get(depositId)).thenReturn(state);
        underTest.setStateManager(stateManager);

        final DcsEvent failureEvent = new DcsEvent();
        final long eventTs = System.currentTimeMillis();
        failureEvent.setDate(DateUtility.toIso8601(eventTs));
        failureEvent.setEventType(Package.Events.INGEST_FAIL);
        final EventManager em = mock(EventManager.class);
        when(em.getEventByType(depositId, Package.Events.INGEST_FAIL)).thenReturn(failureEvent);
        when(em.getEvents(depositId, null)).thenReturn(Arrays.asList(failureEvent));
        state.setEventManager(em);

        final DepositInfo di = underTest.getDepositInfo(depositId);

        verify(stateManager, atLeastOnce()).get(depositId);
        verify(em, atLeastOnce()).getEventByType(depositId, Package.Events.INGEST_FAIL);
        verify(em, atLeastOnce()).getEvents(depositId, null);

        assertNotNull("Expected a non-null DepositInfo", di);

        assertEquals(depositId, di.getDepositID());
        assertTrue(di.hasCompleted());
        assertFalse(di.isSuccessful());
        assertTrue(di.getSummary().contains("with errors"));
        assertTrue(di.getSummary().contains(failureEvent.getDate()));
        assertEquals(BagItDepositManagerImpl.class.getName(), di.getManagerID());
        assertNotNull(di.getDepositStatus());
        assertTrue(IOUtils.toString(di.getDepositStatus().getInputStream()).length() > 0);
    }

    /**
     * Insures that the DepositInfo from a successful ingest has the proper information:
     * <dl>
     * <dd>completed</dd>
     * <dt>true</dt>
     * <dd>successful</dd>
     * <dt>true</dt>
     * <dd>summary</dd>
     * <dt>contains the string 'successfully' and the event date</dt>
     * <dd>depositStatus document</dd>
     * <dt>is greater than zero bytes</dt>
     * </dl>
     *
     * @throws Exception
     */
    @Test
    public void testGetDepositInfoContentSuccessfulIngest() throws Exception {
        final String depositId = "foo";
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        when(stateManager.get(depositId)).thenReturn(state);
        underTest.setStateManager(stateManager);

        final DcsEvent successEvent = new DcsEvent();
        final long eventTs = System.currentTimeMillis();
        successEvent.setDate(DateUtility.toIso8601(eventTs));
        successEvent.setEventType(Package.Events.INGEST_SUCCESS);
        final EventManager em = mock(EventManager.class);
        when(em.getEventByType(depositId, Package.Events.INGEST_SUCCESS)).thenReturn(successEvent);
        when(em.getEvents(depositId, null)).thenReturn(Arrays.asList(successEvent));
        state.setEventManager(em);

        final DepositInfo di = underTest.getDepositInfo(depositId);

        verify(stateManager, atLeastOnce()).get(depositId);
        verify(em, atLeastOnce()).getEventByType(depositId, Package.Events.INGEST_SUCCESS);
        verify(em, atLeastOnce()).getEvents(depositId, null);

        assertNotNull("Expected a non-null DepositInfo", di);

        assertEquals(depositId, di.getDepositID());
        assertTrue(di.hasCompleted());
        assertTrue(di.isSuccessful());
        assertTrue(di.getSummary().contains("successfully"));
        assertTrue(di.getSummary().contains(successEvent.getDate()));
        assertEquals(BagItDepositManagerImpl.class.getName(), di.getManagerID());
        assertNotNull(di.getDepositStatus());
        assertTrue(IOUtils.toString(di.getDepositStatus().getInputStream()).length() > 0);
    }

    /**
     * Insures that the DepositInfo from a in-progress ingest has the proper information:
     * <dl>
     * <dd>completed</dd>
     * <dt>false</dt>
     * <dd>successful</dd>
     * <dt>false</dt>
     * <dd>summary</dd>
     * <dt>contains the string 'in progress' and the deposit event date</dt>
     * <dd>depositStatus document</dd>
     * <dt>is greater than zero bytes</dt>
     * </dl>
     *
     * @throws Exception
     */
    @Test
    public void testGetDepositInfoContentInProgressIngest() throws Exception {
        final String depositId = "foo";
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        when(stateManager.get(depositId)).thenReturn(state);
        underTest.setStateManager(stateManager);

        final DcsEvent depositEvent = new DcsEvent();
        final long eventTs = System.currentTimeMillis();
        depositEvent.setDate(DateUtility.toIso8601(eventTs));
        depositEvent.setEventType(Package.Events.DEPOSIT);

        final EventManager em = mock(EventManager.class);
        when(em.getEventByType(depositId, Package.Events.DEPOSIT)).thenReturn(depositEvent);
        when(em.getEvents(depositId, null)).thenReturn(Arrays.asList(depositEvent));
        state.setEventManager(em);

        final DepositInfo di = underTest.getDepositInfo(depositId);

        verify(stateManager, atLeastOnce()).get(depositId);
        verify(em, atLeastOnce()).getEvents(depositId, null);
        verify(em, atLeastOnce()).getEventByType(depositId, Package.Events.DEPOSIT);

        assertNotNull("Expected a non-null DepositInfo", di);

        assertEquals(depositId, di.getDepositID());
        assertFalse(di.hasCompleted());
        assertFalse(di.isSuccessful());
        assertTrue(di.getSummary().contains("in progress"));
        assertTrue(di.getSummary().contains(depositEvent.getDate()));
        assertEquals(BagItDepositManagerImpl.class.getName(), di.getManagerID());
        assertNotNull(di.getDepositStatus());
        assertTrue(IOUtils.toString(di.getDepositStatus().getInputStream()).length() > 0);
    }

    /**
     * Insures that the DepositInfo from a not-started ingest has the proper information:
     * <dl>
     * <dd>completed</dd>
     * <dt>false</dt>
     * <dd>successful</dd>
     * <dt>false</dt>
     * <dd>summary</dd>
     * <dt>contains the string 'not started' and the current date</dt>
     * <dd>depositStatus document</dd>
     * <dt>is zero bytes</dt>
     * </dl>
     *
     * @throws Exception
     */
    @Test
    public void testGetDepositInfoContentNotStartedIngest() throws Exception {
        final String depositId = "foo";
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        when(stateManager.get(depositId)).thenReturn(state);
        underTest.setStateManager(stateManager);

        final EventManager em = mock(EventManager.class);
        when(em.getEventByType(depositId, Package.Events.DEPOSIT)).thenReturn(null);
        when(em.getEvents(depositId, null)).thenReturn(Collections.<DcsEvent>emptySet());
        state.setEventManager(em);

        final DepositInfo di = underTest.getDepositInfo(depositId);

        verify(stateManager, atLeastOnce()).get(depositId);
        verify(em, atLeastOnce()).getEvents(depositId, null);
        verify(em, atLeastOnce()).getEventByType(depositId, Package.Events.DEPOSIT);

        assertNotNull("Expected a non-null DepositInfo", di);

        assertEquals(depositId, di.getDepositID());
        assertFalse(di.hasCompleted());
        assertFalse(di.isSuccessful());
        assertTrue(di.getSummary().contains("not started"));
        assertEquals(BagItDepositManagerImpl.class.getName(), di.getManagerID());
        assertNotNull(di.getDepositStatus());
        assertEquals(0, IOUtils.toString(di.getDepositStatus().getInputStream()).length());
    }

    /**
     * Insures the implementation properly reports its DepositManager ID.
     *
     * @throws Exception
     */
    @Test
    public void testGetManagerID() throws Exception {
        assertEquals("Unexpected DepositManager ID.",
                BagItDepositManagerImpl.class.getName(), underTest.getManagerID());
    }

    /**
     * Insures that the HierarchicalPrettyPrinter contains zero bytes when there are no events in the event
     * manager to report on.
     *
     * @throws Exception
     */
    @Test
    public void testBuildDepositStatusWithNoEvents() throws Exception {
        final String depositId = "foo";
        final EventManager em = mock(EventManager.class);
        final HierarchicalPrettyPrinter hpp = new HierarchicalPrettyPrinter();

        when(em.getEvents(depositId, null)).thenReturn(Collections.<DcsEvent>emptySet());

        underTest.buildDepositStatus(depositId, em, hpp);

        assertEquals(0, hpp.toString().length());
    }

    /**
     * Insures that the HierarchicalPrettyPrinter contains zero bytes when there are no events in the event
     * manager to report on.
     *
     * @throws Exception
     */
    @Test
    public void testBuildDepositStatusWithNonExistentDepositId() throws Exception {
        final String depositId = "nonexistentId";
        final EventManager em = mock(EventManager.class);
        final HierarchicalPrettyPrinter hpp = new HierarchicalPrettyPrinter();

        when(em.getEvents(depositId, null)).thenReturn(null);

        underTest.buildDepositStatus(depositId, em, hpp);

        assertEquals(0, hpp.toString().length());
    }

    /**
     * Insures that the HierarchicalPrettyPrinter is not empty when there <em>are</em> events in the event manager
     * to report on.
     *
     * @throws Exception
     */
    @Test
    public void testBuildDepositStatus() throws Exception {
        final String depositId = "foo";
        final EventManager em = mock(EventManager.class);
        final HierarchicalPrettyPrinter hpp = new HierarchicalPrettyPrinter();

        final DcsEvent depositEvent = new DcsEvent();
        long now = System.currentTimeMillis();
        depositEvent.setEventType(Package.Events.DEPOSIT);
        depositEvent.setDate(DateUtility.toIso8601(now));

        when(em.getEvents(depositId, null)).thenReturn(Arrays.asList(depositEvent));

        underTest.buildDepositStatus(depositId, em, hpp);

        assertTrue(hpp.toString().length() > 0);
    }

    /**
     * Insures that Events are sorted properly by testing the Comparator used to sort a Set of DcsEvent objects.
     *
     * @throws Exception
     */
    @Test
    public void testDcsEventComparator() throws Exception {

        final DcsEvent earlier = new DcsEvent();
        earlier.setDate(DateUtility.toIso8601(System.currentTimeMillis() - 10000));

        final DcsEvent later = new DcsEvent();
        later.setDate(DateUtility.toIso8601(System.currentTimeMillis()));

        final DcsEvent nullDate = new DcsEvent();
        assertNull(nullDate.getDate());

        BagItDepositManagerImpl.DcsEventComparator underTest = new BagItDepositManagerImpl.DcsEventComparator();

        // If the LHS instance is identical to the RHS instance, then they are equal
        assertTrue(underTest.compare(earlier, earlier) == 0);

        // If the LHS instance is equivalent to the RHS instance, then they are equal
        assertTrue(underTest.compare(earlier, new DcsEvent(earlier)) == 0);

        // Null LHS argument should be less than a non-null RHS argument
        assertTrue(underTest.compare(null, earlier) < 0);

        // A non-null LHS argument should be greater than a null RHS argument
        assertTrue(underTest.compare(earlier, null) > 0);

        // Two null arguments are equal
        assertTrue(underTest.compare(null, null) == 0);

        // A LHS argument that was created before the RHS argument is less than the RHS argument
        assertTrue(underTest.compare(earlier, later) < 0);

        // A LHS argument that was created after the RHS argument is greater than the RHS argument
        assertTrue(underTest.compare(later, earlier) > 0);

        // A LHS argument with a null date should be less than a RHS argument with a date
        assertTrue(underTest.compare(nullDate, earlier) < 0);

        // A LHS argument with a date should be greater than a RHS argument with a null date
        assertTrue(underTest.compare(earlier, nullDate) > 0);

        // Arguments with null dates should be equal
        assertTrue(underTest.compare(nullDate, nullDate) == 0);
    }

    /**
     * Insures that the addDepositEvent(...) method properly composes a Deposit event, and adds it to the EventManager.
     *
     * @throws Exception
     */
    @Test
    public void testAddDepositEvent() throws Exception {

        // Set expectations
        final String expectedEventType = Package.Events.DEPOSIT;
        final String expectedDepositId = "foo";
        final String expectedFilename = "uploaded_file.tar.gz";
        final String expectedAuthenticatedUser = "joeuser";

        // Compose the Content-Disposition header and metadata
        final String contentDisposition = "attachment; filename=\"" + expectedFilename + "\"";
        final Map<String, String> md = new HashMap<String, String>();
        md.put(Http.Header.CONTENT_DISPOSITION, contentDisposition);
        md.put(Http.Header.X_DCS_AUTHENTICATED_USER, expectedAuthenticatedUser);

        // Create the IngestState
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();

        // Mock the Event Manager and place it on the state.
        final EventManager em = mock(EventManager.class);
        state.setEventManager(em);

        // A holder for a boolean value, insuring that our Answer is invoked.
        final BooleanHolder answerCalled = new BooleanHolder();
        // Verify assumptions
        assertFalse(answerCalled.value);

        // The event that will be composed by the EventManager.newEvent(...) call
        DcsEvent depositEvent = new DcsEvent();
        depositEvent.setEventType(expectedEventType);
        depositEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        when(em.newEvent(expectedEventType)).thenReturn(depositEvent);

        // Mock the EventManager.addEvent(...) method, and run our assertions inside the mock.
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals("Unexpected depositId", expectedDepositId, (String) invocationOnMock.getArguments()[0]);
                final DcsEvent event = (DcsEvent) invocationOnMock.getArguments()[1];
                assertNotNull("Expected a non-null Event.", event);

                assertEquals("Unexpected Event type", expectedEventType, event.getEventType());
                assertEquals("Unexpected Event outcome", expectedDepositId, event.getOutcome());
                assertTrue("Expected a username in Event Detail", event.getDetail().contains(expectedAuthenticatedUser));
                assertTrue("Expected a date in Event Detail", event.getDetail().contains(event.getDate()));
                assertTrue("Expected a filename in Event Detail", event.getDetail().contains(expectedFilename));
                assertEquals("Unexpected deposited filename", expectedFilename,
                        event.getTargets().iterator().next().getRef());

                answerCalled.value = true;

                return null;
            }
        }).when(em).addEvent(anyString(), (DcsEvent) anyObject());

        underTest.addDepositEvent(md, expectedDepositId, state);

        assertTrue("Our Answer was not invoked!", answerCalled.value);
    }

    /**
     * Insures that the addExtractionEvent(...) properly creates the extraction event, and adds it to the
     * event manager.
     *
     * @throws Exception
     */
    @Test
    public void testAddExtractionEvent() throws Exception {

        // Set up the expectations: expected depositId, event type, contents of the event
        final String expectedDepositId = "fooDepositId";
        final String expectedEventType = Package.Events.FILE_EXTRACTION;
        final String bagName = "bag";
        final String bagExt = ".tar.gz";
        final String expectedArchiveFileName = bagName + bagExt;
        final List<File> extractedFiles = new ArrayList<File>();
        final File fileOne = new File("foo");
        final File fileTwo = new File("bar");
        extractedFiles.add(fileOne);
        extractedFiles.add(fileTwo);

        // New state
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();

        // Mock the Event Manager and put it on the state.
        final EventManager em = mock(EventManager.class);
        state.setEventManager(em);

        // Mock the Package and PackageSerialization and put it on the state
        final org.dataconservancy.packaging.model.Package thePackage = mock(org.dataconservancy.packaging.model.Package.class);
        final PackageSerialization ser = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(ser);
        when(ser.getExtractDir()).thenReturn(new File(System.getProperty("java.io.tmpdir")));
        when(ser.getBaseDir()).thenReturn(new File(expectedDepositId + File.separator + bagName));
        state.setPackage(thePackage);

        // Compose the expected event and update the Event Manager to return it when asked.
        final DcsEvent extractionEvent = new DcsEvent();
        extractionEvent.setEventType(expectedEventType);
        extractionEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        when(em.newEvent(Package.Events.FILE_EXTRACTION)).thenReturn(extractionEvent);

        // A holder insuring that the assertions in the Answer are executed
        final BooleanHolder eventAdded = new BooleanHolder();
        // Verify assumptions
        assertFalse(eventAdded.value);


        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals("Unexpected value for depositId",
                        expectedDepositId, (String) invocationOnMock.getArguments()[0]);
                final DcsEvent extractionEvent = (DcsEvent) invocationOnMock.getArguments()[1];
                assertEquals("Unexpected value for the Event outcome.",
                        String.valueOf(extractedFiles.size()), extractionEvent.getOutcome());
                assertNotNull("Expected a non-null Event detail.",
                        extractionEvent.getDetail());
                assertTrue("Expected the Event detail to contain the originally uploaded filename.",
                        extractionEvent.getDetail().contains(expectedArchiveFileName));
                assertTrue("Expected the Event detail to contain the depositId.",
                        extractionEvent.getDetail().contains(expectedDepositId));
                assertNotNull("Expected the Event to have targets.",
                        extractionEvent.getTargets());
                assertEquals("Unexpected number of targets: there should be a target for each file extracted.",
                        extractedFiles.size(), extractionEvent.getTargets().size());

                for (DcsEntityReference ref : extractionEvent.getTargets()) {
                    assertTrue("Expected that each extracted file would be present as a target.",
                            extractedFiles.contains(new File(ref.getRef())));
                }

                eventAdded.value = true;
                return null;
            }
        }).when(em).addEvent(expectedDepositId, extractionEvent);

        underTest.addExtractionEvent(expectedDepositId, expectedArchiveFileName, extractedFiles, state);

        assertTrue("Our Answer was not invoked!", eventAdded.value);

    }

    /**
     * Insures that the Content-Disposition header, if present with correct syntax, can be parsed for the uploaded
     * filename.
     *
     * @throws Exception
     */
    @Test
    public void testFilenameFromHeader() throws Exception {
        /*
        19.5.1 Content-Disposition

           The Content-Disposition response-header field has been proposed as a
           means for the origin server to suggest a default filename if the user
           requests that the content is saved to a file. This usage is derived
           from the definition of Content-Disposition in RFC 1806 [35].

                content-disposition = "Content-Disposition" ":"
                                      disposition-type *( ";" disposition-parm )
                disposition-type = "attachment" | disp-extension-token
                disposition-parm = filename-parm | disp-extension-parm
                filename-parm = "filename" "=" quoted-string
                disp-extension-token = token
                disp-extension-parm = token "=" ( token | quoted-string )

           An example is

                Content-Disposition: attachment; filename="fname.ext"

           The receiving user agent SHOULD NOT respect any directory path
           information present in the filename-parm parameter, which is the only
           parameter believed to apply to HTTP implementations at this time. The
           filename SHOULD be treated as a terminal component only.

           If this header is used in a response with the application/octet-
           stream content-type, the implied suggestion is that the user agent
           should not display the response, but directly enter a `save response
           as...' dialog.

           See section 15.5 for Content-Disposition security issues.
         */

        final String expectedFilename = "foo.txt";
        final StringBuilder sb = new StringBuilder("attachment; filename=");
        sb.append('"').append(expectedFilename).append('"');
        final String expectedCd = sb.toString();

        Map<String, String> md = new HashMap<String, String>();
        md.put(Http.Header.CONTENT_DISPOSITION, expectedCd);


        assertEquals(expectedFilename, underTest.filenameFromHeader(md));
        assertNull(underTest.filenameFromHeader(new HashMap<String, String>()));

        md.put(Http.Header.CONTENT_DISPOSITION, "; filename=\"foo.txt\"");
        assertEquals(expectedFilename, underTest.filenameFromHeader(md));

        md.put(Http.Header.CONTENT_DISPOSITION, "asdfasdf");
        assertNull(underTest.filenameFromHeader(md));

        md.put(Http.Header.CONTENT_DISPOSITION, "; foo=\"bar\" ; filename=\"foo.txt\"");
        assertEquals(expectedFilename, underTest.filenameFromHeader(md));

        md.put(Http.Header.CONTENT_DISPOSITION, "; filename=\"foo.txt\" ; foo=\"bar\"");
        assertEquals(expectedFilename, underTest.filenameFromHeader(md));
    }

    /**
     * Insures that the startIngest(...) method properly populates the state manager, and kicks off
     * the ingest bootstrap.
     *
     * @throws Exception
     */
    @Test
    public void testStartIngest() throws Exception {
        final String expectedDepositId = "foo";
        final IngestWorkflowStateImpl expectedState = new IngestWorkflowStateImpl();
        final BooleanHolder startIngestCalled = new BooleanHolder();
        assertFalse(startIngestCalled.value);

        final InMemoryDepositStateManager stateManager = new InMemoryDepositStateManager();
        assertNull(stateManager.get(expectedDepositId));
        underTest.setStateManager(stateManager);

        underTest.setBootstrap(new StatefulBootstrap() {
            @Override
            public void startIngest(String depositId, IngestWorkflowState state) {
                assertEquals(expectedDepositId, depositId);
                assertTrue(expectedState == state);
                startIngestCalled.value = true;
            }
        });

        underTest.startIngest(expectedDepositId, expectedState);

        assertTrue(startIngestCalled.value);
        assertNotNull(stateManager.get(expectedDepositId));
        assertEquals(expectedState, stateManager.get(expectedDepositId));
    }

    @Test(expected = PackageException.class)
    public void testNonPackageFileFormatThrowsException() throws Exception{
        final String depositId = "foo";
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();

        final IdService idService = mock(IdService.class);
        when(idService.create(IdTypes.DEPOSIT)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                // Default method body
                return null;
            }

            @Override
            public String getUid() {
                return depositId;
            }

            @Override
            public String getType() {
                return IdTypes.DEPOSIT;
            }
        });

        /*
         * Mock the State Manager
         * - verifying that invoking the deposit(...) method will eventually
         * - add the proper state to the state manager.  After we invoke deposit(...) we must verify that
         * - the 'stateManagerPutCalled' was flipped to true.
         */
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        when(stateManager.get(anyString())).thenReturn(state);

        final IngestWorkflowStateFactory stateFactory = mock(IngestWorkflowStateFactory.class);
        when(stateFactory.newInstance()).thenReturn(state);
        
        final EventManager em = mock(EventManager.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals("Unexpected value for depositId", depositId, invocationOnMock.getArguments()[0]);
                
                return null;
            }
        }).when(em).addEvent(anyString(), (DcsEvent) anyObject());

        final DcsEvent depositEvent = new DcsEvent();
        depositEvent.setId("depositEventId" + String.valueOf(0));
        depositEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        depositEvent.setEventType(Package.Events.DEPOSIT);
        when(em.newEvent(anyString())).thenReturn(depositEvent);
        
        state.setEventManager(em);
        PackageSelector selector = new PackageSelectorImpl();
        
        underTest.setIdService(idService);
        underTest.setStateManager(stateManager);
        underTest.setStateFactory(stateFactory);
        underTest.setPackageSelector(selector);
        
        java.io.File packageTmp = java.io.File.createTempFile("testMetadataFile", ".txt");
        packageTmp.deleteOnExit();
        
        PrintWriter packageOut = new PrintWriter(packageTmp);
        
        packageOut.println("This is not a package");
        packageOut.close();
        
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(Http.Header.CONTENT_DISPOSITION,
                "attachment; filename=\"" + packageTmp.getName() + "\"");
        
        underTest.deposit(new FileInputStream(packageTmp), Http.MimeType.TEXT_PLAIN,
                          Package.Types.BAGIT_DCS_10, metadata);
    }
    
    @Test(expected = PackageException.class)
    public void testExtractionErrorThrowsException() throws Exception{
        final String depositId = "foo";
        final IngestWorkflowStateImpl state = new IngestWorkflowStateImpl();

        final org.dataconservancy.packaging.model.Package pkg = new PackageImpl(new DescriptionImpl(),
                                                                                new SerializationImpl());
        state.setPackage(pkg);
        final IdService idService = mock(IdService.class);
        when(idService.create(IdTypes.DEPOSIT)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                // Default method body
                return null;
            }

            @Override
            public String getUid() {
                return depositId;
            }

            @Override
            public String getType() {
                return IdTypes.DEPOSIT;
            }
        });

        /*
         * Mock the State Manager
         * - verifying that invoking the deposit(...) method will eventually
         * - add the proper state to the state manager.  After we invoke deposit(...) we must verify that
         * - the 'stateManagerPutCalled' was flipped to true.
         */
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        when(stateManager.get(anyString())).thenReturn(state);

        final IngestWorkflowStateFactory stateFactory = mock(IngestWorkflowStateFactory.class);
        when(stateFactory.newInstance()).thenReturn(state);
        
        final EventManager em = mock(EventManager.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals("Unexpected value for depositId", depositId, invocationOnMock.getArguments()[0]);
                
                return null;
            }
        }).when(em).addEvent(anyString(), (DcsEvent) anyObject());

        final DcsEvent depositEvent = new DcsEvent();
        depositEvent.setId("depositEventId" + String.valueOf(0));
        depositEvent.setDate(DateUtility.toIso8601(System.currentTimeMillis()));
        depositEvent.setEventType(Package.Events.DEPOSIT);
        when(em.newEvent(anyString())).thenReturn(depositEvent);
        
        state.setEventManager(em);
        PackageSelector selector = mock(PackageSelector.class);
        PackageExtractor extractor = mock(PackageExtractor.class);
        when(selector.selectPackageExtractor((InputStream)anyObject(), (Map)anyObject())).thenReturn(extractor);
        when(extractor.getFilesFromPackageStream(anyString(), anyString(), (InputStream)anyObject())).thenThrow(new UnpackException());
        when(extractor.getExtractDirectory()).thenReturn("foo");
        underTest.setIdService(idService);
        underTest.setStateManager(stateManager);
        underTest.setStateFactory(stateFactory);
        underTest.setPackageSelector(selector);
        
        java.io.File packageTmp = java.io.File.createTempFile("testMetadataFile", ".txt");
        packageTmp.deleteOnExit();
        
        PrintWriter packageOut = new PrintWriter(packageTmp);
        
        packageOut.println("This is not a package");
        packageOut.close();
        
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(Http.Header.CONTENT_DISPOSITION,
                "attachment; filename=\"" + packageTmp.getName() + "\"");
        
        underTest.deposit(new FileInputStream(packageTmp), Http.MimeType.TEXT_PLAIN,
                          Package.Types.BAGIT_DCS_10, metadata);
    }

    /**
     * Asserts that when an ingest is cancelled, the state has the cancelled flag set, and that the package files are
     * deleted.
     *
     * @throws Exception
     */
    @Test
    public void testCancel() throws Exception {
        final String depositId = "foo";
        final File depositDir = createTempDirAndFile(depositId);
        assertTrue(depositDir.exists());
        final BagItDepositManagerImpl underTest = new BagItDepositManagerImpl();
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        final IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(stateManager.get(depositId)).thenReturn(state);
        final org.dataconservancy.packaging.model.Package thePackage =
                mock(org.dataconservancy.packaging.model.Package.class);
        when(state.getPackage()).thenReturn(thePackage);
        final PackageSerialization serialization = mock(PackageSerialization.class);
        when(thePackage.getSerialization()).thenReturn(serialization);
        when(serialization.getExtractDir()).thenReturn(depositDir.getParentFile());

        underTest.setStateManager(stateManager);

        underTest.cancel(depositId);

        verify(stateManager).get(depositId);
        verify(state).getPackage();
        verify(thePackage, atLeastOnce()).getSerialization();
        verify(serialization).getExtractDir();
        verify(state).setCancelled();
        assertFalse(depositDir.exists());
    }

    /**
     * Asserts that a cancelled ingest is not resumed.
     *
     * @throws Exception
     */
    @Test
    public void testResumeCancelledIngest() throws Exception {
        final String depositId = "foo";
        final BagItDepositManagerImpl underTest = new BagItDepositManagerImpl();
        final DepositStateManager stateManager = mock(DepositStateManager.class);
        final IngestWorkflowState state = mock(IngestWorkflowState.class);
        final StatefulBootstrap bootstrap = mock(StatefulBootstrap.class);
        when(stateManager.get(depositId)).thenReturn(state);
        when(state.isCancelled()).thenReturn(true);

        underTest.setStateManager(stateManager);
        underTest.setBootstrap(bootstrap);

        try {
            underTest.resume(depositId);
        } catch (Exception e) {
            // don't care
        }

        verify(stateManager, atLeastOnce()).get(depositId);
        verify(state).isCancelled();
        verifyZeroInteractions(bootstrap);
    }

    /**
     * Allows a boolean value to be manipulated by anonymous inner classes.
     */
    private class BooleanHolder {
        private boolean value = false;
    }

    private File createTempDirAndFile(String depositId) throws IOException {
        final File extractDir = File.createTempFile("BagItDepositManagerImplTest-", ".extractDir");
        FileUtils.forceDelete(extractDir);
        FileUtils.forceMkdir(extractDir);
        final File depositDir = new File(extractDir, BagUtil.sanitizeStringForFile(depositId).getPath());
        FileUtils.forceMkdir(depositDir);
        FileUtils.touch(new File(depositDir, "foo.tar.gz"));
        return depositDir;
    }
}


