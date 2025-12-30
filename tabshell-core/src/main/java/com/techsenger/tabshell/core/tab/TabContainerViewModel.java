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

import com.techsenger.patternfx.core.State;
import com.techsenger.tabshell.core.CloseRequestResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public interface TabContainerViewModel<S extends TabViewModel<?>> {

    /**
     * Selected tab property.
     *
     * @return
     */
    ReadOnlyObjectProperty<S> selectedTabProperty();

    /**
     * Returns selected tab view model.
     *
     * @return
     */
    S getSelectedTab();

    /**
     * Makes tab with specified view model selected.
     *
     * @param tabViewModel
     */
    void selectTab(S tabViewModel);

    /**
     * Makes tab with specified index selected.
     *
     * @param tabIndex
     */
    void selectTab(int tabIndex);

    /**
     * Returns the index of the selected tab.
     *
     * @return
     */
    int getSelectedTabIndex();

    /**
     * Returns the property for the selected tab index.
     *
     * @return
     */
    ReadOnlyIntegerProperty selectedTabIndexProperty();

    /**
     * Returns unmodifiable observable list.
     *
     * @return
     */
    ObservableList<? extends S> getTabs();

    default void closeOtherTabs(S tab) {
        var otherTabs = getTabs().stream().filter((t) -> t != tab).collect(Collectors.toList());
        closeTabs(otherTabs);
    }

    default void closeTabs(List<? extends S> tabs) {
        class Closer {

            private int index = 0;

            private void run() {
                S tab = null;
                for (var i = index; i < tabs.size(); i++) {
                    index++;
                    tab = tabs.get(i);
                    if (tab.getMediator().getState() == State.INITIALIZED) {
                        break;
                    }
                }
                if (tab != null) {
                    tab.requestClose(this::handleCloseResult);
                }
            }

            private void handleCloseResult(CloseRequestResult result) {
                if (result == CloseRequestResult.SUCCESS) {
                    run();
                }
            }
        }
        new Closer().run();
    }

    default void closeAllTabs() {
        this.closeTabs(new ArrayList<>(getTabs()));
    }

    default void closeRightTabs(S tab) {
        var index = getTabs().indexOf(tab);
        if (index == -1 || index + 1 == getTabs().size()) {
            return;
        }
        List<S> tabsToClose = new ArrayList<>();
        for (var i = index + 1; i < getTabs().size(); i++) {
            tabsToClose.add(getTabs().get(i));
            this.closeTabs(tabsToClose);
        }
    }

    default void closeLeftTabs(S tab) {
        var index = getTabs().indexOf(tab);
        if (index == -1 || index == 0) {
            return;
        }
        List<S> tabsToClose = new ArrayList<>();
        for (var i = index - 1; i >= 0; i--) {
            tabsToClose.add(getTabs().get(i));
            this.closeTabs(tabsToClose);
        }
    }

    default void closeTab(S tab) {
        tab.requestClose();
    }
}
