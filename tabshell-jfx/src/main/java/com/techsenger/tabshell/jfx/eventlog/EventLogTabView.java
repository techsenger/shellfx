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

package com.techsenger.tabshell.jfx.eventlog;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.tab.AbstractTabView;
import com.techsenger.tabshell.jfx.style.JfxIcons;
import com.techsenger.tabshell.material.SearchField;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jfx.incubator.scene.control.richtext.RichTextArea;

/**
 *
 * @author Pavel Castornii
 */
public class EventLogTabView<T extends EventLogTabViewModel> extends AbstractTabView<T> {

    private final FontIconView recordIconView = new FontIconView();

    private final ToggleButton recordButton = new ToggleButton(null, recordIconView);

    private final Button clearButton = new Button(null, new FontIconView(SharedIcons.CLEAR));

    private final ToggleButton filterButton = new ToggleButton(null, new FontIconView(JfxIcons.FILTER));

    private final ToggleButton selectedOnlyButton = new ToggleButton(null, new FontIconView(JfxIcons.SELECTED_ONLY));

    private final SearchField searchPane = new SearchField();

    private final MenuButton eventTypesButton = new MenuButton("Event Types");

    private final ToolBar toolBar = new ToolBar(recordButton, clearButton, new Separator(Orientation.VERTICAL),
            filterButton, selectedOnlyButton, searchPane, eventTypesButton);

    private final RichTextArea textArea = new RichTextArea();

    private final StringBuilder textBuilder = new StringBuilder();

    public EventLogTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        this.recordButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.recordButton.setTooltip(new Tooltip("Start/Stop"));
        this.clearButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.clearButton.setTooltip(new Tooltip("Clear"));
        this.filterButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.filterButton.setTooltip(new Tooltip("Enable/Disable Filter"));
        this.selectedOnlyButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.selectedOnlyButton.setTooltip(new Tooltip("Selected Node Only"));
        selectedOnlyButton.setOnAction(e -> this.textArea.moveDocumentEnd());
        HBox.setHgrow(searchPane, Priority.ALWAYS);
        eventTypesButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.EXTRA_DENSE);
        viewModel.getEventTypesByClass().values().forEach(t -> {
            var menuItem = new CheckMenuItem(t.getType().getSimpleName());
            menuItem.selectedProperty().bindBidirectional(t.enabledProperty());
            this.eventTypesButton.getItems().add(menuItem);
        });
        var selectAllEvents = new MenuItem("Select All Events");
        selectAllEvents.setOnAction(e -> viewModel.selectAllEvents());
        var deselectAllEvents = new MenuItem("Deselect All Events");
        deselectAllEvents.setOnAction(e -> viewModel.deselectAllEvents());
        eventTypesButton.getItems().addAll(new SeparatorMenuItem(), selectAllEvents, deselectAllEvents);
        this.toolBar.getStyleClass().add(Styles.DENSE);

        textArea.setEditable(false);
        textArea.getStyleClass().add(StyleClasses.MONOSPACE);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        getContentPane().getChildren().addAll(toolBar, textArea);
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        viewModel.getFilteredEntries().addListener((ListChangeListener<LogEntry>) e -> {
            while (e.next()) {
                if (e.wasAdded()) {
                    print(e.getAddedSubList());
                }
                if (e.wasRemoved()) {
                    this.textArea.clear();
                }
            }
        });
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        this.filterButton.selectedProperty().bindBidirectional(viewModel.filterActiveProperty());
        this.selectedOnlyButton.selectedProperty().bindBidirectional(viewModel.selectedOnlyProperty());
        this.searchPane.getTextComboBox().getEditor().textProperty().bindBidirectional(viewModel.searchTextProperty());
        this.recordIconView.iconProperty().bindBidirectional(viewModel.recordIconProperty());
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        this.recordButton.setOnAction(e -> {
            if (this.recordButton.isSelected()) {
                viewModel.start();
            } else {
                viewModel.stop();
            }
        });
        this.clearButton.setOnAction(e -> viewModel.clear());
    }

    private void print(List<? extends LogEntry> entries) {
        textBuilder.setLength(0);
        entries.forEach(e -> textBuilder.append(e.date()).append(" ").append(e.message()));
        var text = textBuilder.toString();

        Platform.runLater(() -> {
            this.textArea.appendText(text + "\n");
            // this.textArea.moveDocumentEnd();
        });

    }
}
