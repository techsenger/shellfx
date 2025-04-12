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

import com.techsenger.mvvm4fx.core.ChildViewModel;
import com.techsenger.tabshell.core.CloseScope;
import com.techsenger.tabshell.core.CloseableViewModel;
import com.techsenger.tabshell.core.IconedViewModel;
import com.techsenger.tabshell.core.SelectableViewModel;
import com.techsenger.tabshell.core.TitledViewModel;
import com.techsenger.tabshell.core.menu.MenuAware;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface TabViewModel extends ChildViewModel, TitledViewModel, IconedViewModel, SelectableViewModel, MenuAware,
        CloseableViewModel {

    /**
     * The property for the component that contains the TabPane with this tab.
     *
     * @return
     */
    ReadOnlyObjectProperty<TabHostViewModel<?>> tabHostProperty();

    /**
     * Return the component that contains the TabPane with this tab.
     *
     * @return
     */
    TabHostViewModel<?> getTabHost();

    /**
     * Returns tab key. This property allows to distinguish different tab types in TabShell.
     *
     * @return
     */
    @Override
    TabKey getKey();

    /**
     * Returns the tooltip property of the component.
     *
     * @return
     */
    StringProperty tooltipProperty();

    /**
     * Returns the tooltip of the component.
     * @return
     */
    String getTooltip();

    /**
     * Sets the tooltip of the component.
     *
     * @param tooltip
     */
    void setTooltip(String tooltip);

    /**
     * Returns true if the tab can be closed and false otherwise.
     *
     * @return
     */
    boolean isReadyToClose();

    /**
     * Prepares the tab for closure (e.g., saves data or validates state).
     *
     * <p>Callback contract:
     * <ul>
     * <li>Called only if preparation succeeds.
     * <li>Must transition tab to "ready-to-close" state ({@code isReadyToClose() == true}).
     * </ul>
     *
     * <p>Failure handling:
     * <ul>
     * <li>If preparation fails, the closing process is silently aborted.
     * <li>No automatic retries or fallbacks are implemented.
     * </ul>
     *
     * @param scope determines closure context (TAB/SHELL)
     * @param retryCallback runnable to execute if preparation succeeds
     */
    void prepareForClose(CloseScope scope, Runnable retryCallback);

    @Override
    void requestClose();

    /**
     * Sets a callback that is called when the tab is closed, removed from the TabPane and deinitialized.
     *
     * @param closedCallback
     */
    void setOnClosed(TabClosedCallback closedCallback);

    /**
     * Returns a callback that is called when the tab is closed, removed from the TabPane and deinitialized.
     *
     * @return
     */
    TabClosedCallback getOnClosed();
}
