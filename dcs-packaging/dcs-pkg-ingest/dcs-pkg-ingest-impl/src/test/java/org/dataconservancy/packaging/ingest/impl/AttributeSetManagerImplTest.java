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


import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.ingest.api.AttributeMatcher;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.ExistingAttributeSetException;
import org.dataconservancy.packaging.ingest.shared.AttributeImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Unit tests for AttributeSetManagerImpl
 */
public class AttributeSetManagerImplTest {

    private AttributeSetManager underTest;
    private String attributeSetId1;
    private String attributeSetId2;
    private String attributeSetId3;
    private String attributeSetId4;
    private AttributeSet temporalAS;
    private AttributeSet updatedTemporalAS;
    private AttributeSet keywordAS;
    private AttributeSet fileAttributeAS;
    private Attribute matchAttr;

    @Before
    public void setUp() {
        underTest = new AttributeSetManagerImpl();
        //seed attributeSetManager with attribute sets

        //Create TEMPORAL attributes
        Attribute createdDateAttr = new AttributeImpl("CREATED.DATE", "DATETIME", "2013-02-13");
        Attribute modifiedDateAttr = new AttributeImpl("MODIFIED.DATE", "DATETIME", "2013-02-14");
        Attribute lastRetrievedDateAttr = new AttributeImpl("RETRIEVED.DATE", "DATETIME", "2013-02-16");
        matchAttr = new AttributeImpl("MATCH.STRING", "STRING", "THIS MATCHES");
        temporalAS = new AttributeSetImpl("TEMPORAL");
        temporalAS.getAttributes().add(createdDateAttr);
        temporalAS.getAttributes().add(modifiedDateAttr);
        temporalAS.getAttributes().add(lastRetrievedDateAttr);

        //Create a SECOND TEMPORAL attributeset
        createdDateAttr = new AttributeImpl("CREATED.DATE", "DATETIME", "2006-02-13");
        modifiedDateAttr = new AttributeImpl("MODIFIED.DATE", "DATETIME", "2006-02-14");
        lastRetrievedDateAttr = new AttributeImpl("RETRIEVED.DATE", "DATETIME", "2006-02-16");
        updatedTemporalAS = new AttributeSetImpl("TEMPORAL.UPDATED");
        updatedTemporalAS.getAttributes().add(createdDateAttr);
        updatedTemporalAS.getAttributes().add(modifiedDateAttr);
        updatedTemporalAS.getAttributes().add(lastRetrievedDateAttr);
        updatedTemporalAS.getAttributes().add(matchAttr);

        //Create KEYWORD attributes
        Attribute disciplineKWAttr = new AttributeImpl("DISCIPLINE.KEYWORD", "STRING", "EARTH-SCIENCE");
        Attribute speciesKWAttr = new AttributeImpl("SPECIES", "STRING", "DOG");
        keywordAS = new AttributeSetImpl("KEYWORD");
        keywordAS.getAttributes().add(disciplineKWAttr);
        keywordAS.getAttributes().add(speciesKWAttr);

        //Create FILE-ATTRIBUTE attributes
        Attribute md5Checksum = new AttributeImpl("DISCIPLINE.KEYWORD", "STRING", "12345678");
        Attribute creatorName = new AttributeImpl("CREATOR.NAME", "STRING", "SUPER MAN");
        fileAttributeAS = new AttributeSetImpl("FILE.ATTRIBUTE");
        fileAttributeAS.getAttributes().add(md5Checksum);
        fileAttributeAS.getAttributes().add(creatorName);
        fileAttributeAS.getAttributes().add(matchAttr);


        //Create ID attributes
        attributeSetId1 = "org.dataconservancy:id:attribute:set:1";
        attributeSetId2 = "org.dataconservancy:id:attribute:set:2";
        attributeSetId3 = "org.dataconservancy:id:attribute:set:3";
        attributeSetId4 = "org.dataconservancy:id:attribute:set:4";
    }

    /**
     * Test that attribute set can be added and retrieved
     */
    @Test
    public void testAddAndRetrieveAttributeSet(){
        assertFalse(underTest.contains(attributeSetId1));
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        assertTrue(underTest.contains(attributeSetId1));
        AttributeSet retrievedAS = underTest.getAttributeSet(attributeSetId1);
        assertEquals(temporalAS.getName(), retrievedAS.getName());
        Attribute metadataAttribute;
        for (Attribute attribute : retrievedAS.getAttributes()) {
             metadataAttribute = new AttributeImpl(attribute.getName(), attribute.getType(), attribute.getValue());
             assertTrue(temporalAS.getAttributes().contains(metadataAttribute));
        }
    }

