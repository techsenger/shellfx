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
import javafx.geometry.VPos;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class LongTextDialogView<VM extends LongTextDialogViewModel<?>> extends AbstractTextDialogView<VM> {

    private final TextArea textArea = new TextArea();

    private final GridPane gridPane = new GridPane();

    public LongTextDialogView(VM viewModel) {
        super(viewModel);
    }

    public LongTextDialogView(VM viewModel, String labelText) {
        super(viewModel, labelText);
    }

    @Override
    protected void buildContent() {
        gridPane.add(getTextLabel(), 0, 0);
        gridPane.add(textArea, 1, 0);
        GridPane.setValignment(getTextLabel(), VPos.TOP);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        textArea.setWrapText(true);
        gridPane.setVgap(Spacing.getVertical());
        gridPane.setHgap(Spacing.getHorizontal());
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        getContentBox().getChildren().add(gridPane);
    }

    protected TextArea getTextArea() {
        return textArea;
    }

    protected GridPane getGridPane() {
        return gridPane;
    }

    @Override
    TextInputControl getInput() {
        return textArea;
    }
}
