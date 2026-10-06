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

import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.slot.Slot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The tree of slots as the registries describe it for one component instance: which slots sit in
 * which one, which control each slot stands for, and which controls fill each group. The builders walk it and
 * report what they build and what looks wrong in it to its logger.
 *
 * @author Pavel Castornii
 */
final class SlotTree<V extends ParentView<?>> {

    private final V view;

    private final Map<Slot<?>, List<SlotRegistration>> childrenByParent = new HashMap<>();

    private final Map<Slot<?>, NodeRegistration> nodesBySlot = new HashMap<>();

    private final Map<Slot<?>, List<LeafRegistration>> leavesByGroup = new HashMap<>();

    private final List<ControlProvider<? super V, ?>> providers = new ArrayList<>();

    private final SlotTreeLogger logger;

    SlotTree(SlotRegistry slotRegistry, ControlRegistry controlRegistry, V view) {
        this.view = view;
        this.logger = new SlotTreeLogger(getViewName());
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

    V getView() {
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
     * Returns the providers of all controls created so far, in the order of their creation: a control is created
     * before the controls put into it.
     */
    List<ControlProvider<? super V, ?>> getProviders() {
        return providers;
    }

    /**
     * Returns the logger that collects the report of the build of this tree.
     */
    SlotTreeLogger getLogger() {
        return logger;
    }

    /**
     * Adds to {@code parent} the groups that have controls registered but are not put into any slot, so their
     * controls can never be shown.
     */
    void logOrphanGroups(SlotTreeLogger.Node parent) {
        var placed = childrenByParent.values().stream().flatMap(List::stream).map(SlotRegistration::getChild)
                .collect(Collectors.toSet());
        leavesByGroup.entrySet().stream()
                .filter(e -> !placed.contains(e.getKey()))
                .sorted(Comparator.comparing(e -> e.getKey().getText()))
                .forEach(e -> parent.add("Group: " + e.getKey().getText()).warn("not put into any menu or tool bar, "
                        + "its " + e.getValue().size() + " controls are never shown"));
    }

    /**
     * Tells whether a provider is registered for the control {@code slot} stands for.
     */
    boolean hasNode(Slot<?> slot) {
        return nodesBySlot.containsKey(slot);
    }

    /**
     * Creates and initializes the provider of the control {@code slot} stands for.
     *
     * @return the control.
     * @throws IllegalStateException if no provider has been registered for the slot
     */
    Object createNode(Slot<?> slot) {
        var registration = nodesBySlot.get(slot);
        if (registration == null) {
            throw new IllegalStateException("No provider is registered for slot " + slot.getText());
        }
        return create(registration);
    }

    /**
     * Creates and initializes the provider of a control put into a group.
     *
     * @return the control of the provider.
     */
    Object createLeaf(LeafRegistration leaf) {
        return create(leaf);
    }

    @SuppressWarnings("unchecked")
    private Object create(AbstractControlRegistration registration) {
        var provider = (ControlProvider<? super V, ?>) registration.createProvider();
        provider.initialize(view);
        providers.add(provider);
        return provider.getControl();
    }
}
