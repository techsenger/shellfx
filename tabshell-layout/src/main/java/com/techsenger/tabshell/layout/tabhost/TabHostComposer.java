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

package com.techsenger.tabshell.layout.tabhost;

import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.core.tab.TabContainerComposer;

/**
 *
 * @author Pavel Castornii
 */
public interface TabHostComposer extends AreaComposer, TabContainerComposer {

    /**
     * Checks if tabs are detached.
     *
     * @return
     */
    boolean areTabsDetached();

    /**
     * Attaches the detached tabs to the {@link TabPane}. The process involves several iteration loops, so it may be
     * relatively costly.
     */
    void attachTabs();

    /**
     * Detaches the tabs from the {@link TabPane}. This operation is required when the tabs need to be temporarily added
     * to other {@link TabPane}s. The process involves several iteration loops, so it may be relatively costly.
     */
    void detachTabs();
}
