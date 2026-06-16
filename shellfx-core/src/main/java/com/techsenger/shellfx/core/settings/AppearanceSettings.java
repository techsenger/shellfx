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
import com.techsenger.shellfx.material.theme.Theme;
import javafx.scene.text.Font;

/**
 * Provides access to the application-wide appearance settings, including density, theme, and fonts. Changes to
 * these settings are propagated to all windows. Settings can be observed for changes via subscription callbacks.
 *
 * @author Pavel Castornii
 */
public interface AppearanceSettings {

    /**
     * Returns the density of the application UI, which controls the sizing of paddings, gaps, and similar spacing
     * properties of UI components.
     *
     * @return the current density value
     */
    @Nullable Density getDensity();

    /**
     * Sets the density of the application UI.
     *
     * @param density the density value to apply
     */
    void setDensity(@Nullable Density density);

    /**
     * Registers a callback to be invoked when the density changes.
     *
     * @param callback the callback to invoke with the new density value
     * @return a {@link SettingsSubscription} that can be used to unsubscribe
     */
    SettingsSubscription onDensityChanged(SettingsCallback<@Nullable Density> callback);

    /**
     * Returns the current application theme.
     *
     * @return the current {@link Theme}
     */
    Theme getTheme();

    /**
     * Sets the application theme.
     *
     * @param theme the {@link Theme} to apply
     */
    void setTheme(Theme theme);

    /**
     * Registers a callback to be invoked when the theme changes.
     *
     * @param callback the callback to invoke with the new {@link Theme}
     * @return a {@link SettingsSubscription} that can be used to unsubscribe
     */
    SettingsSubscription onThemeChanged(SettingsCallback<Theme> callback);

    /**
     * Returns the regular font used for general UI text.
     *
     * @return the current regular {@link Font}
     */
    Font getRegularFont();

    /**
     * Sets the regular font used for general UI text.
     *
     * @param font the {@link Font} to apply
     */
    void setRegularFont(Font font);

    /**
     * Registers a callback to be invoked when the regular font changes.
     *
     * @param callback the callback to invoke with the new regular {@link Font}
     * @return a {@link SettingsSubscription} that can be used to unsubscribe
     */
    SettingsSubscription onRegularFontChanged(SettingsCallback<Font> callback);

    /**
     * Returns the monospace font used for displaying code or terminal-style text.
     *
     * @return the current monospace {@link Font}
     */
    Font getMonospaceFont();

    /**
     * Sets the monospace font used for displaying code or terminal-style text.
     *
     * @param font the {@link Font} to apply
     */
    void setMonospaceFont(Font font);

    /**
     * Registers a callback to be invoked when the monospace font changes.
     *
     * @param callback the callback to invoke with the new monospace {@link Font}
     * @return a {@link SettingsSubscription} that can be used to unsubscribe
     */
    SettingsSubscription onMonospaceFontChanged(SettingsCallback<Font> callback);
}
