/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.layout.dock;

/**
 * Defines which neighboring dock will take over the space when a TabDock is removed from the layout.
 *
 * @author Pavel Castornii
 */
public enum SpaceReceiver {

    /**
     * In a horizontal split: the left neighbor receives the space.
     * In a vertical split: the top neighbor receives the space.
     */
    PREVIOUS,

    /**
     * In a horizontal split: the right neighbor receives the space.
     * In a vertical split: the bottom neighbor receives the space.
     */
    NEXT,

    /**
     * The space is distributed between both neighbors.
     */
    BOTH
}
