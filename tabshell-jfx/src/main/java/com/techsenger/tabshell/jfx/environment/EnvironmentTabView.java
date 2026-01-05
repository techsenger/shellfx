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

package com.techsenger.tabshell.jfx.environment;

import com.techsenger.tabshell.core.tab.AbstractTabView;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class EnvironmentTabView<T extends EnvironmentTabViewModel<?>, S extends EnvironmentTabComponent<?>>
        extends AbstractTabView<T, S> {

    private final TreeTableView<EnvironmentItem> tableView = new TreeTableView<>();

    public EnvironmentTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        tableView.requestFocus();
    }

    @Override
    protected void build() {
        super.build();
        var searchPanel = getComponent().getSearchPanel().getView();
        searchPanel.getSearchField().getTextComboBox().setPromptText("Property");
        searchPanel.getNode().getItems().addAll(searchPanel.getSearchField(), searchPanel.getMatchCaseButton(),
                searchPanel.getRefreshButton());

        TreeTableColumn<EnvironmentItem, String> propertyColumn = new TreeTableColumn<>("Property");
        propertyColumn.setCellValueFactory(param -> {
            var item = param.getValue().getValue();
            var p = new SimpleStringProperty(item.getName());
            return p;
        });
        TreeTableColumn<EnvironmentItem, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> {
            var item = param.getValue().getValue();
            var p = new SimpleStringProperty();
            if (item.getDepth() == EnvironmentItem.PROPERTY_DEPTH) {
                p.set(item.getValue());
            }
            return p;
        });

        tableView.getColumns().addAll(propertyColumn, valueColumn);
        tableView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, "no-header");
        tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setShowRoot(false);
        tableView.setPlaceholder(new Label(""));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        getContentPane().getChildren().addAll(searchPanel.getNode(), tableView);
        var styles = EnvironmentTabView.class.getResource("environment-tab.css").toExternalForm();
        getContentPane().getStylesheets().add(styles);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().getItems().addListener((ListChangeListener<EnvironmentItem>) e -> {
            while (e.next()) {
                if (e.wasRemoved()) {
                    clearTree();
                }
                if (e.wasAdded()) {
                    rebuildTree();
                }
            }
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            var wrapper = getViewModel().getSelectedItemWrapper();
            if (newV == null) {
                wrapper.set(null);
            } else {
                wrapper.set(newV.getValue());
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var vm = getViewModel();
        var searchPanel = getComponent().getSearchPanel().getView();
        searchPanel.getSearchField().setSearchHandler((t) -> vm.refresh());
        searchPanel.getSearchField().setClearHandler(() -> vm.refresh());
        searchPanel.getRefreshButton().setOnAction(e -> vm.refresh());
        tableView.setRowFactory(ttv -> {
            TreeTableRow<EnvironmentItem> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    getViewModel().handleItemClick();
                }
            });
            return row;
        });
    }

    protected TreeTableView<EnvironmentItem> getTableView() {
        return tableView;
    }

    private void rebuildTree() {
        List<TreeItem<EnvironmentItem>> lastTreeItems = new ArrayList<>(Collections.nCopies(3, null));
        for (var item : getViewModel().getItems()) {
            var treeItem = new TreeItem<EnvironmentItem>(item);
            lastTreeItems.set(item.getDepth(), treeItem);
            treeItem.setExpanded(item.isExpanded());
            if (item.getDepth() != EnvironmentItem.ROOT_DEPTH) {
                var parent = lastTreeItems.get(item.getDepth() - 1);
                if (parent != null) {
                    parent.getChildren().add(treeItem);
                }
                if (item.getDepth() == EnvironmentItem.CATEGORY_DEPTH) {
                    treeItem.setExpanded(item.isExpanded());
                    treeItem.expandedProperty().addListener((ov, oldV, newV) -> item.setExpanded(newV));
                }
            }
        }
        // do not use the same root multiple times, as it causes a bug with node expansion
        tableView.setRoot(lastTreeItems.get(0));

    }

    private void clearTree() {
        this.tableView.setRoot(null);
    }
}
