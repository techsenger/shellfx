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

package com.techsenger.shellfx.core.registry;

import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.shellfx.material.menu.MenuGroupName;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
class MenuItemRegistration<T extends ParentFxView<?>> extends AbstractMenuRegistration<T, ManagedMenuItem> {

    private final MenuGroupName groupKey;

    MenuItemRegistration(MenuGroupName groupKey, ControlFactory<T, ManagedMenuItem> factory) {
        super(MenuElementType.ITEM, factory);
        this.groupKey = groupKey;
    }

    public MenuGroupName getGroupKey() {
        return groupKey;
    }
}
