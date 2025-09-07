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

import java.util.UUID;

/**
 *
 * @author Pavel Castornii
 */
class ComponentPosition {

    private final UUID parentUuid;

    private final int index;

    private final double width;

    private final double height;

    ComponentPosition(UUID parentUuid, int index, double width, double height) {
        this.parentUuid = parentUuid;
        this.index = index;
        this.width = width;
        this.height = height;
    }

    public UUID getParentUuid() {
        return parentUuid;
    }

    public int getIndex() {
        return index;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "ComponentPosition [" + "parentUuid:" + parentUuid + ", index:" + index + ", width:" + width
                + ", height:" + height + ']';
    }
}
