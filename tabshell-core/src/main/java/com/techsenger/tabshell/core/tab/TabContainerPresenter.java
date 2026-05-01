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

package com.techsenger.tabshell.core.tab;

import com.techsenger.patternfx.core.ComponentState;
import com.techsenger.patternfx.mvp.ParentPresenter;
import com.techsenger.tabshell.core.CloseRequestResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public interface TabContainerPresenter<V extends TabContainerView> extends ParentPresenter<V>, TabContainerPort {

    void onSelectedTabChanged(int index);

    @Override
    default void closeOtherTabs(TabPort tab) {
        var otherTabs = getView().getComposer().getTabPorts().stream().filter((t) -> t != tab)
                .collect(Collectors.toList());
        closeTabs(otherTabs);
    }

    @Override
    default void closeTabs(List<? extends TabPort> tabs) {
        class Closer {

            private int index = 0;

            private void run() {
                TabPort tab = null;
                for (var i = index; i < tabs.size(); i++) {
                    index++;
                    tab = tabs.get(i);
                    if (tab.getDescriptor().getState() == ComponentState.INITIALIZED) {
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

    @Override
    default void closeAllTabs() {
        this.closeTabs(new ArrayList<>(getView().getComposer().getTabPorts()));
    }

    @Override
    default void closeRightTabs(TabPort tab) {
        var tabs = getView().getComposer().getTabPorts();
        var index = tabs.indexOf(tab);
        if (index == -1 || index + 1 == tabs.size()) {
            return;
        }
        List<TabPort> tabsToClose = new ArrayList<>();
        for (var i = index + 1; i < tabs.size(); i++) {
            tabsToClose.add(tabs.get(i));
            this.closeTabs(tabsToClose);
        }
    }

    @Override
    default void closeLeftTabs(TabPort tab) {
        var tabs = getView().getComposer().getTabPorts();
        var index = tabs.indexOf(tab);
        if (index == -1 || index == 0) {
            return;
        }
        List<TabPort> tabsToClose = new ArrayList<>();
        for (var i = index - 1; i >= 0; i--) {
            tabsToClose.add(tabs.get(i));
            this.closeTabs(tabsToClose);
        }
    }

    @Override
    default void closeTab(TabPort tab) {
        tab.requestClose();
    }
}
