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

/**
 * Supported string comparison operations
 */
public enum MatchOp {

    /**
     * Strings are equal to each other according to {@link String#equals(Object)}.
     */
    EQUAL_TO,

    /**
     * Strings are not equal to each other (the inverse of {@link #EQUAL_TO}
     */
    NOT_EQUAL_TO,

    /**
     * Strings starts with the specified string according to {@link String#startsWith(String)}
     */
    STARTS_WITH,

    /**
     * String ends with the specified string according to {@link String#endsWith(String)}
     */
    ENDS_WITH
}
