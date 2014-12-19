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
 * Matching strategies for {@link ProfileStatement}s that evaluate a <code>Collection</code> of candidate strings.
 */
public enum CollectionMatchStrategy {

    /**
     * The {@link ProfileStatement} must match at least one String from the collection.
     */
    AT_LEAST_ONE,

    /**
     * The {@link ProfileStatement} must match one and only one String from the collection.
     */
    EXACTLY_ONE,

    /**
     * The {@link ProfileStatement} must not match any of the Strings from the collection.
     */
    NONE
}
