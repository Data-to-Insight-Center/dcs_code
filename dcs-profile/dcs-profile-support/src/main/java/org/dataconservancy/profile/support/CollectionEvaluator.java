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

import java.util.BitSet;
import java.util.Collection;

/**
 * Contains logic for evaluating a {@link ProfileStatement} against a <code>Collection</code> of candidate Strings.
 * The logic used depends on the {@link CollectionMatchStrategy strategy} that is selected.
 * <p/>
 * Available strategies are:
 * <ul>
 *     <li>{@link CollectionMatchStrategy#AT_LEAST_ONE}: At least one of the candidate strings in the set must
 *      match the supplied <code>statement</code></li>
 *     <li>{@link CollectionMatchStrategy#EXACTLY_ONE}: Exactly one of the candidate strings in the set must
 *      match the supplied <code>statement</code></li>
 *     <li>{@link CollectionMatchStrategy#NONE}: none of the candidate strings in the set must match the supplied
 *      <code>statement</code></li>
 * </ul>
 */
class CollectionEvaluator {
    private CollectionMatchStrategy strategy;

    CollectionEvaluator(CollectionMatchStrategy strategy) {
        this.strategy = strategy;
    }

    CollectionMatchStrategy getStrategy() {
        return strategy;
    }

    void setStrategy(CollectionMatchStrategy strategy) {
        this.strategy = strategy;
    }

    boolean evaluate(Collection<String> candidates, ProfileStatement statement) {
        if (strategy == null) {
            throw new IllegalStateException("A CollectionMatchStrategy must be set.");
        }

        if (candidates == null) {
            throw new IllegalArgumentException("The set of candidate strings must not be null.");
        }

        if (statement == null) {
            throw new IllegalArgumentException("The profile statement must not be null.");
        }

        final BitSet bits = new BitSet(candidates.size());
        int i = 0;

        switch (strategy) {
            case EXACTLY_ONE:

                // The profile statement must evaluate 'true' against one and only one
                // candidate String

                for (String candidate : candidates) {
                    if (statement.evaluate(candidate)) {
                        bits.set(i);
                    }
                    i++;
                }

                return bits.cardinality() == 1;

            case AT_LEAST_ONE:

                // The profile statement must evaluate 'true' against one
                // candidate String

                for (String candidate : candidates) {
                    if (statement.evaluate(candidate)) {
                        return true;
                    }
                }

                break;

            case NONE:

                // Each profile statement must evaluate 'false' against all candidate
                // Strings

                for (String candidate : candidates) {
                    if (statement.evaluate(candidate)) {
                        bits.set(i);
                    }
                    i++;
                }

                return bits.cardinality() == 0;

        }

        return false;
    }
}
