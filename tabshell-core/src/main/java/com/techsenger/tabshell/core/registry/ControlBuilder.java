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

import com.techsenger.tabshell.material.menu.MenuGroupName;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
import com.techsenger.toolkit.core.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import com.techsenger.patternfx.mvp.FxView;

/**
 *
 * @author Pavel Castornii
 */
public class ControlBuilder {

    private static class MenuDescriptor {

        private final MenuGroupName groupName;

        private final NamedMenu menu;

        MenuDescriptor(MenuGroupName groupName, NamedMenu menu) {
            this.groupName = groupName;
            this.menu = menu;
        }
    }

    private static class GroupDescriptor {

        private final MenuName menuName;

        private final NamedMenuGroup group;

        GroupDescriptor(MenuName menuName, NamedMenuGroup group) {
            this.menuName = menuName;
            this.group = group;
        }
    }

    private static class ItemDescriptor {

        private final MenuGroupName groupName;

        private final NamedMenuItem item;

        ItemDescriptor(MenuGroupName groupName, NamedMenuItem item) {
            this.groupName = groupName;
            this.item = item;
        }
    }

    private final ControlRegistry registry;

    private final Map<MenuName, MenuDescriptor> barMenusByName = new HashMap<>();

    private final Map<MenuGroupName, GroupDescriptor> barGroupsByName = new HashMap<>();

    private final Map<MenuItemName, ItemDescriptor> barItemsByName = new HashMap<>();

    public ControlBuilder(ControlRegistry registry) {
        this.registry = registry;
    }

    public List<Menu> buildMenuBarElements(FxView view) {
        var componentName = view.getPresenter().getDescriptor().getName();
        //we clone collections because there can not be changes during building
        var registrations = new ArrayList<>(registry.getBarMenuRegistrations(componentName));
        buildBarElements(view, registrations);
        List<NamedMenu> topMenus = new ArrayList<>();
        Map<MenuName, Pair<NamedMenu, List<GroupDescriptor>>> menusAndGroupsByMenuName = new HashMap<>();
        //0. adding menus to menu bar or to groups
        for (var entry : this.barMenusByName.entrySet()) {
            var key = entry.getKey();
            var menuDescriptor = entry.getValue();
            if (menuDescriptor.groupName == null) {
                topMenus.add(menuDescriptor.menu);
            } else {
                var groupDescriptor = this.barGroupsByName.get(menuDescriptor.groupName);
                if (groupDescriptor != null) {
                    groupDescriptor.group.getItems().add(menuDescriptor.menu);
                    menuDescriptor.menu.setGroup(groupDescriptor.group);
                }
            }
            menusAndGroupsByMenuName.put(key, new Pair<>(menuDescriptor.menu, new ArrayList<>()));
        }
        //1. adding items to groups
        for (var entry : this.barItemsByName.entrySet()) {
            var key = entry.getKey();
            var itemDescriptor = entry.getValue();
            var groupDescriptor = this.barGroupsByName.get(itemDescriptor.groupName);
            if (groupDescriptor != null) {
                groupDescriptor.group.getItems().add(itemDescriptor.item);
                itemDescriptor.item.setGroup(groupDescriptor.group);
            }
        }
        //2. adding groups to menu content
        for (var entry : this.barGroupsByName.entrySet()) {
            var groupDescriptor = entry.getValue();
            var menusAndGroups = menusAndGroupsByMenuName.get(groupDescriptor.menuName);
            if (menusAndGroups != null) {
                menusAndGroups.getSecond().add(groupDescriptor);
            }
        }
        //3. building menus
        for (var entry : menusAndGroupsByMenuName.entrySet()) {
            var menu = entry.getValue().getFirst();
            var groups = entry.getValue().getSecond();
            List<MenuItem> menuElements = new ArrayList<>();
            Collections.sort(groups, (o1, o2) -> Integer.compare(o1.group.getPosition(), o2.group.getPosition()));
            for (var i = 0; i < groups.size(); i++) {
                var group = groups.get(i).group;
                if (group.getItems().isEmpty()) {
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
        Collections.sort(topMenus, (p1, p2) -> Integer.compare(p1.getPosition(), p2.getPosition()));
        //the problem is that group can contain other menus that will be built later
        //4. removing empty menus
        removeEmptyMenus(topMenus);
        return (List) topMenus;
    }

    /**
     * Building all elements to get their keys.
     *
     * @param view
     * @param regs
     */
    private void buildBarElements(FxView view, List<AbstractMenuRegistration<?>> regs) {
        for (var r : regs) {
            switch (r.getType()) {
                case MENU:
                    var mr = (MenuRegistration) r;
                    var menu = mr.getFactory().create(view);
                    var md = new MenuDescriptor(mr.getGroupName(), menu);
                    barMenusByName.put(menu.getName(), md);
                    break;
                case GROUP:
                    var gr = (MenuGroupRegistration) r;
                    var group = gr.getFactory().create(view);
                    var gd = new GroupDescriptor(gr.getMenuName(), group);
                    barGroupsByName.put(group.getName(), gd);
                    break;
                case ITEM:
                    var ir = (MenuItemRegistration) r;
                    var item = ir.getFactory().create(view);
                    var id = new ItemDescriptor(ir.getGroupKey(), item);
                    barItemsByName.put(item.getName(), id);
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    private void removeEmptyMenus(List<NamedMenu> menus) {
        for (Iterator<NamedMenu> iterator = menus.iterator(); iterator.hasNext();) {
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
