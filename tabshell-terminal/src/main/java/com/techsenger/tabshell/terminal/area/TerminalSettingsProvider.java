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

package com.techsenger.tabshell.terminal.area;

import com.techsenger.jeditermfx.core.HyperlinkStyle;
import com.techsenger.jeditermfx.core.TerminalColor;
import com.techsenger.jeditermfx.core.TextStyle;
import com.techsenger.jeditermfx.core.emulator.ColorPalette;
import com.techsenger.jeditermfx.ui.FxTransformers;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import com.techsenger.toolkit.fx.color.ColorUtils;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
class TerminalSettingsProvider extends DefaultSettingsProvider {

    private final Font monospaceFont;

    private final TerminalPalette terminalPalette;

    TerminalSettingsProvider(Font monospaceFont, TerminalPalette themePalette) {
        this.monospaceFont = monospaceFont;
        this.terminalPalette = themePalette;
    }

    @Override
    public ColorPalette getTerminalColorPalette() {
        return terminalPalette;
    }

    @Override
    public float getTerminalFontSize() {
        return (float) this.monospaceFont.getSize();
    }

    @Override
    public Font getTerminalFont() {
        return monospaceFont;
    }

//    @Override
//    public float getLineSpacing() {
//        return 1.05f;
//    }

    @Override
    public TextStyle getSelectionColor() {
        var fxFgColor = ColorUtils.toColor(terminalPalette.getTextPalette().getSelectionFgColor());
        var terminalFgColor = FxTransformers.fromFxToTerminalColor(fxFgColor);
        var fxBgColor = ColorUtils.toColor(terminalPalette.getTextPalette().getSelectionBgColor());
        var terminalBgColor = FxTransformers.fromFxToTerminalColor(fxBgColor);
        return new TextStyle(terminalFgColor, terminalBgColor);
    }

    @Override
    public boolean useInverseSelectionColor() {
        return false;
    }

    @Override
    public TextStyle getFoundPatternColor() {
        var bgColor = ColorUtils.toColor(terminalPalette.getFoundBackground());
        var fgColor = ColorUtils.toColor(terminalPalette.getFoundForeground());
        return new TextStyle(FxTransformers.fromFxToTerminalColor(fgColor),
                FxTransformers.fromFxToTerminalColor(bgColor));
    }

    @Override
    public TextStyle getHyperlinkColor() {
        return new TextStyle(new TerminalColor(12), new TerminalColor(0));
    }

    public HyperlinkStyle.HighlightMode getHyperlinkHighlightingMode() {
        return HyperlinkStyle.HighlightMode.HOVER_WITH_ORIGINAL_COLOR;
    }

    @Override
    public TerminalColor getDefaultBackground() {
        return terminalPalette.getDefaultBackgroundColor();
    }

    @Override
    public TerminalColor getDefaultForeground() {
        return terminalPalette.getDefaultForegroundColor();
    }
}
