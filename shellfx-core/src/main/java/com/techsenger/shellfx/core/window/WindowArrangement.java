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

package com.techsenger.shellfx.core.window;

/**
 * Defines how windows should be arranged inside the window manager.
 *
 * @author Pavel Castornii
 */
public enum WindowArrangement {

    /**
     * Arranges windows in a cascading layout, where each window is offset from the previous one.
     */
    CASCADE,

    /**
     * Arranges windows in a vertical tile layout, splitting the available space into vertical columns.
     */
    TILE_VERTICAL,

    /**
     * Arranges windows in a horizontal tile layout, splitting the available space into horizontal rows.
     */
    TILE_HORIZONTAL,

    /**
     * Arranges windows in a grid layout, splitting the available space into rows and columns.
     * The number of columns and rows is derived from the container's aspect ratio —
     * wider containers get more columns, taller containers get more rows.
     */
    TILE_GRID
}
