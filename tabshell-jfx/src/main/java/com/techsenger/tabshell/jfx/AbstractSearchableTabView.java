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

package com.techsenger.tabshell.jfx;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.tab.AbstractTabView;
import com.techsenger.tabshell.material.SearchField;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSearchableTabView<T extends AbstractSearchableTabViewModel<?>,
        S extends AbstractSearchableTabComponent<?>> extends AbstractTabView<T, S> {

    private final SearchField searchField = new SearchField(SearchField.SearchMode.AUTO);

    private final ToggleButton matchCaseButton = new ToggleButton(null, new FontIconView(SharedIcons.MATCH_CASE));

    private final Button refreshButton = new Button(null, new FontIconView(SharedIcons.REFRESH));

    private final ToolBar toolBar = new ToolBar();

    public AbstractSearchableTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected void build() {
        super.build();
        HBox.setHgrow(searchField, Priority.ALWAYS);
        this.matchCaseButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.matchCaseButton.setTooltip(new Tooltip("Match Case"));
        this.refreshButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.refreshButton.setTooltip(new Tooltip("Refresh"));
        this.toolBar.getStyleClass().add(Styles.DENSE);
    }

    @Override
    protected void bind() {
        super.bind();
        var vm = getViewModel();
        this.matchCaseButton.selectedProperty().bindBidirectional(vm.caseSensitiveProperty());
        this.searchField.getTextComboBox().getEditor().textProperty().bindBidirectional(vm.searchTextProperty());
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
}
