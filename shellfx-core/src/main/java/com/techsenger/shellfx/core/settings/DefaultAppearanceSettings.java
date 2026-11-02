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

package com.techsenger.shellfx.core.settings;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.Theme;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultAppearanceSettings implements AppearanceSettings {

    private @Nullable Density density;

    private Theme theme;

    private Font regularFont;

    private Font monospaceFont;

    public DefaultAppearanceSettings() {

    }

    public DefaultAppearanceSettings(@Nullable Density density, Font regularFont, Font monospaceFont) {
        setRegularFont(regularFont);
        setMonospaceFont(monospaceFont);
    }

    @Override
    public @Nullable Density getDensity() {
        return this.density;
    }

    @Override
    public void setDensity(@Nullable Density density) {
        this.density = density;
    }

    @Override
    public Theme getTheme() {
        return theme;
    }

    @Override
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    @Override
    public Font getRegularFont() {
        return regularFont;
    }

    @Override
    public void setRegularFont(Font font) {
        this.regularFont = font;
    }

    @Override
    public Font getMonospaceFont() {
        return monospaceFont;
    }

    @Override
    public void setMonospaceFont(Font font) {
        this.monospaceFont = font;
    }
}
