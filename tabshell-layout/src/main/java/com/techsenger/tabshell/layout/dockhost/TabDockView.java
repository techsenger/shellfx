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

package com.techsenger.tabshell.layout.dockhost;

import com.techsenger.tabshell.layout.tabhost.TabHostView;

/**
 *
 * @author Pavel Castornii
 */
public interface TabDockView extends TabHostView {

    interface Composer extends TabHostView.Composer {

        void remove();
    }

    @Override
    Composer getComposer();

    /**
     * Controls the visibility of the drag handle used to move this component.
     * <p>
     * When enabled, a visual drag indicator (e.g., an icon or handle) is shown, allowing the user to initiate drag
     * operations. When disabled, the drag indicator is hidden.
     *
     * @param value {@code true} to show the drag handle, {@code false} to hide it
     */
    void setDraggable(boolean value);

    /**
     * Controls the presence of the minimize button in the component's UI.
     *
     * @param minimizable {@code true} to show the minimize button, {@code false} to remove it
     */
    void setMinimizable(boolean minimizable);

    /**
     * Controls the presence of the close button in the component's UI.
     *
     * @param closable {@code true} to show the close button, {@code false} to remove it
     */
    void setClosable(boolean closable);
}
