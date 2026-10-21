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

package com.techsenger.shellfx.storage.settings;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.theme.AtlantaFxTheme;
import com.techsenger.shellfx.material.theme.Theme;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The default styles of the files of a storage for every theme.
 *
 * @author Pavel Castornii
 */
public final class FileStyleDefaults {

    private static final double HIDDEN_OPACITY = 0.7;

    private static final Map<Theme, Consumer<FileStyleScheme>> FILLERS_BY_THEME = Map.of(
            AtlantaFxTheme.CUPERTINO_LIGHT, FileStyleDefaults::cupertinoLight,
            AtlantaFxTheme.CUPERTINO_DARK, FileStyleDefaults::cupertinoDark,
            AtlantaFxTheme.PRIMER_LIGHT, FileStyleDefaults::primerLight,
            AtlantaFxTheme.PRIMER_DARK, FileStyleDefaults::primerDark,
            AtlantaFxTheme.NORD_LIGHT, FileStyleDefaults::nordLight,
            AtlantaFxTheme.NORD_DARK, FileStyleDefaults::nordDark,
            AtlantaFxTheme.DRACULA, FileStyleDefaults::dracula);

    private static final Map<Theme, FileStyleScheme> SCHEMES_BY_THEME = new ConcurrentHashMap<>();

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
        var palette = theme.getPalette();
        int folderColor = theme.isDark() ? palette.getWarning2Color() : palette.getWarning6Color();
        scheme.getDirectory().getIcon().setColor(folderColor);
        scheme.getDirectoryLink().getIcon().setColor(folderColor);
        var hidden = new FileStyleSettings();
        hidden.setOpacity(HIDDEN_OPACITY);
        scheme.setHidden(hidden);
    }

    private static FileStyleSettings style(@Nullable Integer iconColor, @Nullable Integer textColor) {
        var style = new FileStyleSettings();
        style.getIcon().setColor(iconColor);
        style.getText().setColor(textColor);
        return style;
    }

    private static void cupertinoLight(FileStyleScheme scheme) {
        scheme.setDirectory(style(0x242424, 0x242424));
        scheme.setDirectoryLink(style(0x242424, 0x242424));
        scheme.setFileLink(style(0x000000, 0x000000));
        scheme.setLinkLink(style(0x000000, 0x000000));
        scheme.setOtherLink(style(0x636366, 0x636366));
        scheme.setOther(style(0x77717D, 0x77717D));
        scheme.setFile(style(0x000000, 0x000000));
        scheme.setError(style(0xC66D6D, 0xC66D6D));
    }

    private static void cupertinoDark(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xFFFFFF, 0xFFFFFF));
        scheme.setDirectoryLink(style(0xFFFFFF, 0xFFFFFF));
        scheme.setFileLink(style(0xFFFFFF, 0xFFFFFF));
        scheme.setLinkLink(style(0xFFFFFF, 0xFFFFFF));
        scheme.setOtherLink(style(0xAEAEB2, 0xAEAEB2));
        scheme.setOther(style(0x9B929F, 0x9B929F));
        scheme.setFile(style(0xFFFFFF, 0xFFFFFF));
        scheme.setError(style(0xE88780, 0xE88780));
    }

    private static void primerLight(FileStyleScheme scheme) {
        scheme.setDirectory(style(0x24292F, 0x24292F));
        scheme.setDirectoryLink(style(0x24292F, 0x24292F));
        scheme.setFileLink(style(0x24292F, 0x24292F));
        scheme.setLinkLink(style(0x24292F, 0x24292F));
        scheme.setOtherLink(style(0x57606A, 0x57606A));
        scheme.setOther(style(0x77717D, 0x77717D));
        scheme.setFile(style(0x24292F, 0x24292F));
        scheme.setError(style(0xC66D75, 0xC66D75));
    }

    private static void primerDark(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xC9D1D9, 0xC9D1D9));
        scheme.setDirectoryLink(style(0xC9D1D9, 0xC9D1D9));
        scheme.setFileLink(style(0xC9D1D9, 0xC9D1D9));
        scheme.setLinkLink(style(0xC9D1D9, 0xC9D1D9));
        scheme.setOtherLink(style(0x8B949E, 0x8B949E));
        scheme.setOther(style(0x9B929F, 0x9B929F));
        scheme.setFile(style(0xC9D1D9, 0xC9D1D9));
        scheme.setError(style(0xE58A83, 0xE58A83));
    }

    private static void nordLight(FileStyleScheme scheme) {
        scheme.setDirectory(style(0x2E3440, 0x2E3440));
        scheme.setDirectoryLink(style(0x2E3440, 0x2E3440));
        scheme.setFileLink(style(0x2E3440, 0x2E3440));
        scheme.setLinkLink(style(0x2E3440, 0x2E3440));
        scheme.setOtherLink(style(0x788192, 0x788192));
        scheme.setOther(style(0x817B8B, 0x817B8B));
        scheme.setFile(style(0x2E3440, 0x2E3440));
        scheme.setError(style(0xB9777E, 0xB9777E));
    }

    private static void nordDark(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xECEFF4, 0xECEFF4));
        scheme.setDirectoryLink(style(0xECEFF4, 0xECEFF4));
        scheme.setFileLink(style(0xECEFF4, 0xECEFF4));
        scheme.setLinkLink(style(0xECEFF4, 0xECEFF4));
        scheme.setOtherLink(style(0xB7BECB, 0xB7BECB));
        scheme.setOther(style(0xAAA2B2, 0xAAA2B2));
        scheme.setFile(style(0xECEFF4, 0xECEFF4));
        scheme.setError(style(0xD18A91, 0xD18A91));
    }

    private static void dracula(FileStyleScheme scheme) {
        scheme.setDirectory(style(0xF8F8F2, 0xF8F8F2));
        scheme.setDirectoryLink(style(0xF8F8F2, 0xF8F8F2));
        scheme.setFileLink(style(0xF8F8F2, 0xF8F8F2));
        scheme.setLinkLink(style(0xF8F8F2, 0xF8F8F2));
        scheme.setOtherLink(style(0xBFBFC3, 0xBFBFC3));
        scheme.setOther(style(0x9E96A8, 0x9E96A8));
        scheme.setFile(style(0xF8F8F2, 0xF8F8F2));
        scheme.setError(style(0xE88F83, 0xE88F83));
    }

    private FileStyleDefaults() {
        // Utility class.
    }
}
