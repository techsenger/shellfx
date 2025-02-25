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

import com.techsenger.tabshell.material.menu.KeyedMenuState;
import com.techsenger.tabshell.material.menu.KeyedMenuUpdate;
import com.techsenger.tabshell.material.menu.MenuKey;

/**
 *
 * @author Pavel Castornii
 */
public class SimpleMenuHelper implements MenuHelper {

    private final MenuKey menuKey;

    private final Boolean menuIncluded;

    private final Boolean menuValid;

    public SimpleMenuHelper(MenuKey key) {
        this(key, null, null);
    }

    public SimpleMenuHelper(MenuKey key, Boolean included) {
        this(key, included, null);
    }

    public SimpleMenuHelper(MenuKey key, Boolean included, Boolean valid) {
        this.menuKey = key;
        this.menuIncluded = included;
        this.menuValid = valid;
    }

    @Override
    public MenuKey getMenuKey() {
        return menuKey;
    }

    @Override
    public Boolean getMenuIncluded() {
        return this.menuIncluded;
    }

    @Override
    public Boolean getMenuValid() {
        return this.menuValid;
    }

    @Override
    public KeyedMenuUpdate updateMenu(KeyedMenuState menuState) {
        return null;
    }
}
