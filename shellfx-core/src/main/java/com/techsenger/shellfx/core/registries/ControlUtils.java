/*
 * Copyright 2026 Pavel Castornii.
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

package com.techsenger.shellfx.core.registries;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * Helpers for the controls built by a {@link ControlBuilder}.
 *
 * @author Pavel Castornii
 */
public final class ControlUtils {

    /**
     * Returns the menu items of {@code controls} that can have an accelerator, that is all but menus.
     *
     * @param controls the controls built by a control builder
     * @return a new list
     */
    public static List<MenuItem> getMenuItems(Controls<?, ?> controls) {
        var items = new ArrayList<MenuItem>();
        for (var provider : controls.providers()) {
            if (provider.getControl() instanceof MenuItem item && !(item instanceof Menu)) {
                items.add(item);
            }
        }
        return items;
    }

    private ControlUtils() {
        // empty
    }
}
