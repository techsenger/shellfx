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

/**
 *
 * @author Pavel Castornii
 */
public interface DialogView extends PopupView, Titled, Iconed {

    /**
     * Returns whether this dialog is active.
     *
     * @return {@code true} if the dialog is active, {@code false} otherwise
     */
    boolean isActive();

    /**
     * Sets the value shows if the dialog is currently active.
     *
     * <p>This method is intended to be called exclusively by the {@code DialogManager}. Application code and dialog
     * implementations should not invoke this method directly.
     *
     * @param active {@code true} to mark the dialog as active, {@code false} otherwise
     */
    void setActive(boolean active);

    double getMinWidth();

    void setMinWidth(double value);

    double getMinHeight();

    void setMinHeight(double value);

    double getMaxWidth();

    void setMaxWidth(double value);

    double getMaxHeight();

    void setMaxHeight(double value);

    /**
     * Returns whether moving the dialog outside the bounds of its parent container is allowed.
     *
     * @return {@code true} if the dialog may be moved beyond the parent bounds,
     *         {@code false} if movement is restricted to the parent area
     */
    boolean isOutOfBoundsAllowed();

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

    boolean isResizable();

    void setResizable(boolean value);

    /**
     * Returns whether equal button width rendering is enabled.
     *
     * @return {@code true} if buttons should have equal width, {@code false} otherwise
     */
    boolean isButtonWidthEqual();

    /**
     * Enables or disables equal button width rendering.
     * <p>
     * When set to {@code true}, the view may re-evaluate button sizes and
     * adjust their widths according to the current layout.
     *
     * @param value {@code true} to enable equal button widths, {@code false} to disable
     */
    void setButtonWidthEqual(boolean value);
}
