package org.dataconservancy.packaging.ingest.impl;

import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class InMemoryDepositStateManagerTest {

    @Test
    public void testEldestEntryRemoved() throws Exception {
        final int capacity = 1;

        final InMemoryDepositStateManager underTest = new InMemoryDepositStateManager(capacity);

        assertEquals(capacity, underTest.getSize());

        underTest.put("foo", mock(IngestWorkflowState.class));
        assertNotNull(underTest.get("foo"));

        underTest.put("bar", mock(IngestWorkflowState.class));
        assertNotNull(underTest.get("bar"));

        // foo was evicted from the map when bar was added, because the capacity of the manager is limited to
        // one object
        assertNull(underTest.get("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCapacityZero() throws Exception {
        new InMemoryDepositStateManager(0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCapacityLessThanZero() throws Exception {
        new InMemoryDepositStateManager(-5);
    }
}
