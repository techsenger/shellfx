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

import com.techsenger.tabshell.material.button.ResultButtonName;
import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogShared {

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
     * Sets the pref width of the dialog. This is optional — by default, {@code dialogBox} and {@code contentBox}
     * have {@code minSize} set to {@link Region#USE_PREF_SIZE}, so the dialog will size itself based on its content
     * automatically.
     *
     * @param value the pref width in pixels
     */
    void setPrefWidth(double value);

    /**
     * Sets the pref height of the dialog. This is optional — by default, {@code dialogBox} and {@code contentBox}
     * have {@code minSize} set to {@link Region#USE_PREF_SIZE}, so the dialog will size itself based on its content
     * automatically.
     *
     * @param value the pref height in pixels
     */
    void setPrefHeight(double value);

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
     * Sets the disabled state for the close button in the top right corner of the dialog.
     *
     * @param value {@code true} to disable the close button, {@code false} to enable it
     */
    void setCloseDisabled(boolean value);

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

    /**
     * Sets the title of the component.
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * Sets the icon of the component.
     *
     * @param icon
     */
    void setIcon(Icon<?> icon);
}
