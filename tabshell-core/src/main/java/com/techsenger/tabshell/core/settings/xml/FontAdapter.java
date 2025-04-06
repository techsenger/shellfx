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

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class FontAdapter extends XmlAdapter<XmlFontSettings, Font> {

    @Override
    public Font unmarshal(XmlFontSettings s) {
        return Font.font(s.getFamily(), s.getSize());
    }

    @Override
    public XmlFontSettings marshal(Font v) {
        return new XmlFontSettings(v.getFamily(), v.getSize());
    }
}
