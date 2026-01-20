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

package com.techsenger.tabshell.core.registry;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.material.menu.MenuGroupName;
import com.techsenger.tabshell.material.menu.MenuName;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
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

    private final Map<ComponentName, Set<AbstractMenuRegistration<?>>> barMenuRegistrationsByComponentName =
            new ConcurrentHashMap<>();

    public interface Registration {

        void unregister();
    }

    /**
     * Registers a menu in the specified group. Note - there can be only one menu bar in the component.
     *
     * @param componentName
     * @param groupName the name of the group that this menu will belong to. Null for root menus in the MenuBar
     * @param factory
     * @return
     */
    public Registration registerMenu(ComponentName componentName, MenuGroupName groupName,
            ControlFactory<NamedMenu> factory) {
        var menus = getMenusFor(componentName);
        var reg = new MenuRegistration(groupName, factory);
        menus.add(reg);
        reg.setUnregister(() -> {
            menus.remove(reg);
        });
        return reg;
    }

    /**
     * Registers a menu group.
     *
     * @param componentName
     * @param menuName the name of the menu this group will belong to.
     * @param factory
     * @return
     */
    public Registration registerMenuGroup(ComponentName componentName, MenuName menuName,
            ControlFactory<NamedMenuGroup> factory) {
        var menus = getMenusFor(componentName);
        var reg = new MenuGroupRegistration(menuName, factory);
        menus.add(reg);
        reg.setUnregister(() -> menus.remove(reg));
        return reg;
    }

    /**
     * Registers a menu item in the specified group.
     *
     * @param componentName
     * @param groupName
     * @param factory
     * @return
     */
    public Registration registerMenuItem(ComponentName componentName, MenuGroupName groupName,
            ControlFactory<NamedMenuItem> factory) {
        var menus = getMenusFor(componentName);
        var reg = new MenuItemRegistration(groupName, factory);
        menus.add(reg);
        reg.setUnregister(() -> menus.remove(reg));
        return reg;
    }

    /**
     * Removes all registrations for specific component.
     *
     * @param componentKey
     */
    public void removeRegistrations(ComponentName componentKey) {
        this.barMenuRegistrationsByComponentName.remove(componentKey);
    }

    Set<AbstractMenuRegistration<?>> getBarMenuRegistrations(Name name) {
        return barMenuRegistrationsByComponentName.get(name);
    }

    private Set<AbstractMenuRegistration<?>> getMenusFor(ComponentName componentName) {
        var regs = this.barMenuRegistrationsByComponentName.get(componentName);
        if (regs == null) {
            synchronized (this.barMenuRegistrationsByComponentName) {
                regs = this.barMenuRegistrationsByComponentName.get(componentName);
                if (regs == null) {
                    regs = ConcurrentHashMap.newKeySet();
                    this.barMenuRegistrationsByComponentName.put(componentName, regs);
                }
            }
        }
        return regs;
    }
}
