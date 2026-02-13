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

package com.techsenger.tabshell.devtools;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.find.AbstractFindBaseFxView;
import com.techsenger.tabshell.shared.find.FindFeature;
import com.techsenger.tabshell.shared.find.FindTrigger;
import com.techsenger.tabshell.shared.style.SharedIcons;
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
public class ToolBarFxView<P extends ToolBarPresenter<?, ?>> extends AbstractFindBaseFxView<P>
        implements ToolBarView {

    private final Button refreshButton = new Button(null, new FontIconView(SharedIcons.REFRESH));

    private final ToolBar toolBar = new ToolBar();

    public ToolBarFxView(String prompt) {
        super(FindTrigger.ON_TYPE);
        getFindComboBox().setPromptText(prompt);
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
        getFindComboBox().getStyleClass().add(StyleClasses.EXTRA_DENSE);
        getFindRightBox().getStyleClass().add(StyleClasses.EXTRA_DENSE);

        this.refreshButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.refreshButton.setTooltip(new Tooltip("Refresh"));

        this.toolBar.getStyleClass().add(Styles.DENSE);
        this.toolBar.getItems().addAll(getFindComboBoxWrapper());
        if (getPresenter().getFeatures().contains(FindFeature.FIND_PREVIOUS)) {
            this.toolBar.getItems().addAll(getFindPreviousButton(), getFindNextButton());
        }
        this.toolBar.getItems().addAll(getMatchCaseButton(), refreshButton);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.refreshButton.setOnAction((e) -> getPresenter().handleRefresh());
    }


}
