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

package com.techsenger.tabshell.material.theme;

import java.util.Map;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public interface Theme {

    int getBorderRadius();

    /**
     * Returns colors by name. All color names start with "-color-". For example, .get("-color-bg-default");
     * Returned map contains colors from .root{} from altantafx themes, tabshell themes, base colors.
     *
     * @return
     */
    Map<String, Integer> getColorsByName();

    /**
     * Returns file name that has CSS rules for this theme.
     *
     */
    String getFileName();

    /**
     * Returns high contrast palette with 32 colors. There are 4 colors for red, blue, green, yellow, magenta, cyan
     * that have indexes from 0 to 3 (from the lightest tone to the darkest one).
     *
     * For light themes lighter colors (0, 1) are in BG palette, while darker colors (2, 3) are in FG palette. For dark
     * themes lighter colors (0, 1) are in FG palette, while darker colors (2, 3) are in BG palette.
     *
     * Black and white colors are equal for foreground and background.
     *
     * @return
     */
    ThemePalette32 getHighContrastPalette32();

    /**
     * Returns low contrast palette with 32 colors. There are 4 colors for red, blue, green, yellow, magenta, cyan
     * that have indexes from 0 to 3 (from the lightest tone to the darkest one).
     *
     * For light themes colors (0, 2) are in BG palette and colors (1, 3) are in FG palette. For dark themes
     * colors (0, 2) are in FG palette and colors (1, 3) are in BG palette.
     *
     * Black and white colors are equal for foreground and background.
     *
     * @return
     */
    ThemePalette32 getLowContrastPalette32();

    /**
     * Returns the exact name of this enumeration constant, as declared in the enum.
     *
     * @return the name of this enum constant
     */
    String name();

    /**
     * Returns a human-readable name for this enumeration constant. This can be used for display purposes, e.g., in UI.
     *
     * @return a user-friendly name representing this enum constant
     */
    String getName();

    /**
     * Returns palette for text elements.
     *
     * @return
     */
    ThemePalette getPalette();

    /**
     * Returns palette with 16 colors.
     * @return
     */
    ThemePalette16 getSimplePalette16();

    String getUserAgentStylesheet();

    /**
     * Returns web CSS declarations for this theme, for example - background-color - red. Returned map can be used
     * for creating styles for WebView etc.
     *
     * @return
     */
    Map<String, String> getWebStyle(Font font);

    boolean isDark();
}
