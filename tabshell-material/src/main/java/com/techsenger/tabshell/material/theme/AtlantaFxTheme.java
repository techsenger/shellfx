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
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public enum AtlantaFxTheme implements Theme {

    CUPERTINO_LIGHT("Cupertino Light", "cupertino-light.css", false, 6,
            () -> new CupertinoLight().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createCupertinoPalettes(false)),

    CUPERTINO_DARK("Cupertino Dark", "cupertino-dark.css", true, 6,
            () -> new CupertinoDark().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createCupertinoPalettes(true)),

    PRIMER_LIGHT("Primer Light", "primer-light.css", false, 4,
            () -> new PrimerLight().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createPrimerPalettes(false)),

    PRIMER_DARK("Primer Dark", "primer-dark.css", true, 4,
            () -> new PrimerDark().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createPrimerPalettes(true)),

    NORD_LIGHT("Nord Light", "nord-light.css", false, 1,
            () -> new NordLight().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createNordPalettes(false)),

    NORD_DARK("Nord Dark", "nord-dark.css", true, 1,
            () -> new NordDark().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createNordPalettes(true)),

    DRACULA("Dracula", "dracula.css", true, 6,
            () -> new Dracula().getUserAgentStylesheet(),
            () -> ThemeProvider.getInstance().createDraculaPalettes());

    private final String name;

    private final String fileName;

    private final boolean dark;

    private final int borderRadius;

    private final Supplier<String> stylesheetSupplier;

    private ThemePalette16 simplePalette16;

    private ThemePalette32 highContrastPalette32;

    private ThemePalette32 lowContrastPalette32;

    private ThemePalette palette;

    private Map<String, Integer> colorsByName;

    private final Factory<ThemeProvider.ThemePalettes> palettesFactory;

    private volatile boolean palettesCreated;

    AtlantaFxTheme(String name, String fileName, boolean dark, int borderRadius, Supplier<String> stylesheetSupplier,
            Factory<ThemeProvider.ThemePalettes> palletesFactory) {
        this.name = name;
        this.fileName = fileName;
        this.dark = dark;
        this.borderRadius = borderRadius;
        this.stylesheetSupplier = stylesheetSupplier;
        this.palettesFactory = palletesFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean isDark() {
        return dark;
    }

    @Override
    public int getBorderRadius() {
        return borderRadius;
    }

    @Override
    public Supplier<String> getStylesheetSupplier() {
        return stylesheetSupplier;
    }

    @Override
    public ThemePalette16 getSimplePalette16() {
        createPalettes();
        return simplePalette16;
    }

    @Override
    public ThemePalette32 getHighContrastPalette32() {
        createPalettes();
        return highContrastPalette32;
    }

    @Override
    public ThemePalette32 getLowContrastPalette32() {
        createPalettes();
        return lowContrastPalette32;
    }

    @Override
    public ThemePalette getPalette() {
        createPalettes();
        return palette;
    }

    @Override
    public Map<String, Integer> getColorsByName() {
        createPalettes();
        return colorsByName;
    }

    @Override
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
