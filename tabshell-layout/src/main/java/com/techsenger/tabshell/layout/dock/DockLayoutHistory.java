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

import com.techsenger.tabshell.core.dialog.AbstractDialogViewModel;
import com.techsenger.tabshell.core.area.AbstractAreaHistory;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutHistory<T extends AbstractDialogViewModel> extends AbstractAreaHistory<T> {

    private SideBarHistory<?> rightSideBar;

    private SideBarHistory<?> bottomSideBar;

    private SideBarHistory<?> leftSideBar;

    public SideBarHistory<?> getOrCreateRightSideBar() {
        if (rightSideBar == null) {
            rightSideBar = new SideBarHistory<>();
        }
        return rightSideBar;
    }

    public SideBarHistory<?> getOrCreateBottomSideBar() {
        if (bottomSideBar == null) {
            bottomSideBar = new SideBarHistory<>();
        }
        return bottomSideBar;
    }

    public SideBarHistory<?> getOrCreateLeftSideBar() {
        if (leftSideBar == null) {
            leftSideBar = new SideBarHistory<>();
        }
        return leftSideBar;
    }
}
