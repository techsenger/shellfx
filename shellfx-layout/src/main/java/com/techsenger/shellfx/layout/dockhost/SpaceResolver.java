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

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves which sibling(s) participate in a space change for a TabDock — donating space when it is being
 * added or restored ({@link TabDockOperation.Direction#DONATE}), or receiving space when it is being removed
 * or minimized ({@link TabDockOperation.Direction#RECEIVE}).
 * <p>Not every space change reaches this resolver. It is never invoked when:
 * <ul>
 *     <li>there is only one possible participant — a single neighbor at the edge of a SplitPane, or the sole
 *     node being wrapped into a new SplitPane; the one available node is used directly;</li>
 *     <li>the insertion position was already determined by a drag-and-drop drop indicator — this covers both
 *     an existing TabDock being relocated ({@link TabDockOperation#ARRIVE}) and a brand-new TabDock created by
 *     dragging a single Tab out ({@link TabDockOperation#ADD} in that specific case). In both, the donor is
 *     fully determined by the indicator geometry the user already saw before releasing the drop — deviating
 *     from it here would silently contradict what was shown. See {@link TabDockOperation} for the full rule.</li>
 * </ul>
 * <p>
 * {@code previousSiblings} and {@code nextSiblings} list the TabDock's siblings within the same live SplitPane,
 * in live order, split at the TabDock's own position. Either list may be empty (the TabDock sits at an edge of
 * its SplitPane), but never both. A sibling may be a leaf ({@link AreaNode}) or an entire nested group
 * ({@link GroupNode}) — both are valid participants and are treated identically as a single unit.
 * <p>
 * The returned list must be non-empty and drawn only from {@code previousSiblings}/{@code nextSiblings} — never
 * a node from outside them, never a duplicate. When more than one node is returned, the space is split among
 * them proportionally to each node's own current size.
 *
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface SpaceResolver {

    List<ModelNode> resolve(TabDockOperation operation, List<ModelNode> previousSiblings,
            List<ModelNode> nextSiblings);

    static SpaceResolver nearest() {
        return (op, prev, next) -> {
            var result = new ArrayList<ModelNode>(2);
            if (!prev.isEmpty()) {
                result.add(prev.get(prev.size() - 1));
            }
            if (!next.isEmpty()) {
                result.add(next.get(0));
            }
            return result;
        };
    }

    static SpaceResolver all() {
        return (op, prev, next) -> {
            var result = new ArrayList<ModelNode>(prev.size() + next.size());
            result.addAll(prev);
            result.addAll(next);
            return result;
        };
    }
}
