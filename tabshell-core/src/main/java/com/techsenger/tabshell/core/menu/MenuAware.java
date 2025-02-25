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

import com.techsenger.mvvm4fx.core.ComponentKey;
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
     * Returns the helper for a specific menu.
     * @param menuKey
     * @return
     */
    MenuHelper getMenuHelper(MenuKey menuKey);

    /**
     * Returns the helper for a specific menu item.
     *
     * @param menuItemKey
     * @return
     */
    MenuItemHelper getMenuItemHelper(MenuItemKey menuItemKey);

    /**
     * This method is called on showing menu popup.
     *
     * @param menuKey
     */
    void doOnMenuShowing(MenuKey menuKey);

    /**
     * This method is called on hiding menu popup.
     *
     * <p>Important. This method is called before the action of the MenuItem, as this way it is implemented in JavaFX.
     *
     * @param menuKey
     */
    void doOnMenuHiding(MenuKey menuKey);
}
