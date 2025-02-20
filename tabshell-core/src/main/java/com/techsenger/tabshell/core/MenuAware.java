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

package com.techsenger.tabshell.core;

import com.techsenger.mvvm4fx.core.ComponentKey;
import com.techsenger.tabshell.material.menu.KeyedMenuItemState;
import com.techsenger.tabshell.material.menu.KeyedMenuItemUpdate;
import com.techsenger.tabshell.material.menu.KeyedMenuState;
import com.techsenger.tabshell.material.menu.KeyedMenuUpdate;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;

/**
 * Interface for components that interact with menu.
 *
 * @author Pavel Castornii
 */
public interface MenuAware {

    /**
     * Returns the key of the component.
     * @return
     */
    ComponentKey getKey();

    /**
     * Checks if specific menu is supported. Unsupported menus will be hidden. This method is used only for optional
     * menus.
     *
     * @return set or null.
     */
    boolean isMenuSupported(MenuKey menuKey);

    /**
     * Checks if specific menu item is supported. Unsupported menu items will be hidden. This method is used only for
     * optional menu items.
     *
     * @return set or null.
     */
    boolean isMenuItemSupported(MenuKey menuKey, MenuItemKey itemKey);

    /**
     * Checks if menu is currently valid. Invalid menus will be disabled and their actions will be ignored. This method
     * is used only for validatable menus and is called when user clicks on menu or uses accelerator keys.
     *
     * @param menuKey
     * @return
     */
    boolean isMenuValid(MenuKey menuKey);

    /**
     * Checks if menu item is currently valid. Invalid menu items will be disabled and their actions will be
     * ignored. This method is used only for validatable menu items and is called when user clicks on menu item or uses
     * accelerator keys.
     *
     * @param menuKey
     * @return
     */
    boolean isMenuItemValid(MenuKey menuKey, MenuItemKey itemKey);

    /**
     * Updates menu with specific key. This method will be called only for updatable menus.
     *
     * @param menuKey - the key for this menu. Using class as a key is a bad idea because enum are not extended
     * and one menu can have items with keys from different projects, enumerations.
     * @return
     */
    KeyedMenuUpdate updateMenu(MenuKey menuKey, KeyedMenuState menuState);

    /**
     * Updates menu item with specific key. This method will be called only for updatable menu items.
     *
     * @param itemState the current state of the item
     * @return update object or null if there are no changes.
     */
    KeyedMenuItemUpdate updateMenuItem(MenuKey menuKey, MenuItemKey itemKey, KeyedMenuItemState itemState);

    /**
     * This method is called on showing menu (managed menu or menu with managed items) with specific key.
     *
     * @param menuKey - the key for this menu. Using class as a key is a bad idea because enum are not extended
     * and one menu can have items with keys from different projects, enumerations.
     */
    void doOnMenuShowing(MenuKey menuKey);

    /**
     * This method is called on hiding menu (managed menu or menu with managed items) with specific key.
     *
     * <p>Important. This method is called before the action of the MenuItem, as this way it is implemented in JavaFX.
     *
     * @param menuKey - the key for this menu. Using class as a key is a bad idea because enum are not extended
     * and one menu can have items with keys from different projects, enumerations.
     */
    void doOnMenuHiding(MenuKey menuKey);

    /**
     * This method is called when user clicks on menu item and action should be directed to the current tab.
     * So, this method is used when there is a menu item that can be used for many tabs.
     *
     * @param itemKey
     */
    void doOnSharedMenuItemAction(MenuKey menuKey, MenuItemKey itemKey);
}
