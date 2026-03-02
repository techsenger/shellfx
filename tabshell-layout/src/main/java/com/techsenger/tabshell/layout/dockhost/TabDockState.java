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

/**
 *
 * @author Pavel Castornii
 */
public enum TabDockState {

    /**
     * The TabDock is not attached to any layout. Typically this is the state right after creation or after
     * removal from the layout.
     */
    DETACHED,

    /**
     * The TabDock is attached to the DockHost in its normal docked position. This is the default visible state
     * within the main layout.
     */
    DOCKED,

    /**
     * The TabDock is minimized, usually collapsed into a sidebar or otherwise hidden  from the main layout.
     */
    MINIMIZED,

    /**
     * The TabDock is floating in a separate window, detached from the main DockHost layout. It can be moved freely
     * and exists independently of the DockHost.
     */
    FLOATING
}
