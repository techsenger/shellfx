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

package com.techsenger.tabshell.core;

import com.techsenger.mvvm4fx.core.AbstractHistory;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultTabShellHistory extends AbstractHistory<DefaultTabShellViewModel> {

    private double width;

    private double height;

    private boolean maximized;

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        this.width = 1200;
        this.height = 800;
    }

    @Override
    public void restoreAppearance(DefaultTabShellViewModel viewModel) {
        super.restoreAppearance(viewModel);
        viewModel.setDefaultWidth(width);
        viewModel.setDefaultHeight(height);
        viewModel.maximizedWrapper().set(maximized);
    }

    @Override
    public void saveAppearance(DefaultTabShellViewModel viewModel) {
        super.saveAppearance(viewModel);
        this.width = viewModel.getDefaultWidth();
        this.height = viewModel.getDefaultHeight();
        this.maximized = viewModel.isMaximized();
    }
}
