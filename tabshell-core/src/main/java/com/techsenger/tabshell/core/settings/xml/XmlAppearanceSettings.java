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

package com.techsenger.tabshell.core.settings.xml;

import com.techsenger.tabshell.core.settings.AbstractAppearanceSettings;
import com.techsenger.tabshell.material.theme.Theme;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javafx.scene.text.Font;

/**
 * Getters are overridden because, otherwise, JAXB does not recognize them when setters are overridden.
 *
 * @author Pavel Castornii
 */
public class XmlAppearanceSettings extends AbstractAppearanceSettings {

    public XmlAppearanceSettings() {

    }

    public XmlAppearanceSettings(Font regularFont, Font monospaceFont) {
        super(regularFont, monospaceFont);
    }

    @XmlAttribute(name = "theme")
    @XmlJavaTypeAdapter(ThemeAdapter.class)
    public void setTheme(Theme theme) {
        super.setTheme(theme);
    }

    @Override
    public Theme getTheme() {
        return super.getTheme();
    }

    @Override
    @XmlElement(name = "RegularFont")
    @XmlJavaTypeAdapter(FontAdapter.class)
    public void setRegularFont(Font regularFont) {
        super.setRegularFont(regularFont);
    }

    @Override
    public Font getRegularFont() {
        return super.getRegularFont();
    }

    @Override
    @XmlElement(name = "MonospaceFont")
    @XmlJavaTypeAdapter(FontAdapter.class)
    public void setMonospaceFont(Font monospaceFont) {
        super.setMonospaceFont(monospaceFont);
    }

    @Override
    public Font getMonospaceFont() {
        return super.getMonospaceFont();
    }
}
