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
import com.techsenger.shellfx.material.ControlGroup;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import com.techsenger.shellfx.material.slot.Slot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import java.util.Objects;
import java.util.Set;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;

/**
 * Registry of the providers of the controls that fill the slots: the control a slot stands for (a menu bar, a menu, a
 * context menu), and the controls (menu items, buttons) that are put into the groups. It never creates or inspects a
 * provider; that is the job of a builder such as {@link ControlBuilder}. Where the
 * slots are nested into each other is kept in the {@link SlotRegistry}.
 *
 * <p>Contributions are filed under the slot's own {@link Slot#getComponentClass()}. Registrations can be added or
 * removed at any time and in any order, which is what makes the registry safe to use with dynamically loaded
 * plugins. The kind of a slot is its class, which is what tells the overloads apart.
 *
 * @author Pavel Castornii
 */
public final class ControlRegistry implements ExtensionRegistry {

    private final RegistrationIndex<AbstractControlRegistration> index = new RegistrationIndex<>();

    /**
     * Registers the provider of the menu bar a slot stands for. The factory is not invoked here, only when a
     * builder materializes the control.
     *
     * @param slot    the slot of the menu bar, never {@code null}
     * @param factory the factory of the provider of the menu bar
     * @param <V>     the view type of the component the slot belongs to
     * @return a {@link Registration} that can be used to undo this contribution
     * @throws IllegalStateException if the slot already has a control
     */
    public <V extends ParentView<?>> Registration register(MenuBarSlot<V> slot,
            ControlProviderFactory<V, ? extends MenuBar> factory) {
        return addNode(slot, factory);
    }

    /**
     * Registers the provider of the menu a slot stands for. Its position among the siblings comes from where the
     * slot is put in the {@link SlotRegistry}. The factory is not invoked here, only when a builder materializes
     * the control.
     *
     * @param slot    the slot of the menu, never {@code null}
     * @param factory the factory of the provider of the menu
     * @param <V>     the view type of the component the slot belongs to
     * @return a {@link Registration} that can be used to undo this contribution
     * @throws IllegalStateException if the slot already has a control
     */
    public <V extends ParentView<?>> Registration register(MenuSlot<V> slot,
            ControlProviderFactory<V, ? extends Menu> factory) {
        return addNode(slot, factory);
    }

    /**
     * Registers the provider of the context menu a slot stands for. The factory is not invoked here, only when a
     * builder materializes the control.
     *
     * @param slot    the slot of the context menu, never {@code null}
     * @param factory the factory of the provider of the context menu
     * @param <V>     the view type of the component the slot belongs to
     * @return a {@link Registration} that can be used to undo this contribution
     * @throws IllegalStateException if the slot already has a control
     */
    public <V extends ParentView<?>> Registration register(ContextMenuSlot<V> slot,
            ControlProviderFactory<V, ? extends ContextMenu> factory) {
        return addNode(slot, factory);
    }

    /**
     * Registers the provider of the tool bar a slot stands for. The factory is not invoked here, only when a
     * builder materializes the control.
     *
     * @param slot    the slot of the tool bar, never {@code null}
     * @param factory the factory of the provider of the tool bar
     * @param <V>     the view type of the component the slot belongs to
     * @return a {@link Registration} that can be used to undo this contribution
     * @throws IllegalStateException if the slot already has a control
     */
    public <V extends ParentView<?>> Registration register(ToolBarSlot<V> slot,
            ControlProviderFactory<V, ? extends ToolBar> factory) {
        return addNode(slot, factory);
    }

    /**
     * Registers the provider of the group a slot stands for. A group without a provider is left out by the
     * builders. The factory is not invoked here, only when a builder materializes the control.
     *
     * @param group    the slot of the group, never {@code null}
     * @param factory the factory of the provider of the group
     * @param <V>     the view type of the component the slot belongs to
     * @param <C>     the type of the controls the group holds
     * @return a {@link Registration} that can be used to undo this contribution
     * @throws IllegalStateException if the slot already has a control
     */
    public <V extends ParentView<?>, C> Registration register(GroupSlot<V, C> group,
            ControlProviderFactory<V, ? extends ControlGroup<C>> factory) {
        return addNode(group, factory);
    }

    /**
     * Registers the provider of a control that is put into a group, for example a menu item. The type of the
     * controls comes from the group, so a provider of a wrong kind of control is rejected at compile time. The
     * factory is not invoked here, only when a builder materializes the control.
     *
     * @param group    the slot of the group the control will belong to, never {@code null}
     * @param position the position of the control among the other controls of the group
     * @param factory the factory of the provider of the control
     * @param <V>      the view type of the component the group belongs to
     * @param <C>      the type of the controls the group holds
     * @return a {@link Registration} that can be used to undo this contribution
     */
    public <V extends ParentView<?>, C> Registration register(GroupSlot<V, C> group, int position,
            ControlProviderFactory<V, ? extends C> factory) {
        Objects.requireNonNull(group, "Group can't be null");
        var registration = new LeafRegistration(group, position, factory);
        index.add(group.getComponentClass(), registration);
        return registration;
    }

    /**
     * Returns every registration applicable to the given component instance.
     */
    Set<AbstractControlRegistration> getRegistrationsFor(Object instance) {
        return index.resolve(instance);
    }

    private <V extends ParentView<?>> Registration addNode(Slot<V> slot,
            ControlProviderFactory<V, ?> factory) {
        Objects.requireNonNull(slot, "Slot can't be null");
        for (var registration : index.get(slot.getComponentClass())) {
            if (registration instanceof NodeRegistration && registration.getSlot() == slot) {
                throw new IllegalStateException("Slot '" + slot.getText() + "' already has a control");
            }
        }
        var registration = new NodeRegistration(slot, factory);
        index.add(slot.getComponentClass(), registration);
        return registration;
    }
}
