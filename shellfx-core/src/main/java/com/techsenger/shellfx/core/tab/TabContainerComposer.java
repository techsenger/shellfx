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

package com.techsenger.shellfx.core.tab;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.core.ComponentState;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.close.CloseRequestResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface TabContainerComposer extends ChildComposer, TabContainerPort.ComposerAccess {

    /**
    * Returns an unmodifiable list of tabs. A new list instance is created on each call.
    *
    * @return
    */
    @Override
    @Unmodifiable List<? extends ContainerTabPort> getTabPorts();

    @Override
    ContainerTabPort getSelectedTabPort();

    @Override
    ReadOnlyObjectProperty<? extends ContainerTabPort> selectedTabPortProperty();

    /**
     * Closes all tabs except the specified one.
     *
     * @param tab the {@link ContainerTabPort} to keep open
     */
    default void closeOtherTabs(ContainerTabPort tab) {
        var otherTabs = getTabPorts().stream().filter((t) -> t != tab)
                .collect(Collectors.toList());
        closeTabs(otherTabs);
    }

    /**
     * Closes all tabs in the given list.
     *
     * @param tabs list of {@link ContainerTabPort} instances to close
     */
    default void closeTabs(List<? extends ContainerTabPort> tabs) {
        class Closer {

            private int index = 0;

            private void run() {
                ContainerTabPort tab = null;
                for (var i = index; i < tabs.size(); i++) {
                    index++;
                    tab = tabs.get(i);
                    if (tab.getDescriptor().getState() == ComponentState.INITIALIZED) {
                        break;
                    }
                }
                if (tab != null) {
                    tab.closeSafely(this::handleCloseResult);
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

    /**
     * Closes all tabs in this container.
     */
    default void closeAllTabs() {
        this.closeTabs(new ArrayList<>(getTabPorts()));
    }

    /**
     * Closes all tabs to the right of the specified tab.
     *
     * @param tab the {@link ContainerTabPort} used as a reference point
     */
    default void closeRightTabs(ContainerTabPort tab) {
        var tabs = getTabPorts();
        var index = tabs.indexOf(tab);
        if (index == -1 || index + 1 == tabs.size()) {
            return;
        }
        List<ContainerTabPort> tabsToClose = new ArrayList<>();
        for (var i = index + 1; i < tabs.size(); i++) {
            tabsToClose.add(tabs.get(i));
            this.closeTabs(tabsToClose);
        }
    }

    /**
     * Closes all tabs to the left of the specified tab.
     *
     * @param tab the {@link ContainerTabPort} used as a reference point
     */
    default void closeLeftTabs(ContainerTabPort tab) {
        var tabs = getTabPorts();
        var index = tabs.indexOf(tab);
        if (index == -1 || index == 0) {
            return;
        }
        List<ContainerTabPort> tabsToClose = new ArrayList<>();
        for (var i = index - 1; i >= 0; i--) {
            tabsToClose.add(tabs.get(i));
            this.closeTabs(tabsToClose);
        }
    }

    /**
     * Closes the specified tab.
     *
     * @param tab the {@link ContainerTabPort} to close
     */
    default void closeTab(ContainerTabPort tab) {
        tab.closeSafely();
    }
}
