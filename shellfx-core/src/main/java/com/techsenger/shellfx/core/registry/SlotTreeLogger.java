/*
 * Copyright 2024-2026 Pavel Castornii.
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

package com.techsenger.shellfx.core.registry;

import com.techsenger.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/**
 * The report of one build of a slot tree. {@link #toString()} returns the tree that was built, as it is in the
 * result; {@link #describeProblems()} returns a tree of everything that looks wrong - what was left out and what
 * was built with a flaw - with only the places that lead to the problems. {@link #write(Logger, String)} logs the
 * first at debug level and, if there are problems, the second at warning level. A disabled logger records nothing.
 *
 * @author Pavel Castornii
 */
final class SlotTreeLogger {

    /**
     * A line of the report on the control tree that was built, not on the slot tree it was built from: a menu or
     * group appears only if it built something or lost something, and so does every control built into a group.
     */
    static final class Node {

        /**
         * The text of the line without the problems.
         */
        private final String text;

        /**
         * The lines nested under this one, in the order they were added: the groups of a menu, the controls of a
         * group.
         */
        private final List<Node> children = new ArrayList<>();

        /**
         * What looks wrong in this node, written after its text in the report of the problems.
         */
        private final List<String> problems = new ArrayList<>();

        /**
         * Tells whether the node is in the built tree; a node that is not stays only in the report of the problems.
         */
        private boolean built = true;

        /**
         * The position of the last child added with a position, to find a sibling that shares it.
         */
        private @Nullable Integer lastChildPosition;

        private Node(String text) {
            this.text = text;
        }

        /**
         * Adds a node without a position inside this one.
         */
        Node add(String text) {
            return addChild(text);
        }

        /**
         * Adds a node at {@code position} inside this one; add siblings in the order of their positions, so a
         * sibling at the same position as the previous one gets a problem, as the order of such nodes is not
         * defined.
         */
        Node add(String text, int position) {
            return add(text, position, null);
        }

        /**
         * Adds a node at {@code position} with the details written after the position.
         *
         * @see #add(String, int)
         */
        Node add(String text, int position, @Nullable String details) {
            if (this == STUB) {
                return this;
            }
            var child = addChild(text + ", position: " + position + (details == null ? "" : ", " + details));
            if (lastChildPosition != null && lastChildPosition == position) {
                child.warn("shares the position " + position + " with the previous sibling");
            }
            lastChildPosition = position;
            return child;
        }

        /**
         * Notes a flaw of this node, which is built nevertheless.
         */
        void warn(String problem) {
            if (this != STUB) {
                problems.add(problem);
            }
        }

        /**
         * Notes that this node is not built, and why; it leaves the built tree.
         */
        void skip(String problem) {
            if (this != STUB) {
                problems.add(problem);
                built = false;
            }
        }

        /**
         * Tells whether this node or anything inside of it has a problem.
         */
        boolean hasProblems() {
            return !problems.isEmpty() || children.stream().anyMatch(Node::hasProblems);
        }

        /**
         * Settles a child once it is done: a child that ended up not built leaves the built tree, and goes from the
         * report altogether unless it has problems inside, which it stays as the way to.
         */
        void settle(Node child, boolean built) {
            if (built) {
                return;
            }
            if (child.hasProblems()) {
                child.built = false;
            } else {
                children.remove(child);
            }
        }

        private Node addChild(String text) {
            if (this == STUB) {
                return this;
            }
            var child = new Node(text);
            children.add(child);
            return child;
        }

        private void appendBuilt(StringBuilder result, int depth) {
            if (!built) {
                return;
            }
            append(result, depth, text);
            children.forEach(c -> c.appendBuilt(result, depth + 1));
        }

        private void appendProblems(StringBuilder result, int depth) {
            if (!hasProblems()) {
                return;
            }
            var line = new StringBuilder(text);
            problems.forEach(p -> line.append(", warning: ").append(p));
            append(result, depth, line.toString());
            children.forEach(c -> c.appendProblems(result, depth + 1));
        }

        private void append(StringBuilder result, int depth, String line) {
            if (!result.isEmpty()) {
                result.append(System.lineSeparator());
            }
            result.append(INDENT.repeat(depth)).append(line);
        }
    }

    /**
     * The stand-in for the nodes of a disabled report: it records nothing and every node added to it is itself.
     */
    private static final Node STUB = new Node("");

    private static final String INDENT = "    ";

    private final String viewName;

    private final List<Node> roots = new ArrayList<>();

    private boolean enabled;

    SlotTreeLogger(String viewName) {
        this.viewName = viewName;
    }

    /**
     * Turns the report on or off; while it is off nothing is recorded and nothing is written. It is off by default.
     */
    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Adds a node without a position on the top level of the report.
     */
    Node add(String text) {
        if (!enabled) {
            return STUB;
        }
        var node = new Node(text);
        roots.add(node);
        return node;
    }

    /**
     * Tells whether anything in the report looks wrong.
     */
    boolean hasProblems() {
        return roots.stream().anyMatch(Node::hasProblems);
    }

    /**
     * Describes everything that looks wrong as a tree of only the places that lead to the problems.
     *
     * @return the tree, or an empty text if there are no problems.
     */
    String describeProblems() {
        var result = new StringBuilder();
        roots.forEach(n -> n.appendProblems(result, 0));
        return result.toString();
    }

    /**
     * Logs the built tree at debug level and, if anything looks wrong, the problems at warning level, in a message
     * of their own.
     *
     * @param logger the logger of the builder
     * @param what   what was built, for the first line of the messages
     */
    void write(Logger logger, String what) {
        if (!enabled) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} built for {}:{}{}", what, viewName, System.lineSeparator(), this);
        }
        if (hasProblems()) {
            logger.warn("{} built for {} with warnings:{}{}", what, viewName, System.lineSeparator(),
                    describeProblems());
        }
    }

    @Override
    public String toString() {
        var result = new StringBuilder();
        roots.forEach(n -> n.appendBuilt(result, 0));
        return result.toString();
    }
}
