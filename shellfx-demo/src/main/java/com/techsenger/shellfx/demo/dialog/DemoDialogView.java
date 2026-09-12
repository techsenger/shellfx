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

package com.techsenger.shellfx.demo.dialog;

import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DemoDialogView<VM extends DemoDialogViewModel<?>> extends AbstractDialogView<VM> {

    private final Label fooLabel = new Label("Foo");

    private final TextField fooTextField = new TextField();

    private final Label barLabel = new Label("Bar");

    private final TextField barTextField = new TextField();

    private final GridPane gridPane = new GridPane();

    private final ResultButton okButton = new ResultButton(DemoDailogResultButtons.OK, "OK");

    private final ResultButton cancelButton = new ResultButton(DemoDailogResultButtons.CANCEL, "Cancel");

    public DemoDialogView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        fooTextField.requestFocus();
        fooTextField.deselect();
    }

    @Override
    protected void build() {
        super.build();
        gridPane.setHgap(Spacing.getHorizontal());
        gridPane.setVgap(Spacing.getVertical());
        gridPane.addRow(0, fooLabel, fooTextField);
        fooTextField.setText("No closeRequest for Cancel! See ResultAction!");
        fooTextField.setFocusTraversable(true);
        GridPane.setHgrow(fooTextField, Priority.ALWAYS);
        gridPane.addRow(1, barLabel, barTextField);
        GridPane.setHgrow(barTextField, Priority.ALWAYS);
        barTextField.setFocusTraversable(true);
        VBox.setVgrow(gridPane, Priority.ALWAYS);

        okButton.setDefaultButton(true);
        registerButtons(okButton, cancelButton);
        getButtonWidthGroup().add(okButton, cancelButton);
        getContentBox().getChildren().add(gridPane);
        getContentBox().setSpacing(Spacing.getVertical());
        getFocusTrap().activate();
    }
}
