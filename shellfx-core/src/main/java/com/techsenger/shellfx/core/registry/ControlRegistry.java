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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.shellfx.core.CoreComponents;
import com.techsenger.shellfx.core.menu.MainMenu;
import com.techsenger.shellfx.material.menu.ManagedItem;
import com.techsenger.shellfx.material.menu.ManagedMenu;
import com.techsenger.shellfx.material.menu.ManagedMenuGroup;
import com.techsenger.shellfx.material.menu.MenuGroupName;
import com.techsenger.shellfx.material.menu.MenuName;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javafx.scene.control.MenuItem;

/**
 * Registry for UI controls such as menus, toolbars, context menus, etc. Allows components to contribute UI
 * elements to other components' slots. Each registration returns a {@link Registration} that can be used to undo
 * the extension.
 *
 * @author Pavel Castornii
 */
public class ControlRegistry implements ExtensionRegistry {

    /**
     * Represents a handle for a registered contribution. The holder of this handle is the only one who can undo
     * the registration.
     */
    public interface Registration {

        /**
         * Removes this registration from the registry.
         */
        void unregister();
    }

    /**
     * Provides access to menu registrations for a specific component slot. Instances are lightweight — a new
     * instance is created on each call to {@link ControlRegistry#component(ComponentName)}.
     */
    public class ComponentMenuRegistry {

        private final ComponentName componentName;

        private ComponentMenuRegistry(ComponentName componentName) {
            this.componentName = componentName;
        }

        /**
         * Registers a menu in the specified group.
         *
         * @param groupName the name of the group this menu will belong to, never {@code null}.
         * @param factory   the factory used to create the menu
         * @return a {@link Registration} that can be used to unregister this contribution
         */
        public Registration registerMenu(MenuGroupName groupName,
                ControlFactory<? extends ParentFxView<?>, ManagedMenu> factory) {
            Objects.requireNonNull(groupName, "Group can't be null");
            var menus = getMenusFor(componentName);
            var reg = new MenuRegistration<>(groupName, factory);
            menus.add(reg);
            reg.setUnregister(() -> menus.remove(reg));
            return reg;
        }

        /**
         * Registers a menu as a root menu, without a group.
         *
         * @param factory the factory used to create the menu
         * @return a {@link Registration} that can be used to unregister this contribution
         */
        public Registration registerMenu(ControlFactory<? extends ParentFxView<?>, ManagedMenu> factory) {
            var menus = getMenusFor(componentName);
            var reg = new MenuRegistration<>(null, factory);
            menus.add(reg);
            reg.setUnregister(() -> menus.remove(reg));
            return reg;
        }

        /**
         * Registers a menu group in the specified menu.
         *
         * @param menuName the name of the menu this group will belong to
         * @param factory  the factory used to create the menu group
         * @return a {@link Registration} that can be used to unregister this contribution
         */
        public Registration registerMenuGroup(MenuName menuName,
                ControlFactory<? extends ParentFxView<?>, ManagedMenuGroup> factory) {
            var menus = getMenusFor(componentName);
            var reg = new MenuGroupRegistration<>(menuName, factory);
            menus.add(reg);
            reg.setUnregister(() -> menus.remove(reg));
            return reg;
        }

        /**
         * Registers a menu item in the specified group. Accepts a factory for any managed item type —
         * {@code ManagedMenuItem}, {@code ManagedCheckMenuItem}, {@code ManagedRadioMenuItem}, or any future
         * {@link ManagedItem} implementation — the concrete type is inferred from the factory.
         *
         * @param groupName the name of the group this item will belong to
         * @param factory   the factory used to create the menu item
         * @param <I>       the concrete managed item type produced by the factory
         * @return a {@link Registration} that can be used to unregister this contribution
         */
        public <I extends MenuItem & ManagedItem> Registration registerMenuItem(MenuGroupName groupName,
                ControlFactory<? extends ParentFxView<?>, I> factory) {
            var menus = getMenusFor(componentName);
            var reg = new MenuItemRegistration<>(groupName, factory);
            menus.add(reg);
            reg.setUnregister(() -> menus.remove(reg));
            return reg;
        }
    }

    /**
     * Provides convenient access to the application main menu registrations. This is an alias for
     * {@code component(CoreComponents.SHELL)} that scopes registrations to {@link MainMenu#GROUP} by
     * default.
     */
    public final class MainMenuRegistry extends ComponentMenuRegistry {

        private MainMenuRegistry() {
            super(CoreComponents.SHELL);
        }

        /**
         * Registers a menu in {@link MainMenu#GROUP}.
         *
         * @param factory the factory used to create the menu
         * @return a {@link Registration} that can be used to unregister this contribution
         */
        @Override
        public Registration registerMenu(ControlFactory<? extends ParentFxView<?>, ManagedMenu> factory) {
            return registerMenu(MainMenu.GROUP, factory);
        }
    }

    private final MainMenuRegistry mainMenu = new MainMenuRegistry();

    private final Map<ComponentName, Set<AbstractMenuRegistration<?, ?>>> menuRegistrationsByComponent =
            new ConcurrentHashMap<>();

    /**
     * Returns the registry scoped to the application main menu. Equivalent to calling
     * {@code component(CoreComponents.SHELL)} but with {@link MainMenu#GROUP} as the default group for
     * {@link ComponentMenuRegistry#registerMenu(ControlFactory)}.
     *
     * @return the main menu registry
     */
    public MainMenuRegistry mainMenu() {
        return mainMenu;
    }

    /**
     * Returns a registry view scoped to the component with the given name. Each call returns a new lightweight
     * instance that delegates to the shared internal state of this registry.
     *
     * @param componentName the name of the target component
     * @return a registry scoped to the specified component
     */
    public ComponentMenuRegistry component(ComponentName componentName) {
        return new ComponentMenuRegistry(componentName);
    }

    /**
     * Returns an unmodifiable snapshot of all registrations grouped by component name. Neither the map nor its value
     * sets can be modified through the returned view.
     *
     * <p><b>Note:</b> This method creates a new map on every call and should not be invoked frequently or in
     * performance-sensitive code paths.
     *
     * @return an unmodifiable map of component names to their sets of registrations
     */
    public @Unmodifiable Map<ComponentName, Set<AbstractMenuRegistration<?, ?>>> getMenuRegistrations() {
        return Collections.unmodifiableMap(
            menuRegistrationsByComponent.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> Collections.unmodifiableSet(e.getValue()),
                    (a, b) -> a,
                    LinkedHashMap::new
                ))
        );
    }

    private Set<AbstractMenuRegistration<?, ?>> getMenusFor(ComponentName componentName) {
        return menuRegistrationsByComponent.computeIfAbsent(componentName, k -> ConcurrentHashMap.newKeySet());
    }
}
