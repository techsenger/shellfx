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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ParentFxView;
import java.util.List;
import javafx.scene.control.Tab;

/**
 *
 * @author Pavel Castornii
 */
public interface TabContainerFxView<P extends TabContainerPresenter<?>> extends ParentFxView<P> {

     interface Composer extends ParentFxView.Composer, TabContainerView.Composer {

        void addTab(TabFxView<?> tab);

        void removeTab(TabFxView<?> tab);

        void closeTab(TabFxView<?> tab);

        /**
         * Returns an unmodifiable list of tabs.
         *
         * @return
         */
        @Unmodifiable List<? extends TabFxView<?>> getTabs();

        TabFxView<?> getSelectedTab();
    }

    @Override
    Composer getComposer();

    /**
     * Prevents interaction with the tab header while it is blocked (e.g. during modal operation).
     *
     * @param tab the tab to block
    */
    void setTabHeaderBlocked(Tab tab, boolean blocked);
}
