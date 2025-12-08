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

package com.techsenger.tabshell.material.style;

import javafx.geometry.Dimension2D;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author Pavel Castornii
 */
public final class StyleUtils {

    public static String toStyle(Font font) {
        if (font != null) {
            var result = "-fx-font-size:" + Math.round(font.getSize()) + "px;-fx-font-family:'"
                    + font.getFamily() + "';";
            return result;
        } else {
            return "";
        }
    }

    /**
     * Returns the width and height of a single character when rendered with the specified monospace font.
     *
     * <p>Note: The character height is not equal to the line height.
     *
     * @param font the font to measure (must be monospace for accurate results)
     * @return a {@code Dimension2D} representing character width and height
     */
    public static Dimension2D getMonospaceCharSize(Font font) {
        final Text text = new Text("W");
        text.setFont(font);
        var bounds = text.getBoundsInLocal();
        return new Dimension2D(bounds.getWidth(), bounds.getHeight());
    }

    private StyleUtils() {

    }
}
