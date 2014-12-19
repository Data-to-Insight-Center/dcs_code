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
package org.dataconservancy.packaging.model.impl;

import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PairTest {
    
    /**
     * Tests that certain pairs are equal
     */
    @Test
    public void testEquals() {
        Pair<String, String> stringPair = new Pair<String, String>("foo", "bar");
        Pair<String, String> differentStringPair = new Pair<String, String>("bar", "baz");
        Pair<String, String> samePair = new Pair<String, String>("foo", "bar");
        assertFalse(stringPair.equals(differentStringPair));
        assertEquals(stringPair, stringPair);
        assertEquals(stringPair, samePair);
        
        
        Pair<String, Integer> mixedPair = new Pair<String, Integer>("foo", 1);
        Pair<String, Integer> differentMixedPair = new Pair<String, Integer>("foo", 2);
        Pair<String, Integer> sameMixedPair = new Pair<String, Integer>("foo", 1);
        assertFalse(mixedPair.equals(differentMixedPair));
        assertEquals(mixedPair, sameMixedPair);
        assertEquals(mixedPair, mixedPair);
        
    }
}