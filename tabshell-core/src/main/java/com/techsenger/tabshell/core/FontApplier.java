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

import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.material.style.StyleUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.layout.Pane;

/**
 *
 * @author Pavel Castornii
 */
public class FontApplier {

    private String monoStylesheet;

    public FontApplier(Pane root, AppearanceSettings settings) {
        ValueUtils.callAndAddListener(settings.regularFontProperty(), (ov, t, t1) -> {
            root.setStyle(StyleUtils.toStyle(t1));
        });
        ValueUtils.callAndAddListener(settings.monospaceFontProperty(), (ov, t, t1) -> {
            if (monoStylesheet != null) {
                root.getStylesheets().remove(monoStylesheet);
            }
            monoStylesheet = "data:text/css, .monospace {" + StyleUtils.toStyle(t1) + "}";
            root.getStylesheets().add(monoStylesheet);
        });
    }
}
