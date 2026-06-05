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

package com.techsenger.tabshell.devtools.event;

import atlantafx.base.theme.Styles;
import com.techsenger.connectorfx.event.ConnectorEvent;
import com.techsenger.tabshell.devtools.ToolBarFxView;
import com.techsenger.tabshell.devtools.style.DevToolsIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Pavel Castornii
 */
public class EventToolBarFxView<P extends EventToolBarPresenter<?>> extends ToolBarFxView<P>
        implements EventToolBarView {

    private final FontIconView recordIconView = new FontIconView(DevToolsIcons.RECORD_START);

    private final ToggleButton recordButton = new ToggleButton(null, recordIconView);

    private final Button clearButton = new Button(null, new FontIconView(DevToolsIcons.CLEAR));

    private final ToggleButton filterButton = new ToggleButton(null, new FontIconView(DevToolsIcons.FILTER));

    private final ToggleButton selectedOnlyButton =
            new ToggleButton(null, new FontIconView(DevToolsIcons.SELECTED_ONLY));

    private final MenuButton eventTypesButton = new MenuButton("Event Types");

    private final Label statisticsLabel = new Label("Statistics: ");

    private final Label statisticsDataLabel = new Label();

    public EventToolBarFxView() {
        super("Message", false);
    }

    @Override
    public void setStatistics(String text) {
         statisticsDataLabel.setText(text);
    }

    @Override
    public void setFilterSelected(boolean value) {
        this.filterButton.setSelected(value);
    }

    @Override
    public void setSelectedNodeOnly(boolean value) {
        this.selectedOnlyButton.setSelected(value);
    }

    @Override
    public void setEventTypes(Map<Class<? extends ConnectorEvent>, AtomicBoolean> eventTypesByClass) {
        List<MenuItem> items = new ArrayList<>();
        eventTypesByClass.entrySet().forEach(e -> {
            var menuItem = new CheckMenuItem(e.getKey().getSimpleName());
            menuItem.setSelected(e.getValue().get());
            menuItem.selectedProperty()
                    .addListener((ov, oldV, newV) -> getPresenter().onEventSelected(e.getKey(), newV));
            items.add(menuItem);

        });
        items.sort(Comparator.comparing(MenuItem::getText));
        this.eventTypesButton.getItems().addAll(items);
    }

    @Override
    protected void build() {
        super.build();
        this.recordButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_M);
        this.recordButton.setTooltip(new Tooltip("Start/Stop"));
        this.clearButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_M);
        this.clearButton.setTooltip(new Tooltip("Clear"));
        this.filterButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_M);
        this.filterButton.setTooltip(new Tooltip("Enable/Disable Filter"));
        this.selectedOnlyButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_M);
        this.selectedOnlyButton.setTooltip(new Tooltip("Selected Node Only"));

        // selectedOnlyButton.setOnAction(e -> this.textArea.moveDocumentEnd());
        eventTypesButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        var selectAllTypesItem = new MenuItem("Select All Types");
        selectAllTypesItem.setOnAction(e -> {
            for (var item : eventTypesButton.getItems()) {
                if (item instanceof CheckMenuItem cmi) {
                    cmi.setSelected(true);
                }
            }
            getPresenter().onSelectAllEvents();
        });
        var deselectAllTypesItem = new MenuItem("Deselect All Types");
        deselectAllTypesItem.setOnAction(e -> {
            for (var item : eventTypesButton.getItems()) {
                if (item instanceof CheckMenuItem cmi) {
                    cmi.setSelected(false);
                }
            }
            getPresenter().onDeselectAllEvents();
        });
        eventTypesButton.getItems().addAll(new SeparatorMenuItem(), selectAllTypesItem, deselectAllTypesItem);

        statisticsLabel.setMinWidth(Label.USE_PREF_SIZE);
        statisticsDataLabel.setStyle("-fx-min-width: 6em");
        statisticsDataLabel.setTooltip(new Tooltip("Matched Events / Total Events"));
        getNode().getItems().clear();
        getNode().getItems().addAll(recordButton, clearButton, new Separator(Orientation.VERTICAL),
            filterButton, selectedOnlyButton, getFindComboBoxWrapper(), getMatchCaseButton(),
            eventTypesButton, new Separator(Orientation.VERTICAL), statisticsLabel, statisticsDataLabel);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        recordButton.selectedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                recordIconView.setIcon(DevToolsIcons.RECORD_STOP);
            } else {
                recordIconView.setIcon(DevToolsIcons.RECORD_START);
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        recordButton.setOnAction(e -> getPresenter().onRecord(recordButton.isSelected()));
        clearButton.setOnAction(e -> getPresenter().onClear());
        filterButton.setOnAction(e -> getPresenter().onFilter(filterButton.isSelected()));
        selectedOnlyButton.setOnAction(e -> getPresenter().onSelectedNodeOnly(selectedOnlyButton.isSelected()));
    }
}
