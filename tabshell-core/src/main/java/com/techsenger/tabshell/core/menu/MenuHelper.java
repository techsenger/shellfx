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

package com.techsenger.tabshell.core.menu;

import com.techsenger.tabshell.material.menu.MenuName;
import com.techsenger.tabshell.material.menu.NamedMenuState;
import com.techsenger.tabshell.material.menu.NamedMenuUpdate;

/**
 *
 * @author Pavel Castornii
 */
public interface MenuHelper {

    /**
     * Returns the name of the menu this helper works with.
     *
     * @return
     */
    MenuName getMenuName();

    /**
     * Checks if a specific menu is included. Not included menus will be hidden. This method is used only for optional
     * menus.
     *
     */
    Boolean getMenuIncluded();

    /**
     * Checks if a menu is currently valid. Invalid menus will be disabled and their actions will be ignored.
     * This method is used only for validatable menus and is called when user clicks on menu or uses accelerator keys.
     *
     * @return
     */
    Boolean getMenuValid();

    /**
     * Updates menu with a specific key. This method will be called only for updatable menus.
     *
     * @return
     */
    NamedMenuUpdate updateMenu(NamedMenuState menuState);
}
