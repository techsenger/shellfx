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

import atlantafx.base.theme.Styles;
import com.techsenger.mvvm4fx.core.ComponentHelper;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.hex.AbstractHexEditorTabView;
import com.techsenger.tabshell.hex.CaretShape;
import com.techsenger.tabshell.hex.ColumnSeparator;
import javafx.geometry.Orientation;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabView extends AbstractHexEditorTabView<HexEditorTabViewModel> {

    private final ComboBox<CaretShape> caretShapeComboBox = new ComboBox<>();

    private final ComboBox<ColumnSeparator> columnSeparatorComboBox = new ComboBox<>();

    public HexEditorTabView(ShellView<?> tabShell, HexEditorTabViewModel viewModel) {
        super(tabShell, viewModel);
    }

    @Override
    protected ComponentHelper<?> createComponentHelper() {
        return new HexEditorTabHelper(this);
    }

    @Override
    protected void build(HexEditorTabViewModel viewModel) {
        super.build(viewModel);

        this.caretShapeComboBox.setItems(viewModel.getCaretShapes());
        this.caretShapeComboBox.getStyleClass().add(Styles.DENSE);
        this.caretShapeComboBox.setTooltip(new Tooltip("Caret Shape"));

        this.columnSeparatorComboBox.setItems(viewModel.getColumnSeparators());
        this.columnSeparatorComboBox.getStyleClass().add(Styles.DENSE);
        this.columnSeparatorComboBox.setTooltip(new Tooltip("Column Separator"));

        getToolBar().getItems().addAll(
                getNewButton(),
                getClearButton(),
                new Separator(Orientation.VERTICAL),
                getCutButton(),
                getCopyButton(),
                getPasteButton(),
                new Separator(Orientation.VERTICAL),
                getUndoButton(),
                getRedoButton(),
                new Separator(Orientation.VERTICAL),
                getFindButton(),
                getReplaceButton(),
                new Separator(Orientation.VERTICAL),
                getRowByteCountsComboBox(),
                getColumnsEnabledButton(),
                getColumnByteCountsComboBox(),
                this.columnSeparatorComboBox,
                new Separator(Orientation.VERTICAL),
                this.caretShapeComboBox
        );
        getTopPane().getChildren().addAll(getToolBar(), getVirtualScrollPane());
    }

    @Override
    protected void bind(HexEditorTabViewModel viewModel) {
        super.bind(viewModel);
        this.caretShapeComboBox.valueProperty().bindBidirectional(viewModel.getCaret().shapeProperty());
        this.columnSeparatorComboBox.valueProperty().bindBidirectional(viewModel.columnSeparatorProperty());
    }


}
