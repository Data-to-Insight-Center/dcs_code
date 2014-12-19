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
 * Allows {@link ProfileStatement}s to be linked together in a chain.  Implementations will evaluate
 * all of the statements in a chain.
 */
interface StatementChain {

    /**
     * Allowed logical operators used to join {@link ProfileStatement}s
     */
    enum OP {

        /**
         * All statements in the chain must evaluate to true
         */
        AND,

        /**
         * At least one statement in the chain must evaluate to true
         */
        OR,

        /**
         * None of the statements in the chain must evaluate to true
         */
        NOT
    }

    /**
     * All statements in the chain must evaluate to true
     *
     * @param statements the statements
     * @return the chain
     */
    StatementChain and(ProfileStatement... statements);

    /**
     * One statement in the chain must evaluate to true
     *
     * @param statements the statements
     * @return the chain
     *
     */
    StatementChain or(ProfileStatement... statements);

    /**
     * No statements in the chain may evaluate to true
     *
     * @param statements the statements
     * @return the chain
     */
    StatementChain not(ProfileStatement... statements);

    /**
     * Evaluate the candidate string against the chain of {@link ProfileStatement}s.
     *
     * @param candidate the candidate string
     * @return the result of the evaluation
     */
    boolean evaluate(String candidate);
}
