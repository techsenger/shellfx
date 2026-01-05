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

import java.util.Objects;
import javafx.geometry.Side;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * MousePosition always shows the mouse cursor's position within the {@link AbstractEventContainer}.
 *
 * @author Pavel Castornii
 */
class MousePosition {

    private final Region eventContainer;

    private final MouseEvent event;

    private final Side side;

    private final boolean edgeMode;

    private final boolean overTabHeaderArea;

    MousePosition(Region eventContainer, MouseEvent event, Side side, boolean edgeMode,
            boolean overTabHeaderArea) {
        this.eventContainer = eventContainer;
        this.event = event;
        this.side = side;
        this.edgeMode = edgeMode;
        this.overTabHeaderArea = overTabHeaderArea;
    }

    public Region getEventContainer() {
        return eventContainer;
    }

    public <T extends Region> T getEventContainer(Class<T> clazz) {
        return (T) eventContainer;
    }

    public MouseEvent getEvent() {
        return event;
    }

    public Side getSide() {
        return side;
    }

    public boolean isEdgeMode() {
        return edgeMode;
    }

    public boolean isOverTabHeaderArea() {
        return overTabHeaderArea;
    }

    @Override
    public String toString() {
        return "MousePosition [" + "side:" + side + ", edgeMode:" + edgeMode
                + ", overTabHeaderArea:" + overTabHeaderArea + ']';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.eventContainer);
        hash = 61 * hash + Objects.hashCode(this.side);
        hash = 61 * hash + (this.edgeMode ? 1 : 0);
        hash = 61 * hash + (this.overTabHeaderArea ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MousePosition other = (MousePosition) obj;
        if (this.edgeMode != other.edgeMode) {
            return false;
        }
        if (this.overTabHeaderArea != other.overTabHeaderArea) {
            return false;
        }
        if (!Objects.equals(this.eventContainer, other.eventContainer)) {
            return false;
        }
        return this.side == other.side;
    }
}
