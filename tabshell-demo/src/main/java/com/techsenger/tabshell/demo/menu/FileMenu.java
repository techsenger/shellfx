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

package com.techsenger.tabshell.demo.menu;

import com.techsenger.tabshell.material.menu.DefaultMenuGroupName;
import com.techsenger.tabshell.material.menu.DefaultMenuItemName;
import com.techsenger.tabshell.material.menu.DefaultMenuName;
import com.techsenger.tabshell.material.menu.MenuGroupName;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;

/**
 *
 * @author Pavel Castornii
 */
public interface FileMenu {

    MenuName NAME = new DefaultMenuName();

    MenuGroupName DEMO_GROUP = new DefaultMenuGroupName("Demo");

    MenuItemName MAIN_TAB = new DefaultMenuItemName();

    MenuItemName PAGE_TAB = new DefaultMenuItemName();

    MenuItemName DIALOGS = new DefaultMenuItemName();

    MenuItemName DEV_TOOLS = new DefaultMenuItemName();

    MenuGroupName APPEARANCE_GROUP = new DefaultMenuGroupName("Settings");

    MenuItemName THEME = new DefaultMenuItemName();

    MenuItemName STYLES_TAB = new DefaultMenuItemName();

    MenuGroupName LAST_GROUP = new DefaultMenuGroupName("Last");

    MenuItemName EXIT = new DefaultMenuItemName();
}
