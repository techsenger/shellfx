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

package com.techsenger.tabshell.core.settings;

import com.techsenger.tabshell.core.theme.ShellTheme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAppearanceSettings implements AppearanceSettings {

    private final ObjectProperty<ShellTheme> theme = new SimpleObjectProperty<>();

    private final ObjectProperty<Font> regularFont = new SimpleObjectProperty<>();

    private final ObjectProperty<Font> monospaceFont = new SimpleObjectProperty<>();

    public AbstractAppearanceSettings() {

    }

    public AbstractAppearanceSettings(Font regularFont, Font monospaceFont) {
        setRegularFont(regularFont);
        setMonospaceFont(monospaceFont);
    }

    @Override
    public ObjectProperty<ShellTheme> themeProperty() {
        return theme;
    }

    @Override
    public ShellTheme getTheme() {
        return theme.get();
    }

    @Override
    public void setTheme(ShellTheme theme) {
        this.theme.set(theme);
    }

    @Override
    public ObjectProperty<Font> regularFontProperty() {
        return regularFont;
    }

    @Override
    public Font getRegularFont() {
        return regularFont.get();
    }

    @Override
    public void setRegularFont(Font font) {
        this.regularFont.set(font);
    }

    @Override
    public ObjectProperty<Font> monospaceFontProperty() {
        return monospaceFont;
    }

    @Override
    public Font getMonospaceFont() {
        return monospaceFont.get();
    }

    @Override
    public void setMonospaceFont(Font font) {
        this.monospaceFont.set(font);
    }
}
