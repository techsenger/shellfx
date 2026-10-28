/*
 * Copyright 2026 Pavel Castornii.
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

package com.techsenger.shellfx.storage.settings;

/**
 * The style of a kind of a file, or a mask of a state that is laid over such a style. A {@code null} value means that
 * the value is inherited from the style underneath. The colors are 0xRRGGBBAA: a color of a mask is composited over
 * the color underneath by its alpha, so the alpha FF replaces the color and a smaller alpha tints or dims it.
 *
 * @author Pavel Castornii
 */
public final class FileStyleSettings {

    private IconStyleSettings icon = new IconStyleSettings();

    private TextStyleSettings text = new TextStyleSettings();

    public IconStyleSettings getIcon() {
        return this.icon;
    }

    public void setIcon(IconStyleSettings icon) {
        this.icon = icon;
    }

    public TextStyleSettings getText() {
        return this.text;
    }

    public void setText(TextStyleSettings text) {
        this.text = text;
    }
}
