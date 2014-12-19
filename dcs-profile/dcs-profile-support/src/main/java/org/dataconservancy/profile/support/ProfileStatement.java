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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.dataconservancy.profile.support.MatchOp.ENDS_WITH;
import static org.dataconservancy.profile.support.MatchOp.EQUAL_TO;
import static org.dataconservancy.profile.support.MatchOp.NOT_EQUAL_TO;
import static org.dataconservancy.profile.support.MatchOp.STARTS_WITH;

/**
 * A ProfileStatement encapsulates a comparison operator and a String to compare candidate strings to.  Clients of this
 * class are expected to call one of {@link #evaluate(String)}to determine if this statement evaluates to
 * <code>true</code>.
 *
 * TODO: Make more injection friendly
 */
public class ProfileStatement {

    private MatchOp matchOp;
    private String value;
    private boolean normalize = false;
    private static Map<CollectionMatchStrategy, CollectionEvaluator> statementEvaluators;

    static {
        statementEvaluators = new HashMap<CollectionMatchStrategy, CollectionEvaluator>();
        statementEvaluators.put(CollectionMatchStrategy.AT_LEAST_ONE, new CollectionEvaluator(CollectionMatchStrategy.AT_LEAST_ONE));
        statementEvaluators.put(CollectionMatchStrategy.EXACTLY_ONE, new CollectionEvaluator(CollectionMatchStrategy.EXACTLY_ONE));
        statementEvaluators.put(CollectionMatchStrategy.NONE, new CollectionEvaluator(CollectionMatchStrategy.NONE));
    }

    /**
     * Constructs a new ProfileStatement given a comparison operator and a comparison string.
     *
     * @param matchOp the comparison operator, must not be null
     * @param value the comparison string, must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public ProfileStatement(MatchOp matchOp, String value) {
        if (matchOp == null) {
            throw new IllegalArgumentException("Comparison operator must not be null.");
        }

        if (value == null) {
            throw new IllegalArgumentException("String value must not be null.");
        }

        this.matchOp = matchOp;
        this.value = value;
    }

    /**
     * Constructs a new ProfileStatement given a comparison operator and a comparison string.  If <code>normalize</code>
     * is <code>true</code>, <code>value</code> and candidate strings will be lower-cased and trimmed prior to
     * comparison.
     *
     * @param matchOp the comparison operator, must not be null
     * @param value the comparison string, must not be null
     * @param normalize normalize the string values prior to comparison
     * @throws IllegalArgumentException if any argument is null
     */
    public ProfileStatement(MatchOp matchOp, String value, boolean normalize) {
        this(matchOp, value);
        this.normalize = normalize;
        this.value = value.toLowerCase().trim();
    }

    /**
     * Constructs a statement that must be equal to <code>value</code>.
     *
     * @param value the value
     * @return the ProfileStatement
     */
    public static ProfileStatement equals(String value) {
        return new ProfileStatement(EQUAL_TO, value);
    }

    /**
     * Constructs a statement that must start with <code>value</code>.
     *
     * @param value the value
     * @return the ProfileStatement
     */
    public static ProfileStatement startsWith(String value) {
        return new ProfileStatement(STARTS_WITH, value);
    }

    /**
     * Constructs a statement that must end with <code>value</code>.
     *
     * @param value the value
     * @return the ProfileStatement
     */
    public static ProfileStatement endsWith(String value) {
        return new ProfileStatement(ENDS_WITH, value);
    }

    /**
     * Constructs a statement that must not be equal to <code>value</code>.
     *
     * @param value the value
     * @return the ProfileStatement
     */
    public static ProfileStatement notEqualTo(String value) {
        return new ProfileStatement(NOT_EQUAL_TO, value);
    }

    /**
     * Constructs a normalized statement that must be equal to <code>value</code>.
     *
     * @param value the value
     * @return the ProfileStatement
     */
    public static ProfileStatement normalizedEquals(String value) {
        return new ProfileStatement(EQUAL_TO, value, true);
    }

    /**
     * Constructs a normalized statement that must start with <code>value</code>.
     *
     * @param value the value
     * @return the ProfileStatement
     */
    public static ProfileStatement normalizedStartsWith(String value) {
        return new ProfileStatement(STARTS_WITH, value, true);
    }

    /**
     * Constructs a normalized statement that must end with <code>value</code>.
     *
     * @param value the value
     * @return the ProfileStatement
     */
    public static ProfileStatement normalizedEndsWith(String value) {
        return new ProfileStatement(ENDS_WITH, value, true);
    }

    /**
     * Constructs a normalized statement that must not be equal to <code>value</code>.
     *
     * @param value the value
     * @return the ProfileStatement
     */
    public static ProfileStatement normalizedNotEqualTo(String value) {
        return new ProfileStatement(NOT_EQUAL_TO, value, true);
    }

    /**
     * Evaluates the candidate string against the <code>ProfileStatement</code>'s {@link #getValue() value}, using the
     * {@link #getMatchOp() comparison operator}.
     *
     * @param candidate the candidate string
     * @return true if the candidate string is successfully evaluated against the <code>ProfileStatement</code> string,
     *              false otherwise
     */
    public boolean evaluate(String candidate) {
        if (candidate == null) {
            return false;
        }

        if (normalize) {
            candidate = candidate.toLowerCase().trim();
        }

        return evaluateInternal(candidate, this.value);
    }

    /**
     * Evaluates a collection of candidate strings against the <code>ProfileStatement</code>'s {@link #getValue() value},
     * using the {@link #getMatchOp() comparison operator} and the supplied {@link CollectionMatchStrategy strategy}.
     *
     * @param candidates the collection of candidate strings
     * @param strategy the
     * @return true if the candidate string is successfully evaluated against the <code>ProfileStatement</code> string,
     *              false otherwise
     */
    public boolean evaluate(Collection<String> candidates, CollectionMatchStrategy strategy) {
        if (candidates == null) {
            return false;
        }

        return statementEvaluators.get(strategy).evaluate(candidates, this);
    }

    /**
     * Perform the evaluation of the candidate and comparison strings, which might have been normalized.
     *
     * @param candidate the candidate string
     * @param value the comparison string
     * @return the result of the evaluation according to the comparison operator
     */
    private boolean evaluateInternal(String candidate, String value) {
        switch (matchOp) {
            case ENDS_WITH:
                return candidate.endsWith(value);
            case EQUAL_TO:
                return candidate.equals(value);
            case NOT_EQUAL_TO:
                return !candidate.equals(value);
            case STARTS_WITH:
                return candidate.startsWith(value);
            default:
                return false;
        }
    }

    /**
     * Return the comparison operator used by this <code>ProfileStatement</code> to evaluate candidate strings.
     *
     * @return the comparison operator
     */
    MatchOp getMatchOp() {
        return matchOp;
    }

    /**
     * Return the comparison string used by this <code>ProfileStatement</code> to evaluate candidate strings.
     *
     * @return the comparison string
     */
    String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ProfileStatement{" +
                "matchOp=" + matchOp +
                ", value='" + value + '\'' +
                ", normalize=" + normalize +
                '}';
    }
}
