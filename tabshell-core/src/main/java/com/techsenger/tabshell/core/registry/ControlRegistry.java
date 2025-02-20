/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.core.registry;

import com.techsenger.mvvm4fx.core.ComponentKey;
import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.KeyedMenuGroup;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import com.techsenger.tabshell.material.menu.MenuGroupKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This registry is used if component has a MenuBar/ToolBar/ContextMenu that can contain items that are added by other
 * components. It is supposed that elements can be added in any order (children before parents and vice versa).
 *
 * <p>MenuBar -> Menu -> Group -> Item
 * MenuBar -> Menu -> Group -> Menu -> Group ...
 *
 * @author Pavel Castornii
 */
public class ControlRegistry {

    private final Map<ComponentKey, Set<AbstractMenuRegistration<?>>> barMenuRegistrationsByComponentKey =
            new ConcurrentHashMap<>();

    public interface Registration {

        void unregister();
    }

    /**
     * Registers a menu in the specified group. Note - there can be only one menu bar in the component.
     *
     * @param component
     * @param groupKey the key of the group that this menu will belong to. Null for root menus in the MenuBar
     * @param factory
     * @param position
     * @return
     */
    public Registration registerMenu(ComponentKey component, MenuGroupKey groupKey,
            ControlFactory<KeyedMenu> factory, int position) {
        var menus = getMenusFor(component);
        var reg = new MenuRegistration(groupKey, factory, position);
        menus.add(reg);
        reg.setUnregister(() -> menus.remove(reg));
        return reg;
    }

    /**
     * Registers a menu group.
     *
     * @param component
     * @param menuKey the key of the menu this group will belong to.
     * @param factory
     * @param position
     * @return
     */
    public Registration registerMenuGroup(ComponentKey component, MenuKey menuKey,
            ControlFactory<KeyedMenuGroup> factory, int position) {
        var menus = getMenusFor(component);
        var reg = new MenuGroupRegistration(menuKey, factory, position);
        menus.add(reg);
        reg.setUnregister(() -> menus.remove(reg));
        return reg;
    }

    /**
     * Registers a menu item in the specified group.
     *
     * @param component
     * @param groupKey
     * @param factory
     * @param position
     * @return
     */
    public Registration registerMenuItem(ComponentKey component, MenuGroupKey groupKey,
            ControlFactory<KeyedMenuItem> factory, int position) {
        var menus = getMenusFor(component);
        var reg = new MenuItemRegistration(groupKey, factory, position);
        menus.add(reg);
        reg.setUnregister(() -> menus.remove(reg));
        return reg;
    }

    /**
     * Removes all registrations for specific component.
     *
     * @param componentKey
     */
    public void removeRegistrations(ComponentKey componentKey) {
        this.barMenuRegistrationsByComponentKey.remove(componentKey);
    }

    Set<AbstractMenuRegistration<?>> getBarMenuRegistrations(ComponentKey key) {
        return barMenuRegistrationsByComponentKey.get(key);
    }

    private Set<AbstractMenuRegistration<?>> getMenusFor(ComponentKey componentKey) {
        var regs = this.barMenuRegistrationsByComponentKey.get(componentKey);
        if (regs == null) {
            synchronized (this.barMenuRegistrationsByComponentKey) {
                regs = this.barMenuRegistrationsByComponentKey.get(componentKey);
                if (regs == null) {
                    regs = ConcurrentHashMap.newKeySet();
                    this.barMenuRegistrationsByComponentKey.put(componentKey, regs);
                }
            }
        }
        return regs;
    }
}
