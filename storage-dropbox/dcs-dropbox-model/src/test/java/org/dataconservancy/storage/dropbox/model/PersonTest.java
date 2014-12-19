/*
 * Copyright 2013 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.storage.dropbox.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PersonTest {
    
    private Person one;
    
    private Person two;
    
    private Person three;

    @Before
    public void setUp() throws Exception {
        one = new Person();
        one.setFirstName("Jack");
        one.setLastName("Nicholson");
        one.setMiddleName("John");
        one.setPrefix("Mr.");
        one.setSuffix("Jr.");
        one.setOccupation("Actor");
        one.setOrganization("Hollywood");
        one.setUsername("jack.nicholson");
        one.setPassword("jack123");
        one.setSecretQuestion("What is the name of your dog?");
        one.setSecretAnswer("Lucky");
        
        two = new Person();
        two.setFirstName("Jack");
        two.setLastName("Nicholson");
        two.setMiddleName("John");
        two.setPrefix("Mr.");
        two.setSuffix("Jr.");
        two.setOccupation("Actor");
        two.setOrganization("Hollywood");
        two.setUsername("jack.nicholson");
        two.setPassword("jack123");
        two.setSecretQuestion("What is the name of your dog?");
        two.setSecretAnswer("Lucky");
        
        three = new Person();
        three.setFirstName("David");
        three.setLastName("Beckham");
        three.setMiddleName("Robert Joseph");
        three.setPrefix("Mr.");
        three.setOccupation("Footballer");
        three.setOrganization("Manchester United");
        three.setUsername("david.beckham");
        three.setPassword("david123");
        three.setSecretQuestion("What is the city of your birth?");
        three.setSecretAnswer("Leytonstone");
    }
    
    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertTrue(one.equals(one));
        assertFalse(one.equals(three));
    }
    
    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
    }
    
    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(one.equals(two));
        assertTrue(one.equals(two));
    }
    
    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(one.equals(null));
    }
    
}
