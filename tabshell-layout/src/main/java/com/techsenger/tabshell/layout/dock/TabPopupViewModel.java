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
import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.tab.TabViewModel;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class TabPopupViewModel extends AbstractPaneViewModel {

    private final SideBarViewModel sideBar;

    private final ObservableList<TabViewModel> modifiableTabs = FXCollections.observableArrayList();

    private final ObservableList<? extends TabViewModel> tabs
            = FXCollections.unmodifiableObservableList(modifiableTabs);

    private double oldWidth = 250;

    private double oldHeight = 250;

    public TabPopupViewModel(SideBarViewModel sideBar, TabPopupHistory<?> history) {
        this.sideBar = sideBar;
        getDescriptor().setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> history);
    }

    public SideBarViewModel getSideBar() {
        return sideBar;
    }

    /**
     * Returns the unmodifiable list of tabs.
     *
     * @return
     */
    public ObservableList<? extends TabViewModel> getTabs() {
        return tabs;
    }

    public double getOldWidth() {
        return oldWidth;
    }

    public double getOldHeight() {
        return oldHeight;
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponentNames.TAB_POPUP);
    }

    protected void setOldHeight(double oldHeight) {
        this.oldHeight = oldHeight;
    }

    protected void setOldWidth(double oldWidth) {
        this.oldWidth = oldWidth;
    }

    ObservableList<TabViewModel> getModifiableTabs() {
        return modifiableTabs;
    }
}
