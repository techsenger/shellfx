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

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class TabPopupViewModel<T extends TabDockMediator> extends AbstractAreaViewModel<T> {

    private double oldWidth = 250;

    private double oldHeight = 250;

    private boolean closing;

    private final Side side;

    public TabPopupViewModel(Side side, HistoryProvider<? extends TabPopupHistory> historyProvider) {
        this.side = side;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(historyProvider);
    }

    public double getOldWidth() {
        return oldWidth;
    }

    public double getOldHeight() {
        return oldHeight;
    }

    public Side getSide() {
        return side;
    }

    @Override
    protected TabPopupHistory getHistory() {
        return (TabPopupHistory) super.getHistory();
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        setOldWidth(h.getWidth());
        setOldHeight(h.getHeight());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        // If the user moves the mouse quickly, components may be created
        // and removed even before they have been rendered
        if (getWidth() > 0.1 && getHeight() > 0.1) {
            var h = getHistory();
            h.setWidth(getWidth());
            h.setHeight(getHeight());
        }
    }

    protected void setOldHeight(double oldHeight) {
        this.oldHeight = oldHeight;
    }

    protected void setOldWidth(double oldWidth) {
        this.oldWidth = oldWidth;
    }

    boolean isClosing() {
        return closing;
    }

    void setClosing(boolean closing) {
        this.closing = closing;
    }
}
