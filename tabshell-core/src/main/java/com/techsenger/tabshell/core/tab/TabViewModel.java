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

import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.mvvm4fx.core.ChildViewModel;
import com.techsenger.tabshell.core.IconedViewModel;
import com.techsenger.tabshell.core.SelectableViewModel;
import com.techsenger.tabshell.core.TitledViewModel;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface TabViewModel extends ChildViewModel, TitledViewModel, IconedViewModel, SelectableViewModel, MenuAware {

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
     * Sets closed callback. It is not property.
     *
     * @param closedCallback
     */
    void setOnClosed(TabClosedCallback closedCallback);

    /**
     * Returns closed callback. It is not property.
     *
     * @return
     */
    TabClosedCallback getOnClosed();
}
