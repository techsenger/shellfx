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

package com.techsenger.shellfx.material.button;

import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.MaterialIcons;
import javafx.scene.control.MenuButton;

/**
 * The standard menu button through which components expose developer-only actions built under
 * {@code com.techsenger.shellfx.core.SystemProperties#DEV_MODE} (e.g. opening a dialog directly, with dummy
 * data, for styling/layout work) &mdash; one recognizable place and one recognizable look across the whole
 * application, instead of each component inventing its own. Items are added via the inherited
 * {@link #getItems()}, as with any {@link MenuButton}.
 *
 * @author Pavel Castornii
 */
public class DevToolsButton extends MenuButton {

    private static final String STYLE_CLASS = "dev-tools-button";

    public DevToolsButton() {
        super(null, new FontIconView(MaterialIcons.DEV_TOOLS));
        var css = DevToolsButton.class.getResource("dev-tools-button.css").toExternalForm();
        getStylesheets().add(css);
        getStyleClass().addAll(STYLE_CLASS);
    }
}
