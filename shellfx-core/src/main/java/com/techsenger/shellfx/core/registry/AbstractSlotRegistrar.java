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
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;

/**
 * Base of the registrars that build the tree of slots. Its methods put a slot into another one and remember the
 * registration, so {@link #unregister()} undoes it; they are overloaded like the methods of the
 * {@link SlotRegistry} they delegate to.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSlotRegistrar extends AbstractRegistrar<SlotRegistry> {

    public AbstractSlotRegistrar(SlotRegistry registry) {
        super(registry);
    }

    /**
     * Puts a menu into a menu bar.
     *
     * @param menuBar  the slot of the menu bar
     * @param position the position of the menu among the other menus of the menu bar
     * @param menu     the slot of the menu
     * @param <V>      the view type of the component both slots belong to
     */
    protected <V extends ParentView<?>> void register(MenuBarSlot<V> menuBar, int position, MenuSlot<V> menu) {
        addRegistration(getRegistry().register(menuBar, position, menu));
    }

    /**
     * Puts a group of menu items into a menu.
     *
     * @param menu     the slot of the menu
     * @param position the position of the group among the other groups of the menu
     * @param group    the slot of the group
     * @param <V>      the view type of the component both slots belong to
     */
    protected <V extends ParentView<?>> void register(MenuSlot<V> menu, int position,
            GroupSlot<V, ? extends MenuItem> group) {
        addRegistration(getRegistry().register(menu, position, group));
    }

    /**
     * Puts a group of menu items into a context menu.
     *
     * @param contextMenu the slot of the context menu
     * @param position    the position of the group among the other groups of the context menu
     * @param group       the slot of the group
     * @param <V>         the view type of the component both slots belong to
     */
    protected <V extends ParentView<?>> void register(ContextMenuSlot<V> contextMenu, int position,
            GroupSlot<V, ? extends MenuItem> group) {
        addRegistration(getRegistry().register(contextMenu, position, group));
    }

    /**
     * Puts a group of controls into a tool bar.
     *
     * @param toolBar  the slot of the tool bar
     * @param position the position of the group among the other groups of the tool bar
     * @param group    the slot of the group
     * @param <V>      the view type of the component both slots belong to
     */
    protected <V extends ParentView<?>> void register(ToolBarSlot<V> toolBar, int position,
            GroupSlot<V, ? extends Control> group) {
        addRegistration(getRegistry().register(toolBar, position, group));
    }

    /**
     * Puts a nested menu into a group of menu items.
     *
     * @param group    the slot of the group
     * @param position the position of the nested menu among the other controls of the group
     * @param subMenu  the slot of the nested menu
     * @param <V>      the view type of the component both slots belong to
     */
    protected <V extends ParentView<?>> void register(GroupSlot<V, ? extends MenuItem> group, int position,
            MenuSlot<V> subMenu) {
        addRegistration(getRegistry().register(group, position, subMenu));
    }
}
