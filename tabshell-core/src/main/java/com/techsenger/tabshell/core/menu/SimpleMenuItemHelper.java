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

import com.techsenger.tabshell.material.menu.KeyedMenuItemState;
import com.techsenger.tabshell.material.menu.KeyedMenuItemUpdate;
import com.techsenger.tabshell.material.menu.MenuItemKey;

/**
 *
 * @author Pavel Castornii
 */
public class SimpleMenuItemHelper implements MenuItemHelper {

    private final MenuItemKey itemKey;

    private final Boolean itemIncluded;

    private final Boolean itemValid;

    public SimpleMenuItemHelper(MenuItemKey itemKey) {
        this(itemKey, null, null);
    }

    public SimpleMenuItemHelper(MenuItemKey itemKey, Boolean itemIncluded) {
        this(itemKey, itemIncluded, null);
    }

    public SimpleMenuItemHelper(MenuItemKey itemKey, Boolean itemIncluded, Boolean itemValid) {
        this.itemKey = itemKey;
        this.itemIncluded = itemIncluded;
        this.itemValid = itemValid;
    }

    @Override
    public MenuItemKey getItemKey() {
        return this.itemKey;
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
    public KeyedMenuItemUpdate updateItem(KeyedMenuItemState itemState) {
        return null;
    }

    @Override
    public void doOnItemAction() {

    }
}
