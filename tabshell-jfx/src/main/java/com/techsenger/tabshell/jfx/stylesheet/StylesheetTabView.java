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

package com.techsenger.tabshell.jfx.stylesheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabView<T extends StylesheetTabViewModel<?>, S extends StylesheetTabComponent<?>>
        extends AbstractTabView<T, S> {

    private static final class StylesheetTreeCell extends TreeCell<StylesheetItem> {

        @Override
        protected void updateItem(StylesheetItem info, boolean empty) {
            super.updateItem(info, empty);
            if (empty || info == null) {
                setText(null);
            } else {
                setText(info.name());
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
        var searchPanel = getComponent().getSearchPanel().getView();
        searchPanel.getSearchField().getTextComboBox().setPromptText("NodeClass / StyleClass / Id");
        searchPanel.getNode().getItems().addAll(searchPanel.getSearchField(), searchPanel.getMatchCaseButton(),
                searchPanel.getRefreshButton());

        treeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE);
        treeView.setShowRoot(true);
        treeView.setCellFactory(e -> new StylesheetTreeCell());

        VBox.setVgrow(treeView, Priority.ALWAYS);
        getContentBox().getChildren().addAll(searchPanel.getNode(), treeView);
        rebuiltTree();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().getItems().addListener((ListChangeListener<StylesheetItem>) e -> {
            while (e.next()) {
                if (e.wasRemoved()) {
                    clearTree();
                }
                if (e.wasAdded()) {
                    rebuiltTree();
                }
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var searchPanel = getComponent().getSearchPanel().getView();
        var vm = getViewModel();
        searchPanel.getSearchField().setSearchHandler((t) -> vm.refresh());
        searchPanel.getSearchField().setClearHandler(() -> vm.refresh());
        searchPanel.getRefreshButton().setOnAction(e -> vm.refresh());
    }

    protected TreeView<StylesheetItem> getTreeView() {
        return treeView;
    }

    private void rebuiltTree() {
        var items = getViewModel().getItems();
        List<TreeItem<StylesheetItem>> lastTreeItems = new ArrayList<>(Collections.nCopies(4, null));
        for (var item : items) {
            var treeItem = new TreeItem<StylesheetItem>(item);
            lastTreeItems.set(item.depth(), treeItem);
            treeItem.setExpanded(item.expanded());
            if (item.depth() != StylesheetItem.APPLICATION_DEPTH) {
                var parent = lastTreeItems.get(item.depth() - 1);
                if (parent != null) {
                    parent.getChildren().add(treeItem);
                }
            }
        }
        // do not use the same root multiple times, as it causes a bug with node expansion
        treeView.setRoot(lastTreeItems.get(0));
    }

    private void clearTree() {
        this.treeView.setRoot(null);
    }
}
