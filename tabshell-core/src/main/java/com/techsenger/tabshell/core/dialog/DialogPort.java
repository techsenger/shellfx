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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.material.button.ResultButtonName;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogPort extends DialogShared, PopupPort {

    /**
     * Returns the action to be executed when the dialog is closed via the close button or window close event.
     * <p>
     * This action is typically invoked when the user clicks the close button (X) in the dialog's title bar or
     * closes the dialog through system means, rather than selecting a result button.
     *
     * @return the close action, or {@code null} if no action is set
     */
    Runnable getCloseAction();

    /**
     * Sets the action to be executed when the dialog is closed via the close button or window close event.
     * <p>
     * This action is typically invoked when the user clicks the close button (X) in the dialog's title bar or
     * closes the dialog through system means, rather than selecting a result button.
     *
     * @param action the close action to execute, or {@code null} to clear the action
     */
    void setCloseAction(Runnable action);

    /**
     * Returns the action to be executed when a result button is clicked.
     * <p>
     * This consumer receives the name of the clicked result button and allows the presenter to handle the
     * dialog result appropriately. The consumer is always invoked with a non-null button name.
     *
     * @return the result action consumer, or {@code null} if no action is set
     */
    Consumer<ResultButtonName> getResultAction();

    /**
     * Sets the action to be executed when a result button is clicked.
     * <p>
     * This consumer receives the name of the clicked result button and allows the presenter to handle the
     * dialog result appropriately. The consumer is always invoked with a non-null button name.
     *
     * @param action the result action consumer, or {@code null} to clear the action
     */
    void setResultAction(Consumer<ResultButtonName> action);
}
