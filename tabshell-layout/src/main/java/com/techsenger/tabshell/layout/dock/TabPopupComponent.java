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

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.area.AbstractAreaComponent;
import com.techsenger.tabshell.core.tab.TabComponent;
import com.techsenger.tabshell.core.tab.TabViewModel;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class TabPopupComponent<T extends TabPopupView<?, ?>> extends AbstractAreaComponent<T> {

    protected class Mediator extends AbstractAreaComponent.Mediator implements TabPopupMediator {

        @Override
        public List<? extends TabViewModel> getTabs() {
            return tabs.stream().map(c -> c.getView().getViewModel()).toList();
        }
    }

    private final ObservableList<TabComponent<?>> modifiableTabs = FXCollections.observableArrayList();

    private final ObservableList<TabComponent<?>> tabs =
            FXCollections.unmodifiableObservableList(modifiableTabs);

    private final SideBarComponent<?> sideBar;

    public TabPopupComponent(T view, TabPopupHistory<?> history, SideBarComponent<?> sideBar) {
        super(view);
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> history);
        this.sideBar = sideBar;
    }

    @Override
    public ComponentName getName() {
        return LayoutComponentNames.TAB_POPUP;
    }

    /**
     * Returns an unmodifiable list of tabs.
     *
     * @return
     */
    public ObservableList<TabComponent<?>> getTabs() {
        return tabs;
    }

    public SideBarComponent<?> getSideBar() {
        return sideBar;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    void addTab(TabComponent<?> tab) {
        this.modifiableTabs.add(tab);
        // there can be other children, so index is not used
        getView().getTabPane().getTabs().add(tab.getView().getNode());
        getModifiableChildren().add(tab);
    }

    void removeTab(TabComponent<?> tab) {
        this.modifiableTabs.remove(tab);
        // there can be other children, so index is not used
        getView().getTabPane().getTabs().remove(tab.getView().getNode());
        getModifiableChildren().remove(tab);
    }

}
