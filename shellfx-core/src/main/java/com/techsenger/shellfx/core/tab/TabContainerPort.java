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
import com.techsenger.patternfx.mvp.ParentPort;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface TabContainerPort extends ParentPort {

    interface Composer {

        /**
         * Returns an unmodifiable list of tabs. A new list instance is created on each call.
         *
         * @return
         */
        @Unmodifiable List<? extends TabPort> getTabPorts();

        /**
         * Returns selected tab port.
         */
        TabPort getSelectedTabPort();
    }

    interface  ViewAccess {

        Composer getComposer();
    }

    ViewAccess getViewAccess();

    /**
     * Returns the index of the currently selected tab.
     *
     * @return zero-based index of the selected tab, or {@code -1} if no tab is selected
     */
    int getSelectedTabIndex();

    /**
     * Selects the tab at the given index.
     *
     * @param tabIndex zero-based index of the tab to select
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    void selectTab(int tabIndex);

    /**
     * Closes all tabs except the specified one.
     *
     * @param tab the {@link TabPort} to keep open
     */
    void closeOtherTabs(TabPort tab);

    /**
     * Closes all tabs in the given list.
     *
     * @param tabs list of {@link TabPort} instances to close
     */
    void closeTabs(List<? extends TabPort> tabs);

    /**
     * Closes all tabs in this container.
     */
    void closeAllTabs();

    /**
     * Closes all tabs to the right of the specified tab.
     *
     * @param tab the {@link TabPort} used as a reference point
     */
    void closeRightTabs(TabPort tab);

    /**
     * Closes all tabs to the left of the specified tab.
     *
     * @param tab the {@link TabPort} used as a reference point
     */
    void closeLeftTabs(TabPort tab);

    /**
     * Closes the specified tab.
     *
     * @param tab the {@link TabPort} to close
     */
    void closeTab(TabPort tab);
}
