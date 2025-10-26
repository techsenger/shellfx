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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.tabshell.core.pane.AbstractPaneHistory;

/**
 *
 * @author Pavel Castornii
 */
public class TabPopupHistory<T extends TabPopupViewModel> extends AbstractPaneHistory<T> {

    private double width;

    private double height;

    @Override
    public void restoreAppearance(T viewModel) {
        super.restoreAppearance(viewModel);
        viewModel.setOldWidth(this.width);
        viewModel.setOldHeight(this.height);
    }

    @Override
    public void saveAppearance(T viewModel) {
        super.saveAppearance(viewModel);
        this.width = viewModel.getWidth();
        this.height = viewModel.getHeight();
    }
}
