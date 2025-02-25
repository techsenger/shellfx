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

package com.techsenger.tabshell.demo.dialog;

import com.techsenger.tabshell.core.dialog.AbstractDialogView;
import com.techsenger.tabshell.core.style.SizeConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DemoDialogView extends AbstractDialogView<DemoDialogViewModel> {

    private final Label fooLabel = new Label("Foo");

    private final TextField fooTextField = new TextField();

    private final Label barLabel = new Label("Bar");

    private final TextField barTextField = new TextField();

    private final GridPane gridPane = new GridPane();

    private final Button okButton = new Button("OK");

    private final HBox hBox = new HBox(okButton);

    public DemoDialogView(DemoDialogViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        fooTextField.requestFocus();
    }

    @Override
    protected void build(DemoDialogViewModel viewModel) {
        super.build(viewModel);
        gridPane.setHgap(SizeConstants.INSET);
        gridPane.setVgap(SizeConstants.INSET);
        gridPane.add(fooLabel, 0, 0);
        gridPane.add(fooTextField, 1, 0);
        GridPane.setHgrow(fooTextField, Priority.ALWAYS);
        gridPane.add(barLabel, 0, 1);
        gridPane.add(barTextField, 1, 1);
        GridPane.setHgrow(barTextField, Priority.ALWAYS);
        VBox.setVgrow(gridPane, Priority.ALWAYS);

        okButton.setDefaultButton(true);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        getContentPane().getChildren().addAll(gridPane, hBox);
        getContentPane().setPadding(new Insets(SizeConstants.INSET));
        getContentPane().setSpacing(SizeConstants.INSET);
    }

    @Override
    protected void addHandlers(DemoDialogViewModel viewModel) {
        super.addHandlers(viewModel);
        okButton.setOnAction(e -> viewModel.close());
    }
}
