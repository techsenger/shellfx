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

package com.techsenger.tabshell.core.menu;

import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
public class MenuHelpers {

    private final Map<MenuName, MenuHelper> menuHelpersByName = new HashMap<>();

    private final Map<MenuItemName, MenuItemHelper> menuItemHelpersByName = new HashMap<>();

    public void addAll(MenuHelper... menuHelpers) {
        for (var h : menuHelpers) {
            this.menuHelpersByName.put(h.getMenuName(), h);
        }
    }

    public void removeAll(MenuName... menuNames) {
        for (var k : menuNames) {
            this.menuHelpersByName.remove(k);
        }
    }

    public void addAll(MenuItemHelper... itemHelpers) {
        for (var h : itemHelpers) {
            this.menuItemHelpersByName.put(h.getItemName(), h);
        }
    }

    public void removeAll(MenuItemName... itemNames) {
        for (var k : itemNames) {
            this.menuItemHelpersByName.remove(k);
        }
    }

    public Map<MenuName, MenuHelper> getMenuHelpersByName() {
        return menuHelpersByName;
    }

    public Map<MenuItemName, MenuItemHelper> getMenuItemHelpersByName() {
        return menuItemHelpersByName;
    }
}
