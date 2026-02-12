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

package com.techsenger.tabshell.jfx.eventlog;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;

/**
 *
 * @author Pavel Castornii
 */
public class EventLogTabView<T extends EventLogTabViewModel<?>, S extends EventLogTabComponent<?>>
        extends AbstractTabView<T, S> {

    private final FontIconView recordIconView = new FontIconView();

    private final ToggleButton recordButton = new ToggleButton(null, recordIconView);

    private final Button clearButton = new Button(null, new FontIconView(SharedIcons.CLEAR));

    private final ToggleButton filterButton = new ToggleButton(null, new FontIconView(JfxIcons.FILTER));

    private final ToggleButton selectedOnlyButton = new ToggleButton(null, new FontIconView(JfxIcons.SELECTED_ONLY));

    private final MenuButton eventTypesButton = new MenuButton("Event Types");

    private final CheckBox retainFilteredOutCheckBox = new CheckBox("Retain Filtered Out");

    private final Label statisticsLabel = new Label("Statistics: ");

    private final Label statisticsDataLabel = new Label();

    /**
     * JFX RichTextArea and JFX ListView generates too many events (NodeAdd, NodeRemove), so we use RTFX text area.
     */
    private final InlineCssTextArea textArea = new InlineCssTextArea();

    private final VirtualizedScrollPane<InlineCssTextArea> textScrollPane = new VirtualizedScrollPane(textArea);

    public EventLogTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        var viewModel = getViewModel();
        this.recordButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.recordButton.setTooltip(new Tooltip("Start/Stop"));
        this.clearButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.clearButton.setTooltip(new Tooltip("Clear"));
        this.filterButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.filterButton.setTooltip(new Tooltip("Enable/Disable Filter"));
        this.selectedOnlyButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.selectedOnlyButton.setTooltip(new Tooltip("Selected Node Only"));
        //selectedOnlyButton.setOnAction(e -> this.textArea.moveDocumentEnd());
        this.retainFilteredOutCheckBox.setTooltip(new Tooltip("Retain Filtered Out Entries"));
        eventTypesButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.EXTRA_DENSE);
        viewModel.getEventTypesByClass().values().forEach(t -> {
            var menuItem = new CheckMenuItem(t.getType().getSimpleName());
            menuItem.selectedProperty().bindBidirectional(t.enabledProperty());
            this.eventTypesButton.getItems().add(menuItem);
        });
        var selectAllTypesItem = new MenuItem("Select All Types");
        selectAllTypesItem.setOnAction(e -> viewModel.selectAllEvents());
        var deselectAllTypesItem = new MenuItem("Deselect All Types");
        deselectAllTypesItem.setOnAction(e -> viewModel.deselectAllEvents());
        eventTypesButton.getItems().addAll(new SeparatorMenuItem(), selectAllTypesItem, deselectAllTypesItem);
        statisticsLabel.setMinWidth(Label.USE_PREF_SIZE);
        statisticsDataLabel.setStyle("-fx-min-width: 10em");
        statisticsDataLabel.setTooltip(new Tooltip("Displayed / Retained / Total events"));
        var searchPanel = getComponent().getSearchPanel().getView();
        searchPanel.getSearchField().getTextComboBox().setPromptText("Message");

        searchPanel.getNode().getItems().addAll(recordButton, clearButton, new Separator(Orientation.VERTICAL),
            filterButton, selectedOnlyButton, searchPanel.getSearchField(), searchPanel.getMatchCaseButton(),
            eventTypesButton, retainFilteredOutCheckBox, new Separator(Orientation.VERTICAL), statisticsLabel,
            statisticsDataLabel);

        textArea.setEditable(false);
        textArea.getStyleClass().add(StyleClasses.MONOSPACE);
        VBox.setVgrow(textScrollPane, Priority.ALWAYS);
        getContentBox().getChildren().addAll(searchPanel.getNode(), textScrollPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.getTextSource().addListener(text -> {
            // called from non-JavaFX thread
            if (text != null) {
                Platform.runLater(() -> {
                    this.textArea.appendText(text);
                });
            } else {
                Platform.runLater(() -> {
                    this.textArea.clear();
                });
            }
        });
        ValueUtils.callAndAddListener(viewModel.statisticsProperty(), (ov, oldV, newV) -> {
            // can be called from non-JavaFX thread
            Platform.runLater(() -> statisticsDataLabel.textProperty().set(newV));
        });
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        this.filterButton.selectedProperty().bindBidirectional(viewModel.filterActiveProperty());
        this.selectedOnlyButton.selectedProperty().bindBidirectional(viewModel.selectedOnlyProperty());
        this.recordIconView.iconProperty().bindBidirectional(viewModel.recordIconProperty());
        this.retainFilteredOutCheckBox.selectedProperty().bindBidirectional(viewModel.getFilteredOutRetainedWrapper());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        this.recordButton.setOnAction(e -> {
            if (this.recordButton.isSelected()) {
                viewModel.subscribe();
            } else {
                viewModel.unsubscribe();
            }
        });
        this.clearButton.setOnAction(e -> viewModel.clear());
        var searchPanel = getComponent().getSearchPanel().getView();
        searchPanel.getSearchField().setSearchHandler(t -> viewModel.updateFilter());
        searchPanel.getSearchField().setClearHandler(() -> viewModel.updateFilter());
        searchPanel.getMatchCaseButton().setOnAction(e -> viewModel.updateFilter());
    }
}
