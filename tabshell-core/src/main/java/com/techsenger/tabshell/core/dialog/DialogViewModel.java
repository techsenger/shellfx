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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.tabshell.core.CloseableViewModel;
import com.techsenger.tabshell.core.IconedViewModel;
import com.techsenger.tabshell.core.TitledViewModel;
import com.techsenger.tabshell.core.area.AreaViewModel;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogViewModel<T extends DialogMediator> extends AreaViewModel<T>,
        TitledViewModel, IconedViewModel, CloseableViewModel<T>  {

    /**
     * Returns the scope of the dialog.
     *
     * @return
     */
    DialogScope getScope();

    /**
     * Indicates whether this dialog is currently active.
     *
     * <p>An active dialog is the top-most dialog managed by the {@code DialogManager}
     * and is allowed to receive user interaction.
     *
     * @return the active state property
     */
    ReadOnlyBooleanProperty activeProperty();

    /**
     * Returns the value of {@link #activeProperty()}.
     *
     * @return {@code true} if the dialog is active, {@code false} otherwise
     */
    boolean isActive();

    /**
     * Sets the value of {@link #activeProperty()}.
     *
     * <p>This method is intended to be called exclusively by the {@code DialogManager}. Application code and dialog
     * implementations should not invoke this method directly.
     *
     * @param active {@code true} to mark the dialog as active, {@code false} otherwise
     */
    void setActive(boolean active);
}
