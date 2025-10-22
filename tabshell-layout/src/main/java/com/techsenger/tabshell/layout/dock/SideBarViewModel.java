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

import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.pane.PaneKey;
import com.techsenger.tabshell.core.tab.TabViewModel;
import com.techsenger.tabshell.layout.LayoutComponentKeys;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarViewModel extends AbstractPaneViewModel {

    private final Side side;

    private final SideBarHistory<?> history;

    private final ObservableList<TabDockViewModel> modifiableTabDocks = FXCollections.observableArrayList();

    private final ObservableList<TabDockViewModel> tabDocks =
            FXCollections.unmodifiableObservableList(modifiableTabDocks);

    private TabPopupViewModel popup;

    public SideBarViewModel(Side side, SideBarHistory<?> history) {
        this.side = side;
        this.history = history;
    }

    @Override
    public PaneKey getKey() {
        return LayoutComponentKeys.SIDE_BAR;
    }

    public Side getSide() {
        return side;
    }

    /**
     * Returns an unmodifiable list of minimized tab docks.
     *
     * @return
     */
    public ObservableList<TabDockViewModel> getTabDocks() {
        return tabDocks;
    }

    protected TabPopupViewModel createPopup(TabViewModel tab) {
        return new TabPopupViewModel(this, tab, this.history.getPopup());
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
