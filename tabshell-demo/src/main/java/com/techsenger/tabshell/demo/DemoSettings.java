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

package com.techsenger.tabshell.demo;

import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.core.settings.FontSettings;
import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.core.theme.TabShellTheme;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
class DemoSettings implements Settings {

    private static final class DemoFontSettings implements FontSettings {

        private final IntegerProperty size = new SimpleIntegerProperty(14);

        @Override
        public IntegerProperty sizeProperty() {
            return size;
        }

        @Override
        public int getSize() {
            return size.get();
        }
    }

    private static final class DemoAppearanceSettings implements AppearanceSettings {

        private final FontSettings font = new DemoFontSettings();

        private ObjectProperty<TabShellTheme> theme = new SimpleObjectProperty<>(TabShellTheme.CUPERTINO_DARK);

        @Override
        public ObjectProperty<TabShellTheme> themeProperty() {
            return theme;
        }

        @Override
        public TabShellTheme getTheme() {
            return theme.get();
        }

        @Override
        public FontSettings getFont() {
            return this.font;
        }

    }

    private final AppearanceSettings appearance = new DemoAppearanceSettings();

    @Override
    public AppearanceSettings getAppearance() {
        return appearance;
    }
}
