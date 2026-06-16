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

import javafx.geometry.Point2D;

/**
 *
 * @author Pavel Castornii
 */
final class WindowPositionResolver {

    /**
     * Resolves the coordinates for placing a window within its container according to a {@link WindowPosition}.
     *
     * <p>The container is the {@code Screen} for {@link WindowType#TOP_LEVEL} windows (in which case the result is
     * the window's {@code x}/{@code y} in screen coordinates), or the {@code StackPane} for {@link WindowType#NESTED}
     * windows (in which case the result is the window's {@code layoutX}/{@code layoutY} within the {@code StackPane}).
     *
     * <p>The result is not clamped to the container's bounds — if the offsets push the window partially or fully
     * outside the container, the resolved coordinates reflect that as-is. Callers are responsible for supplying
     * offsets that keep the window within the desired bounds.
     *
     * @param pos             the reference position within the container
     * @param containerWidth  width of the container
     * @param containerHeight height of the container
     * @param windowWidth     width of the window being positioned
     * @param windowHeight    height of the window being positioned
     * @param xOffset         offset added to the computed x coordinate; positive values shift the
     *                        window to the right, negative values shift it to the left
     * @return the resolved coordinates
     */
    static Point2D resolve(WindowPosition pos, double containerWidth, double containerHeight,
            double windowWidth, double windowHeight) {

        double x = switch (pos) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0;
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> (containerWidth - windowWidth) / 2;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> containerWidth - windowWidth;
        };

        double y = switch (pos) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0;
            case CENTER_LEFT, CENTER, CENTER_RIGHT -> (containerHeight - windowHeight) / 2;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> containerHeight - windowHeight;
        };

        return new Point2D(x, y);
    }

    private WindowPositionResolver() {
        // empty
    }
}
