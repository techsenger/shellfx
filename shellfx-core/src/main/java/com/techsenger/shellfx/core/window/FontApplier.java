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

package com.techsenger.shellfx.core.window;

import com.techsenger.shellfx.material.style.StyleUtils;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class FontApplier {

    private final Pane root;

    private String monoStylesheet;

    public FontApplier(Pane root) {
        this.root = root;
    }

    public void setRegularFont(Font font) {
        root.setStyle(StyleUtils.toStyle(font));
    }

    public void setMonospaceFont(Font font) {
        if (monoStylesheet != null) {
            root.getStylesheets().remove(monoStylesheet);
        }
        monoStylesheet = "data:text/css, .monospace {" + StyleUtils.toStyle(font) + "}";
        root.getStylesheets().add(monoStylesheet);
    }
}
