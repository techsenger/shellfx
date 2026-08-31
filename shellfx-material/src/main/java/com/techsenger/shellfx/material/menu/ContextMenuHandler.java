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
import com.techsenger.patternfx.mvp.ParentFxView;

/**
 * Behavior attached to a whole {@link ManagedContextMenu}, deciding whether the popup should be shown at all -
 * independent of whether it happens to have any registered items, the same way a {@link MenuHandler} can decide
 * a nested menu's visibility independent of its children. Implementations call {@code self.setVisible(...)} on
 * the {@link ManagedContextMenu} from {@link #onUpdate()}, exactly like a {@link MenuHandler} does for its
 * {@code ManagedMenu}.
 *
 * @author Pavel Castornii
 */
public interface ContextMenuHandler<T extends ParentFxView<?>> extends Handler {

    static void setHandler(ManagedContextMenu menu, ContextMenuHandler<?> handler) {
        menu.setHandler(handler);
    }

    static @Nullable ContextMenuHandler<?> getHandler(ManagedContextMenu menu) {
        return menu.getHandler();
    }
}
