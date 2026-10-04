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
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.slot.Slot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The tree of slots as the registries describe it for one component instance: which slots sit in
 * which one, which control each slot stands for, and which controls fill each group. The builders walk it.
 *
 * @author Pavel Castornii
 */
final class SlotTree {

    private final ParentView<?> view;

    private final Map<Slot<?>, List<SlotRegistration>> childrenByParent = new HashMap<>();

    private final Map<Slot<?>, NodeRegistration> nodesBySlot = new HashMap<>();

    private final Map<Slot<?>, List<LeafRegistration>> leavesByGroup = new HashMap<>();

    SlotTree(SlotRegistry slotRegistry, ControlRegistry controlRegistry, ParentView<?> view) {
        this.view = view;
        for (var registration : slotRegistry.getRegistrationsFor(view)) {
            childrenByParent.computeIfAbsent(registration.getParent(), k -> new ArrayList<>()).add(registration);
        }
        childrenByParent.values().forEach(children -> children.sort(
                Comparator.comparingInt(SlotRegistration::getPosition)));
        for (var registration : controlRegistry.getRegistrationsFor(view)) {
            if (registration instanceof NodeRegistration node) {
                nodesBySlot.put(node.getSlot(), node);
            } else if (registration instanceof LeafRegistration leaf) {
                leavesByGroup.computeIfAbsent(leaf.getSlot(), k -> new ArrayList<>()).add(leaf);
            }
        }
        leavesByGroup.values().forEach(leaves -> leaves.sort(Comparator.comparingInt(LeafRegistration::getPosition)));
    }

    ParentView<?> getView() {
        return view;
    }

    /**
     * Returns the name of the view's class for logging; an anonymous subclass is named after its closest named
     * superclass.
     */
    String getViewName() {
        Class<?> type = view.getClass();
        while (type.getSimpleName().isEmpty() && type.getSuperclass() != null) {
            type = type.getSuperclass();
        }
        return type.getSimpleName();
    }

    /**
     * Returns the slots put directly into {@code parent}, ordered by position.
     */
    List<SlotRegistration> getChildren(Slot<?> parent) {
        return childrenByParent.getOrDefault(parent, List.of());
    }

    /**
     * Returns the registrations of the controls put into the group {@code group}, ordered by position.
     */
    List<LeafRegistration> getLeaves(Slot<?> group) {
        return leavesByGroup.getOrDefault(group, List.of());
    }

    /**
     * Creates the control {@code slot} stands for.
     *
     * @return the control, or {@code null} if no factory has been registered for the slot.
     */
    @Nullable Object createNode(Slot<?> slot) {
        var registration = nodesBySlot.get(slot);
        return registration == null ? null : registration.create(view);
    }
}
