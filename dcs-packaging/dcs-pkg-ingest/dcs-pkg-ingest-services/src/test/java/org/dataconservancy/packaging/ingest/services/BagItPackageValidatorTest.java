package org.dataconservancy.packaging.ingest.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeType;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.AttributeSetName;
import org.dataconservancy.packaging.model.Metadata;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class BagItPackageValidatorTest {
    private BagItPackageValidator bagItPackageValidator;
    private org.dataconservancy.packaging.model.Package successPackage;
    private org.dataconservancy.packaging.model.Package failurePackage;
    private AttributeSetManager attributeSetManager;
    private AttributeSetManager badAttributeSetManager;
    private BusinessObjectManager businessObjectManager;

    private EventManager eventManager;
    private Set<DcsEvent> eventSet;
    private File parentDir;
    private File dataDir;
    private File fileOne;
    private File fileTwo;
    private File bagitFile;
    private File bagInfoFile;
    private File manifestMd5File;
    private File tagManifestMd5File;
    private File manifestSha1File;
    private File tagManifestSha1File;
    
    @Before
    public void setup() throws Exception {
        bagItPackageValidator = new BagItPackageValidator();

        parentDir = new File("sample_bag");
        parentDir.mkdir();
        parentDir.deleteOnExit();
        
        dataDir = new File(parentDir, "data");
        dataDir.mkdir();
        dataDir.deleteOnExit();
        
        fileOne = File.createTempFile("testFile", ".txt", dataDir);
        fileOne.deleteOnExit();
        PrintWriter writer = new PrintWriter(fileOne);
        writer.println("This is test file one");
        writer.close();
        
        fileTwo = File.createTempFile("testFileTwo", ".txt", dataDir);
        fileTwo.deleteOnExit();
        writer = new PrintWriter(fileTwo);
        writer.println("This is test file two");
        writer.close();
        
        bagitFile = new File(parentDir, "bagit.txt");
        writer = new PrintWriter(bagitFile);
        writer.println("Just writing to the file.");
        writer.close();
        bagitFile.deleteOnExit();
        bagInfoFile = new File(parentDir, "bag-info.txt");
        writer = new PrintWriter(bagInfoFile);
        writer.println("Just writing to the file.");
        writer.close();
        bagInfoFile.deleteOnExit();
        manifestMd5File = new File(parentDir, "manifest-md5.txt");
        writer = new PrintWriter(manifestMd5File);
        writer.println("Just writing to the file.");
        writer.close();
        manifestMd5File.deleteOnExit();
        tagManifestMd5File = new File(parentDir, "tagmanifest-md5.txt");
        writer = new PrintWriter(tagManifestMd5File);
        writer.println("Just writing to the file.");
        writer.close();
        tagManifestMd5File.deleteOnExit();
        manifestSha1File = new File(parentDir, "manifest-sha1.txt");
        writer = new PrintWriter(manifestSha1File);
        writer.println("Just writing to the file.");
        writer.close();
        manifestSha1File.deleteOnExit();
        tagManifestSha1File = new File(parentDir, "tagmanifest-sha1.txt");
        writer = new PrintWriter(tagManifestSha1File);
        writer.println("Just writing to the file.");
        writer.close();
        tagManifestSha1File.deleteOnExit();

        List<File> successFiles = new ArrayList<File>();
        successFiles.add(parentDir);
        successFiles.add(dataDir);
        successFiles.add(fileOne);
        successFiles.add(fileTwo);
        successFiles.add(bagitFile);
        successFiles.add(bagInfoFile);
        successFiles.add(manifestMd5File);
        successFiles.add(tagManifestMd5File);
        successFiles.add(manifestSha1File);
        successFiles.add(tagManifestSha1File);
        
        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(successFiles);
        
        successPackage = new PackageImpl(null, serialization);
        
        attributeSetManager = mock(AttributeSetManager.class);
        businessObjectManager = mock(BusinessObjectManager.class);
        badAttributeSetManager = mock(AttributeSetManager.class);
        
        // Required attributeSets
        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(attributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(attributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        when(attributeSetManager.contains(AttributeSetName.BAGIT)).thenReturn(true);
        when(attributeSetManager.contains(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(true);
        
        eventSet = new HashSet<DcsEvent>();
        eventManager = mock(EventManager.class);
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {

                // Extract the event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the key and the event to be added", args);
                assertEquals("Expected two arguments: the key and the event to be added", 2, args.length);
                assertTrue("Expected argument one to be of type string", args[0] instanceof String);
                assertTrue("Expected argument two to be of type DcsEvent", args[1] instanceof DcsEvent);
                DcsEvent event = (DcsEvent) args[1];
                eventSet.add(event);
                return null;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));
        
        doAnswer(new Answer<DcsEvent>() {
            
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the type of the event to be generated", args);
                assertEquals("Expected one argument: the type of the event to be retrieved", 1, args.length);
                assertTrue("Expected argument one to be of type string", args[0] instanceof String);
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
                assertEquals("Expected two arguments: the id and the type of the event to be retrieved", 2, args.length);
                assertTrue("Expected argument one to be of type string", args[0] instanceof String);
                assertTrue("Expected argument two to be of type string", args[1] instanceof String);
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

    }
    
    @Test
    public void testSuccess() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        bagItPackageValidator.execute("deposit:1", state);

        assertEquals(1, eventSet.size());
        assertEquals(1, state.getEventManager().getEvents("", Package.Events.PACKAGE_VALIDATED).size());
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeSetBagIt() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(badAttributeSetManager.contains(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(true);
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);


        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeSetBagItProfileDataCons() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(badAttributeSetManager.contains(AttributeSetName.BAGIT)).thenReturn(true);
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnPackageWithNoFiles() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        List<File> files = new ArrayList<File>();
        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(files);
        
        failurePackage = new PackageImpl(null, serialization);
        when(state.getPackage()).thenReturn(failurePackage);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingDataDir() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        List<File> files = new ArrayList<File>();
        files.add(parentDir);
        files.add(fileOne);
        files.add(fileTwo);
        files.add(bagitFile);
        files.add(bagInfoFile);
        files.add(manifestMd5File);
        files.add(tagManifestMd5File);
        files.add(manifestSha1File);
        files.add(tagManifestSha1File);
        
        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(files);

        failurePackage = new PackageImpl(null, serialization);
        when(state.getPackage()).thenReturn(failurePackage);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingRequiredElementBagitFile() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        List<File> files = new ArrayList<File>();
        files.add(parentDir);
        files.add(dataDir);
        files.add(fileOne);
        files.add(fileTwo);
        files.add(bagInfoFile);
        files.add(manifestMd5File);
        files.add(tagManifestMd5File);
        files.add(manifestSha1File);
        files.add(tagManifestSha1File);
        
        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(files);
        
        failurePackage = new PackageImpl(null, serialization);
        when(state.getPackage()).thenReturn(failurePackage);
        
        bagItPackageValidator.execute("deposit:1", state);
        
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingRequiredElementBagInfoFile() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        List<File> files = new ArrayList<File>();
        files.add(parentDir);
        files.add(dataDir);
        files.add(fileOne);
        files.add(fileTwo);
        files.add(bagitFile);
        files.add(manifestMd5File);
        files.add(tagManifestMd5File);
        files.add(manifestSha1File);
        files.add(tagManifestSha1File);
        
        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(files);
        
        failurePackage = new PackageImpl(null, serialization);
        when(state.getPackage()).thenReturn(failurePackage);
        
        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingRequiredElementManifestFile() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        List<File> files = new ArrayList<File>();
        files.add(parentDir);
        files.add(dataDir);
        files.add(fileOne);
        files.add(fileTwo);
        files.add(bagitFile);
        files.add(bagInfoFile);
        files.add(tagManifestMd5File);
        files.add(tagManifestSha1File);
        
        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(files);

        failurePackage = new PackageImpl(null, serialization);
        when(state.getPackage()).thenReturn(failurePackage);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingRequiredElementTagManifestFile() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getAttributeSetManager()).thenReturn(attributeSetManager);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        List<File> files = new ArrayList<File>();
        files.add(parentDir);
        files.add(dataDir);
        files.add(fileOne);
        files.add(fileTwo);
        files.add(bagitFile);
        files.add(bagInfoFile);
        files.add(manifestMd5File);
        files.add(manifestSha1File);
        
        PackageSerialization serialization = mock(PackageSerialization.class);
        when(serialization.getFiles()).thenReturn(files);
        
        failurePackage = new PackageImpl(null, serialization);
        when(state.getPackage()).thenReturn(failurePackage);
        
        bagItPackageValidator.execute("deposit:1", state);
    }

    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeManifestEntry() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        // bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
        // "checksumForFileOne"));
        // bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
        // "checksumForFileTwo"));
        // bagitAtts
        // .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeContactName() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        // bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeContactEmail() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        // bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeContactPhone() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        // bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeExternalIdentifier() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        // bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING,
        // "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeBagSize() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        // bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributePayloadOxum() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        // bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);
        
        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeBagCount() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        // bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeBagGroupIdentifier() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        // bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeBaggingDate() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        // bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
       
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeBagitVersion() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        // bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);
        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeTagFileCharacterEncoding() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        // bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING,
        // "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributeBagitProfileIdentifier() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        // bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        // dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
        // "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributePackageBagDir() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        // dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING,
        // "file:///sample_bag/"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
                "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
    
    @Test(expected = StatefulIngestServiceException.class)
    public void testFailOnMissingAttributePackageOreRem() throws StatefulIngestServiceException {
        IngestWorkflowState state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);

        AttributeSet bagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT);
        AttributeSet dcBagitAttSet = new AttributeSetImpl(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);
        Collection<Attribute> bagitAtts = bagitAttSet.getAttributes();
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_NAME, MetadataAttributeType.STRING, "John Doe"));
        // bagitAtts.add(new AttributeImpl(Metadata.CONTACT_EMAIL, MetadataAttributeType.STRING, "JohnDoe@jhu.edu"));
        bagitAtts.add(new AttributeImpl(Metadata.CONTACT_PHONE, MetadataAttributeType.STRING, "555-555-5555"));
        bagitAtts.add(new AttributeImpl(Metadata.EXTERNAL_IDENTIFIER, MetadataAttributeType.STRING, "Some identifier"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_SIZE, MetadataAttributeType.STRING, "75G"));
        bagitAtts.add(new AttributeImpl(Metadata.PAYLOAD_OXUM, MetadataAttributeType.STRING, "189495.52"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_COUNT, MetadataAttributeType.STRING, "1 of 1"));
        bagitAtts.add(new AttributeImpl(Metadata.BAG_GROUP_IDENTIFIER, MetadataAttributeType.STRING, "sample_bag"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGGING_DATE, MetadataAttributeType.STRING, "2013-07-26"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_VERSION, MetadataAttributeType.STRING, "0.97"));
        bagitAtts.add(new AttributeImpl(Metadata.TAG_FILE_CHARACTER_ENCODING, MetadataAttributeType.STRING, "UTF-8"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileOne"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForFileTwo"));
        bagitAtts
                .add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagInfoFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING, "checksumForBagItFile"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestMd5File"));
        bagitAtts.add(new AttributeImpl(Metadata.BAGIT_CHECKSUM, MetadataAttributeType.STRING,
                "checksumForManifestSha1File"));
        
        Collection<Attribute> dcBagitAtts = dcBagitAttSet.getAttributes();
        dcBagitAtts.add(new AttributeImpl(Metadata.BAGIT_PROFILE_IDENTIFIER, MetadataAttributeType.STRING,
                "http://dataconservancy.org/formats/data-conservancy-pkg-0.9"));
        dcBagitAtts.add(new AttributeImpl(Metadata.PKG_BAG_DIR, MetadataAttributeType.STRING, "file:///sample_bag/"));
        // dcBagitAtts.add(new AttributeImpl(Metadata.PKG_ORE_REM, MetadataAttributeType.STRING,
        // "file:///ELOKA002.bag/ORE-REM/8CF61A5C-1EB1-4ED3-9AC6-C072CFA2471C-ReM.xml"));
        
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT)).thenReturn(bagitAttSet);
        when(badAttributeSetManager.getAttributeSet(AttributeSetName.BAGIT_PROFILE_DATACONS_METADATA)).thenReturn(
                dcBagitAttSet);
        
        when(state.getAttributeSetManager()).thenReturn(badAttributeSetManager);
        when(state.getPackage()).thenReturn(successPackage);
        when(state.getEventManager()).thenReturn(eventManager);

        bagItPackageValidator.execute("deposit:1", state);
    }
}
