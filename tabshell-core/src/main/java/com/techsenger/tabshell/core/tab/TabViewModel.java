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

import com.techsenger.patternfx.mvvmx.ChildViewModel;
import com.techsenger.tabshell.core.CloseableViewModel;
import com.techsenger.tabshell.core.IconedViewModel;
import com.techsenger.tabshell.core.SelectableViewModel;
import com.techsenger.tabshell.core.TitledViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface TabViewModel<T extends TabMediator> extends ChildViewModel<T>, TitledViewModel, IconedViewModel,
        SelectableViewModel, CloseableViewModel<T> {

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
     * Returns the closable property of the component.
     *
     * @return
     */
    BooleanProperty closableProperty();

    /**
     * Returns the value of the {@link TabViewModel#closableProperty()}.
     *
     * @return
     */
    boolean isClosable();

    /**
     * Sets the value of the {@link TabViewModel#closableProperty()}.
     * @param closable
     */
    void setClosable(boolean closable);
}
