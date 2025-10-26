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

package com.techsenger.tabshell.layout.splittab;

import com.techsenger.tabshell.core.tab.AbstractShellTabHistory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSplitTabHistory<T extends AbstractSplitTabViewModel> extends AbstractShellTabHistory<T> {

    private double leftDivider;

    private boolean leftPaneVisible;

    private double rightDivider;

    private boolean rightPaneVisible;

    private double bottomDivider;

    private boolean bottomPaneVisible;

    @Override
    public void restoreAppearance(T viewModel) {
        super.restoreAppearance(viewModel);
        viewModel.getLeftDivider().setHistoryPosition(this.leftDivider);
        viewModel.getRightDivider().setHistoryPosition(this.rightDivider);
        viewModel.getBottomDivider().setHistoryPosition(this.bottomDivider);
        viewModel.setLeftPaneVisible(leftPaneVisible);
        viewModel.setRightPaneVisible(rightPaneVisible);
        viewModel.setBottomPaneVisible(bottomPaneVisible);
    }

    @Override
    public void saveAppearance(T viewModel) {
        super.saveAppearance(viewModel);
        this.leftDivider = viewModel.getLeftDivider().getHistoryPosition();
        this.rightDivider = viewModel.getRightDivider().getHistoryPosition();
        this.bottomDivider = viewModel.getBottomDivider().getHistoryPosition();
        this.leftPaneVisible = viewModel.isLeftPaneVisible();
        this.rightPaneVisible = viewModel.isRightPaneVisible();
        this.bottomPaneVisible = viewModel.isBottomPaneVisible();
    }
}
