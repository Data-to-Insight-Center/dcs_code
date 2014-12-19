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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of {@link StatementChain}.
 */
class StatementChainImpl implements StatementChain {

    private LogicalOp logicalOp;
    private List<ProfileStatement> chain = new ArrayList<ProfileStatement>();

    public StatementChainImpl and(ProfileStatement... statements) {
        return buildChain(LogicalOp.AND, statements);
    }

    public StatementChainImpl or(ProfileStatement... statements) {
        return buildChain(LogicalOp.OR, statements);
    }

    public StatementChainImpl not(ProfileStatement... statements) {
        return buildChain(LogicalOp.NOT, statements);
    }

    private StatementChainImpl buildChain(LogicalOp logicalOperator, ProfileStatement... statements) {
        checkList(logicalOperator);
        this.logicalOp = logicalOperator;
        chain.addAll(Arrays.asList(statements));
        return this;
    }

    private void checkList(LogicalOp operator) {
        if (logicalOp != null && operator != logicalOp) {
            throw new IllegalArgumentException("Statements in a single chain can only be joined using a single operator.");
        }
    }

    public boolean evaluate(String candidate) {
        final Iterator<ProfileStatement> statementIterator = chain.iterator();
        final ProfileStatement initialStatement = statementIterator.next();

        boolean result;

        switch (logicalOp) {
            case NOT:
                result = !initialStatement.evaluate(candidate);
                break;
            default:
                result = initialStatement.evaluate(candidate);
        }

        while (statementIterator.hasNext()) {
            ProfileStatement statement = statementIterator.next();
            switch (logicalOp) {
                case AND:
                    result = statement.evaluate(candidate) && result;
                    break;
                case OR:
                    result = statement.evaluate(candidate) || result;
                    break;
                case NOT:
                    result = !statement.evaluate(candidate) && result;
                    break;
            }
        }

        return result;
    }

}
