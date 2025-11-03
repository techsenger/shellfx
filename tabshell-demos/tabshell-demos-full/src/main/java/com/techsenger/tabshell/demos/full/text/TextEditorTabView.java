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

package com.techsenger.tabshell.demos.full.text;

import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.material.textarea.ExtendedTextArea;
import com.techsenger.tabshell.text.editor.AbstractEditorTabView;

/**
 *
 * @author Pavel Castornii
 */
public class TextEditorTabView extends AbstractEditorTabView<TextEditorTabViewModel> {

    public TextEditorTabView(ShellView<?> shell, TextEditorTabViewModel viewModel) {
        super(shell, viewModel, new ExtendedTextArea());
    }

    @Override
    protected void build(TextEditorTabViewModel viewModel) {
        super.build(viewModel);
        getToolBar().getItems().addAll(getClearButton(), getCopyButton(), getCutButton(), getPasteButton(),
                getUndoButton(), getRedoButton(), getWrapTextButton());
        getTopPane().getChildren().addAll(getToolBar(), this.getTextScrollPane());
        getTextAreaMenu().getItems().addAll(getCutItem(), getCopyItem(), getPasteItem());
    }
}
