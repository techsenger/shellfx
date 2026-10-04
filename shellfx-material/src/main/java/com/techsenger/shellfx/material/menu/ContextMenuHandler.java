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

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvvm.ParentView;
import javafx.scene.control.ContextMenu;

/**
 * Behavior attached to a whole {@link ContextMenu}, deciding whether the popup should be shown at all -
 * independent of whether it happens to have any registered items, the same way a {@link MenuHandler} can decide
 * a nested menu's visibility independent of its children. Plain {@code ContextMenu} has no {@code visible}
 * property of its own, so implementations call {@link #setVisible(ContextMenu, boolean)} from {@link #onUpdate()}
 * instead of {@code Menu#setVisible}.
 *
 * @author Pavel Castornii
 */
public interface ContextMenuHandler<T extends ParentView<?>> extends Handler {

    static void setHandler(ContextMenu menu, ContextMenuHandler<?> handler) {
        menu.getProperties().put(handlerKey(), handler);
    }

    static @Nullable ContextMenuHandler<?> getHandler(ContextMenu menu) {
        return (ContextMenuHandler<?>) menu.getProperties().get(handlerKey());
    }

    static void setVisible(ContextMenu menu, boolean visible) {
        menu.getProperties().put(visibleKey(), visible);
    }

    /**
     * Returns whether the popup should be shown.
     *
     * @param menu the context menu to check.
     * @return {@code true} unless {@link #setVisible(ContextMenu, boolean)} hid the menu.
     */
    static boolean isVisible(ContextMenu menu) {
        return !Boolean.FALSE.equals(menu.getProperties().get(visibleKey()));
    }

    private static Object handlerKey() {
        class KeyHolder {
            private static final Object KEY = new Object();
        }
        return KeyHolder.KEY;
    }

    private static Object visibleKey() {
        class KeyHolder {
            private static final Object KEY = new Object();
        }
        return KeyHolder.KEY;
    }
}
