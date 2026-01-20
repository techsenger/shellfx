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

import com.techsenger.tabshell.material.theme.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAppearanceSettings implements AppearanceSettings {

    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>();

    private final ObjectProperty<Font> regularFont = new SimpleObjectProperty<>();

    private final ObjectProperty<Font> monospaceFont = new SimpleObjectProperty<>();

    public AbstractAppearanceSettings() {

    }

    public AbstractAppearanceSettings(Font regularFont, Font monospaceFont) {
        setRegularFont(regularFont);
        setMonospaceFont(monospaceFont);
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
    public SettingsSubscription observeTheme(SettingsObserver<Theme> observer) {
        return SubscriptionUtils.observe(theme, observer);
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
    public SettingsSubscription observeRegularFont(SettingsObserver<Font> observer) {
        return SubscriptionUtils.observe(regularFont, observer);
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
    public SettingsSubscription observeMonospaceFont(SettingsObserver<Font> observer) {
        return SubscriptionUtils.observe(monospaceFont, observer);
    }
}
