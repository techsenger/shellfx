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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.annotations.Unmodifiable;
import java.util.Iterator;
import java.util.List;
import javafx.geometry.Orientation;

/**
 * A group node representing a nested set of {@link ModelNode}s (areas and/or nested groups) arranged along
 * {@code orientation}, with {@code children} describing its sections in order.
 *
 * @author Pavel Castornii
 */
public final class GroupModelNode implements ModelNode, Iterable<ModelNode> {

    private final Orientation orientation;

    private final List<ModelNode> children;

    private final double proportion;

    /**
     * Creates a new group node.
     *
     * @param orientation the orientation of the group
     * @param children this node's sections, in order
     * @param proportion this node's relative size among its siblings, a value between {@code 0} and {@code 1}, or
     *         {@link ModelNodeBuilder#UNSET_PROPORTION}
     */
    public GroupModelNode(Orientation orientation, List<ModelNode> children, double proportion) {
        this.orientation = orientation;
        this.children = List.copyOf(children);
        this.proportion = proportion;
    }

    /**
     * Returns the orientation of this group.
     *
     * @return the group orientation
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Returns an unmodifiable list of this node's sections, in order.
     *
     * @return the child nodes of this group
     */
    public @Unmodifiable List<ModelNode> getChildren() {
        return children;
    }

    @Override
    public double getProportion() {
        return proportion;
    }

    @Override
    public Iterator<ModelNode> iterator() {
        return new TreeIterator(this);
    }
}
