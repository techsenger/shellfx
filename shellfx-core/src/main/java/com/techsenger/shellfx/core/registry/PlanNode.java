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
import com.techsenger.shellfx.material.slot.Slot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A node of the plan of a build: a menu, a group or a tool bar group that is going to be created, with what goes into
 * it - controls and nested menus - in the order of their positions. A slot that has nothing to build is not in the
 * plan.
 *
 * @author Pavel Castornii
 */
final class PlanNode {

    /**
     * A control or a nested node of a node, at a position.
     */
    record Entry(int position, @Nullable LeafRegistration leaf, @Nullable PlanNode node) {

    }

    private final Slot<?> slot;

    private final int position;

    private final List<Entry> entries = new ArrayList<>();

    PlanNode(Slot<?> slot, int position) {
        this.slot = slot;
        this.position = position;
    }

    Slot<?> getSlot() {
        return slot;
    }

    int getPosition() {
        return position;
    }

    List<Entry> getEntries() {
        return entries;
    }

    /**
     * Returns the nested nodes, in the order of their positions.
     */
    List<PlanNode> getNodes() {
        return entries.stream().map(Entry::node).filter(Objects::nonNull).toList();
    }

    boolean isEmpty() {
        return entries.isEmpty();
    }

    void addLeaf(LeafRegistration leaf) {
        entries.add(new Entry(leaf.getPosition(), leaf, null));
    }

    void addNode(PlanNode node) {
        entries.add(new Entry(node.position, null, node));
    }
}
