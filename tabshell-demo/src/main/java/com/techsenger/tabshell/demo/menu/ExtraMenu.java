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

import com.techsenger.tabshell.material.menu.MenuGroupName;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;

/**
 * Every menu, menu item, menu group has its own name  which allows it to be distinguished from other menus,
 * for example, during validation in a tab.
 *
 * @author Pavel Castornii
 */
public interface ExtraMenu {

    MenuName NAME = new MenuName();

    MenuGroupName FOO_GROUP = new MenuGroupName("Foo");

    MenuItemName FOO_ITEM = new MenuItemName(); //group foo

    MenuGroupName BAR_GROUP = new MenuGroupName("Bar");

    MenuItemName BAR_ITEM = new MenuItemName(); //group bar
}
