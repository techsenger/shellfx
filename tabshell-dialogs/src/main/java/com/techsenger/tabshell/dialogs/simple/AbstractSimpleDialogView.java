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

package com.techsenger.tabshell.dialogs.simple;

import com.techsenger.tabshell.core.ActionUtils;
import com.techsenger.tabshell.core.dialog.AbstractDialogView;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * This dialog has two buttons - cancel and ok.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSimpleDialogView<T extends AbstractSimpleDialogViewModel<?>,
        S extends AbstractSimpleDialogComponent<?>> extends AbstractDialogView<T, S> {

    private final Button okButton = new Button();

    private final Button cancelButton = new Button();

    private final HBox buttonBox = new HBox();

    public AbstractSimpleDialogView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected void build() {
        super.build();
        this.buttonBox.getStyleClass().add(StyleClasses.CORNERS_BOTTOM);
        this.buttonBox.setPadding(new Insets(SizeConstants.INSET));
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setSpacing(SizeConstants.INSET);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        okButton.disableProperty().bind(viewModel.okDisableProperty());
        okButton.defaultButtonProperty().bind(viewModel.okDefault());
        okButton.textProperty().bind(viewModel.okTextProperty());
        cancelButton.disableProperty().bind(viewModel.cancelDisableProperty());
        cancelButton.defaultButtonProperty().bind(viewModel.cancelDefaultProperty());
        cancelButton.visibleProperty().bind(viewModel.cancelVisibleProperty());
        cancelButton.textProperty().bind(viewModel.cancelTextProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        okButton.setOnAction(e -> ActionUtils.runIfExists(viewModel.okActionProperty()));
        cancelButton.setOnAction(e -> ActionUtils.runIfExists(viewModel.cancelActionProperty()));
    }

    protected Button getOkButton() {
        return okButton;
    }

    protected Button getCancelButton() {
        return cancelButton;
    }

    protected HBox getButtonBox() {
        return buttonBox;
    }
}
