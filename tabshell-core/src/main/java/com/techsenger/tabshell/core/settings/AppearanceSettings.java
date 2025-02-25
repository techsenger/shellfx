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

import com.techsenger.tabshell.core.theme.TabShellTheme;
import javafx.beans.property.ObjectProperty;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public interface AppearanceSettings {

    ObjectProperty<TabShellTheme> themeProperty();

    TabShellTheme getTheme();

    void setTheme(TabShellTheme theme);

    /**
     * Regular font that used for UI etc.
     *
     * @return
     */
    ObjectProperty<Font> regularFontProperty();

    Font getRegularFont();

    void setRegularFont(Font font);

    /**
     * Monospace font for displaying code or terminal-style text.
     *
     * @return
     */
    ObjectProperty<Font> monospaceFontProperty();

    Font getMonospaceFont();

    void setMonospaceFont(Font font);
}
