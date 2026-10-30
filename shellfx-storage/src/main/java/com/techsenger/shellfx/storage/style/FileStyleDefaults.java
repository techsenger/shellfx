/*
 * Copyright 2026 Pavel Castornii.
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
import com.techsenger.shellfx.material.theme.AtlantaFxTheme;
import com.techsenger.shellfx.material.theme.Theme;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The default styles of the files of a storage for every theme. Calculated on the base of CIEDE2000 (ΔE₀₀) where
 * ΔE₀₀ >= 15.
 *
 * @author Pavel Castornii
 */
public final class FileStyleDefaults {

    /**
     * The alpha (0.3) of the background color that is laid over a hidden file, so that the file is shown dimmed.
     */
    private static final int HIDDEN_ALPHA = 0x4D;

    private static final Map<Theme, FileStyleScheme> SCHEMES_BY_THEME = new ConcurrentHashMap<>();

    private static final Map<Theme, Consumer<FileStyleScheme>> FILLERS_BY_THEME = Map.of(
            AtlantaFxTheme.CUPERTINO_LIGHT, FileStyleDefaults::cupertinoLight,
            AtlantaFxTheme.CUPERTINO_DARK, FileStyleDefaults::cupertinoDark,
            AtlantaFxTheme.PRIMER_LIGHT, FileStyleDefaults::primerLight,
            AtlantaFxTheme.PRIMER_DARK, FileStyleDefaults::primerDark,
            AtlantaFxTheme.NORD_LIGHT, FileStyleDefaults::nordLight,
            AtlantaFxTheme.NORD_DARK, FileStyleDefaults::nordDark,
            AtlantaFxTheme.DRACULA, FileStyleDefaults::dracula);

    /**
     * Returns the default scheme of a theme. The scheme is shared, so the caller must not change it.
     *
     * @param theme the theme
     * @return the scheme of the theme
     * @throws IllegalArgumentException if there are no default styles for the theme
     */
    public static FileStyleScheme getScheme(Theme theme) {
        // the palette of a theme is created on first use, so a scheme is created when it is asked for
        return SCHEMES_BY_THEME.computeIfAbsent(theme, key -> {
            var scheme = new FileStyleScheme();
            fill(scheme, key);
            return scheme;
        });
    }

    /**
     * Gives a scheme the default styles of a theme for the kinds of files a storage knows, which lets a scheme that
     * extends {@link FileStyleScheme} start from them.
     *
     * @param scheme the scheme to fill
     * @param theme the theme
     * @throws IllegalArgumentException if there are no default styles for the theme
     */
    public static void fill(FileStyleScheme scheme, Theme theme) {
        var filler = FILLERS_BY_THEME.get(theme);
        if (filler == null) {
            throw new IllegalArgumentException("No file styles for theme " + theme);
        }
        filler.accept(scheme);
        var hidden = new FileStyleSettings();
        int dimColor = ((theme.getPalette().getDefaultBgColor() & 0xFFFFFF) << 8) | HIDDEN_ALPHA;
        hidden.getIcon().setColor(dimColor);
        hidden.getText().setColor(dimColor);
        scheme.setHidden(hidden);
    }

    /**
     * Creates the style of a file kind from the given colors. A color is 0xRRGGBBAA and is used as it is, so a color
     * that is not opaque is allowed.
     *
     * @param iconColor the color of the icon, or {@code null} to leave it unset
     * @param textColor the color of the text, or {@code null} to leave it unset
     * @return the style
     */
    public static FileStyleSettings style(@Nullable Integer iconColor, @Nullable Integer textColor) {
        var style = new FileStyleSettings();
        style.getIcon().setColor(iconColor);
        style.getText().setColor(textColor);
        return style;
    }

    private static void cupertinoLight(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xAD6200FF, null));
        scheme.setOther(style(0x434345FF, null));
        scheme.setError(style(0xA41E1EFF, 0xA41E1EFF));
    }

    private static void cupertinoDark(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xD18125FF, null));
        scheme.setOther(style(0x838388FF, null));
        scheme.setError(style(0xE0535EFF, 0xE0535EFF));
    }

    private static void primerLight(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xAB6101FF, null));
        scheme.setOther(style(0x475059FF, null));
        scheme.setError(style(0xA41E25FF, 0xA41E25FF));
    }

    private static void primerDark(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xC67B24FF, null));
        scheme.setOther(style(0x747D87FF, null));
        scheme.setError(style(0xDD4352FF, 0xDD4352FF));
    }

    private static void nordLight(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xAA6204FF, null));
        scheme.setOther(style(0x4B5569FF, null));
        scheme.setError(style(0xA41E1FFF, 0xA41E1FFF));
    }

    private static void nordDark(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xD98A2EFF, null));
        scheme.setOther(style(0x949CABFF, null));
        scheme.setError(style(0xE77D79FF, 0xE77D79FF));
    }

    private static void dracula(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xCF7F25FF, null));
        scheme.setOther(style(0x909197FF, null));
        scheme.setError(style(0xE46C68FF, 0xE46C68FF));
    }

    private FileStyleDefaults() {
        // Utility class.
    }
}
