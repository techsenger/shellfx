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

package com.techsenger.shellfx.shared.find;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.shared.style.SharedIcons;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractNavigableFindView<VM extends AbstractNavigableFindViewModel<?, ?>>
        extends AbstractFindView<VM> {

    private final Button findPreviousButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_UP));

    private final Button findNextButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOWN));

    public AbstractNavigableFindView(VM viewModel) {
        super(viewModel);
    }

    @Override
    protected void build() {
        super.build();
        this.findNextButton.setTooltip(new Tooltip("Next"));
        this.findNextButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.findNextButton.setFocusTraversable(false);

        this.findPreviousButton.setTooltip(new Tooltip("Previous"));
        this.findPreviousButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.findPreviousButton.setFocusTraversable(false);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        findNextButton.disableProperty().bind(viewModel.findNextDisabledProperty());
        findPreviousButton.disableProperty().bind(viewModel.findPreviousDisabledProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        findPreviousButton.setOnAction(e -> viewModel.onFindPrevious());
        findNextButton.setOnAction(e -> viewModel.onFindNext());
    }

    protected Button getFindPreviousButton() {
        return findPreviousButton;
    }

    protected Button getFindNextButton() {
        return findNextButton;
    }
}
