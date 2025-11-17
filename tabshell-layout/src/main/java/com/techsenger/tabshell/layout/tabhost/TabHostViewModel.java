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

package com.techsenger.tabshell.layout.tabhost;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.tab.TabContainerViewModel;
import com.techsenger.tabshell.core.tab.TabViewModel;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class TabHostViewModel extends AbstractPaneViewModel implements TabContainerViewModel<TabViewModel> {

    private final ReadOnlyObjectWrapper<TabViewModel> selectedTab = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyIntegerWrapper selectedTabIndex = new ReadOnlyIntegerWrapper();

    private final ObservableList<TabViewModel> modifiableTabs = FXCollections.observableArrayList();

    private final ObservableList<? extends TabViewModel> tabs =
            FXCollections.unmodifiableObservableList(modifiableTabs);

    /**
     * If true then tab header is hidden when tab count is 1.
     */
    private final BooleanProperty tabHeaderAutoHide = new SimpleBooleanProperty(false);

    private final BooleanProperty tabHeaderVisible = new SimpleBooleanProperty(true);

    private List<? extends TabViewModel> detachedTabs = Collections.EMPTY_LIST;

    private final ReadOnlyBooleanWrapper tabsDetached = new ReadOnlyBooleanWrapper();

    public TabHostViewModel() {
        super();
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
    public ObservableList<? extends TabViewModel> getTabs() {
        return this.tabs;
    }

    @Override
    public int getSelectedTabIndex() {
        return this.selectedTabIndex.get();
    }

    @Override
    public ReadOnlyIntegerProperty selectedTabIndexProperty() {
        return this.selectedTabIndex.getReadOnlyProperty();
    }

    /**
     * Returns the unmodifiable list of the detached tabs.
     * @return
     */
    public List<? extends TabViewModel> getDetachedTabs() {
        return detachedTabs;
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

    public ReadOnlyBooleanProperty tabsDetachedProperty() {
        return tabsDetached.getReadOnlyProperty();
    }

    public boolean areTabsDetached() {
        return tabsDetached.get();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponentNames.TAB_HOST);
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

    void setDetachedTabs(List<? extends TabViewModel> detachedTabs) {
        this.detachedTabs = detachedTabs;
    }

    void setTabsDetached(boolean detached) {
        tabsDetached.set(detached);
    }

    private void resolveTabHeaderVisibility() {
        if (this.modifiableTabs.size() == 1) {
            setTabHeaderVisible(false);
        } else {
            setTabHeaderVisible(true);
        }
    }
}
