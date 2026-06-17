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

package com.techsenger.shellfx.material.theme;

import atlantafx.base.theme.Theme;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReader;
import com.techsenger.shellfx.material.style.Stylesheet;
import com.techsenger.toolkit.core.StringUtils;
import com.techsenger.toolkit.fx.color.ColorUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To check what colors are used in current terminal use the following bash script:
 * <pre>
 * #!/bin/bash
 * printf "\nForeground Colors:\n"
 * for i in {0..15}; do
 *    printf "\e[38;5;${i}m████████\e[0m"
 *    if [ $((($i + 1) % 8)) -eq 0 ]; then
 *        printf "\n"
 *    fi
 * done
 * printf "\n"
 *
 * printf "Background Colors:\n"
 * for i in {0..15}; do
 *    printf "\e[48;5;${i}m%8s" ""
 *    if [ $((($i + 1) % 8)) -eq 0 ]; then
 *        printf "\e[0m\n"
 *    fi
 * done
 * printf "\e[0m\n"
 * </pre>
 *
 * Note, that in the first loop ANSI code 38 means FG color, while in the second loop ANSI code 48 means BG color.
 *
 * As we use two FG and BG palettes, assume that dark color (from 16 color palette) was split to colors (2, 3) and
 * bright color was split to colors (0, 1).
 *
 * For all colors except black and white colors are taken from palette (see palettes.html) one by one. For black and
 * white use -color-fg-default, -color-bg-default, and colors next to them. So same colors are in fg and in bg palette.
 *
 * @author Pavel Castornii
 */
final class ThemeProvider {

    static ThemeProvider getInstance() {
        return instance;
    }

    static int colorFromMap(Map<String, Integer> map, String name) {
        return map.get("-color-" + name);
    }

    static class ThemePalettes {

        private final ThemePalette16 simplePalette16;

        private final ThemePalette32 highContrastPalette32;

        private final ThemePalette32 lowContrastPalette32;

        private final ThemePalette palette;

        private final Map<String, Integer> colorsByName;

        ThemePalettes(ThemePalette16 simplePalette16, ThemePalette32 highContrastPalette32,
                ThemePalette32 lowContrastPalette32, ThemePalette textPalette, Map<String, Integer> colorsByName) {
            this.simplePalette16 = simplePalette16;
            this.highContrastPalette32 = highContrastPalette32;
            this.lowContrastPalette32 = lowContrastPalette32;
            this.palette = textPalette;
            this.colorsByName = colorsByName;
        }

        public ThemePalette16 getSimplePalette16() {
            return simplePalette16;
        }

        public ThemePalette32 getHighContrastPalette32() {
            return highContrastPalette32;
        }

        public ThemePalette32 getLowContrastPalette32() {
            return lowContrastPalette32;
        }

        public ThemePalette getPalette() {
            return palette;
        }

        public Map<String, Integer> getColorsByName() {
            return colorsByName;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ThemeProvider.class);

    /**
     * After logger is initialized.
     */
    private static final ThemeProvider instance = new ThemeProvider();

    /**
     * Colors from material.css.
     */
    private final Map<String, Integer> sharedColorsByName;

    /**
     * Colors from atlantafx, material.css, material-X.css.
     */
    private Map<String, Integer> themeColorsByName;

    private ThemeProvider() {
        var fileName = "material.css";
        Map<String, Integer> tempColorsByName;
        try {
            var css = readColors(Stylesheet.class, fileName);
            tempColorsByName = parseColors(css, null);
        } catch (Exception ex) {
            logger.error("Error reading CSS file {}", fileName, ex);
            tempColorsByName = null;
        }
        this.sharedColorsByName = tempColorsByName;
    }

