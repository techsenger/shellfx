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

import com.techsenger.shellfx.layout.tabhost.TabHostPort;

/**
 *
 * @author Pavel Castornii
 */
public interface TabDockPort extends TabHostPort, TabDockShared {

    /**
     * Returns the value that stores the original position of this component in the layout before it was minimized
     * to the SideBar. This position is used to restore the component to its original place when expanded
     * from the SideBar.
     *
     * @return the original {@link MinimizedPosition} of this component before it was minimized to the SideBar,
     *         or {@code null} if the component is not minimized.
     */
    MinimizedPosition getMinimizedPosition();

    TabDockState getState();

    /**
     * Returns whether this TabDock can be dragged and repositioned within the docking layout.
     * <p>
     * When {@code true}, a drag handle is shown in the TabDock's header, allowing the user to grab and move it to
     * a different position in the layout. When {@code false}, no drag handle is displayed and the TabDock remains
     * fixed in place.
     *
     * @return {@code true} if this TabDock is draggable, {@code false} otherwise
     */
    boolean isDraggable();

    /**
     * Returns whether this TabDock can be closed by the user.
     * <p>
     * When {@code true}, a close button is shown in the TabDock's header, allowing the user to remove it from the
     * layout entirely. When {@code false}, no close button is displayed.
     *
     * @return {@code true} if this TabDock can be closed, {@code false} otherwise
     */
    boolean isClosable();

    /**
     * Returns whether this TabDock can be minimized into the sidebar.
     * <p>
     * When {@code true}, a minimize button is shown in the TabDock's header, allowing the user to collapse it into
     * the sidebar while keeping it accessible. When {@code false}, no minimize button is displayed.
     *
     * @return {@code true} if this TabDock can be minimized, {@code false} otherwise
     */
    boolean isMinimizable();
}
