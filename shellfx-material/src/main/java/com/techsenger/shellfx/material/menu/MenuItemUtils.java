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

package com.techsenger.shellfx.material.menu;

import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Helpers shared by the menu classes of this package.
 *
 * @author Pavel Castornii
 */
final class MenuItemUtils {

    /**
     * Tells whether at least one of the items, other than separators, is visible.
     *
     * @param items the items to check.
     * @return {@code true} if there is a visible item.
     */
    static boolean hasVisibleItem(Iterable<? extends MenuItem> items) {
        for (var item : items) {
            if (!(item instanceof SeparatorMenuItem) && item.isVisible()) {
                return true;
            }
        }
        return false;
    }

    private MenuItemUtils() {
        // empty
    }
}
