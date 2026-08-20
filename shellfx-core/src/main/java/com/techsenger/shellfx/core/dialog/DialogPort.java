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

package com.techsenger.shellfx.core.dialog;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.window.WindowPort;
import com.techsenger.shellfx.material.button.ResultButtonName;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogPort extends WindowPort {

    /**
     * Returns the action to be executed when a result button is clicked.
     * <p>
     * This action determines whether the dialog should actually be closed. The consumer receives
     * the name of the clicked button and can implement any logic — including choosing not to close
     * the dialog. If not set, the default action simply closes the dialog.
     *
     * @return the result action consumer, or {@code null} if the default close behavior is used
     */
    Consumer<ResultButtonName> getOnResult();

    /**
     * Sets the action to be executed when a result button is clicked.
     * <p>
     * This action determines whether the dialog should actually be closed. The consumer receives
     * the name of the clicked button and can implement any logic — including choosing not to close
     * the dialog. If not set, the default action simply closes the dialog.
     * The consumer is always invoked with a non-null button name.
     *
     * @param action the result action consumer, or {@code null} to reset to the default close behavior
     */
    void setOnResult(Consumer<ResultButtonName> action);

    /**
     * Returns the list of result button names currently displayed on the left side of the dialog's button bar.
     *
     * @return an unmodifiable list of button names on the left side
     */
    @Unmodifiable List<ResultButtonName> getLeftButtons();

    /**
     * Returns the list of result button names currently displayed on the right side of the dialog's button bar.
     *
     * @return an unmodifiable list of button names on the right side
     */
    @Unmodifiable List<ResultButtonName> getRightButtons();

    /**
     * Returns the disabled state of the specified result button.
     *
     * @param name the name of the result button
     * @return an {@link Optional} containing {@code true} if the button is disabled, {@code false} if enabled,
     *         or empty if the button does not exist
     */
    Optional<Boolean> getButtonDisabled(ResultButtonName name);

    /**
     * Returns whether the specified result button is the default button for the dialog.
     *
     * @param name the name of the result button
     * @return an {@link Optional} containing {@code true} if the button is the default, {@code false} otherwise,
     *         or empty if the button does not exist
     */
    Optional<Boolean> getButtonDefault(ResultButtonName name);

    /**
     * Specifies the result buttons in the left side of the dialog's button bar or removes all of them.
     *
     * @param names the names of the result buttons to add; pass no arguments to remove all buttons.
     */
    void setLeftButtons(ResultButtonName... names);

    /**
     * Specifies the result buttons in the right side of the dialog's button bar or removes all of them.
     *
     * @param names the names of the result buttons to add; pass no arguments to remove all buttons.
     */
    void setRightButtons(ResultButtonName... names);

    /**
     * Sets the disabled state of the specified result button.
     *
     * @param name the name of the result button
     * @param value {@code true} to disable the button, {@code false} to enable it
     */
    void setButtonDisabled(ResultButtonName name, boolean value);

    /**
     * Sets whether the specified result button is the default button for the dialog.
     * <p>
     * The default button is typically activated when the user presses Enter. Only one button should be marked
     * as default at a time.
     *
     * @param name the name of the result button
     * @param value {@code true} to make this button the default, {@code false} otherwise
     */
    void setButtonDefault(ResultButtonName name, boolean value);
}
