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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ProfileStatementTest {

    @Test
    public void testEquals() throws Exception {
        final ProfileStatement underTest = ProfileStatement.equals("foo");
        
        assertTrue(underTest.evaluate("foo"));
        assertFalse(underTest.evaluate("Foo"));
        assertFalse(underTest.evaluate(" foo"));
        assertFalse(underTest.evaluate("foo "));
        assertFalse(underTest.evaluate("bar"));

    }

    @Test
    public void testNormalizedEquals() throws Exception {
        final ProfileStatement underTest = ProfileStatement.normalizedEquals("foo");

        assertTrue(underTest.evaluate("foo"));
        assertTrue(underTest.evaluate("Foo"));
        assertTrue(underTest.evaluate(" foo"));
        assertTrue(underTest.evaluate("foo "));
        assertFalse(underTest.evaluate("bar"));
    }

    @Test
    public void testStartsWith() throws Exception {
        final ProfileStatement underTest = ProfileStatement.startsWith("foo");

        assertTrue(underTest.evaluate("foo"));
        assertTrue(underTest.evaluate("foobar"));
        assertTrue(underTest.evaluate("foo bar"));
        assertFalse(underTest.evaluate(" foo"));
        assertFalse(underTest.evaluate("bar foo"));
    }

    @Test
    public void testNormalizedStartsWith() throws Exception {
        final ProfileStatement underTest = ProfileStatement.normalizedStartsWith("foo");

        assertTrue(underTest.evaluate("foo"));
        assertTrue(underTest.evaluate("Foobar"));
        assertTrue(underTest.evaluate(" Foo"));
        assertTrue(underTest.evaluate(" Foo "));
        assertFalse(underTest.evaluate("bar foo"));
    }

    @Test
    public void testEndsWith() throws Exception {
        final ProfileStatement underTest = ProfileStatement.endsWith("foo");

        assertTrue(underTest.evaluate("foo"));
        assertTrue(underTest.evaluate("barfoo"));
        assertTrue(underTest.evaluate("bar foo"));
        assertTrue(underTest.evaluate(" foo"));
        assertFalse(underTest.evaluate("bar foo "));
    }

    @Test
    public void testNormalizedEndsWith() throws Exception {
        final ProfileStatement underTest = ProfileStatement.normalizedEndsWith("foo");

        assertTrue(underTest.evaluate("foo"));
        assertTrue(underTest.evaluate("BarFoo"));
        assertTrue(underTest.evaluate(" Foo"));
        assertTrue(underTest.evaluate(" Foo "));
        assertFalse(underTest.evaluate("foo bar"));
    }

    @Test
    public void testNotEqualTo() throws Exception {
        final ProfileStatement underTest = ProfileStatement.notEqualTo("foo");

        assertFalse(underTest.evaluate("foo"));
        assertTrue(underTest.evaluate("Foo"));
        assertTrue(underTest.evaluate(" foo"));
        assertTrue(underTest.evaluate("foo "));
        assertTrue(underTest.evaluate("bar"));
    }

    @Test
    public void testNormalizedNotEqualTo() throws Exception {
        final ProfileStatement underTest = ProfileStatement.normalizedNotEqualTo("foo");

        assertFalse(underTest.evaluate("foo"));
        assertFalse(underTest.evaluate("Foo"));
        assertFalse(underTest.evaluate(" foo"));
        assertFalse(underTest.evaluate("foo "));
        assertTrue(underTest.evaluate("bar"));
    }
}
