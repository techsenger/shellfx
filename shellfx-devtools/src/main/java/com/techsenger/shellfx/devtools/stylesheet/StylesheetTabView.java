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

package com.techsenger.shellfx.devtools.stylesheet;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.devtools.FindToolBarPort;
import com.techsenger.shellfx.devtools.ToolBarParams;
import com.techsenger.shellfx.devtools.ToolBarView;
import com.techsenger.shellfx.devtools.ToolBarViewModel;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabView<VM extends StylesheetTabViewModel<?>> extends AbstractTabView<VM> {

    public class Composer extends AbstractTabView<VM>.Composer implements StylesheetTabComposer {

        private final StylesheetTabView<VM> view = StylesheetTabView.this;

        private ToolBarView<?> toolBar;

        @Override
        public void compose() {
            super.compose();

            this.toolBar = createToolBar();
            getModifiableChildren().add(this.toolBar);
            view.getContentBox().getChildren().add(0, this.toolBar.getNode());
        }

        protected ToolBarView<?> createToolBar() {
            var viewModel = new ToolBarViewModel<>(new ToolBarParams(getViewModel().new ToolBarAwarePortImpl()));
            var toolBarView = new ToolBarView<>(viewModel, "NodeClass / StyleClass / ID", false);
            toolBarView.initialize();
            return toolBarView;
        }

        @Override
        public FindToolBarPort getToolBarPort() {
            return this.toolBar == null ? null : this.toolBar.getViewModel();
        }
    }

    private static final class StylesheetTreeCell extends TreeCell<StylesheetItem> {

        @Override
        protected void updateItem(StylesheetItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.name());
            }
        }
    }

    private final TreeView<StylesheetItem> treeView = new TreeView<>();

    public StylesheetTabView(VM viewModel, ShellView<?> shell) {
        super(viewModel, shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new StylesheetTabView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        treeView.getStyleClass().add(StyleClasses.NO_BORDER);
        treeView.setShowRoot(true);
        treeView.setCellFactory(e -> new StylesheetTreeCell());

        VBox.setVgrow(treeView, Priority.ALWAYS);
        getContentBox().getChildren().add(treeView);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().getItems().addListener(
                (ListChangeListener<StylesheetItem>) c -> rebuildTree(getViewModel().getItems()));
        treeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                getViewModel().onStylesheetSelected(newV.getValue());
            }
        });
    }

    protected TreeView<StylesheetItem> getTreeView() {
        return treeView;
    }

    private void rebuildTree(List<StylesheetItem> items) {
        List<TreeItem<StylesheetItem>> lastTreeItems = new ArrayList<>(Collections.nCopies(4, null));
        for (var item : items) {
            var treeItem = new TreeItem<StylesheetItem>(item);
            lastTreeItems.set(item.type().getDepth(), treeItem);
            treeItem.setExpanded(item.expanded());
            if (item.type() != StylesheetItemType.APPLICATION) {
                var parent = lastTreeItems.get(item.type().getDepth() - 1);
                if (parent != null) {
                    parent.getChildren().add(treeItem);
                }
            }
        }
        // do not use the same root multiple times, as it causes a bug with node expansion
        treeView.setRoot(lastTreeItems.get(0));
    }
}
