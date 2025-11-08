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

package com.techsenger.tabshell.hex.editor;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Pavel Castornii
 */
public class HexToolBarView<T extends HexToolBarViewModel> extends AbstractPaneView<T> {

    private final Button newButton = new Button(null, new FontIconView(CoreIcons.ADD));

    private final Button clearButton = new Button(null, new FontIconView(CoreIcons.CLEAR));

    private final Button cutButton = new Button(null, new FontIconView(CoreIcons.CUT));

    private final Button copyButton = new Button(null, new FontIconView(CoreIcons.COPY));

    private final Button pasteButton = new Button(null, new FontIconView(CoreIcons.PASTE));

    private final Button undoButton = new Button(null, new FontIconView(CoreIcons.UNDO));

    private final Button redoButton = new Button(null, new FontIconView(CoreIcons.REDO));

    private final Button findButton = new Button(null, new FontIconView(CoreIcons.FIND));

    private final Button replaceButton = new Button(null, new FontIconView(CoreIcons.REPLACE));

    private final ComboBox<Integer> rowByteCountsComboBox = new ComboBox<>();

    private final ToggleButton columnsEnabledButton =
            new ToggleButton(null, new FontIconView(HexIcons.COLUMNS_ENABLED));

    private final ComboBox<Integer> columnByteCountsComboBox = new ComboBox<>();

    private final ComboBox<NumberBase> offsetNumberBaseComboBox = new ComboBox<>();

    private final ToolBar toolBar = new ToolBar(
            newButton,
            clearButton,
            new Separator(Orientation.VERTICAL),
            cutButton,
            copyButton,
            pasteButton,
            new Separator(Orientation.VERTICAL),
            undoButton,
            redoButton,
            new Separator(Orientation.VERTICAL),
            findButton,
            replaceButton,
            new Separator(Orientation.VERTICAL),
            rowByteCountsComboBox,
            columnsEnabledButton,
            columnByteCountsComboBox,
            offsetNumberBaseComboBox
    );

    public HexToolBarView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ToolBar getNode() {
        return toolBar;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        newButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        newButton.setTooltip(new Tooltip("New"));
        clearButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        clearButton.setTooltip(new Tooltip("Clear"));
        cutButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        cutButton.setTooltip(new Tooltip("Cut"));
        copyButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        copyButton.setTooltip(new Tooltip("Copy"));
        pasteButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        pasteButton.setTooltip(new Tooltip("Paste"));
        undoButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        undoButton.setTooltip(new Tooltip("Undo"));
        redoButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        redoButton.setTooltip(new Tooltip("Redo"));
        findButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        findButton.setTooltip(new Tooltip("Find"));
        replaceButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        replaceButton.setTooltip(new Tooltip("Replace"));

        rowByteCountsComboBox.setItems(viewModel.getRowByteCounts());
        rowByteCountsComboBox.getStyleClass().add(Styles.DENSE);
        rowByteCountsComboBox.setTooltip(new Tooltip("Bytes per Row"));

        columnsEnabledButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        columnsEnabledButton.setTooltip(new Tooltip("Columns Enabled"));

        columnByteCountsComboBox.setItems(viewModel.getColumnByteCounts());
        columnByteCountsComboBox.getStyleClass().add(Styles.DENSE);
        columnByteCountsComboBox.setTooltip(new Tooltip("Bytes per Column"));

        offsetNumberBaseComboBox.setItems(viewModel.getOffsetNumberBases());
        offsetNumberBaseComboBox.getStyleClass().add(Styles.DENSE);
        offsetNumberBaseComboBox.setTooltip(new Tooltip("Offset Display Base"));

        toolBar.getStyleClass().add(StyleClasses.BLEND);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        this.rowByteCountsComboBox.valueProperty().bindBidirectional(viewModel.rowByteCountProperty());
        this.columnByteCountsComboBox.valueProperty().bindBidirectional(viewModel.columnByteCountProperty());
        this.columnsEnabledButton.selectedProperty().bindBidirectional(viewModel.columnsEnabledProperty());
        this.offsetNumberBaseComboBox.valueProperty().bindBidirectional(viewModel.offsetNumberBaseProperty());
    }

    public Button getNewButton() {
        return newButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getCutButton() {
        return cutButton;
    }

    public Button getCopyButton() {
        return copyButton;
    }

    public Button getPasteButton() {
        return pasteButton;
    }

    public Button getUndoButton() {
        return undoButton;
    }

    public Button getRedoButton() {
        return redoButton;
    }

    public Button getFindButton() {
        return findButton;
    }

    public Button getReplaceButton() {
        return replaceButton;
    }

    public ComboBox<Integer> getRowByteCountsComboBox() {
        return rowByteCountsComboBox;
    }

    public ToggleButton getColumnsEnabledButton() {
        return columnsEnabledButton;
    }

    public ComboBox<Integer> getColumnByteCountsComboBox() {
        return columnByteCountsComboBox;
    }

    public ComboBox<NumberBase> getOffsetNumberBaseComboBox() {
        return offsetNumberBaseComboBox;
    }
}
