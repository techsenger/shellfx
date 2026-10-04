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
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import com.techsenger.shellfx.material.slot.Slot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import java.util.Objects;
import java.util.Set;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;

/**
 * Registry of the tree of slots: which slot is put into which one, and at what position. It knows nothing about
 * controls; they are bound to the slots in the {@link ControlRegistry}.
 *
 * <p>Only the pairs that make sense can be registered, and the compiler rejects the others: a menu bar holds menus,
 * a menu and a context menu hold groups of menu items, a tool bar holds groups of controls, and a group of menu
 * items holds nested menus. The kind of a slot is its class, which is what tells the overloads apart.
 *
 * <p>Contributions are filed under the parent's {@link Slot#getComponentClass()}, so a slot filed under a base
 * view type is picked up by every subtype. Registrations can be added or removed at any time and in any order,
 * which is what makes the registry safe to use with dynamically loaded plugins.
 *
 * @author Pavel Castornii
 */
public final class SlotRegistry implements ExtensionRegistry {

    private final RegistrationIndex<SlotRegistration> index = new RegistrationIndex<>();

    /**
     * Puts a menu into a menu bar.
     *
     * @param menuBar  the slot of the menu bar, never {@code null}
     * @param position the position of the menu among the other menus of the menu bar
     * @param menu     the slot of the menu, never {@code null}
     * @param <V>      the view type of the component both slots belong to
     * @return a {@link Registration} that can be used to undo this contribution
     */
    public <V extends ParentView<?>> Registration register(MenuBarSlot<V> menuBar, int position, MenuSlot<V> menu) {
        return add(menuBar, position, menu);
    }

    /**
     * Puts a group of menu items into a menu.
     *
     * @param menu     the slot of the menu, never {@code null}
     * @param position the position of the group among the other groups of the menu
     * @param group    the slot of the group, never {@code null}
     * @param <V>      the view type of the component both slots belong to
     * @return a {@link Registration} that can be used to undo this contribution
     */
    public <V extends ParentView<?>> Registration register(MenuSlot<V> menu, int position,
            GroupSlot<V, ? extends MenuItem> group) {
        return add(menu, position, group);
    }

    /**
     * Puts a group of menu items into a context menu.
     *
     * @param contextMenu the slot of the context menu, never {@code null}
     * @param position    the position of the group among the other groups of the context menu
     * @param group       the slot of the group, never {@code null}
     * @param <V>         the view type of the component both slots belong to
     * @return a {@link Registration} that can be used to undo this contribution
     */
    public <V extends ParentView<?>> Registration register(ContextMenuSlot<V> contextMenu, int position,
            GroupSlot<V, ? extends MenuItem> group) {
        return add(contextMenu, position, group);
    }

    /**
     * Puts a group of controls into a tool bar.
     *
     * @param toolBar  the slot of the tool bar, never {@code null}
     * @param position the position of the group among the other groups of the tool bar
     * @param group    the slot of the group, never {@code null}
     * @param <V>      the view type of the component both slots belong to
     * @return a {@link Registration} that can be used to undo this contribution
     */
    public <V extends ParentView<?>> Registration register(ToolBarSlot<V> toolBar, int position,
            GroupSlot<V, ? extends Control> group) {
        return add(toolBar, position, group);
    }

    /**
     * Puts a nested menu into a group of menu items.
     *
     * @param group    the slot of the group, never {@code null}
     * @param position the position of the nested menu among the other controls of the group
     * @param subMenu  the slot of the nested menu, never {@code null}
     * @param <V>      the view type of the component both slots belong to
     * @return a {@link Registration} that can be used to undo this contribution
     */
    public <V extends ParentView<?>> Registration register(GroupSlot<V, ? extends MenuItem> group, int position,
            MenuSlot<V> subMenu) {
        return add(group, position, subMenu);
    }

    /**
     * Returns every registration applicable to the given component instance.
     */
    Set<SlotRegistration> getRegistrationsFor(Object instance) {
        return index.resolve(instance);
    }

    private <V extends ParentView<?>> Registration add(Slot<V> parent, int position, Slot<V> child) {
        Objects.requireNonNull(parent, "Parent can't be null");
        Objects.requireNonNull(child, "Child can't be null");
        var registration = new SlotRegistration(parent, position, child);
        index.add(parent.getComponentClass(), registration);
        return registration;
    }
}
