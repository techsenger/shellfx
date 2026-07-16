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

/**
 * A node in a docking layout tree.
 * <p>
 * A {@code ModelNode} is either a leaf ({@link AreaNode}, holding a single area) or a branch ({@link GroupNode},
 * holding an ordered set of child nodes along an orientation). Both kinds come in two flavors:
 * <ul>
 *     <li><b>Immutable</b> — built via {@link ModelNodeBuilder}, an independent, unchanging description of a
 *     layout used for construction, restoration, and serialization.</li>
 *     <li><b>Live</b> — obtained via {@code DockHostView.Composer#getModelNode(AreaFxView)}, a thin, lazily
 *     evaluated view over the docking layout's actual current state, used for anchor-based navigation. Each call
 *     to {@link #getParent()} (or, on a {@link GroupNode}, {@link GroupNode#getChildren()}) reflects the tree as
 *     it is at the moment of the call — not a snapshot taken earlier.
 * </ul>
 *
 * @author Pavel Castornii
 */
public interface ModelNode {

    /**
     * Returns this node's relative size among its siblings.
     *
     * @return a value between {@code 0} and {@code 1}, or {@link ModelNodeBuilder#UNSET_PROPORTION} if no explicit
     *         proportion was set and the size is distributed automatically
     */
    double getProportion();

    /**
     * Returns the group this node belongs to.
     * <p>
     * For an immutable node, this is fixed at construction time and never changes. For a live node, this is
     * resolved afresh on every call against the docking layout's current state.
     *
     * @return the parent group, or {@code null} if this node is currently the root of its tree
     */
    @Nullable GroupNode getParent();
}
