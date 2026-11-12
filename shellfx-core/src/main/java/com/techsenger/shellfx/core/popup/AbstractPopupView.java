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

package com.techsenger.shellfx.core.popup;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.window.AbstractWindowView;
import com.techsenger.shellfx.material.pane.WaitingPane;
import javafx.scene.layout.Region;
import javafx.stage.Window;

/**
 * The base view of a popup: a bare nested window that is placed by anchors and can wait.
 *
 * @param <VM> the view model of the popup
 * @author Pavel Castornii
 */
public abstract class AbstractPopupView<VM extends AbstractPopupViewModel<?>> extends AbstractWindowView<VM>
        implements PopupView<VM> {

    /**
     * Creates a new popup backed by the given JavaFX window.
     *
     * @param viewModel the view model of this popup
     * @param stylesheetRegistry the registry of the stylesheets that every window applies
     * @param window the JavaFX window that backs a top-level popup; {@code null} for a nested one
     */
    public AbstractPopupView(VM viewModel, StylesheetRegistry stylesheetRegistry, @Nullable Window window) {
        super(viewModel, stylesheetRegistry, window);
    }

    @Override
    protected void build() {
        super.build();
        // a popup is placed by anchors, so unlike a window its node must be free to stretch between them
        getWindowNode().setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        getWindowNode().setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        getContentBox().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        WaitingPane.install(getWindowNode(), getViewModel().waitingProperty());
    }
}
