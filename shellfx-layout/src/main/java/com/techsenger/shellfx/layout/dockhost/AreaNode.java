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

import com.techsenger.shellfx.core.area.AreaFxView;

/**
 * A leaf {@link ModelNode} holding a single area.
 *
 * @author Pavel Castornii
 */
public interface AreaNode extends ModelNode {

    /**
     * Returns the area held by this node.
     *
     * @return the area
     */
    AreaFxView<?> getArea();

    /**
     * Returns whether this node's area is the docking layout's main area.
     *
     * @return {@code true} if this is the main area, {@code false} otherwise
     */
    boolean isMain();
}
