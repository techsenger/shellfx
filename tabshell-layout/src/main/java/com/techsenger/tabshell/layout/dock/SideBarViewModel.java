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
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarViewModel<T extends SideBarMediator> extends AbstractAreaViewModel<T> {

    private final SideBarHistory history;

    private final Side side;

    public SideBarViewModel(SideBarHistory history, Side side) {
        this.history = history;
        this.side = side;
    }

    public Side getSide() {
        return side;
    }

    public SideBarHistory getHistory() {
        return history;
    }
}
