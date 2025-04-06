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

package com.techsenger.tabshell.demos.core;

import com.techsenger.tabshell.core.settings.AbstractAppearanceSettings;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.core.settings.ViewerSettings;
import com.techsenger.tabshell.core.theme.TabShellTheme;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
class DemoSettings implements Settings {

    @Override
    public ViewerSettings getViewer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static final class DemoAppearanceSettings extends AbstractAppearanceSettings {

        DemoAppearanceSettings() {
            super(Font.font("Ubuntu", 14), Font.font("Ubuntu Mono", 14));
            setTheme(TabShellTheme.CUPERTINO_DARK);
        }
    }

    private final AppearanceSettings appearance = new DemoAppearanceSettings();

    @Override
    public AppearanceSettings getAppearance() {
        return appearance;
    }
}
