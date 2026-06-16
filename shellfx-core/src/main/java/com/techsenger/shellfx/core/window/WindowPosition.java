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
 * Reference position used to align a window within its container (the {@code Screen} for {@link WindowType#TOP_LEVEL}
 * windows, or the {@code StackPane} for {@link WindowType#NESTED} windows).
 *
 * @author Pavel Castornii
 */
public enum WindowPosition {

    /** Places the window at the bottom-center of the container. */
    BOTTOM_CENTER,

    /** Places the window at the bottom-left of the container. */
    BOTTOM_LEFT,

    /** Places the window at the bottom-right of the container. */
    BOTTOM_RIGHT,

    /** Places the window at the center of the container. */
    CENTER,

    /** Places the window at the center-left of the container. */
    CENTER_LEFT,

    /** Places the window at the center-right of the container. */
    CENTER_RIGHT,

    /** Places the window at the top-center of the container. */
    TOP_CENTER,

    /** Places the window at the top-left of the container. */
    TOP_LEFT,

    /** Places the window at the top-right of the container. */
    TOP_RIGHT,
}
