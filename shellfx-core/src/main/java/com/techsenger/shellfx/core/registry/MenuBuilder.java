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

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.shellfx.core.CoreComponents;
import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.menu.MainMenu;
import com.techsenger.shellfx.material.menu.ManagedItem;
import com.techsenger.shellfx.material.menu.ManagedMenu;
import com.techsenger.shellfx.material.menu.ManagedMenuGroup;
import com.techsenger.shellfx.material.menu.MenuGroupName;
import com.techsenger.shellfx.material.menu.MenuName;
import com.techsenger.toolkit.core.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class MenuBuilder {

    private static class MenuDescriptor {

        private final MenuGroupName groupName;

        private final ManagedMenu menu;

        MenuDescriptor(MenuGroupName groupName, ManagedMenu menu) {
            this.groupName = groupName;
            this.menu = menu;
        }
    }

    private static class GroupDescriptor {

        private final MenuName menuName;

        private final ManagedMenuGroup group;

        GroupDescriptor(MenuName menuName, ManagedMenuGroup group) {
            this.menuName = menuName;
            this.group = group;
        }
    }

    private static final class BuildContext {

        private final Map<MenuName, MenuDescriptor> menusByName = new HashMap<>();

        private final Map<MenuGroupName, GroupDescriptor> groupsByName = new HashMap<>();

        private final Map<MenuGroupName, Set<MenuItem>> itemsByGroup = new HashMap<>();
    }

    private final ControlRegistry registry;

    public MenuBuilder(ControlRegistry registry) {
        this.registry = registry;
    }

    /**
     * Builds the list of top-level menus for the application main menu. Only menus registered under
     * {@link MainMenu#GROUP} are included as top-level entries. Empty menus are removed from the result.
     *
     * @param shell the shell view passed to each control factory
     * @return a sorted list of top-level {@link Menu} instances ready to be added to the main {@code MenuBar}
     */
    public List<Menu> buildMainMenus(ShellFxView<?> shell) {
        var registrations = registry.getMenuRegistrations().get(CoreComponents.SHELL);
        if (registrations == null) {
            return List.of();
        }
        var ctx = new BuildContext();
        buildElements(shell, new ArrayList<>(registrations), ctx);
        List<ManagedMenu> topMenus = new ArrayList<>();
        Map<MenuName, Pair<ManagedMenu, List<GroupDescriptor>>> menusAndGroups = new HashMap<>();
        // Distribute menus — top-level ones go to the result list, nested ones are added to their parent group
        for (var entry : ctx.menusByName.entrySet()) {
            var descriptor = entry.getValue();
            if (descriptor.groupName == MainMenu.GROUP) {
                topMenus.add(descriptor.menu);
            } else {
                var groupDescriptor = ctx.groupsByName.get(descriptor.groupName);
                if (groupDescriptor != null) {
                    groupDescriptor.group.getItems().add(descriptor.menu);
                    descriptor.menu.setGroup(groupDescriptor.group);
                }
            }
            menusAndGroups.put(entry.getKey(), new Pair<>(descriptor.menu, new ArrayList<>()));
        }
        assembleMenus(ctx, menusAndGroups);
        Collections.sort(topMenus, (p1, p2) -> Integer.compare(p1.getPosition(), p2.getPosition()));
        removeEmptyMenus(topMenus);
        return (List) topMenus;
    }

    /**
     * Builds a single {@link Menu} for the given component and menu name. All groups and items registered under
     * the specified component are assembled into the menu. Empty menus are removed from the result.
     *
     * @param view          the component view passed to each control factory
     * @param componentName the name of the component that owns the menu registrations
     * @param menuName      the name of the menu to build
     * @return the assembled {@link Menu}, or {@code null} if no registration exists for the given component and
     *         menu name
     */
    public Menu buildMenu(ParentFxView<?> view, ComponentName componentName, MenuName menuName) {
        var registrations = registry.getMenuRegistrations().get(componentName);
        if (registrations == null) {
            return null;
        }
        var ctx = new BuildContext();
        buildElements(view, new ArrayList<>(registrations), ctx);
        var menuDescriptor = ctx.menusByName.get(menuName);
        if (menuDescriptor == null) {
            return null;
        }
        Map<MenuName, Pair<ManagedMenu, List<GroupDescriptor>>> menusAndGroups = new HashMap<>();
        for (var entry : ctx.menusByName.entrySet()) {
            var descriptor = entry.getValue();
            if (descriptor.groupName != null) {
                var groupDescriptor = ctx.groupsByName.get(descriptor.groupName);
                if (groupDescriptor != null) {
                    groupDescriptor.group.getItems().add(descriptor.menu);
                    descriptor.menu.setGroup(groupDescriptor.group);
                }
            }
            menusAndGroups.put(entry.getKey(), new Pair<>(descriptor.menu, new ArrayList<>()));
        }
        assembleMenus(ctx, menusAndGroups);
        if (removeEmptyMenus(menuDescriptor.menu)) {
            return null;
        }
        return menuDescriptor.menu;
    }

    /**
     * Instantiates all menu elements from the given registrations and populates the build context.
     *
     * @param view the component view passed to each control factory
     * @param regs the list of registrations to process
     * @param ctx  the context that accumulates menus, groups, and items
     */
    private void buildElements(ParentFxView<?> view, List<AbstractMenuRegistration<?, ?>> regs, BuildContext ctx) {
        for (var r : regs) {
            switch (r.getType()) {
                case MENU:
                    var mr = (MenuRegistration<ParentFxView<?>>) r;
                    var menu = mr.getFactory().create(view);
                    ctx.menusByName.put(menu.getName(), new MenuDescriptor(mr.getGroupName(), menu));
                    break;
                case GROUP:
                    var gr = (MenuGroupRegistration<ParentFxView<?>>) r;
                    var group = gr.getFactory().create(view);
                    ctx.groupsByName.put(group.getName(), new GroupDescriptor(gr.getMenuName(), group));
                    break;
                case ITEM:
                    var ir = (MenuItemRegistration<ParentFxView<?>, ?>) r;
                    var item = ir.getFactory().create(view);
                    ctx.itemsByGroup.computeIfAbsent(ir.getGroupKey(), k -> new HashSet<>()).add(item);
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    /**
     * Assembles groups and items into their parent menus. Groups are sorted by position and separated by
     * {@link SeparatorMenuItem}s. Empty groups are skipped. Any item or nested menu that implements
     * {@link ManagedItem} has its group back-reference set; items of other kinds are still added to the menu but
     * are not tracked as belonging to a group.
     *
     * @param ctx            the build context containing all instantiated elements
     * @param menusAndGroups a map from menu name to its menu instance and the list of groups that belong to it
     */
    private void assembleMenus(BuildContext ctx,
            Map<MenuName, Pair<ManagedMenu, List<GroupDescriptor>>> menusAndGroups) {
        // Assign items to their groups
        for (var entry : ctx.itemsByGroup.entrySet()) {
            var groupDescriptor = ctx.groupsByName.get(entry.getKey());
            if (groupDescriptor != null) {
                for (var item : entry.getValue()) {
                    groupDescriptor.group.getItems().add(item);
                    if (item instanceof ManagedItem managedItem) {
                        managedItem.setGroup(groupDescriptor.group);
                    }
                }
            }
        }
        // Assign groups to their parent menus
        for (var entry : ctx.groupsByName.entrySet()) {
            var groupDescriptor = entry.getValue();
            var menusAndGroup = menusAndGroups.get(groupDescriptor.menuName);
            if (menusAndGroup != null) {
                menusAndGroup.getSecond().add(groupDescriptor);
            }
        }
        // Build each menu from its sorted groups
        for (var entry : menusAndGroups.entrySet()) {
            var menu = entry.getValue().getFirst();
            var groups = entry.getValue().getSecond();
            List<MenuItem> menuElements = new ArrayList<>();
            groups.sort((o1, o2) -> Integer.compare(o1.group.getPosition(), o2.group.getPosition()));
            for (var i = 0; i < groups.size(); i++) {
                var group = groups.get(i).group;
                if (group.getItems().isEmpty()) {
                    continue;
                }
                if (i != 0) {
                    menuElements.add(new SeparatorMenuItem());
                }
                group.sort();
                menuElements.addAll(group.getItems());
            }
            menu.getItems().addAll(menuElements);
        }
    }

    private void removeEmptyMenus(List<ManagedMenu> menus) {
        for (Iterator<ManagedMenu> iterator = menus.iterator(); iterator.hasNext();) {
            Menu menu = iterator.next();
            if (removeEmptyMenus(menu)) {
                iterator.remove();
            }
        }
    }

    private boolean removeEmptyMenus(Menu menu) {
        for (Iterator<MenuItem> iterator = menu.getItems().iterator(); iterator.hasNext();) {
            MenuItem item = iterator.next();
            if (item instanceof Menu) {
                if (removeEmptyMenus((Menu) item)) {
                    iterator.remove();
                }
            }
        }
        return menu.getItems().isEmpty();
    }
}
