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

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.tab.AbstractTabView;
import com.techsenger.tabshell.material.SearchField;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
                setText(info.getName());
            }
        }
    }

    private final SearchField searchField = new SearchField(SearchField.SearchMode.AUTO);

    private final ToggleButton matchCaseButton = new ToggleButton(null, new FontIconView(SharedIcons.MATCH_CASE));

    private final Button refreshButton = new Button(null, new FontIconView(SharedIcons.REFRESH));

    private final ToolBar toolBar = new ToolBar(searchField, matchCaseButton, refreshButton);

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
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.getTextComboBox().setPromptText("NodeClass / StyleClass / Id");

        this.matchCaseButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.matchCaseButton.setTooltip(new Tooltip("Match Case"));
        this.refreshButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.refreshButton.setTooltip(new Tooltip("Refresh"));
        this.toolBar.getStyleClass().add(Styles.DENSE);

        treeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE);
        treeView.setShowRoot(true);
        treeView.setCellFactory(e -> new StylesheetTreeCell());

        VBox.setVgrow(treeView, Priority.ALWAYS);
        getContentPane().getChildren().addAll(toolBar, treeView);
    }

    @Override
    protected void bind() {
        super.bind();
        var vm = getViewModel();
        this.matchCaseButton.selectedProperty().bindBidirectional(vm.caseSensitiveProperty());
        this.searchField.getTextComboBox().getEditor().textProperty().bindBidirectional(vm.searchTextProperty());
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
        this.searchField.setSearchHandler((t) -> vm.refresh());
        this.searchField.setClearHandler(() -> vm.refresh());
        this.refreshButton.setOnAction(e -> vm.refresh());
    }

    protected SearchField getSearchField() {
        return searchField;
    }

    protected ToggleButton getMatchCaseButton() {
        return matchCaseButton;
    }

    protected Button getRefreshButton() {
        return refreshButton;
    }

    protected ToolBar getToolBar() {
        return toolBar;
    }

    protected TreeView<StylesheetItem> getTreeView() {
        return treeView;
    }

    private void rebuiltTree(StylesheetNode rootInfo) {
        var rootItem = new TreeItem<StylesheetItem>(rootInfo);
        rootItem.setExpanded(true);
        var windowInfo = rootInfo.getChildren().get(0);
        var windowItem = new TreeItem<StylesheetItem>(windowInfo);
        windowItem .setExpanded(true);
        rootItem.getChildren().add(windowItem);

        for (var nodeInfo : windowInfo.getChildren()) {
            var nodeItem = new TreeItem<StylesheetItem>(nodeInfo);
            for (var s : nodeInfo.getStylesheets()) {
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
