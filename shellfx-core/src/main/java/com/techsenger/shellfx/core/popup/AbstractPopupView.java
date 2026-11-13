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
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.material.pane.WaitingPane;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.Objects;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Window;

/**
 * The base view of a popup: a bare window that can wait. A nested one is placed by anchors, a top-level one is shown
 * by the view itself at a point on the screen.
 *
 * @param <VM> the view model of the popup
 * @author Pavel Castornii
 */
public abstract class AbstractPopupView<VM extends AbstractPopupViewModel<?>> extends AbstractWindowView<VM>
        implements PopupView<VM> {

    private final @Nullable Window owner;

    /**
     * Creates a new nested popup, or one backed by the given JavaFX popup.
     *
     * @param viewModel the view model of this popup
     * @param stylesheetRegistry the registry of the stylesheets that every window applies
     * @param popup the JavaFX popup that backs a top-level popup; {@code null} for a nested one
     */
    public AbstractPopupView(VM viewModel, StylesheetRegistry stylesheetRegistry, @Nullable Popup popup) {
        super(viewModel, stylesheetRegistry, popup);
        this.owner = null;
    }

    /**
     * Creates a new top-level popup that shows itself when its view model becomes visible and hides itself when the
     * view model becomes invisible. It is shown at the {@code x} and {@code y} of its view model.
     *
     * @param viewModel the view model of this popup
     * @param stylesheetRegistry the registry of the stylesheets that every window applies
     * @param owner the window the popup is shown on
     */
    public AbstractPopupView(VM viewModel, StylesheetRegistry stylesheetRegistry, Window owner) {
        super(viewModel, stylesheetRegistry, new Popup());
        this.owner = owner;
        getWindow().getContent().add(getNode());
    }

    @Override
    public Popup getWindow() {
        return (Popup) super.getWindow();
    }

    @Override
    protected void build() {
        super.build();
        // a popup is placed by anchors, so unlike a window its node must be free to stretch between them
        getWindowNode().setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        getWindowNode().setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        getContentBox().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        var viewModel = getViewModel();
        WaitingPane.install(getWindowNode(), viewModel.waitingProperty());
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            // the JavaFX popup hides itself, which the view model sees as the visible property becoming false
            getWindow().autoHideProperty().bind(viewModel.autoHideProperty());
        }
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            var popup = getWindow();
            var popupOwner = Objects.requireNonNull(owner, "A top-level popup needs an owner");
            ValueUtils.callAndAddListener(viewModel.visibleProperty(), (ov, oldV, newV) -> {
                if (newV) {
                    popup.show(popupOwner);
                } else {
                    popup.hide();
                }
            });
        }
    }
}
