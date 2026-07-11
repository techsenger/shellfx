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
 * A split node representing a nested {@code SplitPane} along {@code orientation}, with {@code children} describing its
 * sections in order.
 *
 * @author Pavel Castornii
 */
public final class SplitModelNode implements ModelNode, Iterable<ModelNode> {

    private final Orientation orientation;

    private final List<ModelNode> children;

    private final double proportion;

    /**
     * Creates a new split node.
     *
     * @param orientation the orientation of the split
     * @param children this node's sections, in order
     * @param proportion this node's relative size among its siblings, a value between {@code 0} and {@code 1}, or
     *         {@link ModelNodeBuilder#UNSET_PROPORTION}
     */
    public SplitModelNode(Orientation orientation, List<ModelNode> children, double proportion) {
        this.orientation = orientation;
        this.children = List.copyOf(children);
        this.proportion = proportion;
    }

    /**
     * Returns the orientation of this split.
     *
     * @return the split orientation
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Returns an unmodifiable list of this node's sections, in order.
     *
     * @return the child nodes of this split
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
