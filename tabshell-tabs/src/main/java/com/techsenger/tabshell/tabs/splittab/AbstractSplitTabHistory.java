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

package com.techsenger.tabshell.tabs.splittab;

import com.techsenger.tabshell.core.tab.AbstractShellTabHistory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSplitTabHistory<T extends AbstractSplitTabViewModel> extends AbstractShellTabHistory<T> {

    private DividerHistory leftDivider;

    private boolean leftPaneVisible;

    private DividerHistory rightDivider;

    private boolean rightPaneVisible;

    private DividerHistory bottomDivider;

    private boolean bottomPaneVisible;

    public DividerHistory getLeftDivider() {
        return leftDivider;
    }

    public DividerHistory getRightDivider() {
        return rightDivider;
    }

    public DividerHistory getBottomDivider() {
        return bottomDivider;
    }

    public boolean isLeftPaneVisible() {
        return leftPaneVisible;
    }

    public void setLeftPaneVisible(boolean leftPaneVisible) {
        this.leftPaneVisible = leftPaneVisible;
    }

    public boolean isRightPaneVisible() {
        return rightPaneVisible;
    }

    public void setRightPaneVisible(boolean rightPaneVisible) {
        this.rightPaneVisible = rightPaneVisible;
    }

    public boolean isBottomPaneVisible() {
        return bottomPaneVisible;
    }

    public void setBottomPaneVisible(boolean bottomPaneVisible) {
        this.bottomPaneVisible = bottomPaneVisible;
    }

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        this.leftDivider = new DividerHistory();
        this.leftDivider.setPosition(0.25);
        this.rightDivider = new DividerHistory();
        this.rightDivider.setPosition(0.75);
        this.bottomDivider = new DividerHistory();
        this.bottomDivider.setPosition(0.75);
    }

    @Override
    public void restoreAppearance(T viewModel) {
        super.restoreAppearance(viewModel);
        viewModel.getLeftDivider().setHistoryPosition(this.leftDivider.getPosition());
        viewModel.getRightDivider().setHistoryPosition(this.rightDivider.getPosition());
        viewModel.getBottomDivider().setHistoryPosition(this.bottomDivider.getPosition());
        viewModel.setLeftPaneVisible(leftPaneVisible);
        viewModel.setRightPaneVisible(rightPaneVisible);
        viewModel.setBottomPaneVisible(bottomPaneVisible);
    }

    @Override
    public void saveAppearance(T viewModel) {
        super.saveAppearance(viewModel);
        this.leftDivider.setPosition(viewModel.getLeftDivider().getHistoryPosition());
        this.rightDivider.setPosition(viewModel.getRightDivider().getHistoryPosition());
        this.bottomDivider.setPosition(viewModel.getBottomDivider().getHistoryPosition());
        this.leftPaneVisible = viewModel.isLeftPaneVisible();
        this.rightPaneVisible = viewModel.isRightPaneVisible();
        this.bottomPaneVisible = viewModel.isBottomPaneVisible();
    }
}
