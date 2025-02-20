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

package com.techsenger.tabshell.kit.terminal;

import com.techsenger.jeditermfx.core.Color;
import com.techsenger.jeditermfx.core.TerminalColor;
import com.techsenger.jeditermfx.core.TextStyle;
import com.techsenger.jeditermfx.core.emulator.ColorPalette;
import com.techsenger.tabshell.core.theme.TabShellTheme;
import com.techsenger.tabshell.core.theme.ThemePalette;

/**
 * The main purpose of this terminal is not run TUI applications, but to execute some commands because there is no
 * sense to run commander and use its terminal to run a TUI application, because it is much easier to run
 * terminal and start a TUI application there. So, colors firstly must be good for running terminal commands.
 *
 * @author Pavel Castornii
 */
class TerminalPalette extends ColorPalette {

    private Color[] fgColors;

    private Color[] bgColors;

    private TabShellTheme theme;

    private TerminalPaletteType paletteType;

    private int fgColorIndex;

    private int bgColorIndex;

    private ThemePalette textPalette;

    TerminalPalette(TabShellTheme theme, TerminalPaletteType paletteType) {
        this.paletteType = paletteType;
        setTheme(theme);
    }

    @Override
    public Color getForegroundByColorIndex(int colorIndex) {
        return fgColors[colorIndex];
    }

    @Override
    protected Color getBackgroundByColorIndex(int colorIndex) {
        return bgColors[colorIndex];
    }

    /**
     * We assume, that theme is always changed on JavaFX thread.
     * @param theme
     */
    public void setTheme(TabShellTheme theme) {
        this.theme = theme;
        if (theme.isDark()) {
            fgColorIndex = 15;
            bgColorIndex = 0;
        } else {
            fgColorIndex = 0;
            bgColorIndex = 15;
        }
        this.textPalette = theme.getPalette();
        this.createColors();
    }

    public TextStyle getDefaultStyle() {
        return new TextStyle(new TerminalColor(fgColorIndex), new TerminalColor(bgColorIndex));
    }

    public ThemePalette getTextPalette() {
        return textPalette;
    }

    public void setPaletteType(TerminalPaletteType paletteType) {
        this.paletteType = paletteType;
        this.createColors();
    }

    public int getFoundBackground() {
        return theme.getPalette().getFoundBgColor();
    }

    public int getFoundForeground() {
        return theme.getPalette().getFoundFgColor();
    }

    private void createColors() {
        this.fgColors =  new Color[16];
        this.bgColors =  new Color[16];
        int[] fg;
        int[] bg;
        switch (this.paletteType) {
            case THEME_16:
                fg = theme.getSimplePalette16().getColors();
                bg = fg;
            break;
            case THEME_32_HC:
                fg = theme.getHighContrastPalette32().getFgColors();
                bg = theme.getHighContrastPalette32().getBgColors();
            break;
            case THEME_32_LC:
                fg = theme.getLowContrastPalette32().getFgColors();
                bg = theme.getLowContrastPalette32().getBgColors();
            break;
            default:
                throw new AssertionError();
        }
        for (var i = 0; i < 16; i++) {
            this.fgColors[i] = new Color(fg[i]);
            this.bgColors[i] = new Color(bg[i]);
        }
    }
}
