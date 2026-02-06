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

import com.techsenger.tabshell.core.Iconed;
import com.techsenger.tabshell.core.Titled;
import com.techsenger.tabshell.core.popup.PopupView;
import com.techsenger.tabshell.material.button.ResultButtonName;
import java.util.Optional;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogView extends DialogShared, PopupView, Titled, Iconed {

    /**
     * Sets the value shows if the dialog is currently active.
     *
     * <p>This method is intended to be called exclusively by the {@code DialogManager}. Application code and dialog
     * implementations should not invoke this method directly.
     *
     * @param active {@code true} to mark the dialog as active, {@code false} otherwise
     */
    void setActive(boolean active);

    /**
     * Sets the minimum width of the dialog.
     *
     * @param value the minimum width in pixels
     */
    void setMinWidth(double value);

    /**
     * Sets the minimum height of the dialog.
     *
     * @param value the minimum height in pixels
     */
    void setMinHeight(double value);

    /**
     * Sets the maximum width of the dialog.
     *
     * @param value the maximum width in pixels
     */
    void setMaxWidth(double value);

    /**
     * Sets the maximum height of the dialog.
     *
     * @param value the maximum height in pixels
     */
    void setMaxHeight(double value);

    /**
     * Enables or disables the ability to move the dialog outside the bounds of its parent container.
     * <p>
     * When enabled, only a minimum top constraint may be applied.
     * When disabled, dialog movement is fully constrained to the parent bounds.
     *
     * @param outOfBoundsAllowed {@code true} to allow moving outside parent bounds,
     *                           {@code false} to restrict movement to the parent area
     */
    void setOutOfBoundsAllowed(boolean outOfBoundsAllowed);

    /**
     * Sets whether the dialog can be resized by the user.
     *
     * @param value {@code true} to make the dialog resizable, {@code false} to disable resizing
     */
    void setResizable(boolean value);

    /**
     * Enables or disables equal button width rendering.
     * <p>
     * When set to {@code true}, the view may re-evaluate button sizes and adjust their widths according to the
     * current layout.
     *
     * @param value {@code true} to enable equal button widths, {@code false} to disable
     */
    void setButtonWidthEqual(boolean value);

    /**
     * Sets the disabled state for the close button in the top right corner of the dialog.
     *
     * @param value {@code true} to disable the close button, {@code false} to enable it
     */
    void setCloseDisabled(boolean value);

    /**
     * Adds result buttons to the left side of the dialog's button bar.
     * <p>
     * Buttons are added in the order specified. If a button with the given name already exists on the left side,
     * it is not added again.
     *
     * @param names the names of the result buttons to add
     */
    void addLeftButtons(ResultButtonName... names);

    /**
     * Removes result buttons from the left side of the dialog's button bar.
     *
     * @param names the names of the result buttons to remove
     */
    void removeLeftButtons(ResultButtonName... names);

    /**
     * Removes all result buttons from the left side of the dialog's button bar.
     */
    void removeLeftButtons();

    /**
     * Adds result buttons to the right side of the dialog's button bar.
     * <p>
     * Buttons are added in the order specified. If a button with the given name already exists on the right side,
     * it is not added again.
     *
     * @param names the names of the result buttons to add
     */
    void addRightButtons(ResultButtonName... names);

    /**
     * Removes result buttons from the right side of the dialog's button bar.
     *
     * @param names the names of the result buttons to remove
     */
    void removeRightButtons(ResultButtonName... names);

    /**
     * Removes all result buttons from the right side of the dialog's button bar.
     */
    void removeRightButtons();

    /**
     * Sets the visibility of the specified result button.
     *
     * @param name the name of the result button
     * @param value {@code true} to make the button visible, {@code false} to hide it
     */
    void setButtonVisible(ResultButtonName name, boolean value);

    /**
     * Returns the visibility state of the specified result button.
     *
     * @param name the name of the result button
     * @return an {@link Optional} containing {@code true} if the button is visible, {@code false} if hidden,
     *         or empty if the button does not exist
     */
    Optional<Boolean> getButtonVisible(ResultButtonName name);

    /**
     * Sets the disabled state of the specified result button.
     *
     * @param name the name of the result button
     * @param value {@code true} to disable the button, {@code false} to enable it
     */
    void setButtonDisabled(ResultButtonName name, boolean value);

    /**
     * Returns the disabled state of the specified result button.
     *
     * @param name the name of the result button
     * @return an {@link Optional} containing {@code true} if the button is disabled, {@code false} if enabled,
     *         or empty if the button does not exist
     */
    Optional<Boolean> getButtonDisabled(ResultButtonName name);

    /**
     * Sets the display text of the specified result button.
     *
     * @param name the name of the result button
     * @param value the text to display on the button
     */
    void setButtonText(ResultButtonName name, String value);

    /**
     * Returns the display text of the specified result button.
     *
     * @param name the name of the result button
     * @return an {@link Optional} containing the button text, or empty if the button does not exist
     */
    Optional<String> getButtonText(ResultButtonName name);

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

    /**
     * Returns whether the specified result button is the default button for the dialog.
     *
     * @param name the name of the result button
     * @return an {@link Optional} containing {@code true} if the button is the default, {@code false} otherwise,
     *         or empty if the button does not exist
     */
    Optional<Boolean> getButtonDefault(ResultButtonName name);
}
