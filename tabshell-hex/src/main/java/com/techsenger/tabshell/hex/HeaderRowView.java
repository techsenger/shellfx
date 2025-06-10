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

package com.techsenger.tabshell.hex;

import com.techsenger.tabshell.core.style.StyleClasses;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 *
 * @author Pavel Castornii
 */
class HeaderRowView extends AbstractRowView<HeaderRowViewModel> {

    HeaderRowView(HeaderRowViewModel viewModel, AbstractHexEditorTabView<?> editor) {
        super(viewModel, editor);
    }

    @Override
    public void rebuild() {
        var editorVM = getViewModel().getEditor();
        getHexPane().clear();
        //canvas width prevents resetting the panel width
        getHexPane().getCanvas().setWidth(0);
        getAsciiPane().clear();
        getAsciiPane().getCanvas().setWidth(0);

        var labelText = " ".repeat(editorVM.getOffsetLength());
        getOffsetLabel().setText(labelText);

        var hexContentBox = getHexPane().getContentBox();
        hexContentBox.setSpacing(editorVM.getCharWidth());
        hexContentBox.setPadding(new Insets(0, editorVM.getCharWidth(),
                0, editorVM.getCharWidth()));
        var children = hexContentBox.getChildren();
        children.clear();

        for (byte i = 0; i < editorVM.getRowByteCount(); i++) {
            if (editorVM.areColumnsEnabled() && i != 0 && i % editorVM.getColumnByteCount() == 0) {
                if (editorVM.getColumnSeparator() == ColumnSeparator.SPACE) {
                    //we use regions as they stretch
                    var separator = new Region();
                    separator.getStyleClass().add("space");
                    children.add(separator);
                } else {
                    var separator = new Region();
                    separator.getStyleClass().add("line");
                    children.add(separator);
                }
            }
            var text = new Text(NumberBaseUtils.convert(i, editorVM.getOffsetNumberBase(), 2));
            text.getStyleClass().add("text");
            children.add(text);
        }
    }

    @Override
    protected void build(HeaderRowViewModel viewModel) {
        super.build(viewModel);
        getNode().getStyleClass().addAll("header-row", StyleClasses.MONOSPACE);
    }
}
