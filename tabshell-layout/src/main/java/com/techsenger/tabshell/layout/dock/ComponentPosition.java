/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.layout.dock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
class ComponentPosition {

    enum NodeType {

        SPLIT_SPACE, TAB_DOCK, MAIN
    }

    static class SnapshotNode {

        private final UUID uuid;

        private final NodeType type;

        private final Orientation orientation;

        private final List<SnapshotNode> children = new ArrayList<>();

        private SnapshotNode parent;

        SnapshotNode(UUID uuid, NodeType type, Orientation orientation) {
            this.uuid = uuid;
            this.type = type;
            this.orientation = orientation;
        }

        public UUID getUuid() {
            return uuid;
        }

        public NodeType getType() {
            return type;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public List<SnapshotNode> getChildren() {
            return children;
        }

        public SnapshotNode getParent() {
            return parent;
        }

        public void setParent(SnapshotNode parent) {
            this.parent = parent;
        }
    }

    static String toString(ComponentPosition.SnapshotNode root) {
        StringBuilder sb = new StringBuilder();
        printSnapshotNode(root, 0, sb);
        return sb.toString();
    }

    private static void printSnapshotNode(ComponentPosition.SnapshotNode node, int depth, StringBuilder sb) {
        if (node == null) {
            return;
        }
        String indent = " ".repeat(depth * 4);

        sb.append("\n")
                .append(indent)
                .append(node.getType())
                .append(" [uuid: ")
                .append(node.getUuid());
        if (node.getOrientation() != null) {
            sb.append(", orientation: ")
                    .append(node.getOrientation());
        }
        sb.append("]");
        for (ComponentPosition.SnapshotNode child : node.getChildren()) {
            printSnapshotNode(child, depth + 1, sb);
        }
    }

    private final SnapshotNode snapshotRoot;

    private final UUID uuid;

    private final int index;

    private final double width;

    private final double height;

    ComponentPosition(SnapshotNode snapshotRoot, UUID uuid, int index, double width, double height) {
        this.snapshotRoot = snapshotRoot;
        this.uuid = uuid;
        this.index = index;
        this.width = width;
        this.height = height;
    }

    public SnapshotNode getSnapshotRoot() {
        return snapshotRoot;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getIndex() {
        return index;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "ComponentPosition [" + "uuid:" + uuid + ", index:" + index + ", width:" + width
                + ", height:" + height + ']';
    }
}
