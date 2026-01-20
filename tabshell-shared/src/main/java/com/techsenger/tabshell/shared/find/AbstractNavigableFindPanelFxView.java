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

package com.techsenger.tabshell.shared.find;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractNavigableFindPanelFxView<P extends AbstractNavigableFindPanelPresenter<?, ?>>
        extends AbstractFindPanelFxView<P> implements NavigableFindPanelView {

    private final Button findPreviousButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_UP));

    private final Button findNextButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOWN));

    @Override
    public void setFindNextDisable(boolean value) {
        this.findNextButton.setDisable(value);
    }

    @Override
    public boolean isFindNextDisable() {
        return findNextButton.isDisable();
    }

    @Override
    public void setFindPreviousDisable(boolean value) {
        this.findPreviousButton.setDisable(value);
    }

    @Override
    public boolean isFindPreviousDisable() {
        return this.findPreviousButton.isDisable();
    }

    @Override
    protected void build() {
        super.build();
        this.findPreviousButton.setTooltip(new Tooltip("Previous"));
        this.findPreviousButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.findPreviousButton.setFocusTraversable(false);
        this.findNextButton.setTooltip(new Tooltip("Next"));
        this.findNextButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.findNextButton.setFocusTraversable(false);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var p = getPresenter();
        findPreviousButton.setOnAction(e -> p.handleFindPrevious());
        findNextButton.setOnAction(e -> p.handleFindNext());
    }

    protected Button getFindPreviousButton() {
        return findPreviousButton;
    }

    protected Button getFindNextButton() {
        return findNextButton;
    }
}
