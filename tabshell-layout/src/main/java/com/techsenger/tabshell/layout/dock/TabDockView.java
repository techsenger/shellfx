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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.tabshell.layout.tabhost.TabHostView;

/**
 *
 * @author Pavel Castornii
 */
public interface TabDockView extends TabHostView {

    /**
     * Returns the value that defines whether this component can be dragged by the user.
     *
     * @return the current draggable state of this component
     */
    boolean isDraggable();

    /**
     * Sets the value that defines whether this component can be dragged by the user.
     *
     * @param value the new draggable state for this component
     */
    void setDraggable(boolean value);

}
