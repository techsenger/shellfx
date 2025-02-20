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

package com.techsenger.tabshell.core.page;

import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.tabshell.material.icon.IconViewBox;
import javafx.scene.control.Label;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageView<T extends AbstractPageViewModel> extends AbstractPaneView<T>
        implements PageView<T> {

    private final Label titleLabel = new Label();

    private final IconViewBox iconViewBox = new IconViewBox();

    public AbstractPageView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void doOnSelected() {
        getViewModel().selectedWrapper().set(true);
        requestFocus();
    }

    @Override
    public void doOnDeselected() {
        getViewModel().selectedWrapper().set(false);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        this.iconViewBox.iconProperty().bind(viewModel.iconProperty());
        this.titleLabel.textProperty().bind(viewModel.titleProperty());
    }

    protected Label getTitleLabel() {
        return titleLabel;
    }

    public IconViewBox getIconViewBox() {
        return iconViewBox;
    }
}
