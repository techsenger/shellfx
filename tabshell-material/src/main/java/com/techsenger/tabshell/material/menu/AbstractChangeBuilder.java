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

package com.techsenger.tabshell.material.menu;

import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
abstract class AbstractChangeBuilder<T extends NamedMenuItemUpdate, S extends AbstractChangeBuilder<T, S>> {

    private final T change;

    /**
     * It allows to avoid multiple casting.
     */
    private final S parent;

    AbstractChangeBuilder(T change) {
        this.change = change;
        this.parent = (S) this;
    }

    public S text(String text) {
        this.change.setText(text);
        this.change.setTextChanged(true);
        return parent;
    }

    public S icon(Icon<?> icon) {
        this.change.setIcon(icon);
        this.change.setIconChanged(true);
        return parent;
    }

    public T build() {
        return this.change;
    }
}
