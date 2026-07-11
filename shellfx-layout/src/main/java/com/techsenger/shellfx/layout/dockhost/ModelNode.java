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

/**
 * A node in a docking layout tree, describing a {code SplitPane} or a single {@code SplitPane} section.
 * <p>
 * A node is either an {@link AreaModelNode} (a leaf holding an area) or a {@link SplitModelNode} (a nested split
 * holding further nodes). Only one {@link AreaModelNode} in the whole tree cab be the main area — see
 * {@link AreaModelNode#isMain()}.
 *
 * @author Pavel Castornii
 */
public sealed interface ModelNode permits AreaModelNode, SplitModelNode  {

    /**
     * Returns this node's relative size among its siblings.
     *
     * @return a value between {@code 0} and {@code 1}, or
     *         {@link ModelNodeBuilder#UNSET_PROPORTION}
     */
    double getProportion();
}