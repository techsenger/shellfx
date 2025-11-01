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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.geometry.Orientation;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class ComponentPosition {

    private final List<UUID> pathFromRoot;

    private final List<UUID> siblings;

    private final Orientation orientation;

    private final Side side;

    private final UUID uuid;

    private final int index;

    private final double width;

    private final double height;

    private transient Map<UUID, Integer> pathIndexesByUuid;

    private transient Map<UUID, Integer> siblingIndexesByUuid;

    ComponentPosition(List<UUID> pathFromRoot, List<UUID> siblings, Orientation orientation, Side side,
            UUID uuid, int index, double width, double height) {
        this.pathFromRoot = pathFromRoot;
        this.siblings = siblings;
        this.orientation = orientation;
        this.side = side;
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

    /**
     * Returns one of four sides. If the side is {@link Side#TOP} or {@link Side#BOTTOM} it is minimized to the bottom
     * side bar.
     *
     * @return
     */
    public Side getSide() {
        return side;
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

    public void updateUuid(UUID oldUuid, UUID newUuid) {
        var pathIndex = this.pathIndexesByUuid.remove(oldUuid);
        if (pathIndex != null) {
            this.pathFromRoot.set(pathIndex, newUuid);
            this.pathIndexesByUuid.put(newUuid, pathIndex);
        }
        var siblingIndex = this.siblingIndexesByUuid.remove(oldUuid);
        if (siblingIndex != null) {
            this.siblings.set(siblingIndex, newUuid);
            this.siblingIndexesByUuid.put(newUuid, siblingIndex);
        }
    }

    @Override
    public String toString() {
        return "ComponentPosition [" + "pathFromRoot:" + pathFromRoot + ", siblings:" + siblings
                + ", orientation:" + orientation + ", side:" + side + ", uuid:" + uuid + ", index:" + index
                + ", width:" + width + ", height:" + height + ']';
    }

    void buildMaps() {
        pathIndexesByUuid = new HashMap<>();
        for (var i = 0; i < this.pathFromRoot.size(); i++) {
            pathIndexesByUuid.put(this.pathFromRoot.get(i), i);
        }
        siblingIndexesByUuid = new HashMap<>();
        for (var i = 0; i < this.siblings.size(); i++) {
            siblingIndexesByUuid.put(this.siblings.get(i), i);
        }
    }
}
