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

/**
 *
 * @author Pavel Castornii
 */
public interface DialogShared {

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
