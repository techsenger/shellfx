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

import java.util.List;
import java.util.UUID;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public class ComponentPosition {

    private final List<UUID> pathFromRoot;

    private final List<UUID> siblings;

    private final Orientation orientation;

    private final UUID uuid;

    private final int index;

    private final double width;

    private final double height;

    ComponentPosition(List<UUID> pathFromRoot, List<UUID> siblings, Orientation orientation, UUID uuid, int index,
            double width, double height) {
        this.pathFromRoot = pathFromRoot;
        this.siblings = siblings;
        this.orientation = orientation;
        this.uuid = uuid;
        this.index = index;
        this.width = width;
        this.height = height;
    }

    public List<UUID> getPathFromRoot() {
        return pathFromRoot;
    }

    public List<UUID> getSiblings() {
        return siblings;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public UUID getUuid() {
        return uuid;
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
        return "ComponentPosition [" + "pathFromRoot:" + pathFromRoot + ", siblings:" + siblings
                + ", orientation:" + orientation + ", uuid:" + uuid + ", index:" + index
                + ", width:" + width + ", height:" + height + ']';
    }
}
