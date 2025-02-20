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

package com.techsenger.tabshell.core.tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This interface contains methods that can be used in component view, that contains TabPane.
 *
 * @author Pavel Castornii
 */
public interface TabPaneHolderView<T extends TabView<?>> {

    /**
     * Opens tabs and makes it selected.
     *
     * @param tabView
     */
    void openTab(T tabView);

    /**
     * Closes tab.
     *
     * @param tabView
     */
    void closeTab(T tabView);

    /**
     * Returns current tab or null.
     *
     * @return
     */
    T getSelectedTab();

    default void closeOtherTabs(List<ComponentTab> tabs, ComponentTab tab) {
        var otherTabs = tabs.stream().filter((t) -> t != tab).collect(Collectors.toList());
        for (var t: otherTabs) {
            this.closeTab(t);
        }
    }

    default void closeTabs(Collection<ComponentTab> tabs) {
        for (var tab : tabs) {
            this.closeTab(tab);
        }
    }

    default void closeAllTabs(List<ComponentTab> tabs) {
        //new list is created to avoid concurrent modification exception
        this.closeTabs(new ArrayList<>(tabs));
    }

    default void closeRightTabs(List<ComponentTab> tabs, ComponentTab tab) {
        var index = tabs.indexOf(tab);
        if (index == -1 || index + 1 == tabs.size()) {
            return;
        }
        List<ComponentTab> tabsToClose = new ArrayList<>();
        for (var i = index + 1; i < tabs.size(); i++) {
            tabsToClose.add(tabs.get(i));
            this.closeTabs(tabsToClose);
        }
    }

    default void closeLeftTabs(List<ComponentTab> tabs, ComponentTab tab) {
        var index = tabs.indexOf(tab);
        if (index == -1 || index == 0) {
            return;
        }
        List<ComponentTab> tabsToClose = new ArrayList<>();
        for (var i = index - 1; i >= 0; i--) {
            tabsToClose.add(tabs.get(i));
            this.closeTabs(tabsToClose);
        }
    }

    void closeTab(ComponentTab tab);
}
