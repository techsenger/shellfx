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

package com.techsenger.tabshell.demos.core.menu;

import com.techsenger.tabshell.material.menu.MenuGroupKey;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;

/**
 * Every menu, menu item, menu group has its own key  which allows it to be distinguished from other menus,
 * for example, during validation in a tab.
 *
 * @author Pavel Castornii
 */
public interface DemoMenuKeys {

    MenuKey DEMO = new MenuKey();

    MenuGroupKey ONE = new MenuGroupKey();

    MenuGroupKey TWO = new MenuGroupKey();

    MenuItemKey NEW = new MenuItemKey(); //group 1

    MenuItemKey EXIT = new MenuItemKey(); //group 2
}
