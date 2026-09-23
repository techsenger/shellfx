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

package com.techsenger.shellfx.devtools.shared;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.devtools.style.DevToolsIcons;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.shared.find.AbstractFindView;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarView<VM extends ToolBarViewModel<?>> extends AbstractFindView<VM> {

    private final Button refreshButton = new Button(null, new FontIconView(DevToolsIcons.REFRESH));

    private final ToolBar toolBar = new ToolBar();

    public ToolBarView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public ToolBar getNode() {
        return this.toolBar;
    }

    @Override
    protected void build() {
        super.build();
        HBox.setHgrow(getFindComboBoxWrapper(), Priority.ALWAYS);
        getFindComboBoxWrapper().setPadding(Insets.EMPTY);
        getFindComboBox().setMaxWidth(Double.MAX_VALUE);
        getFindComboBox().getStyleClass().add(StyleClasses.SIZE_M);
        getFindRightBox().getStyleClass().add(StyleClasses.SIZE_M);

        this.refreshButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.refreshButton.setTooltip(new Tooltip("Refresh"));

        this.toolBar.getItems().addAll(getFindComboBoxWrapper(), getMatchCaseButton(), refreshButton);
    }

    @Override
    protected void bind() {
        super.bind();
        getFindComboBox().promptTextProperty().bind(getViewModel().findPromptProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.refreshButton.setOnAction((e) -> getViewModel().onRefresh());
    }
}