    ThemePalettes createCupertinoPalettes(boolean dark) {
        if (!dark) {
            this.themeColorsByName = createThemeColors("material-cupertino-light.css");
            int[] blacks = new int[]{c("base-8"), c("fg-default")};
            int[] reds = new int[]{c("danger-4"), c("danger-5"), c("danger-6"), c("danger-7")};
            int[] greens = new int[]{c("success-4"), c("success-5"), c("success-6"), c("success-7")};
            int[] yellows = new int[]{c("warning-4"), c("warning-5"), c("warning-6"), c("warning-7")};
            int[] blues = new int[]{c("accent-4"), c("accent-5"), c("accent-6"), c("accent-7")};
            int[] whites = new int[]{c("bg-default"), c("base-4")};
            var palettes = createPalettes(false, blacks, reds, greens, yellows, blues, whites);
            logger.debug("Created palettes for cupertino light theme");
            return palettes;
        } else {
            this.themeColorsByName = createThemeColors("material-cupertino-dark.css");
            int[] blacks = new int[]{c("base-6"), c("bg-default")};
            int[] reds = new int[]{c("danger-5"), c("danger-6"), c("danger-7"), c("danger-8")};
            int[] greens = new int[]{c("success-5"), c("success-6"), c("success-7"), c("success-8")};
            int[] yellows = new int[]{c("warning-5"), c("warning-6"), c("warning-7"), c("warning-8")};
            int[] blues = new int[]{c("accent-5"), c("accent-6"), c("accent-7"), c("accent-8")};
            int[] whites = new int[]{c("fg-default"), c("base-3")};
            var palettes = createPalettes(true, blacks, reds, greens, yellows, blues, whites);
            logger.debug("Created palettes for cupertino dark theme");
            return palettes;
        }
    }

    ThemePalettes createPrimerPalettes(boolean dark) {
        if (!dark) {
            this.themeColorsByName = createThemeColors("material-primer-light.css");
            int[] blacks = new int[]{c("base-6"), c("fg-default")};
            int[] reds = new int[]{c("danger-3"), c("danger-4"), c("danger-5"), c("danger-6")};
            int[] greens = new int[]{c("success-3"), c("success-4"), c("success-5"), c("success-6")};
            int[] yellows = new int[]{c("warning-3"), c("warning-4"), c("warning-5"), c("warning-6")};
            int[] blues = new int[]{c("accent-3"), c("accent-4"), c("accent-5"), c("accent-6")};
            int[] whites = new int[]{c("bg-default"), c("base-2")};
            var palettes = createPalettes(false, blacks, reds, greens, yellows, blues, whites);
            logger.debug("Created palettes for primer light theme");
            return palettes;
        } else {
            this.themeColorsByName = createThemeColors("material-primer-dark.css");
            int[] blacks = new int[]{c("base-6"), c("bg-default")};
            int[] reds = new int[]{c("danger-4"), c("danger-5"), c("danger-6"), c("danger-7")};
            int[] greens = new int[]{c("success-4"), c("success-5"), c("success-6"), c("success-7")};
            int[] yellows = new int[]{c("warning-4"), c("warning-5"), c("warning-6"), c("warning-7")};
            int[] blues = new int[]{c("accent-4"), c("accent-5"), c("accent-6"), c("accent-7")};
            int[] whites = new int[]{c("fg-default"), c("base-3")};
            var palettes = createPalettes(true, blacks, reds, greens, yellows, blues, whites);
            logger.debug("Created palettes for primer dark theme");
            return palettes;
        }
    }

