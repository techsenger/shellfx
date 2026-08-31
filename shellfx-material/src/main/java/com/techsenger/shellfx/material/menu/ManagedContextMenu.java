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
import javafx.scene.control.ContextMenu;

/**
 * A {@code ContextMenu} that can carry a {@link ContextMenuHandler} deciding whether the whole popup should be
 * shown, the same way a {@link ManagedMenu} can decide its own visibility - unlike {@code Menu}, plain
 * {@code ContextMenu} has no {@code visible} property of its own, so this adds one for that purpose alone.
 *
 * <p>{@link #getHandler()}/{@link #setHandler(ContextMenuHandler)} are package-private - callers go through
 * {@link ContextMenuHandler}'s own {@code setHandler}/{@code getHandler}, which keeps the same call shape as
 * {@link MenuHandler}/{@link MenuItemHandler} even though this class stores the handler in a dedicated field
 * rather than a shared properties map.
 *
 * @author Pavel Castornii
 */
public class ManagedContextMenu extends ContextMenu {

    private boolean visible = true;

    private @Nullable ContextMenuHandler<?> handler;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Nullable ContextMenuHandler<?> getHandler() {
        return handler;
    }

    void setHandler(@Nullable ContextMenuHandler<?> handler) {
        this.handler = handler;
    }
}
