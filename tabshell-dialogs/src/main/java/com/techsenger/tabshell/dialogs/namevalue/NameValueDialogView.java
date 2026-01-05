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

import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogView;
import com.techsenger.tabshell.material.style.SizeConstants;
import javafx.geometry.Insets;
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
public class NameValueDialogView<T extends NameValueDialogViewModel<?>,
        S extends NameValueDialogComponent<?>> extends AbstractSimpleDialogView<T, S> {

    private final Label nameLabel = new Label("Name");

    private final TextField nameTextField = new TextField();

    private final Label valueLabel = new Label("Value");

    private final TextArea valueTextArea = new TextArea();

    private final GridPane gridPane = new GridPane();

    public NameValueDialogView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        nameTextField.requestFocus();
    }

    @Override
    protected void makeEqualButtons() {

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

        gridPane.setVgap(SizeConstants.INSET);
        gridPane.setHgap(SizeConstants.INSET);
        getButtonBox().getChildren().add(getOkButton());
        gridPane.setPadding(new Insets(SizeConstants.INSET, SizeConstants.INSET, 0, SizeConstants.INSET));
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        getContentPane().getChildren().addAll(gridPane, getButtonBox());
    }

    @Override
    protected void bind() {
        super.bind();
        var vm = getViewModel();
        nameTextField.textProperty().bind(vm.nameProperty());
        valueTextArea.textProperty().bind(vm.valueProperty());
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
