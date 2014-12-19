package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestPhase;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.model.*;
import org.dataconservancy.packaging.model.Package;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility methods to produce Mockito instances of ingest workflow objects.
 */
public class MockUtil {

    public static AttributeSetManager mockAttributeSetManager() {
        return mock(AttributeSetManager.class);
    }

    public static BusinessObjectManager mockBusinessObjectManager() {
        return mock(BusinessObjectManager.class);
    }

    public static EventManager mockEventManager() {
        return mock(EventManager.class);
    }

    public static Package mockPackage() {
        PackageDescription desc = mock(PackageDescription.class);
        PackageSerialization ser = mock(PackageSerialization.class);
        Package thePackage = mock(Package.class);
        when(thePackage.getSerialization()).thenReturn(ser);
        when(thePackage.getDescription()).thenReturn(desc);
        return thePackage;
    }

    public static IngestWorkflowState mockState() {
        IngestWorkflowState state = mock(IngestWorkflowState.class);

        AttributeSetManager asm = mockAttributeSetManager();
        BusinessObjectManager bom = mockBusinessObjectManager();
        EventManager em = mockEventManager();
        Package thePackage = mockPackage();

        when(state.getAttributeSetManager()).thenReturn(asm);
        when(state.getBusinessObjectManager()).thenReturn(bom);
        when(state.getEventManager()).thenReturn(em);
        when(state.getPackage()).thenReturn(thePackage);
        when(state.getIngestPhase()).thenReturn(mock(IngestPhase.class));

        return state;
    }
}
