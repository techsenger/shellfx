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

import com.techsenger.mvvm4fx.core.ChildView;
import com.techsenger.mvvm4fx.core.ParentView;
import com.techsenger.tabshell.core.CloseScope;
import com.techsenger.tabshell.core.CloseableView;
import com.techsenger.tabshell.core.SelectableView;
import com.techsenger.tabshell.core.menu.MenuAware;

/**
 * A view for components that has a root with JavaFX Tab class.
 *
 * @author Pavel Castornii
 */
public interface TabView<T extends TabViewModel> extends ChildView<T>, SelectableView, CloseableView, MenuAware {

    /**
     * Sets the parent component for this tab. At the same time, for optimization purposes, the parent component
     * does not keep references to the Tab components, since all tabs can be retrieved from the TabPane.
     *
     * @param parent
     */
    void setParent(ParentView<?> parent);

    /**
     * Is called when there is a new attempt to close the tab or the shell.
     *
     * @return true if it can be closed. Otherwise returns false.
     */
    boolean doOnCloseAttempt(CloseScope scope, Runnable repeatCallback);

    @Override
    ComponentTab getNode();

    @Override
    void close();
}
