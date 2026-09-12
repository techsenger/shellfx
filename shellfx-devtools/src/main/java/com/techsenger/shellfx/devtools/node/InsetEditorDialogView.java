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

package com.techsenger.shellfx.devtools.node;

import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public class InsetEditorDialogView<VM extends InsetEditorDialogViewModel<?>> extends AbstractEditorDialogView<VM> {

    private final TextField topTextField = new TextField();

    private final TextField rightTextField = new TextField();

    private final TextField bottomTextField = new TextField();

    private final TextField leftTextField = new TextField();

    public InsetEditorDialogView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        HBox.setHgrow(topTextField, Priority.ALWAYS);
        HBox.setHgrow(rightTextField, Priority.ALWAYS);
        HBox.setHgrow(bottomTextField, Priority.ALWAYS);
        HBox.setHgrow(leftTextField, Priority.ALWAYS);

        getRowBox().getChildren().addAll(topTextField, rightTextField, bottomTextField, leftTextField);
    }

    @Override
    protected void bind() {
        super.bind();
        topTextField.textProperty().bindBidirectional(getViewModel().topProperty());
        rightTextField.textProperty().bindBidirectional(getViewModel().rightProperty());
        bottomTextField.textProperty().bindBidirectional(getViewModel().bottomProperty());
        leftTextField.textProperty().bindBidirectional(getViewModel().leftProperty());
    }
}
