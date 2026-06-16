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

import com.techsenger.patternfx.mvp.ParentView;

/**
 *
 * @author Pavel Castornii
 */
public interface TabContainerView extends ParentView, TabContainerPort.ViewAccess {

    interface Composer extends ParentView.Composer, TabContainerPort.Composer {

    }

    @Override
    Composer getComposer();

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
}
