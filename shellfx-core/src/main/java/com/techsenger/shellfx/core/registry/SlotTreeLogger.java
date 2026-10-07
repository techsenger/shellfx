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
 * The report of one build of a slot tree, made of two trees. The built tree is what was actually built, with the
 * hints and warnings of the controls; it is written while the controls are created. The defects tree is what the
 * registrations lack - a missing provider, a group put nowhere, controls at the same position - with only the places
 * that lead to the defects; it is written while the build is planned. {@link #write(Logger, String)} logs the built
 * tree at debug level, followed by the slots that the filter of the build rejected, and, if there are any, the
 * defects followed by the paths to the warnings of the built tree at warning level, in one message. So a build is
 * logged in at most two messages. A disabled logger records nothing.
 *
 * @author Pavel Castornii
 */
final class SlotTreeLogger {

    /**
     * A line of a report tree: a menu, a group or a control.
     */
    static final class Node {

        /**
         * The text of the line without the hints and warnings.
         */
        private final String text;

        /**
         * Tells whether siblings added with a position are checked for sharing it.
         */
        private final boolean checkPositions;

        /**
         * The lines nested under this one, in the order they were added: the groups of a menu, the controls of a
         * group.
         */
        private final List<Node> children = new ArrayList<>();

        /**
         * What is worth knowing about this node, written after its text in the built tree.
         */
        private final List<String> hints = new ArrayList<>();

        /**
         * What looks wrong in this node, written after its text.
         */
        private final List<String> warnings = new ArrayList<>();

        /**
         * The position of the last child added with a position, to find a sibling that shares it.
         */
        private @Nullable Integer lastChildPosition;

        private Node(String text, boolean checkPositions) {
            this.text = text;
            this.checkPositions = checkPositions;
        }

        /**
         * Adds a node without a position inside this one.
         */
        Node add(String text) {
            return addChild(text);
        }

        /**
         * Adds a node at {@code position} inside this one; in the defects tree add siblings in the order of their
         * positions, so a sibling at the same position as the previous one gets a warning, as the order of such
         * nodes is not defined.
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
            if (checkPositions && lastChildPosition != null && lastChildPosition == position) {
                child.warn("shares the position " + position + " with the previous sibling");
            }
            lastChildPosition = position;
            return child;
        }

        /**
         * Notes something worth knowing about this node that is not wrong.
         */
        void hint(String hint) {
            if (this != STUB) {
                hints.add(hint);
            }
        }

        /**
         * Notes a flaw of this node.
         */
        void warn(String warning) {
            if (this != STUB) {
                warnings.add(warning);
            }
        }

        /**
         * Tells whether this node or anything inside of it has a warning.
         */
        boolean hasWarnings() {
            return !warnings.isEmpty() || children.stream().anyMatch(Node::hasWarnings);
        }

        private Node addChild(String text) {
            if (this == STUB) {
                return this;
            }
            var child = new Node(text, checkPositions);
            children.add(child);
            return child;
        }

        private void appendAll(StringBuilder result, int depth) {
            var line = new StringBuilder(text);
            hints.forEach(h -> line.append(", hint: ").append(h));
            warnings.forEach(w -> line.append(", warning: ").append(w));
            append(result, depth, line.toString());
            children.forEach(c -> c.appendAll(result, depth + 1));
        }

        private void appendWarned(StringBuilder result, int depth) {
            if (!hasWarnings()) {
                return;
            }
            var line = new StringBuilder(text);
            warnings.forEach(w -> line.append(", warning: ").append(w));
            append(result, depth, line.toString());
            children.forEach(c -> c.appendWarned(result, depth + 1));
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
    private static final Node STUB = new Node("", false);

    private static final String INDENT = "    ";

    private final String viewName;

    private @Nullable Node builtRoot;

    private @Nullable Node defectsRoot;

    private final List<String> filteredOut = new ArrayList<>();

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
     * Starts the built tree with its root.
     *
     * @throws IllegalStateException if the built tree has been started already
     */
    Node addBuilt(String text) {
        if (!enabled) {
            return STUB;
        }
        if (builtRoot != null) {
            throw new IllegalStateException("The built tree has been started already");
        }
        builtRoot = new Node(text, false);
        return builtRoot;
    }

    /**
     * Starts the defects tree with its root.
     *
     * @throws IllegalStateException if the defects tree has been started already
     */
    Node addDefects(String text) {
        if (!enabled) {
            return STUB;
        }
        if (defectsRoot != null) {
            throw new IllegalStateException("The defects tree has been started already");
        }
        defectsRoot = new Node(text, true);
        return defectsRoot;
    }

    /**
     * Notes a slot that the filter of the build rejected; it is listed under the built tree.
     */
    void addFilteredOut(String slot) {
        if (enabled) {
            filteredOut.add(slot);
        }
    }

    /**
     * Describes the slots that the filter rejected in one line.
     *
     * @return the line, or an empty text if no slot was rejected.
     */
    String describeFilteredOut() {
        return filteredOut.isEmpty() ? "" : "Filtered out slots: " + String.join(", ", filteredOut);
    }

    /**
     * Tells whether anything in the registrations looks wrong.
     */
    boolean hasDefects() {
        return defectsRoot != null && defectsRoot.hasWarnings();
    }

    /**
     * Tells whether anything in the built controls looks wrong.
     */
    boolean hasWarnings() {
        return builtRoot != null && builtRoot.hasWarnings();
    }

    /**
     * Describes the defects of the registrations as a tree of only the places that lead to them.
     *
     * @return the tree, or an empty text if there are no defects.
     */
    String describeDefects() {
        var result = new StringBuilder();
        if (defectsRoot != null) {
            defectsRoot.appendWarned(result, 0);
        }
        return result.toString();
    }

    /**
     * Describes the warnings of the built controls as a tree of only the places that lead to them.
     *
     * @return the tree, or an empty text if there are no warnings.
     */
    String describeWarnings() {
        var result = new StringBuilder();
        if (builtRoot != null) {
            builtRoot.appendWarned(result, 0);
        }
        return result.toString();
    }

    /**
     * Logs the built tree with the filtered out slots at debug level and, if there are any, the defects followed by
     * the paths to the warnings at warning level; at most two messages.
     *
     * @param logger the logger of the builder
     * @param what   what was built, for the first line of the messages
     */
    void write(Logger logger, String what) {
        if (!enabled) {
            return;
        }
        if (logger.isDebugEnabled()) {
            var rejected = filteredOut.isEmpty() ? "" : System.lineSeparator() + describeFilteredOut();
            logger.debug("{} built for {}:{}{}{}", what, viewName, System.lineSeparator(), this, rejected);
        }
        if (hasDefects() || hasWarnings()) {
            var problems = new StringBuilder(describeDefects());
            if (hasWarnings()) {
                problems.append(problems.isEmpty() ? "" : System.lineSeparator()).append(describeWarnings());
            }
            logger.warn("{} built for {} with {}:{}{}", what, viewName, hasDefects() ? "defects" : "warnings",
                    System.lineSeparator(), problems);
        }
    }

    @Override
    public String toString() {
        var result = new StringBuilder();
        if (builtRoot != null) {
            builtRoot.appendAll(result, 0);
        }
        return result.toString();
    }
}
