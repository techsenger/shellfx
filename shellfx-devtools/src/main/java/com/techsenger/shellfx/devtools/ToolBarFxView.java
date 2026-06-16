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

package com.techsenger.shellfx.devtools;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.devtools.style.DevToolsIcons;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.shared.find.AbstractFindBaseFxView;
import com.techsenger.shellfx.shared.find.FindTrigger;
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
public class ToolBarFxView<P extends ToolBarPresenter<?>> extends AbstractFindBaseFxView<P>
        implements ToolBarView {

    private final Button refreshButton = new Button(null, new FontIconView(DevToolsIcons.REFRESH));

    private final ToolBar toolBar = new ToolBar();

    private final boolean findNavigation;

    public ToolBarFxView(String prompt, boolean findNavigation) {
        super(FindTrigger.ON_TYPE);
        getFindComboBox().setPromptText(prompt);
        this.findNavigation = findNavigation;
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

        getFindNextButton().getStyleClass().add(StyleClasses.SIZE_M);
        getFindPreviousButton().getStyleClass().add(StyleClasses.SIZE_M);
        getMatchCaseButton().getStyleClass().add(StyleClasses.SIZE_M);
        this.refreshButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_M);
        this.refreshButton.setTooltip(new Tooltip("Refresh"));

        this.toolBar.getStyleClass().add(Styles.DENSE);
        this.toolBar.getItems().addAll(getFindComboBoxWrapper());
        if (this.findNavigation) {
            this.toolBar.getItems().addAll(getFindPreviousButton(), getFindNextButton());
        }
        this.toolBar.getItems().addAll(getMatchCaseButton(), refreshButton);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.refreshButton.setOnAction((e) -> getPresenter().onRefresh());
    }


}