    /**
     * Test that proper exception is thrown when an AttributeSet is added with a non-unique key
     */
    @Test (expected = ExistingAttributeSetException.class)
    public void testAddAttributeSetWithExistingKey() {
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        underTest.addAttributeSet(attributeSetId1, keywordAS);
    }

    /**
     * Test updating a attribute set with all new values. Expect old values to be gone. new values to be added.
     */
    @Test
    public void testUpdateAttributeSet() {
        //add intial attribute set
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        //test that the attribute set was added
        assertTrue(underTest.contains(attributeSetId1));
        //examined the added attribute set
        AttributeSet retrievedAS = underTest.getAttributeSet(attributeSetId1);
        assertEquals(temporalAS.getName(), retrievedAS.getName());
        //test that the attributes' properties are correctly preserved
        Attribute metadataAttribute;
        for (Attribute attribute : retrievedAS.getAttributes()) {
            metadataAttribute = new AttributeImpl(attribute.getName(), attribute.getType(), attribute.getValue());
            assertTrue(temporalAS.getAttributes().contains(metadataAttribute));
        }

        //update the attribute set
        underTest.updateAttributeSet(attributeSetId1, updatedTemporalAS);
        //examined the updated attribute set
        AttributeSet newlyRetrievedAS = underTest.getAttributeSet(attributeSetId1);
        assertEquals(updatedTemporalAS.getName(), newlyRetrievedAS.getName());
        //test that the the old attributes' properties are gone
        for (Attribute attribute : newlyRetrievedAS.getAttributes()) {
            metadataAttribute = new AttributeImpl(attribute.getName(), attribute.getType(), attribute.getValue());
            assertFalse(temporalAS.getAttributes().contains(metadataAttribute));
        }
        //test that the the new attributes' properties are in place
        for (Attribute attribute : newlyRetrievedAS.getAttributes()) {
            metadataAttribute = new AttributeImpl(attribute.getName(), attribute.getType(), attribute.getValue());
            assertTrue(updatedTemporalAS.getAttributes().contains(metadataAttribute));
        }

    }

    /**
     * test that when updating attribute set with a non-existing key,
     * the attribute set will be added to the manager silently
     */
    @Test
    public void testUpdateNonExistingAttributeSet() {
        //add intial attribute set
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        assertTrue(underTest.contains(attributeSetId1));
        assertFalse(underTest.contains(attributeSetId2));

        //update attribute set with a non-existing key
        underTest.updateAttributeSet(attributeSetId2, updatedTemporalAS);
        assertTrue(underTest.contains(attributeSetId1));
        assertTrue(underTest.contains(attributeSetId2));
    }

    /**
     * test that when an attribute set is removed, the AttributeSetManager will no longer return it.
     */
    @Test
    public void testRemoveAttributeSet() {
        //add intial attribute set
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        assertTrue(underTest.contains(attributeSetId1));

        underTest.removeAttributeSet(attributeSetId1);
        assertFalse(underTest.contains(attributeSetId1));
        assertNull(underTest.getAttributeSet(attributeSetId1));

    }

    /**
     * test that removing non-existing attribute set silently fails (does nothing)
     */
    @Test
    public void testRemoveNonExistingAttributeSet() {
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        assertTrue(underTest.contains(attributeSetId1));
        assertFalse(underTest.contains(attributeSetId2));
        underTest.removeAttributeSet(attributeSetId2);
        assertTrue(underTest.contains(attributeSetId1));
        assertFalse(underTest.contains(attributeSetId2));
    }

    /**
     * Test that retrieving non-existing attribute set returns null
     */
    @Test
    public void testGetNonExistingAttributeSet() {
        assertNull(underTest.getAttributeSet(attributeSetId1));
    }

    /**
     * Test that contains() return true of the key exists in the manager, and return false if the key doesn't exist
     */
    @Test
    public void testContainsKey() {
        underTest.addAttributeSet(attributeSetId1, keywordAS);
        assertTrue(underTest.contains(attributeSetId1));
        assertFalse(underTest.contains(attributeSetId2));
    }

    /**
     * Test matches when there are expected matches. Expects only AttributeSet with matching attributes to be returned.
     */
    @Test
    public void testMatches() {
        //Add AttributeSet with no matching attribute
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        underTest.addAttributeSet(attributeSetId3, keywordAS);

        //Add AttributeSet with expected matching attribute
        underTest.addAttributeSet(attributeSetId2, updatedTemporalAS);
        underTest.addAttributeSet(attributeSetId4, fileAttributeAS);

        //Make sure all added AttributeSets are there
        assertTrue(underTest.contains(attributeSetId1));
        assertTrue(underTest.contains(attributeSetId2));
        assertTrue(underTest.contains(attributeSetId3));
        assertTrue(underTest.contains(attributeSetId4));

        Set<String> expectedNameOfMatchingAS = new HashSet<String>();
        expectedNameOfMatchingAS.add(updatedTemporalAS.getName());
        expectedNameOfMatchingAS.add(fileAttributeAS.getName());

        Set<AttributeSet> matchingAS = underTest.matches(matchAttr);
        for (AttributeSet attributeSet : matchingAS) {
            assertTrue(expectedNameOfMatchingAS.contains(attributeSet.getName()));
        }
    }

