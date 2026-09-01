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

import com.techsenger.shellfx.core.area.AbstractAreaPresenter;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.tab.TabContainerPresenter;
import com.techsenger.shellfx.core.tab.TabPort;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class TabHostPresenter<V extends TabHostView> extends AbstractAreaPresenter<V>
        implements TabContainerPresenter<V>, TabHostPort {

    private boolean tabHeaderVisible = true;

    private int tabCount;

    private int selectedTabIndex;

    public TabHostPresenter(V view, AreaParams params) {
        super(view, params);
    }

    @Override
    public ViewAccess getViewAccess() {
        return getView();
    }

    @Override
    public void onSelectedTabChanged(int index) {
        this.selectedTabIndex = index;
    }

    @Override
    public boolean isTabHeaderVisible() {
        return tabHeaderVisible;
    }

    public void setTabHeaderVisible(boolean tabHeaderVisible) {
        if (this.tabHeaderVisible == tabHeaderVisible) {
            return;
        }
        this.tabHeaderVisible = tabHeaderVisible;
        getView().updateTabHeaderVisible(tabHeaderVisible);
    }

    @Override
    public int getSelectedTabIndex() {
        return this.selectedTabIndex;
    }

    @Override
    public int getTabCount() {
        return this.tabCount;
    }

    @Override
    public void selectTab(int tabIndex) {
        getView().selectTab(tabIndex);
    }

    protected void onTabCountChanged(int tabCount) {
        this.tabCount = tabCount;
    }

    protected void onCloseOtherTabs(TabPort tab) {
        closeOtherTabs(tab);
    }

    protected void onCloseTabs(List<? extends TabPort> tabs) {
        closeTabs(tabs);
    }

    protected void onCloseAllTabs() {
        closeAllTabs();
    }

    protected void onCloseRightTabs(TabPort tab) {
        closeRightTabs(tab);
    }

    protected void onCloseLeftTabs(TabPort tab) {
        closeLeftTabs(tab);
    }

    protected void onCloseTab(TabPort tab) {
        closeTab(tab);
    }
}
