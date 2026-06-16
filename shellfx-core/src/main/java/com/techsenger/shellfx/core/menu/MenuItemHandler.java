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

package com.techsenger.shellfx.core.menu;

import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public interface MenuItemHandler<T extends ParentFxView<?>> extends Handler {

    static void setHandler(ManagedMenuItem item, MenuItemHandler<?> handler) {
       item.getProperties().put(key(), handler);
    }

    static MenuItemHandler<?> getHandler(ManagedMenuItem item) {
        return (MenuItemHandler<?>) item.getProperties().get(key());
    }

    private static Object key() {
        class KeyHolder {
            private static final Object KEY = new Object();
        }
        return KeyHolder.KEY;
    }

    void onAction();
}
