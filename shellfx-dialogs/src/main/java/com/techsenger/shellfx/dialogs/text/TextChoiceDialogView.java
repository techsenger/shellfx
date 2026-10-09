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

import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class TextChoiceDialogView<VM extends TextChoiceDialogViewModel<?>> extends AbstractDialogView<VM> {

    private final Label label = new Label("Text");

    private final ComboBox<String> comboBox = new ComboBox<>();

    private final HBox textBox = new HBox(label, comboBox);

    private final ResultButton cancelButton = new ResultButton(TextDialogButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(TextDialogButtons.OK, "OK");

    public TextChoiceDialogView(VM viewModel) {
        super(viewModel);
    }

    public TextChoiceDialogView(VM viewModel, String labelText) {
        super(viewModel);
        label.setText(labelText);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(comboBox);
    }

    @Override
    protected void build() {
        super.build();
        label.setMinWidth(Label.USE_PREF_SIZE);
        comboBox.setItems(getViewModel().getTexts());
        HBox.setHgrow(comboBox, Priority.ALWAYS);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        textBox.setSpacing(Spacing.getHorizontal());
        textBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(textBox, Priority.NEVER);
        getContentBox().getChildren().add(textBox);
        getContentBox().setSpacing(Spacing.getVertical());

        registerButtons(cancelButton, okButton);
        getButtonWidthGroup().add(cancelButton, okButton);
        getFocusTrap().activate();
    }

    @Override
    protected void bind() {
        super.bind();
        comboBox.valueProperty().bindBidirectional(getViewModel().textProperty());
    }

    protected Label getLabel() {
        return label;
    }

    protected ComboBox<String> getComboBox() {
        return comboBox;
    }

    protected HBox getTextBox() {
        return textBox;
    }
}
