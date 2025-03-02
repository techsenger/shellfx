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

package com.techsenger.tabshell.kit.core.style;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author Pavel Castornii
 */
public final class StyleUtils {

    /**
     * Returns the width of the char for monospace font.
     *
     * @param font
     * @return
     */
    public static double getMonospaceCharWidth(Font font) {
        final Text text = new Text("0");
        text.setFont(font);
        double width = text.getBoundsInLocal().getWidth();
        return width;
    }

    private StyleUtils() {
        //empty
    }
}
