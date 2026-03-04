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

package com.techsenger.tabshell.devtools.stylesheet;

import com.techsenger.patternfx.mvp.ComposeParameters;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.devtools.ToolBarFxView;
import com.techsenger.tabshell.devtools.ToolBarPort;
import com.techsenger.tabshell.devtools.ToolBarPresenter;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.find.FindFeature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabFxView<P extends StylesheetTabPresenter<?, ?>> extends AbstractTabFxView<P>
        implements StylesheetTabView {

    public class Composer extends AbstractTabFxView<P>.Composer implements StylesheetTabComposer {

        private final StylesheetTabFxView<P> view = StylesheetTabFxView.this;

        @Override
        public void compose(ComposeParameters params) {
            super.compose(params);

            view.toolBar = createToolBar();
            view.toolBar.getPresenter().initialize();
            view.getModifiableChildren().add(view.toolBar);
            view.getContentBox().getChildren().add(0, view.toolBar.getNode());
        }

        protected ToolBarFxView<?> createToolBar() {
            var view = new ToolBarFxView<>("NodeClass / StyleClass / ID");
            var presenter = new ToolBarPresenter<>(view, getPresenter().new ToolBarAwarePortImpl(),
                    FindFeature.MATCH_CASE);
            return view;
        }

        @Override
        public ToolBarPort getToolBar() {
            return view.toolBar.getPresenter();
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

    private ToolBarFxView<?> toolBar;

    public StylesheetTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void setItems(List<StylesheetItem> items) {
        rebuildTree(items);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new StylesheetTabFxView.Composer();
    }

    @Override
    protected void initialize() {
        super.initialize();
        Platform.runLater(() -> getPresenter().onAdded());
    }

    @Override
    protected void build() {
        super.build();
        treeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, StyleClasses.NO_BORDER);
        treeView.setShowRoot(true);
        treeView.setCellFactory(e -> new StylesheetTreeCell());

        VBox.setVgrow(treeView, Priority.ALWAYS);
        getContentBox().getChildren().add(treeView);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        treeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                getPresenter().onStylesheetSelected(newV.getValue());
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
