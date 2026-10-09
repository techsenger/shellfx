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

package com.techsenger.shellfx.dialogs.text;

import com.techsenger.shellfx.material.style.Spacing;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class TextDialogView<VM extends TextDialogViewModel<?>> extends AbstractTextDialogView<VM> {

    private final TextField textField = new TextField();

    private final HBox textBox = new HBox(getLabel(), textField);

    public TextDialogView(VM viewModel) {
        super(viewModel);
    }

    public TextDialogView(VM viewModel, String labelText) {
        super(viewModel, labelText);
    }

    @Override
    protected void buildContent() {
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setMaxWidth(Double.MAX_VALUE);
        textBox.setSpacing(Spacing.getHorizontal());
        textBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(textBox, Priority.NEVER);
        getContentBox().getChildren().add(textBox);
        getContentBox().setSpacing(Spacing.getVertical());
    }

    protected TextField getTextField() {
        return textField;
    }

    protected HBox getTextBox() {
        return textBox;
    }

    @Override
    TextInputControl getInput() {
        return textField;
    }
}
