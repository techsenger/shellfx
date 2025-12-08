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

package com.techsenger.tabshell.demos.core.settings;

import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.core.settings.xml.XmlAppearanceSettings;
import com.techsenger.tabshell.core.settings.xml.XmlSettings;
import com.techsenger.tabshell.core.settings.xml.XmlTabSymbolSettings;
import com.techsenger.tabshell.core.settings.xml.XmlViewerSettings;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public final class DemoSettings {

    public static Settings createSettings() {
        var regularFont = Font.font("System", 14);
        var monospaceFont = Font.font("Monospace", 14);
        var tabSymbol = new XmlTabSymbolSettings();
        tabSymbol.setSize(4);
        tabSymbol.setUseSpaces(true);
        var appearance = new XmlAppearanceSettings(regularFont, monospaceFont);
        appearance.setTheme(AtlantaFxTheme.CUPERTINO_DARK);
        var viewer = new XmlViewerSettings(monospaceFont, tabSymbol);
        var settings = new XmlSettings(appearance, viewer);
        return settings;
    }

    private DemoSettings() {
        //empty
    }
}
