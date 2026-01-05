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

package com.techsenger.tabshell.demos.full.menu;

import com.techsenger.tabshell.material.menu.MenuGroupName;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;

/**
 * Every menu, menu item, menu group has its own key  which allows it to be distinguished from other menus,
 * for example, during validation in a tab.
 *
 * @author Pavel Castornii
 */
public interface DemoMenuNames {

    MenuName DEMO = new MenuName();

    MenuGroupName DEFAULT = new MenuGroupName("Default");

    MenuItemName TEXT_EDITOR = new MenuItemName();

    MenuItemName HEX_EDITOR = new MenuItemName();

    MenuItemName TERMINAL = new MenuItemName();

    MenuItemName DIALOGS = new MenuItemName();

    MenuItemName DOCK_LAYOUT = new MenuItemName();

    MenuItemName JFX_DOCK = new MenuItemName();

    MenuItemName WEB_BROWSER = new MenuItemName();
}
