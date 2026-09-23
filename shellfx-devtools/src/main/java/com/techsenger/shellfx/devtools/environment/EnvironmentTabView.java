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

package com.techsenger.shellfx.devtools.environment;

import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.core.window.WindowContainerView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.devtools.shared.ToolBarParams;
import com.techsenger.shellfx.devtools.shared.ToolBarPort;
import com.techsenger.shellfx.devtools.shared.ToolBarView;
import com.techsenger.shellfx.devtools.shared.ToolBarViewModel;
import com.techsenger.shellfx.dialogs.namevalue.FullNameValueDialogPort;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogView;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogViewModel;
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
public class EnvironmentTabView<VM extends EnvironmentTabViewModel<?>> extends AbstractTabView<VM> {

    public class Composer extends AbstractTabView<VM>.Composer implements EnvironmentTabComposer {

        private final EnvironmentTabView<VM> view = EnvironmentTabView.this;

        private ToolBarView<?> toolBar;

        @Override
        public void compose() {
            super.compose();
            this.toolBar = createToolBar();
            getModifiableChildren().add(this.toolBar);
            getContentBox().getChildren().add(0, this.toolBar.getNode());
        }

        @Override
        public ToolBarPort getToolBarPort() {
            return this.toolBar == null ? null : this.toolBar.getViewModel();
        }

        @Override
        public FullNameValueDialogPort openNameValueDialog(DialogParams params) {
            var dialog = createNameValueDialog(params);
            if (params.getWindowType() == WindowType.NESTED) {
                view.windowContainer.addDialog(dialog);
            } else {
                dialog.getStage().initOwner(getNode().getContent().getScene().getWindow());
                dialog.getStage().show();
            }
            return dialog.getViewModel();
        }

        protected ToolBarView<?> createToolBar() {
            var viewModel = new ToolBarViewModel<ChildComposer>(
                    new ToolBarParams(getViewModel(), "Property"));
            var toolBarView = new ToolBarView<>(viewModel);
            toolBarView.initialize();
            return toolBarView;
        }

        protected NameValueDialogView<?> createNameValueDialog(DialogParams params) {
            var viewModel = new NameValueDialogViewModel<>(params);
            var dialogView = new NameValueDialogView<>(viewModel);
            dialogView.initialize();
            return dialogView;
        }
    }

    private final TreeTableView<EnvironmentItem> tableView = new TreeTableView<>();

    private final WindowContainerView.Composer windowContainer;

    public EnvironmentTabView(VM viewModel, ShellView<?> shell, WindowContainerView.Composer windowContainer) {
        super(viewModel, shell);
        this.windowContainer = windowContainer;
    }

    @Override
    public void requestFocus() {
        tableView.requestFocus();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new EnvironmentTabView.Composer();
    }

    @Override
    protected void build() {
        super.build();

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
            if (item.getType() == EnvironmentItemType.PROPERTY) {
                p.set(item.getValue());
            }
            return p;
        });

        tableView.getColumns().addAll(propertyColumn, valueColumn);
        tableView.getStyleClass().add("no-header");
        tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setShowRoot(false);
        tableView.setPlaceholder(new Label(""));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        getContentBox().getChildren().add(tableView);
        var styles = EnvironmentTabView.class.getResource("environment-tab.css").toExternalForm();
        getContentBox().getStylesheets().add(styles);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().getItems().addListener(
                (ListChangeListener<EnvironmentItem>) c -> rebuildTree(getViewModel().getItems()));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        tableView.setRowFactory(ttv -> {
            TreeTableRow<EnvironmentItem> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    getViewModel().onItemRequested(row.getItem());
                }
            });
            return row;
        });
    }

    protected TreeTableView<EnvironmentItem> getTableView() {
        return tableView;
    }

    private void rebuildTree(List<EnvironmentItem> items) {
        List<TreeItem<EnvironmentItem>> lastTreeItems = new ArrayList<>(Collections.nCopies(3, null));
        for (var item : items) {
            var treeItem = new TreeItem<EnvironmentItem>(item);
            lastTreeItems.set(item.getType().getDepth(), treeItem);
            treeItem.setExpanded(item.isExpanded());
            if (item.getType() != EnvironmentItemType.ROOT) {
                var parent = lastTreeItems.get(item.getType().getDepth() - 1);
                if (parent != null) {
                    parent.getChildren().add(treeItem);
                }
                if (item.getType() == EnvironmentItemType.CATEGORY) {
                    treeItem.setExpanded(item.isExpanded());
                    treeItem.expandedProperty().addListener((ov, oldV, newV) -> item.setExpanded(newV));
                }
            }
        }
        // do not use the same root multiple times, as it causes a bug with node expansion
        tableView.setRoot(lastTreeItems.get(0));
    }
}
