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
public class TextEditorDialogView<VM extends TextEditorDialogViewModel<?>> extends AbstractEditorDialogView<VM> {

    private TextField textField = createTextField();

    public TextEditorDialogView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        this.textField.requestFocus();
    }

    @Override
    protected void build() {
        super.build();
        HBox.setHgrow(textField, Priority.ALWAYS);
        getRowBox().getChildren().add(textField);
    }

    @Override
    protected void bind() {
        super.bind();
        textField.textProperty().bindBidirectional(getViewModel().valueProperty());
    }
}
