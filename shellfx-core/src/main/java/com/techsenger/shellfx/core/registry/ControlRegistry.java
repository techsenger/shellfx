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

import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.shellfx.material.menu.ManagedItem;
import com.techsenger.shellfx.material.menu.ManagedMenu;
import com.techsenger.shellfx.material.menu.ManagedMenuGroup;
import com.techsenger.shellfx.material.menu.MenuGroupName;
import com.techsenger.shellfx.material.menu.MenuName;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.control.MenuItem;

/**
 * Registry for UI controls such as menus, toolbars, context menus, etc. Allows components to contribute UI
 * elements to other components' slots. Each registration returns a {@link Registration} that can be used to undo
 * the extension.
 *
 * <p>Contributions are filed under the target slot's own {@link MenuName#getComponentClass()}/
 * {@link MenuGroupName#getComponentClass()} (e.g. {@code ShellFxView.class}). Resolving controls for an actual
 * component instance ({@link #getRegistrationsFor(Object)}) walks that instance's own class, its superclasses, and
 * its interfaces, so a slot filed under a base view type is automatically picked up by every subtype, without
 * either side having to know about the other in advance.
 *
 * <p>The resolved-controls cache is keyed by {@link Class#getName()} rather than by the {@link Class} object itself:
 * a plugin module is typically loaded through its own {@code ClassLoader}/JPMS layer, and a {@code Class} holds a
 * strong reference to its defining loader (and, transitively, to everything else that loader defined) — so keeping
 * {@code Class} objects themselves in a cache that outlives the plugin would leak the whole layer. A name string
 * carries none of that.
 *
 * @author Pavel Castornii
 */
public final class ControlRegistry implements ExtensionRegistry {

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

    private final Map<Class<?>, Set<AbstractMenuRegistration<?, ?>>> registrationsByClass = new ConcurrentHashMap<>();

    private final Map<String, Set<AbstractMenuRegistration<?, ?>>> resolvedByClassName = new ConcurrentHashMap<>();

    /**
     * Registers a menu in the specified group. The group's own view type pins the view type {@code factory} must
     * accept, so a factory built for a different component is rejected at compile time; the group's
     * {@link MenuGroupName#getComponentClass()} determines which component(s) the registration applies to.
     *
     * @param groupName the name of the group this menu will belong to, never {@code null}.
     * @param factory   the factory used to create the menu
     * @return a {@link Registration} that can be used to unregister this contribution
     */
    public <V extends ParentFxView<?>> Registration registerMenu(MenuGroupName<V> groupName,
            ControlFactory<V, ManagedMenu> factory) {
        Objects.requireNonNull(groupName, "Group can't be null");
        var reg = new MenuRegistration<>(groupName, factory);
        register(groupName.getComponentClass(), reg);
        return reg;
    }

    /**
     * Registers a menu group in the specified menu. The menu's own view type pins the view type {@code factory}
     * must accept, so a factory built for a different component is rejected at compile time; the menu's
     * {@link MenuName#getComponentClass()} determines which component(s) the registration applies to.
     *
     * @param menuName the name of the menu this group will belong to
     * @param factory  the factory used to create the menu group
     * @return a {@link Registration} that can be used to unregister this contribution
     */
    public <V extends ParentFxView<?>> Registration registerMenuGroup(MenuName<V> menuName,
            ControlFactory<V, ManagedMenuGroup> factory) {
        var reg = new MenuGroupRegistration<>(menuName, factory);
        register(menuName.getComponentClass(), reg);
        return reg;
    }

    /**
     * Registers a menu item in the specified group. Accepts a factory for any managed item type —
     * {@code ManagedMenuItem}, {@code ManagedCheckMenuItem}, {@code ManagedRadioMenuItem}, or any future
     * {@link ManagedItem} implementation — the concrete type is inferred from the factory. The group's own view
     * type pins the view type {@code factory} must accept, so a factory built for a different component is
     * rejected at compile time; the group's {@link MenuGroupName#getComponentClass()} determines which
     * component(s) the registration applies to.
     *
     * @param groupName the name of the group this item will belong to
     * @param factory   the factory used to create the menu item
     * @param <I>       the concrete managed item type produced by the factory
     * @return a {@link Registration} that can be used to unregister this contribution
     */
    public <I extends MenuItem & ManagedItem, V extends ParentFxView<?>> Registration registerMenuItem(
            MenuGroupName<V> groupName, ControlFactory<V, I> factory) {
        var reg = new MenuItemRegistration<>(groupName, factory);
        register(groupName.getComponentClass(), reg);
        return reg;
    }

    /**
     * Returns every registration applicable to the given component instance: its own class, filed registrations
     * for every ancestor class, and for every interface it (or an ancestor) implements. The result is cached by
     * {@link Class#getName()} and recomputed lazily the first time a given class is seen after a registry change.
     *
     * @param instance the component instance controls are being resolved for
     * @return the merged, applicable registrations
     */
    Set<AbstractMenuRegistration<?, ?>> getRegistrationsFor(Object instance) {
        var type = instance.getClass();
        return resolvedByClassName.computeIfAbsent(type.getName(), n -> resolve(type));
    }

    private void register(Class<?> componentClass, AbstractMenuRegistration<?, ?> reg) {
        var regs = registrationsByClass.computeIfAbsent(componentClass, k -> ConcurrentHashMap.newKeySet());
        regs.add(reg);
        resolvedByClassName.clear();
        reg.setUnregister(() -> {
            regs.remove(reg);
            resolvedByClassName.clear();
        });
    }

    private Set<AbstractMenuRegistration<?, ?>> resolve(Class<?> type) {
        var result = new HashSet<AbstractMenuRegistration<?, ?>>();
        collect(type, result, new HashSet<>());
        return result;
    }

    private void collect(Class<?> type, Set<AbstractMenuRegistration<?, ?>> result, Set<Class<?>> visited) {
        if (type == null || !visited.add(type)) {
            return;
        }
        var regs = registrationsByClass.get(type);
        if (regs != null) {
            result.addAll(regs);
        }
        collect(type.getSuperclass(), result, visited);
        for (var iface : type.getInterfaces()) {
            collect(iface, result, visited);
        }
    }
}
