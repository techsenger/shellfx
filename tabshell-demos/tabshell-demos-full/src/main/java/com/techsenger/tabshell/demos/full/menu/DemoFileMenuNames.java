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

package com.techsenger.tabshell.demos.full.menu;

import com.techsenger.tabshell.core.menu.FileMenuNames;
import com.techsenger.tabshell.material.menu.MenuItemName;

/**
 * Every menu, menu item, menu group has its own key  which allows it to be distinguished from other menus,
 * for example, during validation in a tab.
 *
 * @author Pavel Castornii
 */
public interface DemoFileMenuNames extends FileMenuNames {

    MenuItemName TEXT_EDITOR = new MenuItemName();

    MenuItemName HEX_EDITOR = new MenuItemName();

    MenuItemName TERMINAL = new MenuItemName();

    MenuItemName THEME = new MenuItemName();

    MenuItemName DIALOGS = new MenuItemName();

    MenuItemName DOCK_TAB = new MenuItemName();
}