    /**
     * Test matches when value is null. Expect AttributeSets with attributes matching name and type to be returned.
     */
    @Test
    public void testMatchesWithNullValue() {
        //add AttributeSets with attribute of "CREATED.DATE" name and "DATETIME" type
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        underTest.addAttributeSet(attributeSetId2, updatedTemporalAS);

        //add AttributeSets with no-matching attributes
        underTest.addAttributeSet(attributeSetId3, keywordAS);
        underTest.addAttributeSet(attributeSetId4, fileAttributeAS);

        //Make sure all added AttributeSets are there
        assertTrue(underTest.contains(attributeSetId1));
        assertTrue(underTest.contains(attributeSetId2));
        assertTrue(underTest.contains(attributeSetId3));
        assertTrue(underTest.contains(attributeSetId4));

        Attribute randomAttribute = new AttributeImpl("CREATED.DATE", "DATETIME", null);
        Set<AttributeSet> matchingAS = underTest.matches(randomAttribute);
        assertEquals(2, matchingAS.size());

        Collection<Attribute> attributes;
        Attribute retrievedAttribute;
        for (AttributeSet attributeSet : matchingAS) {
            attributes = attributeSet.getAttributes();
            for (Attribute attribute : attributes) {
                retrievedAttribute = new AttributeImpl(attribute.getName(), attribute.getType(), attribute.getValue());
                assertTrue(temporalAS.getAttributes().contains(retrievedAttribute)
                        || updatedTemporalAS.getAttributes().contains(retrievedAttribute));
            }
        }
    }

    /**
     * Test matches when there are no expected matches. Expects not-null empty set to be returned.
     */
    @Test
    public void testMatchesNoMatch() {
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        underTest.addAttributeSet(attributeSetId3, keywordAS);
        underTest.addAttributeSet(attributeSetId2, updatedTemporalAS);
        underTest.addAttributeSet(attributeSetId4, fileAttributeAS);

        //Make sure all added AttributeSets are there
        assertTrue(underTest.contains(attributeSetId1));
        assertTrue(underTest.contains(attributeSetId2));
        assertTrue(underTest.contains(attributeSetId3));
        assertTrue(underTest.contains(attributeSetId4));

        Attribute randomAttribute = new AttributeImpl("NAME", "TYPE", "VALUE");
        Set<AttributeSet> matchingAS = underTest.matches(randomAttribute);
        assertNotNull(matchingAS);
        assertEquals(0, matchingAS.size());
    }

    @Test
    public void testAttributeMatcherSimple() throws Exception {
        underTest.addAttributeSet(attributeSetId1, temporalAS);
        underTest.addAttributeSet(attributeSetId3, keywordAS);
        underTest.addAttributeSet(attributeSetId2, updatedTemporalAS);
        underTest.addAttributeSet(attributeSetId4, fileAttributeAS);

        // Make sure all added AttributeSets are there
        assertTrue(underTest.contains(attributeSetId1));
        assertTrue(underTest.contains(attributeSetId2));
        assertTrue(underTest.contains(attributeSetId3));
        assertTrue(underTest.contains(attributeSetId4));

        // Attempt to retrieve all AttributeSets with an AttributeMatcher
        Set<AttributeSet> results = underTest.matches(new AttributeMatcher() {
            @Override
            public boolean matches(String attributeSetName, Attribute candidateToMatch) {
                return true;
            }
        });

        assertTrue(results.contains(temporalAS));
        assertTrue(results.contains(keywordAS));
        assertTrue(results.contains(updatedTemporalAS));
        assertTrue(results.contains(fileAttributeAS));

        // Attempt to retrieve no AttributeSets with an AttributeMatcher
        results = underTest.matches(new AttributeMatcher() {
            @Override
            public boolean matches(String attributeSetName, Attribute candidateToMatch) {
                return false;
            }
        });

        assertTrue(results.isEmpty());

        // Attempt to retrieve AttributeSets using a regex
        final Pattern toMatch = Pattern.compile(".*\\.STRING");
        results = underTest.matches(new AttributeMatcher() {
            @Override
            public boolean matches(String attributeSetName, Attribute candidateToMatch) {
                return toMatch.matcher(candidateToMatch.getName()).matches();
            }
        });

        assertEquals(2, results.size());
        assertTrue(results.contains(updatedTemporalAS));
        assertTrue(results.contains(fileAttributeAS));
    }
}
