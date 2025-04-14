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

package com.techsenger.tabshell.demos.full;

import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.material.textarea.ExtendedTextArea;
import com.techsenger.tabshell.text.editor.AbstractEditorTabView;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;

/**
 *
 * @author Pavel Castornii
 */
public class EditorTabView extends AbstractEditorTabView<EditorTabViewModel> {

    private final Button infoButton = new Button("Info");

    private final Button warningButton = new Button("Warning");

    private final Button errorButton = new Button("Error");

    private final Button yesNoButton = new Button("YesNo");

    EditorTabView(ShellView<?> shell, EditorTabViewModel viewModel) {
        super(shell, viewModel, new ExtendedTextArea());
    }

    @Override
    protected void build(EditorTabViewModel viewModel) {
        super.build(viewModel);
        getToolBar().getItems().addAll(getClearButton(), getCopyButton(), getCutButton(), getPasteButton(),
                getUndoButton(), getRedoButton(), getWrapTextButton(), new Separator(Orientation.VERTICAL),
                infoButton, warningButton, errorButton, yesNoButton);
        getTopPane().getChildren().addAll(getToolBar(), this.getTextScrollPane());
        getTextAreaMenu().getItems().addAll(getCutItem(), getCopyItem(), getPasteItem());
    }

    @Override
    protected void addHandlers(EditorTabViewModel viewModel) {
        super.addHandlers(viewModel);
        infoButton.setOnAction(e -> viewModel.showInfoDialog());
        warningButton.setOnAction(e -> viewModel.showWarningDialog());
        errorButton.setOnAction(e -> viewModel.showErrorDialog());
        yesNoButton.setOnAction(e -> viewModel.showYesNoDialog());
    }
}
