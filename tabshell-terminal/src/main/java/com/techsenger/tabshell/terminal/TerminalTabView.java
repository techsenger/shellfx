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

package com.techsenger.tabshell.terminal;

import atlantafx.base.theme.Styles;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import com.techsenger.jeditermfx.ui.TerminalPanel;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabView<T extends TerminalTabViewModel<?>, S extends TerminalTabComponent<?>>
        extends AbstractShellTabView<T, S> {

    private final Button newButton = new Button(null, new FontIconView(SharedIcons.ADD));

    private final Button clearButton = new Button(null, new FontIconView(SharedIcons.CLEAR));

    private final Button copyButton = new Button(null, new FontIconView(SharedIcons.COPY));

    private final Button pasteButton = new Button(null, new FontIconView(SharedIcons.PASTE));

    private final Button selectAllButton = new Button(null, new FontIconView(SharedIcons.SELECT_ALL));

    private final Button openUrlButton = new Button(null, new FontIconView(SharedIcons.OPEN_IN_NEW));

    private final Button findButton = new Button(null, new FontIconView(SharedIcons.FIND));

    private final Button pageUpButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOUBLE_UP));

    private final Button pageDownButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOUBLE_DOWN));

    private final Button lineUpButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_UP));

    private final Button lineDownButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOWN));

    private final Label paletteLabel = new Label("Palette");

    private final ComboBox<TerminalPaletteType> paletteTypesComboBox = new ComboBox<>();

    private final ToolBar toolBar = new ToolBar(clearButton, new Separator(Orientation.VERTICAL),
            copyButton, pasteButton, selectAllButton, new Separator(Orientation.VERTICAL), openUrlButton, findButton,
            new Separator(Orientation.VERTICAL), pageUpButton, pageDownButton, lineUpButton, lineDownButton);

    private KitJediTermFxWidget widget;

    public TerminalTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void doOnSelected() {
        super.doOnSelected();
        requestFocus();
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(widget.getTerminalPanel().getCanvas());
    }

    protected void showFind(FindPaneView find) {
        getContentPane().getChildren().add(find.getNode());
        find.requestFocus();
    }

    protected void hideFind(FindPaneView find) {
        getContentPane().getChildren().remove(find.getNode());
        widget.getTerminalPanel().getCanvas().requestFocus();
    }

    @Override
    protected void build() {
        super.build();
        var viewModel = getViewModel();
        this.widget = new KitJediTermFxWidget(80, 24, viewModel.createSettingsProvider(), () -> {
            if (getComponent().getFindPane() == null) {
                viewModel.showFind();
            } else {
                getComponent().getFindPane().getView().getFindComboBox().getEditor().requestFocus();
            }
        });
        widget.setTtyConnector(viewModel.getTtyConnector());
        widget.addHyperlinkFilter(new DefaultHyperlinkFilter());
        VBox.setVgrow(widget.getPane(), Priority.ALWAYS);
        getContentPane().getChildren().addAll(toolBar, widget.getPane());

        newButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        newButton.setTooltip(new Tooltip("New"));
        clearButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        clearButton.setTooltip(new Tooltip("Clear"));
        copyButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        copyButton.setTooltip(new Tooltip("Copy"));
        pasteButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        pasteButton.setTooltip(new Tooltip("Paste"));
        selectAllButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        selectAllButton.setTooltip(new Tooltip("Select All"));
        openUrlButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        openUrlButton.setTooltip(new Tooltip("Open URL"));
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
        this.paletteTypesComboBox.setItems(getViewModel().getPaletteTypes());
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
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        this.paletteTypesComboBox.valueProperty().bindBidirectional(viewModel.paletteTypeProperty());
        this.copyButton.disableProperty().bind(viewModel.copyDisableProperty());
        this.openUrlButton.disableProperty().bind(viewModel.openUrlDisableProperty());
        viewModel.selectedTextWrapper().bind(this.widget.getTerminalPanel().selectedTextProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().focusRequiredSource().addListener((value) -> {
            if (value) {
                requestFocusSimply();
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        final TerminalPanel terminalPanel = this.widget.getTerminalPanel();
        this.newButton.setOnAction(e -> {
            viewModel.createNewTerminal();
        });
        this.clearButton.setOnAction(e -> {
            terminalPanel.clearBuffer();
            requestFocusSimply();
        });
        this.copyButton.setOnAction(e -> {
            terminalPanel.handleCopy(false, false);
            requestFocusSimply();
        });
        this.pasteButton.setOnAction(e -> {
            terminalPanel.handlePaste();
            requestFocusSimply();
        });
        this.selectAllButton.setOnAction(e -> {
            terminalPanel.selectAll();
            requestFocusSimply();
        });
        this.openUrlButton.setOnAction(e -> {
            terminalPanel.openSelectedTextAsURL();
            requestFocusSimply();
        });
        this.findButton.setOnAction(e -> viewModel.showFind());
        this.pageUpButton.setOnAction(e -> {
            terminalPanel.pageUp();
            requestFocusSimply();
        });
        this.pageDownButton.setOnAction(e -> {
            terminalPanel.pageDown();
            requestFocusSimply();
        });
        this.lineUpButton.setOnAction(e -> {
            terminalPanel.scrollUp();
            requestFocusSimply();
        });
        this.lineDownButton.setOnAction(e -> {
            terminalPanel.scrollDown();
            requestFocusSimply();
        });
        getContentPane().addEventFilter(KeyEvent.KEY_PRESSED, (e) -> {
            if (getComponent().getFindPane() != null && e.getCode() == KeyCode.ESCAPE) {
                viewModel.hideFind();
                e.consume();
            }
        });
    }

    @Override
    protected void initialize() {
        super.initialize();
        widget.start();
    }

    @Override
    protected void deinitialize() {
        widget.close();
        widget.getTtyConnector().close();
        super.deinitialize();
    }

    protected KitJediTermFxWidget getWidget() {
        return widget;
    }

    private void requestFocusSimply() {
        this.widget.getTerminalPanel().getCanvas().requestFocus();
    }
}
