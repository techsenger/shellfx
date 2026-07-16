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

import java.util.List;
import javafx.geometry.Orientation;

/**
 * A branch {@link ModelNode} representing a group of areas and/or nested groups arranged along
 * {@link #getOrientation()}, with {@link #getChildren()} describing its sections in order.
 *
 * @author Pavel Castornii
 */
public interface GroupNode extends ModelNode, Iterable<ModelNode> {

    /**
     * Returns the orientation along which this group's children are arranged.
     *
     * @return the group orientation
     */
    Orientation getOrientation();

    /**
     * Returns this node's children, in order.
     * <p>
     * For an immutable node, this list is fixed at construction time. For a live node, it is recomputed on every
     * call from the docking layout's current state.
     *
     * @return the child nodes of this group
     */
    List<ModelNode> getChildren();
}
