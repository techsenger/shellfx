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

package com.techsenger.shellfx.layout.dockhost;

/**
 *
 * @author Pavel Castornii
 */
public interface TabDockShared {

    /**
     * Sets whether this TabDock can be dragged and repositioned within the docking layout.
     * <p>
     * When set to {@code true}, a drag handle is added to the TabDock's header, allowing the user to grab and move
     * it to a different position in the layout. When set to {@code false}, the drag handle is removed and the
     * TabDock remains fixed in place.
     *
     * @param value {@code true} to enable dragging and show the drag handle, {@code false} to disable it
     */
    void setDraggable(boolean value);

    /**
     * Sets whether this TabDock can be minimized into the sidebar.
     * <p>
     * When set to {@code true}, a minimize button is added to the TabDock's header, allowing the user to collapse
     * it into the sidebar while keeping it accessible. When set to {@code false}, the minimize button is removed.
     *
     * @param minimizable {@code true} to enable minimizing and show the minimize button, {@code false} to disable it
     */
    void setMinimizable(boolean minimizable);

    /**
     * Sets whether this TabDock can be closed by the user.
     * <p>
     * When set to {@code true}, a close button is added to the TabDock's header, allowing the user to remove it
     * from the layout entirely. When set to {@code false}, the close button is removed.
     *
     * @param closable {@code true} to enable closing and show the close button, {@code false} to disable it
     */
    void setClosable(boolean closable);
}
