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
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogShared {

    /**
     * Returns whether this dialog is active.
     *
     * @return {@code true} if the dialog is active, {@code false} otherwise
     */
    boolean isActive();

    /**
     * Returns the minimum width of the dialog.
     *
     * @return the minimum width in pixels
     */
    double getMinWidth();

    /**
     * Returns the minimum height of the dialog.
     *
     * @return the minimum height in pixels
     */
    double getMinHeight();

    /**
     * Returns the maximum width of the dialog.
     *
     * @return the maximum width in pixels
     */
    double getMaxWidth();

    /**
     * Returns the maximum height of the dialog.
     *
     * @return the maximum height in pixels
     */
    double getMaxHeight();

    /**
     * Returns whether moving the dialog outside the bounds of its parent container is allowed.
     *
     * @return {@code true} if the dialog may be moved beyond the parent bounds,
     *         {@code false} if movement is restricted to the parent area
     */
    boolean isOutOfBoundsAllowed();

    /**
     * Returns whether the dialog can be resized by the user.
     *
     * @return {@code true} if the dialog is resizable, {@code false} otherwise
     */
    boolean isResizable();

    /**
     * Returns whether equal button width rendering is enabled.
     *
     * @return {@code true} if buttons should have equal width, {@code false} otherwise
     */
    boolean isButtonWidthEqual();

    /**
     * Returns whether the close button in the top right corner of the dialog is disabled.
     *
     * @return {@code true} if the close button is disabled, {@code false} otherwise
     */
    boolean isCloseDisabled();

    /**
     * Returns the list of result button names currently displayed on the left side of the dialog's button bar.
     *
     * @return an unmodifiable list of button names on the left side
     */
    List<ResultButtonName> getLeftButtons();

    /**
     * Returns the list of result button names currently displayed on the right side of the dialog's button bar.
     *
     * @return an unmodifiable list of button names on the right side
     */
    List<ResultButtonName> getRightButtons();
}
