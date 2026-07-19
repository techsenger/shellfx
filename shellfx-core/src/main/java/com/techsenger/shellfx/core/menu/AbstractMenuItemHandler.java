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
import com.techsenger.shellfx.material.menu.ManagedItem;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractMenuItemHandler<T extends ParentFxView<?>, I extends MenuItem & ManagedItem>
        extends AbstractHandler<T> implements MenuItemHandler<T> {

    private final I item;

    protected AbstractMenuItemHandler(T component, I item) {
        super(component);
        this.item = item;
    }

    protected I getItem() {
        return item;
    }
}
