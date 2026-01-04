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

package com.techsenger.tabshell.jfx.environment;

import com.techsenger.tabshell.jfx.AbstractSearchableTabView;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class EnvironmentTabView<T extends EnvironmentTabViewModel<?>, S extends EnvironmentTabComponent<?>>
        extends AbstractSearchableTabView<T, S> {

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
        // getSearchField().getTextComboBox().setPromptText("Property");
        getToolBar().getItems().addAll(getSearchField(), getMatchCaseButton(), getRefreshButton());

        TreeTableColumn<EnvironmentItem, String> propertyColumn = new TreeTableColumn<>("Property");
        propertyColumn.setCellValueFactory(param -> {
            var item = param.getValue().getValue();
            var p = new SimpleStringProperty(item.name());
            return p;
        });
        TreeTableColumn<EnvironmentItem, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> {
            var item = param.getValue().getValue();
            var p = new SimpleStringProperty();
            if (item instanceof EnvironmentDataItem di) {
                p.set(di.value());
            }
            return p;
        });

        tableView.getColumns().addAll(propertyColumn, valueColumn);
        tableView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE);
        tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setShowRoot(false);
        tableView.setPlaceholder(new Label(""));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        getContentPane().getChildren().addAll(getToolBar(), tableView);
        var styles = EnvironmentTabView.class.getResource("environment-tab.css").toExternalForm();
        getContentPane().getStylesheets().add(styles);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().getItems().addListener((ListChangeListener<EnvironmentDataItem>) e -> {
            while (e.next()) {
                if (e.wasRemoved()) {
                    clearTree();
                }
                if (e.wasAdded()) {
                    createTree(e.getAddedSubList());
                }
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var vm = getViewModel();
        getSearchField().setSearchHandler((t) -> vm.refresh());
        getSearchField().setClearHandler(() -> vm.refresh());
        getRefreshButton().setOnAction(e -> vm.refresh());
    }

    protected TreeTableView<EnvironmentItem> getTableView() {
        return tableView;
    }

    private void createTree(List<? extends EnvironmentDataItem> items) {
        var root = new TreeItem<EnvironmentItem>();
        // do not use the same root multiple times, as it causes a bug with node expansion
        tableView.setRoot(root);
        TreeItem<EnvironmentItem> catTreeItem = null;
        EnvironmentCategory cat = null;
        for (var i : items) {
            if (cat != i.category()) {
                cat = i.category();
                catTreeItem = new TreeItem<EnvironmentItem>(new EnvironmentCategoryItem(cat.name()));
                var vmExpanded = getViewModel().getExpandedByCategory().get(cat);
                catTreeItem.setExpanded(vmExpanded.get());
                catTreeItem.expandedProperty().addListener((ov, oldV, newV) -> vmExpanded.set(newV));
                root.getChildren().add(catTreeItem);
            }
            var dataTreeItem = new TreeItem<EnvironmentItem>(i);
            catTreeItem.getChildren().add(dataTreeItem);
        }
    }

    private void clearTree() {
        this.tableView.setRoot(null);
    }
}
