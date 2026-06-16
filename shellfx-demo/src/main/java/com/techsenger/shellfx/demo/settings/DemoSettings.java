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

package com.techsenger.shellfx.demo.settings;

import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.settings.DefaultAppearanceSettings;
import com.techsenger.shellfx.core.settings.Density;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.material.theme.AtlantaFxTheme;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public final class DemoSettings {

    public static ShellSettings createSettings() {
        var regularFont = Font.font("System", 14);
        var monospaceFont = Font.font("Monospace", 14);
        var appearance = new DefaultAppearanceSettings(null, regularFont, monospaceFont);
        appearance.setTheme(AtlantaFxTheme.CUPERTINO_DARK);
        appearance.setDensity(Density.S);
        var settings = new ShellSettings() {

            @Override
            public AppearanceSettings getAppearance() {
                return appearance;
            }

        };
        return settings;
    }

    private DemoSettings() {
        //empty
    }
}
