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

package com.techsenger.tabshell.core.settings;

import com.techsenger.annotations.Nullable;
import com.techsenger.tabshell.material.theme.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultAppearanceSettings implements AppearanceSettings {

    private final ObjectProperty<String> density = new SimpleObjectProperty<>();

    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>();

    private final ObjectProperty<Font> regularFont = new SimpleObjectProperty<>();

    private final ObjectProperty<Font> monospaceFont = new SimpleObjectProperty<>();

    public DefaultAppearanceSettings() {

    }

    public DefaultAppearanceSettings(@Nullable String density, Font regularFont, Font monospaceFont) {
        setRegularFont(regularFont);
        setMonospaceFont(monospaceFont);
    }

    @Override
    public String getDensity() {
        return this.density.get();
    }

    @Override
    public void setDensity(String density) {
        this.density.set(density);
    }

    @Override
    public SettingsSubscription onDensityChanged(SettingsCallback<String> callback) {
        return SubscriptionUtils.onChanged(density, callback);
    }

    @Override
    public Theme getTheme() {
        return theme.get();
    }

    @Override
    public void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    @Override
    public SettingsSubscription onThemeChanged(SettingsCallback<Theme> callback) {
        return SubscriptionUtils.onChanged(theme, callback);
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
    public SettingsSubscription onRegularFontChanged(SettingsCallback<Font> callback) {
        return SubscriptionUtils.onChanged(regularFont, callback);
    }

    @Override
    public Font getMonospaceFont() {
        return monospaceFont.get();
    }

    @Override
    public void setMonospaceFont(Font font) {
        this.monospaceFont.set(font);
    }

    @Override
    public SettingsSubscription onMonospaceFontChanged(SettingsCallback<Font> callback) {
        return SubscriptionUtils.onChanged(monospaceFont, callback);
    }
}
