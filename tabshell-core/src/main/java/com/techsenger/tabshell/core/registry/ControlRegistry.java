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
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.material.menu.MenuGroupName;
import com.techsenger.tabshell.material.menu.MenuName;
import com.techsenger.tabshell.material.menu.ManagedMenu;
import com.techsenger.tabshell.material.menu.ManagedMenuGroup;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;
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

    public interface Registration {

        void unregister();
    }

    public static class MainMenuRegistry {

        private final Set<AbstractMenuRegistration<?, ?>> registrations = ConcurrentHashMap.newKeySet();

        /**
         * Registers a menu in the specified group. Note - there can be only one menu bar in the component.
         *
         * @param groupName the name of the group that this menu will belong to. Null for root menus in the MenuBar
         * @param factory
         * @return
         */
        public Registration registerMenu(MenuGroupName groupName, ControlFactory<ShellFxView<?>, ManagedMenu> factory) {
            var reg = new MenuRegistration<>(groupName, factory);
            registrations.add(reg);
            reg.setUnregister(() -> {
                registrations.remove(reg);
            });
            return reg;
        }

        /**
         * Registers a menu group.
         *
         * @param menuName the name of the menu this group will belong to.
         * @param factory
         * @return
         */
        public Registration registerMenuGroup(MenuName menuName,
                ControlFactory<ShellFxView<?>, ManagedMenuGroup> factory) {
            var reg = new MenuGroupRegistration<>(menuName, factory);
            registrations.add(reg);
            reg.setUnregister(() -> registrations.remove(reg));
            return reg;
        }

        /**
         * Registers a menu item in the specified group.
         *
         * @param groupName
         * @param factory
         * @return
         */
        public Registration registerMenuItem(MenuGroupName groupName,
                ControlFactory<ShellFxView<?>, ManagedMenuItem> factory) {
            var reg = new MenuItemRegistration<>(groupName, factory);
            registrations.add(reg);
            reg.setUnregister(() -> registrations.remove(reg));
            return reg;
        }

        Set<AbstractMenuRegistration<?, ?>> getRegistrations() {
            return registrations;
        }
    }

    private final MainMenuRegistry mainMenu = new MainMenuRegistry();

    private final Map<ComponentName, Set<AbstractMenuRegistration<?, ?>>> registrationsByComponentName =
            new ConcurrentHashMap<>();

    public MainMenuRegistry mainMenu() {
        return mainMenu;
    }

    /**
     * Registers a menu in the specified group. Note - there can be only one menu bar in the component.
     *
     * @param componentName the name of the owner of the menu
     * @param groupName the name of the group that this menu will belong to. Null for root menus in the MenuBar
     * @param factory
     * @return
     */
    public Registration registerMenu(ComponentName componentName, MenuGroupName groupName,
            ControlFactory<ParentFxView<?>, ManagedMenu> factory) {
        var menus = getMenusFor(componentName);
        var reg = new MenuRegistration<>(groupName, factory);
        menus.add(reg);
        reg.setUnregister(() -> {
            menus.remove(reg);
        });
        return reg;
    }

    /**
     * Registers a menu group.
     *
     * @param componentName the name of the owner of the menu
     * @param menuName the name of the menu this group will belong to.
     * @param factory
     * @return
     */
    public Registration registerMenuGroup(ComponentName componentName, MenuName menuName,
            ControlFactory<ParentFxView<?>, ManagedMenuGroup> factory) {
        var menus = getMenusFor(componentName);
        var reg = new MenuGroupRegistration<>(menuName, factory);
        menus.add(reg);
        reg.setUnregister(() -> menus.remove(reg));
        return reg;
    }

    /**
     * Registers a menu item in the specified group.
     *
     * @param componentName the name of the owner of the menu
     * @param groupName
     * @param factory
     * @return
     */
    public Registration registerMenuItem(ComponentName componentName,
            MenuGroupName groupName, ControlFactory<? extends ParentFxView<?>, ManagedMenuItem> factory) {
        var menus = getMenusFor(componentName);
        var reg = new MenuItemRegistration<>(groupName, factory);
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
        this.registrationsByComponentName.remove(componentKey);
    }

    Set<AbstractMenuRegistration<?, ?>> getBarMenuRegistrations(Name name) {
        return registrationsByComponentName.get(name);
    }

    private Set<AbstractMenuRegistration<?, ?>> getMenusFor(ComponentName componentName) {
        return registrationsByComponentName.computeIfAbsent(
                componentName,
                k -> ConcurrentHashMap.newKeySet());
    }
}
