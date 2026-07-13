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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.area.AbstractAreaFxView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Orientation;

/**
 * A builder for constructing immutable {@link ModelNode} trees.
 * <p>
 * Use {@link #root(Orientation, Consumer)} to start building the tree, then {@link #area(AbstractAreaFxView, double)}
 * / {@link #mainArea(AbstractAreaFxView, double)} for leaf nodes and {@link #split(Orientation, double, Consumer)}
 * for nested splits within the given callbacks. Overloads without a {@code proportion} use {@link #UNSET_PROPORTION},
 * leaving the actual size to be distributed automatically among siblings.
 * <p>
 * At most one {@link #mainArea(AbstractAreaFxView, double)} may be added across the whole tree — a tree may have zero
 * or one main area, never more.
 *
 * @author Pavel Castornii
 */
public final class ModelNodeBuilder {

    /**
     * Shared, tree-wide tracker ensuring that at most one main area is added.
     */
    private static final class MainTracker {

        private boolean found;

        void markMain() {
            if (found) {
                throw new IllegalStateException("Only one main area is allowed per tree");
            }
            found = true;
        }
    }

    /**
     * Creates the root split node representing the top-level {@code SplitPane}, with an automatically distributed
     * proportion. Its children are added by invoking the given callback on the new node's builder.
     *
     * @param orientation the orientation of the split
     * @param children callback that adds the node's children
     * @return the resulting node
     */
    public static SplitModelNode root(Orientation orientation, Consumer<ModelNodeBuilder> children) {
        return root(orientation, UNSET_PROPORTION, children);
    }

    /**
     * Creates the root split node representing the top-level {@code SplitPane}. Its children are added by invoking
     * the given callback on the new node's builder.
     *
     * @param orientation the orientation of the split
     * @param proportion this node's relative size, a value between {@code 0} and {@code 1}, or
     *          {@link #UNSET_PROPORTION}
     * @param children callback that adds the node's children
     * @return the resulting node
     * @throws IllegalStateException if no children were added within the callback
     */
    public static SplitModelNode root(Orientation orientation, double proportion, Consumer<ModelNodeBuilder> children) {
        var root = new ModelNodeBuilder(orientation, null, false, proportion, new MainTracker());
        children.accept(root);
        return root.build();
    }

    /**
     * Sentinel value indicating that no explicit proportion was set for a node; its size should be distributed
     * automatically among siblings.
     */
    public static final double UNSET_PROPORTION = -1;

    private final @Nullable Orientation orientation;

    private final List<ModelNodeBuilder> children = new ArrayList<>();

    private final @Nullable AbstractAreaFxView<?> area;

    private final boolean main;

    private final double proportion;

    private final MainTracker mainTracker;

    private ModelNodeBuilder(@Nullable Orientation orientation, @Nullable AbstractAreaFxView<?> area, boolean main,
            double proportion, MainTracker mainTracker) {
        this.orientation = orientation;
        this.area = area;
        this.main = main;
        this.proportion = proportion;
        this.mainTracker = mainTracker;
        if (main) {
            mainTracker.markMain();
        }
    }

    /**
     * Adds a leaf node holding the given area, with an automatically distributed proportion.
     *
     * @param area the leaf's area
     * @return this builder
     */
    public ModelNodeBuilder area(AbstractAreaFxView<?> area) {
        return area(area, UNSET_PROPORTION);
    }

    /**
     * Adds a leaf node holding the given area.
     *
     * @param area the leaf's area
     * @param proportion this node's relative size among its siblings, a value between {@code 0} and {@code 1}, or
     *         {@link #UNSET_PROPORTION}
     * @return this builder
     */
    public ModelNodeBuilder area(AbstractAreaFxView<?> area, double proportion) {
        children.add(new ModelNodeBuilder(null, area, false, proportion, mainTracker));
        return this;
    }

    /**
     * Adds a leaf node holding the given area, marking it as the layout's main area, with an automatically
     * distributed proportion.
     *
     * @param area the leaf's area
     * @return this builder
     * @throws IllegalStateException if a main area was already added elsewhere in the tree
     */
    public ModelNodeBuilder mainArea(AbstractAreaFxView<?> area) {
        return mainArea(area, UNSET_PROPORTION);
    }

    /**
     * Adds a leaf node holding the given area, marking it as the layout's main area.
     *
     * @param area the leaf's area
     * @param proportion this node's relative size among its siblings, a value between {@code 0} and {@code 1}, or
     *         {@link #UNSET_PROPORTION}
     * @return this builder
     * @throws IllegalStateException if a main area was already added elsewhere in the tree
     */
    public ModelNodeBuilder mainArea(AbstractAreaFxView<?> area, double proportion) {
        children.add(new ModelNodeBuilder(null, area, true, proportion, mainTracker));
        return this;
    }

    /**
     * Adds a split node representing a nested {@code SplitPane}, with an automatically distributed proportion.
     * Its children are added by invoking the given callback on the new node's builder.
     *
     * @param orientation the orientation of the nested split
     * @param children callback that adds the node's children
     * @return this builder
     */
    public ModelNodeBuilder split(Orientation orientation, Consumer<ModelNodeBuilder> children) {
        return split(orientation, UNSET_PROPORTION, children);
    }

    /**
     * Adds a split node representing a nested {@code SplitPane}. Its children are added by invoking the given callback
     * on the new node's builder.
     *
     * @param orientation the orientation of the nested split
     * @param proportion this node's relative size among its siblings, a value between {@code 0} and {@code 1}, or
     *         {@link #UNSET_PROPORTION}
     * @param children callback that adds the node's children
     * @return this builder
     * @throws IllegalArgumentException if {@code orientation} equals this node's orientation
     */
    public ModelNodeBuilder split(Orientation orientation, double proportion, Consumer<ModelNodeBuilder> children) {
        if (orientation == this.orientation) {
            throw new IllegalArgumentException("Child orientation must differ from parent orientation");
        }
        var child = new ModelNodeBuilder(orientation, null, false, proportion, mainTracker);
        children.accept(child);
        this.children.add(child);
        return this;
    }

    private SplitModelNode build() {
        // orientation is always non-null here — root(...) is the only entry point,
        // and its orientation parameter is non-null.
        if (children.isEmpty()) {
            throw new IllegalStateException("A split node must have at least one child");
        }
        var builtChildren = children.stream().map(ModelNodeBuilder::buildNode).toList();
        return new SplitModelNode(orientation, builtChildren, proportion);
    }

    private ModelNode buildNode() {
        if (orientation == null) {
            return new AreaModelNode(area, main, proportion);
        }
        if (children.isEmpty()) {
            throw new IllegalStateException("A split node must have at least one child");
        }
        var builtChildren = children.stream().map(ModelNodeBuilder::buildNode).toList();
        return new SplitModelNode(orientation, builtChildren, proportion);
    }
}
