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

package com.techsenger.tabshell.jfx.stylesheet;

import com.techsenger.tabshell.jfx.AbstractSearchableTabView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabView<T extends StylesheetTabViewModel<?>, S extends StylesheetTabComponent<?>>
        extends AbstractSearchableTabView<T, S> {

    private static final class StylesheetTreeCell extends TreeCell<StylesheetItem> {

        @Override
        protected void updateItem(StylesheetItem info, boolean empty) {
            super.updateItem(info, empty);
            if (empty || info == null) {
                setText(null);
            } else {
                setText(info.getName());
            }
        }
    }

    private final TreeView<StylesheetItem> treeView = new TreeView<>();

    public StylesheetTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        getSearchField().getTextComboBox().setPromptText("NodeClass / StyleClass / Id");
        getToolBar().getItems().addAll(getSearchField(), getMatchCaseButton(), getRefreshButton());

        treeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE);
        treeView.setShowRoot(true);
        treeView.setCellFactory(e -> new StylesheetTreeCell());

        VBox.setVgrow(treeView, Priority.ALWAYS);
        getContentPane().getChildren().addAll(getToolBar(), treeView);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(getViewModel().rootProperty(), (ov, oldV, newV) -> rebuiltTree(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var vm = getViewModel();
        getSearchField().setSearchHandler((t) -> vm.refresh());
        getSearchField().setClearHandler(() -> vm.refresh());
        getRefreshButton().setOnAction(e -> vm.refresh());
    }

    protected TreeView<StylesheetItem> getTreeView() {
        return treeView;
    }

    private void rebuiltTree(StylesheetDataItem root) {
        // do not use the same root multiple times, as it causes a bug with node expansion
        var rootItem = new TreeItem<StylesheetItem>(root);
        rootItem.setExpanded(true);
        var windowVMItem = root.getChildren().get(0);
        var windowItem = new TreeItem<StylesheetItem>(windowVMItem);
        windowItem .setExpanded(true);
        rootItem.getChildren().add(windowItem);

        for (var vmItem : windowVMItem.getChildren()) {
            var nodeItem = new TreeItem<StylesheetItem>(vmItem);
            for (var s : vmItem.getStylesheets()) {
                StylesheetItem stylesheetItem = new StylesheetItem() {
                    @Override
                    public String getName() {
                        return s;
                    }
                };
                nodeItem.getChildren().add(new TreeItem<>(stylesheetItem));
            }
            windowItem.getChildren().add(nodeItem);
        }
        treeView.setRoot(rootItem);
    }
}
