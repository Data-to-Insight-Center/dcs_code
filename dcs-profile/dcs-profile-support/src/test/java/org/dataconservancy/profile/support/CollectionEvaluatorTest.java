/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.profile.support;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class CollectionEvaluatorTest {

    private Set<String> candidates = null;

    @Before
    public void setUp() {
        candidates = new HashSet<String>();
        candidates.add("foo");
        candidates.add("bar");
        candidates.add("baz");
        candidates.add("barbaz");
    }

    @Test
    public void testEvaluateAtLeastOne() {
        final CollectionEvaluator underTest = new CollectionEvaluator(CollectionMatchStrategy.AT_LEAST_ONE);

        assertTrue(underTest.evaluate(candidates, ProfileStatement.startsWith("foo")));
        assertFalse(underTest.evaluate(candidates, ProfileStatement.startsWith("joe")));
    }

    @Test
    public void testEvaluateExactlyOne() {
        final CollectionEvaluator underTest = new CollectionEvaluator(CollectionMatchStrategy.EXACTLY_ONE);

        assertTrue(underTest.evaluate(candidates, ProfileStatement.startsWith("foo")));
        assertFalse(underTest.evaluate(candidates, ProfileStatement.endsWith("baz")));
    }

    @Test
    public void testEvaluateNone() {
        final CollectionEvaluator underTest = new CollectionEvaluator(CollectionMatchStrategy.NONE);

        assertFalse(underTest.evaluate(candidates, ProfileStatement.startsWith("foo")));
        assertFalse(underTest.evaluate(candidates, ProfileStatement.endsWith("baz")));
        assertTrue(underTest.evaluate(candidates, ProfileStatement.startsWith("harry")));
    }
}
