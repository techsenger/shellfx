/*
 * Copyright 2024-2026 Pavel Castornii.
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

package com.techsenger.shellfx.layout.tabhost;

import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.tab.ContainerTabPort;
import com.techsenger.shellfx.core.tab.TabContainerViewModel;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class TabHostViewModel<C extends TabHostComposer> extends AbstractAreaViewModel<C>
        implements TabContainerViewModel<C>, FullTabHostPort {

    private final BooleanProperty tabHeaderVisible = new SimpleBooleanProperty(true);

    private final ObservableSource<Integer> selectTabSource = new SimpleObservableSource<>();

    public TabHostViewModel(AreaParams params) {
        super(params);
    }

    @Override
    public TabHostPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < getComposer().getTabCount()) {
            selectTabSource.next(tabIndex);
        }
    }

    @Override
    public boolean isTabHeaderVisible() {
        return tabHeaderVisible.get();
    }

    @Override
    public void setTabHeaderVisible(boolean tabHeaderVisible) {
        this.tabHeaderVisible.set(tabHeaderVisible);
    }

    @Override
    public BooleanProperty tabHeaderVisibleProperty() {
        return tabHeaderVisible;
    }

    protected void onCloseOtherTabs(ContainerTabPort tab) {
        getComposer().closeOtherTabs(tab);
    }

    protected void onCloseTabs(List<? extends ContainerTabPort> tabs) {
        getComposer().closeTabs(tabs);
    }

    protected void onCloseAllTabs() {
        getComposer().closeAllTabs();
    }

    protected void onCloseRightTabs(ContainerTabPort tab) {
        getComposer().closeRightTabs(tab);
    }

    protected void onCloseLeftTabs(ContainerTabPort tab) {
        getComposer().closeLeftTabs(tab);
    }

    protected void onCloseTab(ContainerTabPort tab) {
        getComposer().closeTab(tab);
    }

    ObservableSource<Integer> selectTabSource() {
        return selectTabSource;
    }
}
