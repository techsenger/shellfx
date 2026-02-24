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
public class SimpleMenuItemHelper implements MenuItemHelper {

    private final MenuItemName itemName;

    private final Boolean itemIncluded;

    private final Boolean itemValid;

    public SimpleMenuItemHelper(MenuItemName itemName) {
        this(itemName, null, null);
    }

    public SimpleMenuItemHelper(MenuItemName itemName, Boolean itemIncluded) {
        this(itemName, itemIncluded, null);
    }

    public SimpleMenuItemHelper(MenuItemName itemName, Boolean itemIncluded, Boolean itemValid) {
        this.itemName = itemName;
        this.itemIncluded = itemIncluded;
        this.itemValid = itemValid;
    }

    @Override
    public MenuItemName getItemName() {
        return this.itemName;
    }

    @Override
    public Boolean getItemIncluded() {
        return this.itemIncluded;
    }

    @Override
    public Boolean getItemValid() {
        return this.itemValid;
    }

    @Override
    public NamedMenuItemUpdate updateItem(NamedMenuItemState itemState) {
        return null;
    }

    @Override
    public void onItemAction() {

    }
}
