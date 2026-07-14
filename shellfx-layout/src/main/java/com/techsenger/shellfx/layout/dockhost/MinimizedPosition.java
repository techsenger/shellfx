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

import javafx.geometry.Side;

/**
 * Records the state of a {@link TabDockFxView} at the moment it was minimized: the path from the root to its
 * (still-existing) logical parent split, the side it is minimized to, and its size.
 * <p>
 * Unlike the old {@code ComponentPosition}, this does not record an index or siblings — the {@link
 * TabDockContainer} itself is never discarded on minimize, it simply leaves the live {@code SplitPane#getItems()}
 * while remaining in its logical parent's {@code logicalItems}. Restoring is therefore a matter of re-inserting the
 * same container at the position implied by {@code logicalItems}, not reconstructing a lost position.
 *
 * @author Pavel Castornii
 */
public class MinimizedPosition {

    private final Side side;

    private final double width;

    private final double height;

    MinimizedPosition(Side side, double width, double height) {
        this.side = side;
        this.width = width;
        this.height = height;
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

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "MinimizedPosition [side:" + side + ", width:" + width + ", height:" + height + ']';
    }
}
