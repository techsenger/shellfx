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
import com.techsenger.tabshell.material.menu.NamedMenuItemState;
import com.techsenger.tabshell.material.menu.NamedMenuItemUpdate;

/**
 *
 * @author Pavel Castornii
 */
public interface MenuItemDelegate {

    /**
     * Returns the name of the item this delegate works with.
     *
     * @return
     */
    MenuItemName getItemName();

    /**
     * Checks if a specific menu item is included. Not included menu items will be hidden. This method is used only for
     * optional menu items.
     *
     */
    Boolean getItemIncluded();

    /**
     * Checks if menu item is currently valid. Invalid menu items will be disabled and their actions will be
     * ignored. This method is used only for validatable menu items and is called when user clicks on menu item or uses
     * accelerator keys.
     *
     * @return
     */
    Boolean getItemValid();

    /**
     * Updates the menu item's text and icon before it is shown. This method is called only for updatable menu items,
     * allowing the component to adjust the item's appearance based on the current application state.
     *
     * @param itemState the current state of the item
     * @return update object with changes to apply, or null if there are no changes
     */
    NamedMenuItemUpdate updateItem(NamedMenuItemState itemState);

    /**
     * This method is called when user clicks on menu item and action should be directed to the current tab.
     * So, this method is used when there is a menu item that can be used for many tabs.
     *
     */
    void onItemAction();
}
