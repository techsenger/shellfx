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

package com.techsenger.tabshell.devtools.environment;

import com.techsenger.patternfx.mvp.ComposeParameters;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogContainerFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.devtools.ToolBarFxView;
import com.techsenger.tabshell.devtools.ToolBarPort;
import com.techsenger.tabshell.devtools.ToolBarPresenter;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogFxView;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogPort;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogPresenter;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.find.FindFeature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
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
public class EnvironmentTabFxView<P extends EnvironmentTabPresenter<?, ?>> extends AbstractTabFxView<P> implements
        EnvironmentTabView {

    public class Composer extends AbstractTabFxView<P>.Composer implements EnvironmentTabComposer {

        private final EnvironmentTabFxView<P> view = EnvironmentTabFxView.this;

        @Override
        public void compose(ComposeParameters params) {
            super.compose(params);
            view.toolBar = createToolBar();
            view.toolBar.getPresenter().initialize();
            getModifiableChildren().add(view.toolBar);
            getContentBox().getChildren().add(0, view.toolBar.getNode());
        }

        @Override
        public ToolBarPort getToolBar() {
            return view.toolBar.getPresenter().getPort();
        }

        @Override
        public NameValueDialogPort addNameValueDialog() {
            var dialog = createNameValueDialog();
            dialogContainer.addDialog(dialog);
            return dialog.getPresenter().getPort();
        }

        protected ToolBarFxView<?> createToolBar() {
            var view = new ToolBarFxView<>("Property");
            var presenter = new ToolBarPresenter<>(view, getPresenter().new ToolBarAwarePortImpl(),
                    FindFeature.MATCH_CASE);
            return view;
        }

        protected NameValueDialogFxView<?> createNameValueDialog() {
            var view = new NameValueDialogFxView<>(true);
            var presenter = new NameValueDialogPresenter<>(view);
            return view;
        }
    }

    private final TreeTableView<EnvironmentItem> tableView = new TreeTableView<>();

    private ToolBarFxView<?> toolBar;

    private final DialogContainerFxView.Composer dialogContainer;

    public EnvironmentTabFxView(ShellFxView<?> shell, DialogContainerFxView.Composer dialogContainer) {
        super(shell);
        this.dialogContainer = dialogContainer;
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
    public void setItems(List<EnvironmentItem> items) {
        rebuildTree(items);
    }

    @Override
    protected Composer createComposer() {
        return new EnvironmentTabFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        setTitle("Environment");
        setClosable(false);

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
        tableView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, "no-header");
        tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setShowRoot(false);
        tableView.setPlaceholder(new Label(""));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        getContentBox().getChildren().add(tableView);
        var styles = EnvironmentTabView.class.getResource("environment-tab.css").toExternalForm();
        getContentBox().getStylesheets().add(styles);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        tableView.setRowFactory(ttv -> {
            TreeTableRow<EnvironmentItem> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    getPresenter().onItemRequested(row.getItem());
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
