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

package com.techsenger.shellfx.core.registrars;

import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.ControlGroup;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import com.techsenger.shellfx.base.registrar.AbstractRegistrar;
import com.techsenger.shellfx.core.registries.ControlProviderFactory;
import com.techsenger.shellfx.core.registries.ControlRegistry;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;

/**
 * Base of the registrars that bind controls to slots. Its methods register a control provider and remember the
 * registration, so {@link #unregister()} undoes it.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractControlRegistrar extends AbstractRegistrar<ControlRegistry> {

    public AbstractControlRegistrar(ControlRegistry registry) {
        super(registry);
    }

    /**
     * Registers the provider of the menu bar a slot stands for.
     *
     * @param slot    the slot of the menu bar
     * @param factory the factory of the provider of the menu bar
     * @param <V>     the view type of the component the slot belongs to
     * @throws IllegalStateException if the slot already has a control
     */
    protected <V extends ParentView<?>> void register(MenuBarSlot<V> slot,
            ControlProviderFactory<V, ? extends MenuBar> factory) {
        addRegistration(getRegistry().register(slot, factory));
    }

    /**
     * Registers the provider of the menu a slot stands for.
     *
     * @param slot    the slot of the menu
     * @param factory the factory of the provider of the menu
     * @param <V>     the view type of the component the slot belongs to
     * @throws IllegalStateException if the slot already has a control
     */
    protected <V extends ParentView<?>> void register(MenuSlot<V> slot,
            ControlProviderFactory<V, ? extends Menu> factory) {
        addRegistration(getRegistry().register(slot, factory));
    }

    /**
     * Registers the provider of the context menu a slot stands for.
     *
     * @param slot    the slot of the context menu
     * @param factory the factory of the provider of the context menu
     * @param <V>     the view type of the component the slot belongs to
     * @throws IllegalStateException if the slot already has a control
     */
    protected <V extends ParentView<?>> void register(ContextMenuSlot<V> slot,
            ControlProviderFactory<V, ? extends ContextMenu> factory) {
        addRegistration(getRegistry().register(slot, factory));
    }

    /**
     * Registers the provider of the tool bar a slot stands for.
     *
     * @param slot    the slot of the tool bar
     * @param factory the factory of the provider of the tool bar
     * @param <V>     the view type of the component the slot belongs to
     * @throws IllegalStateException if the slot already has a control
     */
    protected <V extends ParentView<?>> void register(ToolBarSlot<V> slot,
            ControlProviderFactory<V, ? extends ToolBar> factory) {
        addRegistration(getRegistry().register(slot, factory));
    }

    /**
     * Registers the provider of the group a slot stands for.
     *
     * @param group    the slot of the group
     * @param factory the factory of the provider of the group
     * @param <V>     the view type of the component the group belongs to
     * @param <C>     the type of the controls the group holds
     * @throws IllegalStateException if the slot already has a control
     */
    protected <V extends ParentView<?>, C> void register(GroupSlot<V, C> group,
            ControlProviderFactory<V, ? extends ControlGroup<C>> factory) {
        addRegistration(getRegistry().register(group, factory));
    }

    /**
     * Registers the provider of a control that is put into a group, for example a menu item.
     *
     * @param group    the slot of the group the control will belong to
     * @param position the position of the control among the other controls of the group
     * @param factory the factory of the provider of the control
     * @param <V>      the view type of the component the group belongs to
     * @param <C>      the type of the controls the group holds
     */
    protected <V extends ParentView<?>, C> void register(GroupSlot<V, C> group, int position,
            ControlProviderFactory<V, ? extends C> factory) {
        addRegistration(getRegistry().register(group, position, factory));
    }

    /**
     * Registers the provider of a control that is put into a group, for views of a subtype of the view the group is
     * declared for, so the provider gets only views of that subtype.
     *
     * @param group     the slot of the group the control will belong to; its view type must be {@code viewClass}
     *                  or its supertype
     * @param position  the position of the control among the other controls of the group
     * @param viewClass the class of the view the provider is written for
     * @param factory   the factory of the provider of the control
     * @param <V>       the view type of the component the provider is written for
     * @param <C>       the type of the controls the group holds
     */
    protected <V extends ParentView<?>, C> void register(GroupSlot<? super V, C> group, int position,
            Class<V> viewClass, ControlProviderFactory<V, ? extends C> factory) {
        addRegistration(getRegistry().register(group, position, viewClass, factory));
    }
}
