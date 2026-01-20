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

import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface TabContainerPort<S extends TabPort> {

    /**
     * Returns selected tab view model.
     *
     * @return
     */
    S getSelectedTab();

    /**
     * Returns the index of the selected tab.
     *
     * @return
     */
    int getSelectedTabIndex();

    /**
     * Selects tab by index.
     *
     * @param tabIndex
     */
    void selectTab(int tabIndex);

    /**
     * Returns unmodifiable observable list of tabs.
     *
     * @return
     */
    List<? extends S> getTabs();

    void closeOtherTabs(S tab);

    void closeTabs(List<? extends S> tabs);

    void closeAllTabs();

    void closeRightTabs(S tab);

    void closeLeftTabs(S tab);

    void closeTab(S tab);
}
