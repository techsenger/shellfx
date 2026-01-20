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

package com.techsenger.tabshell.terminal.toolbar;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.tabshell.terminal.TerminalPaletteType;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalToolBarFxView<P extends TerminalToolBarPresenter<?, ?>> extends AbstractAreaFxView<P>
        implements TerminalToolBarView {

    private final Button newButton = new Button(null, new FontIconView(SharedIcons.ADD));

    private final Button clearButton = new Button(null, new FontIconView(SharedIcons.CLEAR));

    private final Button copyButton = new Button(null, new FontIconView(SharedIcons.COPY));

    private final Button pasteButton = new Button(null, new FontIconView(SharedIcons.PASTE));

    private final Button selectAllButton = new Button(null, new FontIconView(SharedIcons.SELECT_ALL));

    private final Button findButton = new Button(null, new FontIconView(SharedIcons.FIND));

    private final Button pageUpButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOUBLE_UP));

    private final Button pageDownButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOUBLE_DOWN));

    private final Button lineUpButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_UP));

    private final Button lineDownButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOWN));

    private final Label paletteLabel = new Label("Palette");

    private final ComboBox<TerminalPaletteType> paletteTypesComboBox = new ComboBox<>();

    private final ToolBar toolBar = new ToolBar(newButton, clearButton, new Separator(Orientation.VERTICAL),
            copyButton, pasteButton, selectAllButton, new Separator(Orientation.VERTICAL), findButton,
            new Separator(Orientation.VERTICAL), pageUpButton, pageDownButton, lineUpButton, lineDownButton);

    @Override
    public void requestFocus() {

    }

    @Override
    public ToolBar getNode() {
        return toolBar;
    }

    @Override
    public void setPaletteTypes(List<TerminalPaletteType> types) {
        this.paletteTypesComboBox.setItems(FXCollections.observableArrayList(types));
    }

    @Override
    public TerminalPaletteType getPaletteType() {
        return this.paletteTypesComboBox.getSelectionModel().getSelectedItem();
    }

    @Override
    public void setPaletteType(TerminalPaletteType type) {
        this.paletteTypesComboBox.getSelectionModel().select(type);
    }

    @Override
    public void setCopyDisable(boolean value) {
        this.copyButton.setDisable(value);
    }

    @Override
    protected void build() {
        super.build();
        newButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        newButton.setTooltip(new Tooltip("New"));
        clearButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        clearButton.setTooltip(new Tooltip("Clear"));
        copyButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        copyButton.setTooltip(new Tooltip("Copy"));
        copyButton.setDisable(true);
        pasteButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        pasteButton.setTooltip(new Tooltip("Paste"));
        selectAllButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        selectAllButton.setTooltip(new Tooltip("Select All"));
        findButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        findButton.setTooltip(new Tooltip("Find"));
        pageUpButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        pageUpButton.setTooltip(new Tooltip("Page Up"));
        pageDownButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        pageDownButton.setTooltip(new Tooltip("Page Down"));
        lineUpButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        lineUpButton.setTooltip(new Tooltip("Line Up"));
        lineDownButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        lineDownButton.setTooltip(new Tooltip("Line Down"));

        var stretchablePane = new Pane();
        HBox.setHgrow(stretchablePane, Priority.ALWAYS);
        this.paletteTypesComboBox.setStyle("-fx-min-width:16em");
        this.paletteTypesComboBox.getStyleClass().add(Styles.DENSE);
        Callback<ListView<TerminalPaletteType>, ListCell<TerminalPaletteType>> cellFactory = (p) -> {
            return new ListCell<TerminalPaletteType>() {

                    @Override
                    protected void updateItem(TerminalPaletteType item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
        };
        paletteTypesComboBox.setCellFactory(cellFactory);
        paletteTypesComboBox.setButtonCell(cellFactory.call(null));
        this.toolBar.getItems().addAll(stretchablePane, paletteLabel, this.paletteTypesComboBox);
        this.toolBar.getStyleClass().add(StyleClasses.BLEND);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var presenter = getPresenter();
        this.paletteTypesComboBox.getSelectionModel().selectedItemProperty()
                .addListener((ov, oldV, newV) -> presenter.handlePaletteTypeChanged(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var presenter = getPresenter();
        newButton.setOnAction(e -> presenter.handleNewAction());
        clearButton.setOnAction(e -> presenter.handleClearAction());
        copyButton.setOnAction(e -> presenter.handleCopyAction());
        pasteButton.setOnAction(e -> presenter.handlePasteAction());
        selectAllButton.setOnAction(e -> presenter.handleSelectAllAction());
        findButton.setOnAction(e -> presenter.handleFindAction());
        pageUpButton.setOnAction(e -> presenter.handlePageUpAction());
        pageDownButton.setOnAction(e -> presenter.handlePageDownAction());
        lineUpButton.setOnAction(e -> presenter.handleLineUpAction());
        lineDownButton.setOnAction(e -> presenter.handleLineDownAction());
    }

    protected Button getNewButton() {
        return newButton;
    }

    protected Button getClearButton() {
        return clearButton;
    }

    protected Button getCopyButton() {
        return copyButton;
    }

    protected Button getPasteButton() {
        return pasteButton;
    }

    protected Button getSelectAllButton() {
        return selectAllButton;
    }

    protected Button getFindButton() {
        return findButton;
    }

    protected Button getPageUpButton() {
        return pageUpButton;
    }

    protected Button getPageDownButton() {
        return pageDownButton;
    }

    protected Button getLineUpButton() {
        return lineUpButton;
    }

    protected Button getLineDownButton() {
        return lineDownButton;
    }

    protected Label getPaletteLabel() {
        return paletteLabel;
    }

    protected ComboBox<TerminalPaletteType> getPaletteTypesComboBox() {
        return paletteTypesComboBox;
    }
}
