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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class ViewerSettings {

    private ObjectProperty<Font> font = new SimpleObjectProperty<>();

    @XmlElement(name = "TabSymbol")
    private TabSymbolSettings tabSymbol;

    public ViewerSettings() {

    }

    public ViewerSettings(Font font, TabSymbolSettings tabSymbol) {
        setFont(font);
        this.tabSymbol = tabSymbol;
    }

    public ObjectProperty<Font> fontProperty() {
        return font;
    }

    public Font getFont() {
        return font.get();
    }

    @XmlElement(name = "Font")
    @XmlJavaTypeAdapter(FontAdapter.class)
    public void setFont(Font font) {
        this.font.set(font);
    }

    public TabSymbolSettings getTabSymbol() {
        return tabSymbol;
    }
}
