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

import com.techsenger.tabshell.core.area.AreaPort;

/**
 *
 * @author Pavel Castornii
 */
public interface TabDockPort extends AreaPort {

    /**
     * Returns the value that stores the original position of this component in the layout before it was minimized
     * to the SideBar. This position is used to restore the component to its original place when expanded
     * from the SideBar.
     *
     * @return the original {@link ComponentPosition} of this component before it was minimized to the SideBar,
     *         or {@code null} if the component is not minimized.
     */
    ComponentPosition getMinimizedPosition();

    /**
     * Sets the value that stores the original position of this component in the layout before it was minimized
     * to the SideBar. This position is used to restore the component to its original place when expanded
     * from the SideBar.
     *
     * @param position the original {@link ComponentPosition} of the component before minimization
     */
    void setMinimizedPosition(ComponentPosition position);

    TabDockState getState();
}
