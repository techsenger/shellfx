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

package com.techsenger.tabshell.tabs.tabmanager;

import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.pane.PaneKey;
import com.techsenger.tabshell.core.tab.TabHostViewModel;
import com.techsenger.tabshell.core.tab.TabViewModel;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class TabManagerViewModel extends AbstractPaneViewModel implements MenuAware,
        TabHostViewModel<TabViewModel> {

    private static final Logger logger = LoggerFactory.getLogger(TabManagerViewModel.class);

    private final ReadOnlyObjectWrapper<TabViewModel> selectedTab = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyIntegerWrapper selectedTabIndex = new ReadOnlyIntegerWrapper();

    private final PaneKey key;

    private final ObservableList<TabViewModel> modifiableTabs = FXCollections.observableArrayList();

    private final ObservableList<TabViewModel> unmodifiableTabs =
            FXCollections.unmodifiableObservableList(modifiableTabs);

    /**
     * If true then tab header is hidden when tab count is 1.
     */
    private final BooleanProperty tabHeaderAutoHide = new SimpleBooleanProperty(false);

    private final BooleanProperty tabHeaderVisible = new SimpleBooleanProperty(true);

    public TabManagerViewModel(PaneKey key) {
        super();
        this.key = key;
        this.modifiableTabs.addListener((ListChangeListener<? super TabViewModel>) (change) -> {
            if (this.tabHeaderAutoHide.get()) {
                resolveTabHeaderVisibility();
            }

        });
        this.tabHeaderAutoHide.addListener((ov, oldV, newV) -> {
            if (newV) {
                resolveTabHeaderVisibility();
            } else {
                this.tabHeaderVisible.set(true);
            }
        });
    }

    @Override
    public PaneKey getKey() {
        return this.key;
    }

    @Override
    public void doOnMenuShowing(MenuKey menuKey) {
        var tab = this.selectedTab.get();
        if (tab != null) {
            tab.doOnMenuShowing(menuKey);
        }
    }

    @Override
    public void doOnMenuHiding(MenuKey menuKey) {
        var tab = this.selectedTab.get();
        if (tab != null) {
            tab.doOnMenuHiding(menuKey);
        }
    }

    @Override
    public MenuHelper getMenuHelper(MenuKey menuKey) {
        var tab = this.selectedTab.get();
        if (tab != null) {
            return tab.getMenuHelper(menuKey);
        } else {
            return null;
        }
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemKey menuItemKey) {
        var tab = this.selectedTab.get();
        if (tab != null) {
            return tab.getMenuItemHelper(menuItemKey);
        } else {
            return null;
        }
    }

    @Override
    public ReadOnlyObjectProperty<TabViewModel> selectedTabProperty() {
        return selectedTab.getReadOnlyProperty();
    }

    @Override
    public TabViewModel getSelectedTab() {
        return selectedTab.get();
    }

    @Override
    public void selectTab(TabViewModel tabViewModel) {
        var tabIndex = modifiableTabs.indexOf(tabViewModel);
        if (tabIndex >= 0) {
            selectTab(tabIndex);
        }
    }

    @Override
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < this.modifiableTabs.size())  {
            this.selectedTabIndex.set(tabIndex);
        }
    }

    @Override
    public ObservableList<TabViewModel> getTabs() {
        return this.unmodifiableTabs;
    }

    @Override
    public int getSelectedTabIndex() {
        return this.selectedTabIndex.get();
    }

    @Override
    public ReadOnlyIntegerProperty selectedTabIndexProperty() {
        return this.selectedTabIndex.getReadOnlyProperty();
    }

    public BooleanProperty tabHeaderAutoHideProperty() {
        return tabHeaderAutoHide;
    }

    public boolean isTabHeaderAutoHide() {
        return tabHeaderAutoHide.get();
    }

    public void setTabHeaderAutoHide(boolean value) {
        this.tabHeaderAutoHide.set(value);
    }

    ReadOnlyObjectWrapper<TabViewModel> selectedTabWrapper() {
        return this.selectedTab;
    }

    ReadOnlyIntegerWrapper selectedTabIndexWrapper() {
        return selectedTabIndex;
    }

    ObservableList<TabViewModel> getModifiableTabs() {
        return modifiableTabs;
    }

    BooleanProperty tabHeaderVisibleProperty() {
        return tabHeaderVisible;
    }

    boolean isTabHeaderVisible() {
        return tabHeaderVisible.get();
    }

    void setTabHeaderVisible(boolean value) {
        this.tabHeaderVisible.set(value);
    }

    private void resolveTabHeaderVisibility() {
        if (this.modifiableTabs.size() == 1) {
            setTabHeaderVisible(false);
        } else {
            setTabHeaderVisible(true);
        }
    }
}
