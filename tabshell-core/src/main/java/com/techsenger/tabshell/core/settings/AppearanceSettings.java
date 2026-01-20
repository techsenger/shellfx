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
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public interface AppearanceSettings {

    Theme getTheme();

    void setTheme(Theme theme);

    SettingsSubscription observeTheme(SettingsObserver<Theme> observer);

    Font getRegularFont();

    void setRegularFont(Font font);

    /**
     * Regular font that used for UI etc.
     *
     * @return
     */
    SettingsSubscription observeRegularFont(SettingsObserver<Font> observer);

    Font getMonospaceFont();

    void setMonospaceFont(Font font);

    /**
     * Monospace font for displaying code or terminal-style text.
     *
     * @return
     */
    SettingsSubscription observeMonospaceFont(SettingsObserver<Font> observer);
}
