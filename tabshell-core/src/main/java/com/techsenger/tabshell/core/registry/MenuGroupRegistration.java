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

package com.techsenger.tabshell.core.registry;

import com.techsenger.tabshell.material.menu.KeyedMenuGroup;
import com.techsenger.tabshell.material.menu.MenuKey;

/**
 *
 * @author Pavel Castornii
 */
class MenuGroupRegistration extends AbstractMenuRegistration<KeyedMenuGroup> {

    private final MenuKey menuKey;

    MenuGroupRegistration(MenuKey menuKey, ControlFactory<KeyedMenuGroup> factory, int position) {
        super(MenuElementType.GROUP, factory, position);
        this.menuKey = menuKey;
    }

    public MenuKey getMenuKey() {
        return menuKey;
    }
}