    ThemePalettes createNordPalettes(boolean dark) {
        //Nord light palette has same colors as nord dark (see palettes.html).
        if (!dark) {
            this.themeColorsByName = createThemeColors("material-nord-light.css");
            int[] blacks = new int[]{c("base-6"), c("fg-default")};
            int[] reds = new int[]{c("danger-4"), c("danger-5"), c("danger-6"), c("danger-7")};
            int[] greens = new int[]{c("success-4"), c("success-5"), c("success-6"), c("success-7")};
            int[] yellows = new int[]{c("warning-4"), c("warning-5"), c("warning-6"), c("warning-7")};
            int[] blues = new int[]{c("accent-4"), c("accent-5"), c("accent-6"), c("accent-7")};
            int[] whites = new int[]{c("bg-default"), c("base-3")};
            var palettes = createPalettes(false, blacks, reds, greens, yellows, blues, whites);
            logger.debug("Created palettes for nord light theme");
            return palettes;
        } else {
            this.themeColorsByName = createThemeColors("material-nord-dark.css");
            int[] blacks = new int[]{c("base-6"), c("bg-default")};
            int[] reds = new int[]{c("danger-4"), c("danger-5"), c("danger-6"), c("danger-7")};
            int[] greens = new int[]{c("success-4"), c("success-5"), c("success-6"), c("success-7")};
            int[] yellows = new int[]{c("warning-4"), c("warning-5"), c("warning-6"), c("warning-7")};
            int[] blues = new int[]{c("accent-4"), c("accent-5"), c("accent-6"), c("accent-7")};
            int[] whites = new int[]{c("fg-default"), c("base-4")};
            var palettes = createPalettes(true, blacks, reds, greens, yellows, blues, whites);
            logger.debug("Created palettes for nord dark theme");
            return palettes;
        }
    }

    ThemePalettes createDraculaPalettes() {
        this.themeColorsByName = createThemeColors("material-dracula.css");
        int[] blacks = new int[]{c("base-5"), c("bg-default")};
        int[] reds = new int[]{c("danger-5"), c("danger-6"), c("danger-7"), c("danger-8")};
        int[] greens = new int[]{c("success-5"), c("success-6"), c("success-7"), c("success-8")};
        int[] yellows = new int[]{c("warning-5"), c("warning-6"), c("warning-7"), c("warning-8")};
        int[] blues = new int[]{c("accent-5"), c("accent-6"), c("accent-7"), c("accent-8")};
        int[] whites = new int[]{c("fg-default"), c("base-1")};
        var palettes = createPalettes(true, blacks, reds, greens, yellows, blues, whites);
        logger.debug("Created palettes for dracula theme");
        return palettes;
    }

    ThemePalettes createJavaFxPalettes() {
        return new ThemePalettes(null, null, null, null, null);
    }

    private ThemePalettes createPalettes(boolean dark, int[] blacks, int[] reds, int[] greens, int[] yellows,
        int[] blues, int[] whites) {
        int[] magentas = new int[]{
            ColorUtils.intermediate(reds[0], blues[0]), ColorUtils.intermediate(reds[1], blues[1]),
            ColorUtils.intermediate(reds[2], blues[2]), ColorUtils.intermediate(reds[3], blues[3])
        };
        int[] cyans = new int[]{
            ColorUtils.intermediate(greens[0], blues[0]), ColorUtils.intermediate(greens[1], blues[1]),
            ColorUtils.intermediate(greens[2], blues[2]), ColorUtils.intermediate(greens[3], blues[3]),
        };
        //palette 16
        var palette16 = new DefaultThemePalette16(new int[]{
            blacks[1], reds[3],  greens[3], yellows[3], blues[3], magentas[3], cyans[3], whites[1],
            //Bright versions of the ISO colors
            blacks[0], reds[0], greens[0], yellows[0], blues[0], magentas[0], cyans[0], whites[0],
        });
        int a;
        int b;
        int c;
        int d;
        //high constrast palette 32
        if (dark) {
            a = 1; b = 0; c = 3; d = 2;
        } else {
            a = 3; b = 2; c = 1; d = 0;
        }
        var highContrastPalette32 = new DefaultThemePalette32(
            new int[]{
                blacks[1], reds[a], greens[a], yellows[a], blues[a], magentas[a], cyans[a], whites[1],
                //Bright versions of the ISO colors
                blacks[0], reds[b], greens[b], yellows[b], blues[b], magentas[b], cyans[b], whites[0],
            },
            new int[]{
                blacks[1], reds[c], greens[c], yellows[c], blues[c], magentas[c], cyans[c], whites[1],
                //Bright versions of the ISO colors
                blacks[0], reds[d], greens[d], yellows[d], blues[d], magentas[d], cyans[d], whites[0],
            }
        );
        //low constrast palette 32
        if (dark) {
            a = 2; b = 0; c = 3; d = 1;
        } else {
            a = 3; b = 1; c = 2; d = 0;
        }
        var lowContrastPalette32 = new DefaultThemePalette32(
            new int[]{
                blacks[1], reds[a], greens[a], yellows[a], blues[a], magentas[a], cyans[a], whites[1],
                //Bright versions of the ISO colors
                blacks[0], reds[b], greens[b], yellows[b], blues[b], magentas[b], cyans[b], whites[0],
            },
            new int[]{
                blacks[1], reds[c], greens[c], yellows[c], blues[c], magentas[c], cyans[c], whites[1],
                //Bright versions of the ISO colors
                blacks[0], reds[d], greens[d], yellows[d], blues[d], magentas[d], cyans[d], whites[0],
            }
        );
        var textPalette = new DefaultThemePalette(this.themeColorsByName);
        var palettes = new ThemePalettes(palette16, highContrastPalette32, lowContrastPalette32,
                textPalette, this.themeColorsByName);
        return palettes;
    }

