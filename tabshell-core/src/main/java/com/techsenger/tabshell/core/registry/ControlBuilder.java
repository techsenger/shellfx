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

import com.techsenger.mvvm4fx.core.View;
import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.KeyedMenuGroup;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import com.techsenger.tabshell.material.menu.MenuGroupKey;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import com.techsenger.toolkit.core.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class ControlBuilder {

    private static class MenuDescriptor {

        private final MenuGroupKey groupKey;

        private final int position;

        private final KeyedMenu menu;

        MenuDescriptor(MenuGroupKey groupKey, int position, KeyedMenu menu) {
            this.groupKey = groupKey;
            this.position = position;
            this.menu = menu;
        }
    }

    private static class GroupDescriptor {

        private final MenuKey menuKey;

        private final int position;

        private final KeyedMenuGroup group;

        GroupDescriptor(MenuKey menuKey, int position, KeyedMenuGroup group) {
            this.menuKey = menuKey;
            this.position = position;
            this.group = group;
        }
    }

    private static class ItemDescriptor {

        private final MenuGroupKey groupKey;

        private final int position;

        private final KeyedMenuItem item;

        ItemDescriptor(MenuGroupKey groupKey, int position, KeyedMenuItem item) {
            this.groupKey = groupKey;
            this.position = position;
            this.item = item;
        }
    }

    private final ControlRegistry registry;

    private final Map<MenuKey, MenuDescriptor> barMenusByKey = new HashMap<>();

    private final Map<MenuGroupKey, GroupDescriptor> barGroupsByKey = new HashMap<>();

    private final Map<MenuItemKey, ItemDescriptor> barItemsByKey = new HashMap<>();

    public ControlBuilder(ControlRegistry registry) {
        this.registry = registry;
    }

    public List<Menu> buildMenuBarElements(View<?> view) {
        var componentKey = view.getViewModel().getKey();
        //we clone collections because there can not be changes during building
        var registrations = new ArrayList<>(registry.getBarMenuRegistrations(componentKey));
        buildBarElements(view, registrations);
        List<Pair<Integer, Menu>> positionAndTopMenus = new ArrayList<>();
        Map<MenuKey, Pair<KeyedMenu, List<GroupDescriptor>>> menusAndGroupsByMenuKey = new HashMap<>();
        //0. adding menus to menu bar or to groups
        for (var entry : this.barMenusByKey.entrySet()) {
            var key = entry.getKey();
            var menuDescriptor = entry.getValue();
            if (menuDescriptor.groupKey == null) {
                positionAndTopMenus.add(new Pair<>(menuDescriptor.position, menuDescriptor.menu));
            } else {
                var groupDescriptor = this.barGroupsByKey.get(menuDescriptor.groupKey);
                if (groupDescriptor != null) {
                    groupDescriptor.group.addItem(menuDescriptor.position, menuDescriptor.menu);
                }
            }
            menusAndGroupsByMenuKey.put(key, new Pair<>(menuDescriptor.menu, new ArrayList<>()));
        }
        //1. adding items to groups
        for (var entry : this.barItemsByKey.entrySet()) {
            var key = entry.getKey();
            var itemDescriptor = entry.getValue();
            var groupDescriptor = this.barGroupsByKey.get(itemDescriptor.groupKey);
            if (groupDescriptor != null) {
                groupDescriptor.group.addItem(itemDescriptor.position, itemDescriptor.item);
            }
        }
        //2. adding groups to menu content
        for (var entry : this.barGroupsByKey.entrySet()) {
            var groupDescriptor = entry.getValue();
            var menusAndGroups = menusAndGroupsByMenuKey.get(groupDescriptor.menuKey);
            if (menusAndGroups != null) {
                menusAndGroups.getSecond().add(groupDescriptor);
            }
        }
        //3. building menus
        for (var entry : menusAndGroupsByMenuKey.entrySet()) {
            var menu = entry.getValue().getFirst();
            var groups = entry.getValue().getSecond();
            List<MenuItem> menuElements = new ArrayList<>();
            Collections.sort(groups, (o1, o2) -> Integer.compare(o1.position, o2.position));
            for (var i = 0; i < groups.size(); i++) {
                var group = groups.get(i).group;
                if (group.isEmpty()) {
                    continue;
                }
                if (i != 0) {
                    menuElements.add(new SeparatorMenuItem());
                }
                group.sort();
                for (var v : group.getItems()) {
                    menuElements.add(v);
                }
            }
            menu.getItems().addAll(menuElements);
        }
        Collections.sort(positionAndTopMenus, (p1, p2) -> Integer.compare(p1.getFirst(), p2.getFirst()));
        var menus = positionAndTopMenus
                .stream()
                .map(p -> p.getSecond())
                .collect(Collectors.toCollection((ArrayList::new)));
        //the problem is that group can contain other menus that will be built later
        //4. removing empty menus
        removeEmptyMenus(menus);
        return menus;
    }

    /**
     * Building all elements to get their keys.
     *
     * @param view
     * @param regs
     */
    private void buildBarElements(View<?> view, List<AbstractMenuRegistration<?>> regs) {
        for (var r : regs) {
            switch (r.getType()) {
                case MENU:
                    var mr = (MenuRegistration) r;
                    var menu = mr.getFactory().create(view);
                    var md = new MenuDescriptor(mr.getGroupKey(), mr.getPosition(), menu);
                    barMenusByKey.put(menu.getKey(), md);
                    break;
                case GROUP:
                    var gr = (MenuGroupRegistration) r;
                    var group = gr.getFactory().create(view);
                    var gd = new GroupDescriptor(gr.getMenuKey(), gr.getPosition(), group);
                    barGroupsByKey.put(group.getKey(), gd);
                    break;
                case ITEM:
                    var ir = (MenuItemRegistration) r;
                    var item = ir.getFactory().create(view);
                    var id = new ItemDescriptor(ir.getGroupKey(), ir.getPosition(), item);
                    barItemsByKey.put(item.getKey(), id);
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    private void removeEmptyMenus(List<Menu> menus) {
        for (Iterator<Menu> iterator = menus.iterator(); iterator.hasNext();) {
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
