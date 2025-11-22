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

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarViewModel extends AbstractAreaViewModel {

    private final Side side;

    private final SideBarHistory<?> history;

    private final ObservableList<TabDockViewModel> modifiableTabDocks = FXCollections.observableArrayList();

    private final ObservableList<? extends TabDockViewModel> tabDocks =
            FXCollections.unmodifiableObservableList(modifiableTabDocks);

    private TabPopupViewModel popup;

    public SideBarViewModel(Side side, SideBarHistory<?> history) {
        this.side = side;
        this.history = history;
    }

    public Side getSide() {
        return side;
    }

    /**
     * Returns an unmodifiable list of minimized tab docks.
     *
     * @return
     */
    public ObservableList<? extends TabDockViewModel> getTabDocks() {
        return tabDocks;
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponentNames.SIDE_BAR);
    }

    protected TabPopupViewModel createPopup() {
        return new TabPopupViewModel(this, this.history.getOrCreatePopup());
    }

    protected TabPopupViewModel getPopup() {
        return popup;
    }

    ObservableList<TabDockViewModel> getModifiableTabDocks() {
        return modifiableTabDocks;
    }

    void setPopup(TabPopupViewModel popup) {
        this.popup = popup;
    }
}
