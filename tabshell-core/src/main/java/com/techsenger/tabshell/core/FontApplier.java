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

package com.techsenger.tabshell.core;

import com.techsenger.tabshell.material.style.StyleUtils;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class FontApplier {

    private String monoStylesheet;

    public FontApplier(Pane root, ObjectProperty<Font> regularFont, ObjectProperty<Font> monospaceFont) {
        updateRegularFont(root, regularFont.get());
        regularFont.addListener((ov, oldV, newV) -> updateRegularFont(root, newV));
        updateMonospaceFont(root, monospaceFont.get());
        monospaceFont.addListener((ov, oldV, newV) -> updateMonospaceFont(root, newV));
    }

    private void updateRegularFont(Pane root, Font font) {
        root.setStyle(StyleUtils.toStyle(font));
    }

    private void updateMonospaceFont(Pane root, Font font) {
        if (monoStylesheet != null) {
            root.getStylesheets().remove(monoStylesheet);
        }
        monoStylesheet = "data:text/css, .monospace {" + StyleUtils.toStyle(font) + "}";
        root.getStylesheets().add(monoStylesheet);
    }
}
