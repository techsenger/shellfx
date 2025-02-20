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

package com.techsenger.tabshell.kit.core.settings;

import com.techsenger.tabshell.core.theme.TabShellTheme;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public class AppearanceSettings implements com.techsenger.tabshell.core.settings.AppearanceSettings {

    private ObjectProperty<TabShellTheme> theme = new SimpleObjectProperty<>();

    @XmlElement(name = "Font")
    private FontSettings font;

    public AppearanceSettings() {

    }

    public AppearanceSettings(FontSettings font) {
        this.font = font;
    }

    @Override
    public ObjectProperty<TabShellTheme> themeProperty() {
        return theme;
    }

    @Override
    public TabShellTheme getTheme() {
        return theme.get();
    }

    @XmlAttribute(name = "theme")
    @XmlJavaTypeAdapter(ThemeAdapter.class)
    public void setTheme(TabShellTheme theme) {
        this.theme.set(theme);
    }

    @Override
    public FontSettings getFont() {
        return font;
    }
}