    private Map<String, Integer> createThemeColors(String baseFile) {
        try {
            //reading atlantafx css file
            var css = readColors(Theme.class, baseFile.substring("material-".length()));
            var atlantaColorsByName = parseColors(css, null);
            //now we add shared colors, because they must override atlantafx colors
            atlantaColorsByName.putAll(sharedColorsByName);
            //reading shellfx base css file
            css = readColors(Stylesheet.class, baseFile);
            var shellThemeColorsByName = parseColors(css, atlantaColorsByName);
            return shellThemeColorsByName;
        } catch (Exception ex) {
            logger.error("Error reading CSS file {}", baseFile, ex);
            return null;
        }
    }

    private String readColors(Class<?> anchorClass, String cssName) throws IOException {
        String packageName = anchorClass.getPackageName();
        Module thatModule = anchorClass.getModule();
        Module thisModule = this.getClass().getModule();
        if (!thatModule.isOpen(packageName, thisModule)) {
            throw new IllegalStateException(StringUtils.format("Module {} didn't open package {} to module {}",
                    thatModule.getName(), packageName, thisModule.getName()));
        }
        String packagePath = packageName.replaceAll(Pattern.quote("."), "/");
        String cssPath = "/" + packagePath + "/" + cssName;
        logger.debug("Resolved CSS file path: {}", cssPath);
        //relative path
        try (InputStream is = thatModule.getResourceAsStream(cssPath);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {
            String content = br.lines().collect(Collectors.joining("\n"));
            return content;
        }
    }

    private Map<String, Integer> parseColors(String cssContent, Map<String, Integer> colorsByName) throws Exception {
        Map<String, Integer> finalColorsByName;
        if (colorsByName == null) {
            finalColorsByName = new HashMap<>();
        } else {
            finalColorsByName = new HashMap<>(colorsByName);
        }
        CascadingStyleSheet css = CSSReader.readFromString(cssContent, StandardCharsets.UTF_8, ECSSVersion.CSS30);
        if (css != null) {
            for (CSSStyleRule rule : css.getAllStyleRules()) {
                if (rule.getSelectorCount() == 1 && rule.getSelectorAtIndex(0).getAsCSSString().equals(".root")) {
                    for (CSSDeclaration decl : rule.getAllDeclarations()) {
                        if (decl.getProperty().startsWith("-color-")) {
                            var value = decl.getExpressionAsCSSString();
                            if (value.startsWith("-color-")) {
                                //reference to another color
                                finalColorsByName.put(decl.getProperty(), finalColorsByName.get(value));
                            } else {
                                var color = Color.web(value);
                                var colorStr = color.toString();
                                if (colorStr.endsWith("FF")) {
                                    finalColorsByName.put(decl.getProperty(),
                                            Integer.parseUnsignedInt(colorStr.substring(2), 16));
                                } else {
                                    finalColorsByName.put(decl.getProperty(),
                                            Integer.parseUnsignedInt(colorStr.substring(2, 8), 16));
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return finalColorsByName;
    }

    private int c(String name) {
        return colorFromMap(this.themeColorsByName, name);
    }
}
