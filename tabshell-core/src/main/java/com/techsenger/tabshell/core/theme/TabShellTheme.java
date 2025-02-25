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

package com.techsenger.tabshell.core.theme;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.techsenger.toolkit.core.function.Factory;
import com.techsenger.toolkit.fx.color.ColorUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.application.Application;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public enum TabShellTheme {

    CUPERTINO_LIGHT("Cupertino Light", "cupertino-light.css", false, 6,
            () -> new CupertinoLight().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createCupertinoPalettes(false), true),

    CUPERTINO_DARK("Cupertino Dark", "cupertino-dark.css", true, 6,
            () -> new CupertinoDark().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createCupertinoPalettes(true), true),

    PRIMER_LIGHT("Primer Light", "primer-light.css", false, 4,
            () -> new PrimerLight().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createPrimerPalettes(false), true),

    PRIMER_DARK("Primer Dark", "primer-dark.css", true, 4,
            () -> new PrimerDark().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createPrimerPalettes(true), true),

    NORD_LIGHT("Nord Light", "nord-light.css", false, 1,
            () -> new NordLight().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createNordPalettes(false), true),

    NORD_DARK("Nord Dark", "nord-dark.css", true, 1,
            () -> new NordDark().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createNordPalettes(true), true),

    DRACULA("Dracula", "dracula.css", true, 6,
            () -> new Dracula().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createDraculaPalettes(), true),

    /**
     * NOT SUPPORTED. Use this theme only for testing debugging purposes.
     */
    MODENA("Modena", "modena.css", false, 4,
            () -> Application.STYLESHEET_MODENA,
            () -> ThemeProvider.getInstance().createJavaFxPalettes(), false),

    /**
     * NOT SUPPORTED. Use this theme only for testing debugging purposes.
     */
    CASPIAN("Caspian", "caspian.css", false, 4,
            () -> Application.STYLESHEET_CASPIAN,
            () -> ThemeProvider.getInstance().createJavaFxPalettes(), false);

    private final String name;

    private final String fileName;

    private final boolean dark;

    private final int borderRadius;

    private final boolean supported;

    private final Supplier<String> stylesheetSupplier;

    private ThemePalette16 simplePalette16;

    private ThemePalette32 highContrastPalette32;

    private ThemePalette32 lowContrastPalette32;

    private ThemePalette palette;

    private Map<String, Integer> colorsByName;

    private final Factory<ThemeProvider.ThemePalettes> palettesFactory;

    private volatile boolean palettesCreated;

    TabShellTheme(String name, String fileName, boolean dark, int borderRadius, Supplier<String> stylesheetSupplier,
            Factory<ThemeProvider.ThemePalettes> palletesFactory, boolean supported) {
        this.name = name;
        this.fileName = fileName;
        this.dark = dark;
        this.borderRadius = borderRadius;
        this.stylesheetSupplier = stylesheetSupplier;
        this.palettesFactory = palletesFactory;
        this.supported = supported;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns file name that has CSS rules for this theme.
     *
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Resolves the name of the file that contains specific for this theme CSS rules.
     *
     * <p>Generating CSS file in AtlantaFX is done via code with conditions. So, it is impossible to override some
     * styles using only JavaFX CSS features. That's why in some cases it is necessary to add rules specific for
     * concrete theme. At the same time if it possible it must be avoided.
     *
     * @param baseName for example "foo"
     * @return resolved name, for example "foo-dracula.css"
     */
    public String resolveFileName(String baseName) {
        return baseName + "-" + getFileName();
    }

    public boolean isDark() {
        return dark;
    }

    public int getBorderRadius() {
        return borderRadius;
    }

    public Supplier<String> getStylesheetSupplier() {
        return stylesheetSupplier;
    }

    /**
     * Returns palette with 16 colors.
     * @return
     */
    public ThemePalette16 getSimplePalette16() {
        createPalettes();
        return simplePalette16;
    }

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
    public ThemePalette32 getHighContrastPalette32() {
        createPalettes();
        return highContrastPalette32;
    }

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
    public ThemePalette32 getLowContrastPalette32() {
        createPalettes();
        return lowContrastPalette32;
    }

    /**
     * Returns palette for text elements.
     *
     * @return
     */
    public ThemePalette getPalette() {
        createPalettes();
        return palette;
    }

    /**
     * Returns colors by name. All color names start with "-color-". For example, .get("-color-bg-default");
     * Returned map contains colors from .root{} from altantafx themes, tabshell themes, base colors.
     *
     * @return
     */
    public Map<String, Integer> getColorsByName() {
        createPalettes();
        return colorsByName;
    }

    /**
     * Returns web CSS declarations for this theme, for example - background-color - red. Returned map can be used
     * for creating styles for WebView etc.
     *
     * @return
     */
    public Map<String, String> getWebStyle(Font font) {
        var map = new HashMap<String, String>();
        var palette = getPalette();
        var bgColor = ColorUtils.toHexWithAlpha(ColorUtils.toColor(palette.getDefaultBgColor()));
        map.put("background-color", bgColor);
        var fgColor = ColorUtils.toHexWithAlpha(ColorUtils.toColor(palette.getDefaultFgColor()));
        map.put("color", fgColor);
        map.put("font-size", String.valueOf(Math.round(font.getSize())) + "px");
        map.put("font-family", "'" + font.getFamily() + "'");
        return map;
    }

    /**
     * Indicates if this theme is supported by TabShell. Returns false for Modena and Caspian themes
     * and true for all others.
     *
     * @return
     */
    public boolean isSupported() {
        return supported;
    }

    /**
     * Creates colors for theme, if they have not been created yet.
     */
    private void createPalettes() {
        if (!this.palettesCreated) {
            synchronized (this) {
                if (!this.palettesCreated) {
                    this.palettesCreated = true;
                    var p = palettesFactory.create();
                    this.simplePalette16 = p.getSimplePalette16();
                    this.highContrastPalette32 = p.getHighContrastPalette32();
                    this.lowContrastPalette32 = p.getLowContrastPalette32();
                    this.palette = p.getPalette();
                    this.colorsByName = p.getColorsByName();
                }
            }
        }
    }
}
