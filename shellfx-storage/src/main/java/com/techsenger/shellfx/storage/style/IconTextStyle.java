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

package com.techsenger.shellfx.storage.style;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.toolkit.fx.color.ColorUtils;

/**
 * The inline styles an item (a file or a storage) is shown with. A style is a list of CSS declarations, such as
 * {@code -fx-text-fill: red;}, and a style without values clears the style of the element.
 *
 * @author Pavel Castornii
 * @param iconStyle the style of the icon
 * @param textStyle the style of the texts
 */
public record IconTextStyle(@Nullable String iconStyle, @Nullable String textStyle) {

    /**
     * Creates the style that paints the icon and the texts with the default foreground color of the theme.
     *
     * @param theme the theme
     * @return the style
     */
    public static IconTextStyle foreground(Theme theme) {
        var color = ColorUtils.toCssRgba(theme.getPalette().getDefaultFgColor());
        return new IconTextStyle("-fx-fill: " + color + ";", "-fx-text-fill: " + color + ";");
    }
}
