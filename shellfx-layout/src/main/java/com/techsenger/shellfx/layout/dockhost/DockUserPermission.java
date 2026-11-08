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
 * An operation with a dock that the user may be allowed to perform. The permissions restrict only the actions of
 * the user: the API is not restricted by them.
 *
 * @author Pavel Castornii
 */
public enum DockUserPermission {

    /**
     * The user can create a new dock by dropping a tab onto the edge of the layout or of another dock.
     */
    CREATE,

    /**
     * The user can move a dock to another position of the layout. A dock that is not draggable cannot be moved
     * anyway.
     */
    MOVE,

    /**
     * The user can minimize a dock to a side bar. A dock that is not minimizable cannot be minimized anyway. Restoring
     * a minimized dock is always allowed.
     */
    MINIMIZE
}
