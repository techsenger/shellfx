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

import com.techsenger.tabshell.core.area.AbstractAreaViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class TabPopupViewModel<T extends TabDockMediator> extends AbstractAreaViewModel<T> {

    private double oldWidth = 250;

    private double oldHeight = 250;

    private boolean closing;

    public TabPopupViewModel() {
    }

    public double getOldWidth() {
        return oldWidth;
    }

    public double getOldHeight() {
        return oldHeight;
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
