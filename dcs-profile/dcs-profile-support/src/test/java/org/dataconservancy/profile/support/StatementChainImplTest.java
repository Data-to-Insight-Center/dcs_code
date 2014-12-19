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
public class StatementChainImplTest {

    @Test
    public void testAnd() throws Exception {
        final StatementChainImpl underTest = new StatementChainImpl();
        underTest.and(ProfileStatement.startsWith("foo"), ProfileStatement.endsWith("bar"), ProfileStatement.notEqualTo("foobar"));
        assertFalse(underTest.evaluate("foobar"));
        assertTrue(underTest.evaluate("foo bar"));
        assertFalse(underTest.evaluate("foo"));
        assertFalse(underTest.evaluate("bar"));
        assertFalse(underTest.evaluate("bar foo"));
    }

    @Test
    public void testOr() throws Exception {
        final StatementChainImpl underTest = new StatementChainImpl();
        underTest.or(ProfileStatement.startsWith("biz"), ProfileStatement.endsWith("baz"), ProfileStatement.notEqualTo("foobar"));
        assertFalse(underTest.evaluate("foobar"));
        assertTrue(underTest.evaluate("foo baz"));
        assertTrue(underTest.evaluate("foo"));
        assertTrue(underTest.evaluate("baz"));
        assertTrue(underTest.evaluate("anything, really"));
    }

    @Test
    public void testNot() throws Exception {
        final StatementChainImpl underTest = new StatementChainImpl();
        underTest.not(ProfileStatement.startsWith("biz"), ProfileStatement.endsWith("baz"), ProfileStatement.equals("foobar"));
        assertFalse(underTest.evaluate("foobar"));
        assertFalse(underTest.evaluate("foo baz"));
        assertTrue(underTest.evaluate("foo"));
        assertFalse(underTest.evaluate("baz"));
        assertTrue(underTest.evaluate("anything, really"));
    }
}
