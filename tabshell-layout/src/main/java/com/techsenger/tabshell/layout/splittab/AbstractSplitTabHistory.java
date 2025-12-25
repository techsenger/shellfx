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
public abstract class AbstractSplitTabHistory extends AbstractShellTabHistory {

    private double leftDivider;

    private boolean leftPaneVisible;

    private double rightDivider;

    private boolean rightPaneVisible;

    private double bottomDivider;

    private boolean bottomPaneVisible;

    public double getLeftDivider() {
        return leftDivider;
    }

    public void setLeftDivider(double leftDivider) {
        this.leftDivider = leftDivider;
    }

    public boolean isLeftPaneVisible() {
        return leftPaneVisible;
    }

    public void setLeftPaneVisible(boolean leftPaneVisible) {
        this.leftPaneVisible = leftPaneVisible;
    }

    public double getRightDivider() {
        return rightDivider;
    }

    public void setRightDivider(double rightDivider) {
        this.rightDivider = rightDivider;
    }

    public boolean isRightPaneVisible() {
        return rightPaneVisible;
    }

    public void setRightPaneVisible(boolean rightPaneVisible) {
        this.rightPaneVisible = rightPaneVisible;
    }

    public double getBottomDivider() {
        return bottomDivider;
    }

    public void setBottomDivider(double bottomDivider) {
        this.bottomDivider = bottomDivider;
    }

    public boolean isBottomPaneVisible() {
        return bottomPaneVisible;
    }

    public void setBottomPaneVisible(boolean bottomPaneVisible) {
        this.bottomPaneVisible = bottomPaneVisible;
    }
}
