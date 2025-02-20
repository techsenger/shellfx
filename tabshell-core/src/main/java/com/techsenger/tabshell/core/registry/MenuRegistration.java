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

import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.MenuGroupKey;

/**
 *
 * @author Pavel Castornii
 */
class MenuRegistration extends AbstractMenuRegistration<KeyedMenu> {

    private final MenuGroupKey groupKey;

    MenuRegistration(MenuGroupKey groupKey, ControlFactory<KeyedMenu> factory, int position) {
        super(MenuElementType.MENU, factory, position);
        this.groupKey = groupKey;
    }

    public MenuGroupKey getGroupKey() {
        return groupKey;
    }
}
