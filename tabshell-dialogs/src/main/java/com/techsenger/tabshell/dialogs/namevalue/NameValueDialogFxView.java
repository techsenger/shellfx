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

package com.techsenger.tabshell.dialogs.namevalue;

import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.style.Spacing;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class NameValueDialogFxView<P extends NameValueDialogPresenter<?>>
        extends AbstractDialogFxView<P> implements NameValueDialogView {

    private final Label nameLabel = new Label("Name");

    private final TextField nameTextField = new TextField();

    private final Label valueLabel = new Label("Value");

    private final TextArea valueTextArea = new TextArea();

    private final GridPane gridPane = new GridPane();

    private final ResultButton cancelButton = new ResultButton(NameValueButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(NameValueButtons.OK, "OK");

    public NameValueDialogFxView() {

    }

    public NameValueDialogFxView(String nameLabelText, String valueLabelText) {
        nameLabel.setText(nameLabelText);
        valueLabel.setText(valueLabelText);
    }

    @Override
    public void requestFocus() {
        nameTextField.requestFocus();
        nameTextField.deselect();
    }

    @Override
    public void setName(String name) {
        this.nameTextField.setText(name);
    }

    @Override
    public void setNameEditable(boolean value) {
        this.nameTextField.setEditable(value);
    }

    @Override
    public void setValue(String value) {
        this.valueTextArea.setText(value);
    }

    @Override
    public void setValueEditable(boolean value) {
        this.valueTextArea.setEditable(value);
    }

    @Override
    protected void build() {
        super.build();
        nameLabel.setMinWidth(Label.USE_PREF_SIZE);
        gridPane.add(nameLabel, 0, 0);
        gridPane.add(nameTextField, 1, 0);
        nameTextField.setEditable(false);
        GridPane.setHgrow(nameTextField, Priority.ALWAYS);

        gridPane.add(valueLabel, 0, 1);
        valueLabel.setMinWidth(Label.USE_PREF_SIZE);
        GridPane.setValignment(valueLabel, VPos.TOP);
        gridPane.add(valueTextArea, 1, 1);
        valueTextArea.setEditable(false);
        valueTextArea.setWrapText(true);
        GridPane.setHgrow(valueTextArea, Priority.ALWAYS);
        GridPane.setVgrow(valueTextArea, Priority.ALWAYS);

        gridPane.setVgap(Spacing.VERTICAL);
        gridPane.setHgap(Spacing.HORIZONTAL);
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        getContentBox().getChildren().add(gridPane);

        okButton.setDefaultButton(true);
        registerButtons(cancelButton, okButton);
    }

    protected Label getNameLabel() {
        return nameLabel;
    }

    protected TextField getNameTextField() {
        return nameTextField;
    }

    protected Label getValueLabel() {
        return valueLabel;
    }

    protected TextArea getValueTextArea() {
        return valueTextArea;
    }

    protected GridPane getGridPane() {
        return gridPane;
    }
}
