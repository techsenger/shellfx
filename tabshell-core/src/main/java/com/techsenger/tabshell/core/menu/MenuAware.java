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

import com.techsenger.patternfx.mvvmx.ComponentViewModel;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;

/**
 * Interface for components that interact with menu.
 *
 * @author Pavel Castornii
 */
public interface MenuAware {

    /**
     * Returns the view model.
     *
     * @return
     */
    ComponentViewModel getViewModel();

    /**
     * Returns the helper for a specific menu.
     *
     * @param menuName
     * @return
     */
    MenuHelper getMenuHelper(MenuName menuName);

    /**
     * Returns the helper for a specific menu item.
     *
     * @param menuItemName
     * @return
     */
    MenuItemHelper getMenuItemHelper(MenuItemName menuItemName);

    /**
     * This method is called on showing menu popup.
     *
     * @param menuName
     */
    void doOnMenuShowing(MenuName menuName);

    /**
     * This method is called on hiding menu popup.
     *
     * <p>Important. This method is called before the action of the MenuItem, as this way it is implemented in JavaFX.
     *
     * @param menuName
     */
    void doOnMenuHiding(MenuName menuName);
}
